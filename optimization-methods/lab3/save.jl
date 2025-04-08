function nelder_mead(f, x_start;
    step=0.1, no_improve_thr=1e-6,
    no_improv_break=10, max_iter=0,
    alpha=1.0, gamma=2.0, rho=-0.5, sigma=0.5)
# Initialize
dim = length(x_start)
prev_best = f(x_start)
no_improv = 0
res = [[x_start, prev_best]]

# Generate simplex
for i in 1:dim
x = copy(x_start)
x[i] += step
score = f(x)
push!(res, [x, score])
end

# Start simplex iterations
iters = 0
while true
# Order the results by the score
sort!(res, by = x -> x[2])

best = res[1][2]

# Break after max_iter iterations
if max_iter != 0 && iters >= max_iter
return res[1]
end
iters += 1

# Break after no_improv_break iterations with no improvement
println("...best so far: ", best)

if best < prev_best - no_improve_thr
no_improv = 0
prev_best = best
else
no_improv += 1
end

if no_improv >= no_improv_break
return res[1]
end

# Compute the centroid of the best points (excluding the worst)
x0 = zeros(dim)
for tup in res[1:end-1]
for (i, c) in enumerate(tup[1])
x0[i] += c / (length(res) - 1)
end
end

# Reflection
xr = x0 + alpha * (x0 - res[end][1])
rscore = f(xr)
if res[1][2] <= rscore < res[end-1][2]
pop!(res)
push!(res, [xr, rscore])
continue
end

# Expansion
if rscore < res[1][2]
xe = x0 + gamma * (x0 - res[end][1])
escore = f(xe)
if escore < rscore
pop!(res)
push!(res, [xe, escore])
continue
else
pop!(res)
push!(res, [xr, rscore])
continue
end
end

# Contraction
xc = x0 + rho * (x0 - res[end][1])
cscore = f(xc)
if cscore < res[end][2]
pop!(res)
push!(res, [xc, cscore])
continue
end

# Reduction
x1 = res[1][1]
nres = []
for tup in res
redx = x1 + sigma * (tup[1] - x1)
score = f(redx)
push!(nres, [redx, score])
end
res = nres
end
end

# Test
function rosenbrock(x)
return (1 - x[1])^2 + 100 * (x[2] - x[1]^2)^2
end

x_start = [0.0, 0.0]

println(nelder_mead(rosenbrock, x_start))