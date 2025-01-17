---
year: 2024
author: "Караник А.А."
group: "ИУ9-72Б"
teacher: "Посевин Д.П."
subject: "Численные методы линейной алгебры"
name: "Реализация сингулярного разложения матрицы (SVD)"
number: "9"
---

# Цель работы

Целью работы является самостоятельное изучение сингулярного разложения матрицы или SVD разложения.

# Реализация

Исходный код программы:
```julia
using LinearAlgebra

function custom_svd(A)
    eigen_vals, V = eigen(A' * A)

    sorted_indices = sortperm(eigen_vals, rev=true)
    eigen_vals = eigen_vals[sorted_indices]
    
    V = V[:, sorted_indices]

    S = sqrt.(clamp.(eigen_vals, 1e-5, Inf))

    U = (A * V) ./ S'

    return U, S, V
end

function verify_svd(U, S, V, original_matrix)
    reconstructed_matrix = U * Diagonal(S) * V'
    is_correct = norm(original_matrix - reconstructed_matrix) < 1e-5
    return is_correct, reconstructed_matrix
end

function compare_svd(A)
    U_custom, S_custom, V_custom = custom_svd(A)
    U_lib, S_lib, V_lib = svd(A)
    is_correct_custom, reconstructed_custom = verify_svd(U_custom, S_custom, V_custom, A)
    is_correct_lib, reconstructed_lib = verify_svd(U_lib, S_lib, V_lib, A)
    return (U_custom, S_custom, V_custom, is_correct_custom, 
            U_lib, S_lib, V_lib, is_correct_lib,
            reconstructed_custom, reconstructed_lib)
end

A = [1 2 3; 4 5 6; 7 8 9]

U_custom, S_custom, V_custom, is_correct_custom,
U_lib, S_lib, V_lib, is_correct_lib,
reconstructed_custom, reconstructed_lib = compare_svd(A)

println("Собственная реализация:")
println("U:")
println(U_custom)
println("\nS:")
println(S_custom)
println("\nV:")
println(V_custom)

println("\nКорректность: ", is_correct_custom)

println("\nБиблиотечная реализация:")
println("U:")
println(U_lib)
println("\nS:")
println(S_lib)
println("\nV:")
println(V_lib)
println("\nКорректность: ", is_correct_lib)

println("\nСравнение восстановленных матриц:")
println("Собственная:")
println(reconstructed_custom)
println("\nБиблиотечная:")
println(reconstructed_lib)
```

# Результаты

![результаты](temp/1.png){width=10cm}

# Вывод

В ходе выполенения данной лабораторной работы было изучено сингулярное разложение матрицы или SVD разложение.