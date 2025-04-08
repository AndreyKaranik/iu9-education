using Printf
using Random
using Dates

function f(x)
    return (x-1)^3 + (x-1)^2
end

function df(x)
    return 3(x-1)^2 + 2(x-1)
end

function d2f(x)
    return 6(x-1) + 2
end

function bisection_method(f, a, b)
    if f(a) * f(b) > 0
        return nothing
    end
    while abs(b - a) > 1e-5
        c = (a + b) / 2
        if f(c) == 0
            return c
        elseif f(a) * f(c) < 0
            b = c
        else
            a = c
        end
    end
    return (a + b) / 2
end

function golden_section_method(f, a, b)
    if f(a) * f(b) > 0
        return nothing
    end
    φ = (1 + sqrt(5)) / 2
    x1 = b - (b - a) / φ
    x2 = a + (b - a) / φ
    while abs(b - a) > 1e-5
        if abs(f(x1)) < abs(f(x2))
            b = x2
        else
            a = x1
        end
        x1 = b - (b - a) / φ
        x2 = a + (b - a) / φ
    end
    return (a + b) / 2
end

function fibonacci_method(f, a, b, n=20)
    if f(a) * f(b) > 0
        return nothing
    end

    fib = [1, 1]
    for i in 3:n+1
        push!(fib, fib[end] + fib[end-1])
    end

    x1 = a + (b - a) * fib[n-1] / fib[n+1]
    x2 = a + (b - a) * fib[n] / fib[n+1]
    
    for k in 1:n-1
        if abs(f(x1)) < abs(f(x2))
            b = x2
        else
            a = x1
        end
        if n-k-1 > 0
            x1 = a + (b - a) * fib[n-k-1] / fib[n-k+1]
            x2 = a + (b - a) * fib[n-k] / fib[n-k+1]
        end
    end
    return (a + b) / 2
end

function find_critical_points(method, a, b, n)
    critical_points = []
    x = a
    while x < b
        x_next = x + (b - a) / n
        if x_next > b
            break
        end
        root = method(df, x, x_next)
        if root !== nothing
            push!(critical_points, root)
        end
        x = x_next
    end
    return critical_points
end

function check_unimodality(method, a, b, n)
    t_start = now()
    crit_points = find_critical_points(method, a, b, n)
    elapsed_time = now() - t_start

    if isempty(crit_points)
        println("Метод $(method): нет критических точек, функция монотонна ⇒ унимодальна.")
        return true, elapsed_time
    end

    second_derivatives = [d2f(x) for x in crit_points]

    println("Метод $(method):")
    println("Критические точки: ", crit_points)
    println("Значения второй производной: ", second_derivatives)

    if all(d -> d > 0, second_derivatives) || all(d -> d < 0, second_derivatives)
        println("Функция унимодальна (имеет единственный экстремум).")
        return true, elapsed_time
    else
        println("Функция не унимодальна (есть и минимум, и максимум).")
        return false, elapsed_time
    end
end

methods = [bisection_method, golden_section_method, fibonacci_method]
results = Dict()

for method in methods
    println("\n==============================")
    println("Тест метода: ", method)
    unimodal, time_taken = check_unimodality(method, -10, 1.5, 100)
    results[method] = time_taken
    println("Время выполнения: ", time_taken)
end

println("\n==============================")
println("Сравнение методов по времени выполнения:")
for (method, time) in results
    @printf "%s: %s\n" method time
end