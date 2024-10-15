---
year: 2024
author: "Караник А.А."
group: "ИУ9-72Б"
teacher: "Посевин Д.П."
subject: "Численные методы линейной алгебры"
name: "Относительные и вычислительные погрешности"
number: 4
---

# Цель работы

Целью данной лабораторной работы является изучение относительных и вычислительных погрешностей при решении систем линейных алгебраических уравнений (СЛАУ), а также анализ влияния малых возмущений в данных (погрешности в матрице коэффициентов и векторах правой части) на решение. Работа также включает оценку числа обусловленности матриц и сравнение оцененных и фактических относительных погрешностей решения.

# Реализация

Исходный код программы:
```julia
using LinearAlgebra

function inf_norm_vector(vec)
    return maximum(abs.(vec))
end

function inf_norm_matrix(mat)
    return maximum([sum(abs.(mat[i, :])) for i in 1:size(mat, 1)])
end

function predefined_system()
    coeff_matrix = [924 176; 66 744]
    solution_vector = [0.3, 1.05]
    force_vector = [462, 801]
    delta_coeff = [0 0; 0 0]
    delta_force = [1, 1]
    return coeff_matrix, solution_vector, force_vector, delta_coeff, delta_force
end

function random_system(n)
    coeff_matrix = rand(-100.0:100.0, n, n)
    solution_vector = rand(n)
    force_vector = coeff_matrix * solution_vector
    delta_coeff = rand(n, n)
    delta_force = rand(n)
    return coeff_matrix, solution_vector, force_vector, delta_coeff, delta_force
end

function condition_number_inf(mat)
    inv_mat = inv(mat)
    return inf_norm_matrix(inv_mat) * inf_norm_matrix(mat)
end

function estimate_relative_error(A, mu_A, f, delta_A, delta_f)
    rel_f = inf_norm_vector(delta_f) / inf_norm_vector(f)
    rel_A = inf_norm_matrix(delta_A) / inf_norm_matrix(A)
    estimated_rel_error = mu_A * (rel_f + rel_A)
    return estimated_rel_error
end

function compute_actual_relative_error(A, x_true, f, delta_A, delta_f)
    perturbed_A = A + delta_A
    perturbed_f = f + delta_f
    x_perturbed = inv(perturbed_A) * perturbed_f
    delta_x = x_true - x_perturbed
    actual_rel_error = inf_norm_vector(delta_x) / inf_norm_vector(x_true)
    return actual_rel_error
end

function compare_errors(A, x, f, delta_A, delta_f)
    mu_A = condition_number_inf(A)
    estimated_error = estimate_relative_error(A, mu_A, f, delta_A, delta_f)
    actual_error = compute_actual_relative_error(A, x, f, delta_A, delta_f)
    comparison = actual_error <= estimated_error ? "≤" : "≥"
    println("Число обусловленности = $mu_A")
    println("Реальная относительная ошибка: $actual_error $comparison Оцененная ошибка: $estimated_error")
end

function gaussian_elimination(A, b)
    n = size(A, 1)
    A = copy(A)
    b = copy(b)

    max_coeff = maximum(abs.(A))
    row_ratios = zeros(n-1)

    for i in 1:n-1
        for j in i+1:n
            factor = A[j, i] / A[i, i]
            A[j, i:end] .-= factor * A[i, i:end]
            b[j] -= factor * b[i]
        end
        row_ratios[i] = maximum(abs.(A[i, :])) / max_coeff
    end

    g_A = maximum(row_ratios)

    solution = zeros(n)
    for i in n:-1:1
        solution[i] = b[i] / A[i, i]
        for j in i-1:-1:1
            b[j] -= A[j, i] * solution[i]
        end
    end

    return solution, g_A
end

N = 100
A_rand, x_rand, f_rand, delta_A_rand, delta_f_rand = random_system(N)
println("\nРандомная матрица: (...)")
compare_errors(A_rand, x_rand, f_rand, delta_A_rand, delta_f_rand)
A_lab, x_lab, f_lab, delta_A_lab, delta_f_lab = predefined_system()
println("Система из лабораторной работы: (...)")
compare_errors(A_lab, x_lab, f_lab, delta_A_lab, delta_f_lab)
```

# Результаты

![результаты](temp/1.png){width=10cm}

# Вывод

В ходе выполнения лабораторной работы было показано, что величина погрешностей при решении СЛАУ напрямую зависит от числа обусловленности матрицы коэффициентов.