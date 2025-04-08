using LinearAlgebra

function coordinate_descent(f, x0; step_size=0.1, tol=1e-6, max_iter=1000)
    x = copy(x0)
    n = length(x0)
    iter_count = 0
    
    for iter in 1:max_iter
        x_old = copy(x)

        for i in 1:n
            f_current = f(x)
            x_forward = copy(x); x_forward[i] += step_size
            x_backward = copy(x); x_backward[i] -= step_size

            f_forward = f(x_forward)
            f_backward = f(x_backward)

            if f_forward < f_current && f_forward <= f_backward
                x[i] += step_size
            elseif f_backward < f_current
                x[i] -= step_size
            end
        end
        
        if norm(x - x_old) < tol
            iter_count = iter
            break
        end
    end
    
    println("Число итераций метода покоординатного спуска: ", iter_count)
    return x
end

# Функция обратного переменного шага для поиска оптимального α
function reverse_variable_step(f, x, e; Δ0=1.0, β=0.5, tol=1e-6, max_iter=10000)
    α0 = 0.0
    α1 = Δ0
    y0 = f(x + α0 * e)
    y1 = f(x + α1 * e)
    iter_count = 0

    for k in 1:max_iter
        if abs(y0 - y1) < tol
            iter_count = k
            break
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
    iter_count = 0

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
            iter_count = k
            break
        end
    end
    println("Число итераций метода Гаусса-Зейделя: ", iter_count)
    return x
end

function hooke_jeeves(f, x0; Δ=1.0, β=0.5, tol=1e-6, max_iter=100)
    x = copy(x0)
    n = length(x)
    Δx = fill(Δ, n)  # Вектор шагов по каждой координате
    iter_count = 0

    while maximum(abs.(Δx)) > tol && iter_count < max_iter
        x_prev = copy(x)
        x_new = copy(x)

        # Исследующий поиск
        for i in 1:n
            x_test = copy(x_new)
            x_test[i] += Δx[i]
            if f(x_test) < f(x_new)
                x_new = x_test
            else
                x_test[i] = x_new[i] - Δx[i]  # Пробуем в другую сторону
                if f(x_test) < f(x_new)
                    x_new = x_test
                end
            end
        end

        if x_new == x  # Если все шаги неудачны
            Δx *= β   # Уменьшаем шаги
        else
            # Движение по образцу
            x = 2 * x_new - x_prev
            if f(x) > f(x_new)  # Если движение по образцу не уменьшает f
                x = x_new  # Оставляем только исследующий поиск
            end
        end
        iter_count += 1
    end
    println("Число итераций метода Хука-Дживса: ", iter_count)
    return x
end

f(x) = sum(x .^ 2)  # Квадратичная функция
x0 = [40.0, -50.0]    # Начальная точка
true_minimum = zeros(length(x0))

# Запуск методов с замером времени
println("\n--- Покоординатный спуск ---")
@time xmin_cd = coordinate_descent(f, x0)
println("Минимум найден в точке: ", xmin_cd)
println("Погрешность: ", norm(xmin_cd - true_minimum))

println("\n--- Гаусс-Зейдель ---")
@time xmin_gs = gauss_seidel(f, x0)
println("Минимум найден в точке: ", xmin_gs)
println("Погрешность: ", norm(xmin_gs - true_minimum))

println("\n--- Хук-Дживс ---")
@time xmin_hj = hooke_jeeves(f, x0)
println("Минимум найден в точке: ", xmin_hj)
println("Погрешность: ", norm(xmin_hj - true_minimum))
