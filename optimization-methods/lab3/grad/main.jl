using LinearAlgebra

function rosenbrock(x)
    sum(100 * (x[i+1] - x[i]^2)^2 + (1 - x[i])^2 for i in 1:length(x)-1)
end

function numerical_gradient(f, x, eps=1e-8)
    grad = zeros(length(x))
    for i in 1:length(x)
        x_step = copy(x)
        x_step[i] += eps
        grad[i] = (f(x_step) - f(x)) / eps
    end
    return grad
end

function gradient_descent(f, x0; lr=0.001, tol=1e-6, max_iters=10000)
    x = copy(x0)
    for iter in 1:max_iters
        grad = numerical_gradient(f, x)
        if norm(grad) < tol
            println("Сошлось за $iter итераций")
            return x
        end
        x -= lr * grad
    end
    println("Достигнуто максимальное число итераций")
    return x
end

x0 = [-1.2, 1.0]
result = gradient_descent(rosenbrock, x0)

println("Найденный минимум: ", result)