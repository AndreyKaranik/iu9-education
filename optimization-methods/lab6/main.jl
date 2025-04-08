using LinearAlgebra
using Plots

gr()

function df(f, x::Vector{Float64}, i, h=1e-5)
    x_plus = copy(x)
    x_minus = copy(x)
    
    x_plus[i] += h
    x_minus[i] -= h
    
    df_dx = (f(x_plus) - f(x_minus)) / (2h)
    
    return df_dx
end

function gradient(f, x::Vector{Float64})
    grad = []
    for i in 1:length(x)
        push!(grad, df(f, x, i))
    end
    return grad
end
     

function swann_method(f, x0, h=0.1)
    first = x0
    second = x0 + h
    if f(second) > f(first)
        h = -h
        first, second = second, second + h
    end
    last = second + h
    
    while f(last) < f(second)
        h *= 2
        first, second, last = second, last, last + h
    end

    if second > last
        first, second, last = last, second, first
    end

    return first, last
end

function golden_section_search(f, a, b, eps=1e-5)
    phi = (sqrt(5) - 1) / 2 
    x1 = b - phi * (b - a)
    x2 = a + phi * (b - a)
    
    while abs(b - a) > eps
        if f(x1) <= f(x2)
            b = x2
        else
            a = x1
        end
        x1 = b - phi * (b - a)
        x2 = a + phi * (b - a)
    end
    
    return (a + b) / 2
end

function conjugate_gradient(f, x0)
    eps1, eps2 = 1e-6, 1e-10
    x = x0
    prev_x = copy(x)
    grad = gradient(f, x)
    d = -grad
    trajectory = [x]
    
    while true
        prev_grad=copy(grad)
        l, r = swann_method(alpha -> f(x + alpha * d), 1e-7)
        alpha = golden_section_search(alpha -> f(x + alpha * d), l, r)
        x += alpha * d
        grad = gradient(f, x)

        if norm(x - prev_x) < eps1 || norm(f(x) - f(prev_x)) < eps2
            break
        end

        beta = dot(grad, grad) / dot(prev_grad, prev_grad)
        d = -grad + beta * d
        prev_x = copy(x)
        push!(trajectory, x)
    end
    
    return x, trajectory
end

function multiparam_search(f, x0)
    eps1, eps2 = 1e-6, 1e-10
    x = x0
    prev_x = copy(x)
    trajectory = [x]
    n = 2
    i = 0
    while true        
        i += 1
        grad = gradient(f, x)
        delta = x - prev_x
        
        if mod(i, n) == 0
            delta = Vector{Float64}([0, 0])
        end
        
        g = a -> f(x - a[1] * grad + a[2] * delta)
        
        a_k, tr = conjugate_gradient(g, prev_x)
        x, prev_x = x - a_k[1] * grad + a_k[2] * delta, x
        
        if norm(f(x) - f(prev_x)) < eps1
            break
        end

        prev_x = copy(x)
        push!(trajectory, x)
    end
    
    return x, trajectory
end

f(x) = x[1]^2 + x[2]^2
x0 = [10.0, -5.0] 

x = -20:0.1:10
y = -20:0.1:20
x_min, trajectory = multiparam_search(f, x0)
x_coords = [point[1] for point in trajectory]
y_coords = [point[2] for point in trajectory]
contour(x, y, (x, y) -> f([x, y]), levels = 10, xlabel="x", ylabel="y", colorbar=false, size=(500, 500))
scatter!(x_coords, y_coords, markersize=2, markershape=:circle, markercolor=:red, label = "")
p = plot!(x_coords, y_coords, label="", line=:red)
display(p)
println("Точка минимума: $x_min\nКол-во итераций $(length(trajectory)-1)")
    