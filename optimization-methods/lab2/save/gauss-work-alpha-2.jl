using LinearAlgebra

# Функция обратного переменного шага для поиска оптимального α
function reverse_variable_step(f, x, e; Δ0=1.0, β=0.5, tol=1e-6, max_iter=10000)
    α0 = 0.0
    α1 = Δ0
    y0 = f(x + α0 * e)
    y1 = f(x + α1 * e)

    for _ in 1:max_iter
        if abs(y0 - y1) < tol
            return (α0 + α1) / 2
        elseif y0 > y1
            α0, y0 = α1, y1
            α1 = α1 + Δ0
            y1 = f(x + α1 * e)
        else
            Δ0 *= -β
            α1 = α0 + Δ0
            y1 = f(x + α1 * e)
        end
    end
    return (α0 + α1) / 2
end

function gauss_seidel(f, x0; tol=1e-6, max_iter=10000)
    x = copy(x0)
    n = length(x)

    for k in 1:max_iter
        x_prev = copy(x)

        for i in 1:n
            e = zeros(n)
            e[i] = 1  # Единичный вектор вдоль i-й координаты
            
            # Оптимизируем по α
            α_opt = reverse_variable_step(f, x, e)
            x += α_opt * e
        end

        if norm(f(x_prev) - f(x)) < tol
            print(k)
            break
        end
    end
    return x
end

# Пример использования
f(x) = sum(x .^ 3)  # Минимизация суммы квадратов (должно сходиться к (0,0))
x0 = [2.0, -10.0]  # Начальная точка

xmin = gauss_seidel(f, x0)
println("Минимум найден в точке: ", xmin)
