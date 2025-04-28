using Random
using Plots
using HTTP
using JSON
using Distributed
using Base.Threads: @spawn, ReentrantLock, lock
using ProgressMeter

const POPULATION_SIZE = 30
const GENERATIONS = 500
const NUM_WORKERS = 3

const DISTANCE_PARAM = 0.5
const MIGRATION_INTERVAL = 10
const MIGRATION_COUNT = 5

const Chromosome = Vector{Bool}

function evaluate_schwefel(x, y)
    return 418.9829 * 2 - (x * sin(sqrt(abs(x))) + y * sin(sqrt(abs(y))))
end

function decode_chromosome(chrom::Vector{Bool})
    half = div(length(chrom), 2)
    x_bits = chrom[1:half]
    y_bits = chrom[half+1:end]
    x = binary_to_float(x_bits, -500.0, 500.0)
    y = binary_to_float(y_bits, -500.0, 500.0)
    return x, y
end

function binary_to_float(bits::Vector{Bool}, min_val::Float64, max_val::Float64)
    intval = sum(bits[i] * 2^(length(bits)-i) for i in 1:length(bits))
    max_int = 2^length(bits) - 1
    min_val + (max_val - min_val) * intval / max_int
end

function create_random_chromosome()
    rand(Bool, 32)
end

function compute_fitness(chromo::Chromosome)
    x, y = decode_chromosome(chromo)
    -evaluate_schwefel(x, y)
end

function calculate_distance(a::Chromosome, b::Chromosome)
    min_val = 0
    max_val = 1
    n = length(a)
    dist = 0
    for i in 1:n
        dist += abs((a[i]-b[i]) / (max_val - min_val))
    end
    normalized = dist / n
    return normalized^DISTANCE_PARAM
end

function apply_mutation!(chromo::Chromosome, mutation_prob::Float64)
    for i in eachindex(chromo)
        if rand() < mutation_prob
            chromo[i] = !chromo[i]
            break
        end
    end
end

function perform_crossover(parent1::Chromosome, parent2::Chromosome)
    point = rand(2:31)
    child1 = vcat(parent1[1:point], parent2[point+1:end])
    child2 = vcat(parent2[1:point], parent1[point+1:end])
    return child1, child2
end

function select_parent(pop::Vector{Chromosome})
    a, b = rand(pop, 2)
    compute_fitness(a) > compute_fitness(b) ? a : b
end

function evolve_population!(population)
    elite_size = div(POPULATION_SIZE, 3)
    sort!(population, by=compute_fitness, rev=true)
    elite = population[1:elite_size]
    new_population = Chromosome[]
    
    while length(new_population) < POPULATION_SIZE - elite_size
        parent1 = select_parent(population)
        parent2 = select_parent(population)
        child1, child2 = perform_crossover(parent1, parent2)
        
        distance = calculate_distance(parent1, parent2)
        mutation_prob = 1.0 - distance
        
        apply_mutation!(child1, mutation_prob)
        apply_mutation!(child2, mutation_prob)
        push!(new_population, child1)
        if length(new_population) < POPULATION_SIZE - elite_size
            push!(new_population, child2)
        end
    end
    vcat(elite, new_population)
end

function handle_messages(ws, population, expected_messages)
    for _ in 1:expected_messages
        msg = HTTP.WebSockets.receive(ws)
        data = JSON.parse(msg)
        for raw_ind in data["individuals"]
            chromo = map(Bool, raw_ind)
            push!(population, chromo)
        end
    end
    sort!(population, by=compute_fitness, rev=true)
    resize!(population, POPULATION_SIZE)
end

function worker_process(id::Int, population::Vector{Chromosome},
                    history::Vector{Tuple{Int, Vector{Float64}, Vector{Float64}, Vector{Float64}}},
                    global_best::Base.RefValue{Tuple{Float64, Float64, Float64}},
                    history_lock::ReentrantLock)
    for generation in 1:GENERATIONS
        if generation % MIGRATION_INTERVAL == 0
            elite_before = population[1:div(POPULATION_SIZE, 2)]
            HTTP.WebSockets.open("ws://127.0.1.1:3333") do ws
                top_individuals = population[1:MIGRATION_COUNT]
                msg = Dict("type" => "migration", "from" => "worker_$id", "individuals" => top_individuals)
                HTTP.WebSockets.send(ws, JSON.json(msg))
                handle_messages(ws, population, NUM_WORKERS - 1)
            end
            population = vcat(elite_before, population)
            sort!(population, by=compute_fitness, rev=true)
            resize!(population, POPULATION_SIZE)
        end

        population = evolve_population!(population)
        decoded = [decode_chromosome(ch) for ch in population]
        xs = [p[1] for p in decoded]
        ys = [p[2] for p in decoded]
        zs = [evaluate_schwefel(x, y) for (x, y) in decoded]

        min_z = minimum(zs)
        idx = argmin(zs)
        best_x, best_y = xs[idx], ys[idx]

        if min_z < global_best[][3]
            global_best[] = (best_x, best_y, min_z)
        end

        lock(history_lock) do
            push!(history, (generation, xs, ys, zs))
        end
    end
end

function run_workers()
    pop = [create_random_chromosome() for _ in 1:(POPULATION_SIZE * NUM_WORKERS)]
    subpopulations = [pop[i:NUM_WORKERS:end] for i in 1:NUM_WORKERS]

    global_best = Ref((0.0, 0.0, Inf))
    history = Vector{Tuple{Int, Vector{Float64}, Vector{Float64}, Vector{Float64}}}()
    history_lock = ReentrantLock()
    
    start_time = time()

    @sync for i in 1:NUM_WORKERS
        @spawn worker_process(i, subpopulations[i], history, global_best, history_lock)
    end
    
    elapsed = time() - start_time
    println("Execution time: $(round(elapsed, digits=2)) seconds")
    
    best_x, best_y, best_f = global_best[]
    println("\nBest solution: f(x, y) = $(round(best_f, digits=5)) at x = $(round(best_x, digits=5)), y = $(round(best_y, digits=5))")
    return history, best_f 
end

function run_server()
    clients = Dict{String, HTTP.WebSockets.WebSocket}()

    function handle_client(ws)
        id = string(rand(UInt))
        clients[id] = ws

        while !HTTP.WebSockets.isclosed(ws)
            msg = String(HTTP.WebSockets.receive(ws))
            data = JSON.parse(msg)
            if data["type"] == "migration"
                println("Received migration from $(data["from"])")
                for (other_id, client_ws) in clients
                    if other_id != data["from"] && !HTTP.WebSockets.isclosed(client_ws)
                        HTTP.WebSockets.send(client_ws, JSON.json(data))
                    end
                end
            end
        end
    end

    HTTP.WebSockets.listen("127.0.1.1", 3333) do ws
        handle_client(ws)
    end
end

function generate_plots(history, save_dir)
    x_range = -500:10:500
    y_range = -500:10:500
    z_vals = [evaluate_schwefel(x, y) for x in x_range, y in y_range]
    
    progress = Progress(length(history), 1, "Generating plots...")
    for (i, (gen, xs, ys, zs)) in enumerate(history)
        plt = surface(x_range, y_range, z_vals',
            xlabel="x", ylabel="y", zlabel="f(x, y)",
            title="Generation $gen",
            c = :viridis, legend = false,
            size = (1200, 900), dpi = 300)
        
        scatter3d!(plt, xs, ys, zs, 
            color = :red, 
            marker = :circle, 
            markersize = 5,
            markeralpha = 0.6)
        
        savefig(plt, "$(save_dir)/generation_$(i).png")
        next!(progress)
    end
end

function run_classic_evolution()
    population = [create_random_chromosome() for _ in 1:POPULATION_SIZE]
    global_best = Ref((0.0, 0.0, Inf))
    history = Vector{Tuple{Int, Vector{Float64}, Vector{Float64}, Vector{Float64}}}()
    
    println("Starting classic evolution...")
    start_time = time()
    
    for generation in 1:GENERATIONS
        population = evolve_population!(population)
        decoded = [decode_chromosome(ch) for ch in population]
        xs = [p[1] for p in decoded]
        ys = [p[2] for p in decoded]
        zs = [evaluate_schwefel(x, y) for (x, y) in decoded]
        
        min_z = minimum(zs)
        idx = argmin(zs)
        best_x, best_y = xs[idx], ys[idx]
        
        if min_z < global_best[][3]
            global_best[] = (best_x, best_y, min_z)
        end
        
        push!(history, (generation, xs, ys, zs))
    end
    
    elapsed = time() - start_time
    println("Execution time: $(round(elapsed, digits=2)) seconds")
    
    best_x, best_y, best_f = global_best[]
    println("\nBest solution: f(x, y) = $(round(best_f, digits=5)) at x = $(round(best_x, digits=5)), y = $(round(best_y, digits=5))")
    return history, global_best[][3]
end

function compare_algorithms()
    println("=== Parallel Algorithm ===")
    history_parallel, best_par = run_workers()
    
    println("\n=== Classic Algorithm ===")
    history_classic, best_seq = run_classic_evolution()
    
    plt = plot(title="Convergence Comparison", 
        xlabel="Generation", ylabel="Best Value",
        legend=:topright)
        
    par_hist = Vector{Tuple{Int, Vector{Float64}, Vector{Float64}, Vector{Float64}}}()
    k = 0
    for h in history_parallel 
        if k % NUM_WORKERS == 0
            push!(par_hist, h)
        end
        k += 1
    end
    
    plot!(plt, [minimum(h[4]) for h in par_hist], 
        label="Parallel", linewidth=2)
    
    plot!(plt, [minimum(h[4]) for h in history_classic], 
        label="Classic", linewidth=2)
    
    savefig(plt, "image.png")
    
    println("\nFinal comparison:")
    println("Parallel: ", round(best_par, digits=5))
    println("Classic: ", round(best_seq, digits=5))
end

if abspath(PROGRAM_FILE) == @__FILE__
    if length(ARGS) > 0 && ARGS[1] == "master"
        run_server()
    else
        compare_algorithms()
    end
end