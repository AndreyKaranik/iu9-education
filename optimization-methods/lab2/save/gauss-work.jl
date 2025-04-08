using LinearAlgebra

# Функция обратного переменного шага
function reverse_variable_step(f, x, e, Δ0=1.0, β=0.5, tol=1e-6, max_iter=100)
    x0 = x
    x1 = x + Δ0 * e
    y0, y1 = f(x0), f(x1)
    
    for _ in 1:max_iter
        if abs(y0 - y1) < tol
            return (x0 + x1) / 2
        elseif y0 > y1
            x0, y0 = x1, y1
            x1 = x1 + Δ0 * e
            y1 = f(x1)
        else
            Δ0 *= -β
            x1 = x0 + Δ0 * e
            y1 = f(x1)
        end
    end
    return (x0 + x1) / 2
end

# Метод Гаусса-Зейделя с использованием обратного переменного шага
function gauss_seidel(f, x0; tol=1e-6, max_iter=100)
    x = copy(x0)
    n = length(x)
    
    for k in 1:max_iter
        x_prev = copy(x)
        
        for i in 1:n
            e = zeros(n)
            e[i] = x[i] / norm(x)
            
            x = reverse_variable_step(f, x, e)
        end
        
        if norm(f(x_prev) - f(x)) < tol
            break
        end
    end
    return x
end

# Пример использования
f(x) = sum(x .^ 2)  # Пример: минимизация суммы квадратов координат

x0 = [2.0, -3.0]  # Начальная точка
xmin = gauss_seidel(f, x0)
println("Минимум найден в точке: ", xmin)
