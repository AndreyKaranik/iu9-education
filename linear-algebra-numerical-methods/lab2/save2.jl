using LinearAlgebra
using PyPlot

n = 100
range_min = -10000
range_max = 10000
A = rand(n, n) * (range_max - range_min) .+ range_min
x_original = rand(n) * (range_max - range_min) .+ range_min
f = A * x_original

function gauss_classic(A, f)
    A_aug = hcat(A, f)
    n = size(A, 1)
    
    for i in 1:n
        pivot = A_aug[i, i]
        A_aug[i, :] /= pivot
        
        for j in i+1:n
            factor = A_aug[j, i]
            A_aug[j, :] -= factor * A_aug[i, :]
        end
    end

    x = zeros(n)
    for i in n:-1:1
        x[i] = A_aug[i, end] - A_aug[i, i+1:end-1]' * x[i+1:end]
    end
    
    return x
end

function gauss_by_rows(A, f)
    n = size(A, 1)
    A_aug = [A f]
    row_perm = collect(1:n)

    for k in 1:n-1

        max_col = argmax(abs.(A_aug[k, k:n])) + k - 1

        if max_col != k
            A_aug[:, [k, max_col]] = A_aug[:, [max_col, k]]
            row_perm[[k, max_col]] = row_perm[[max_col, k]]
        end

        for i in k+1:n
            factor = A_aug[i, k] / A_aug[k, k]
            A_aug[i, k:end] -= factor * A_aug[k, k:end]
        end
    end

    x = zeros(n) 
    for j in n:-1:1
        x[j] = (A_aug[j, end] - A_aug[j, j+1:end-1]' * x[j+1:end]) / A_aug[j, j]
    end

    return x[row_perm]
end

function gauss_by_columns(A, f)
    n = size(A, 1)
    A_aug = [A f]

    for k in 1:n-1
        max_row = argmax(abs.(A_aug[k:n, k])) + k - 1

        if max_row != k
            A_aug[[k, max_row], :] = A_aug[[max_row, k], :]
        end

        for i in k+1:n
            factor = A_aug[i, k] / A_aug[k, k]
            A_aug[i, k:end] -= factor * A_aug[k, k:end]
        end
    end

    x = zeros(n)
    for j in n:-1:1
        x[j] = (A_aug[j, end] - A_aug[j, j+1:end-1]' * x[j+1:end]) / A_aug[j, j]
    end

    return x
end

function gauss_combined(A, f)
    n = size(A, 1)
    A_aug = [A f]
    row_perm = collect(1:n)
    col_perm = collect(1:n)

    for k in 1:n-1

        max_val, max_index = findmax(abs.(A_aug[k:n, k:n]))
        max_row, max_col = max_index[1] + k - 1, max_index[2] + k - 1

        if max_row != k
            A_aug[[k, max_row], :] = A_aug[[max_row, k], :]
            row_perm[[k, max_row]] = row_perm[[max_row, k]]
        end

        if max_col != k
            A_aug[:, [k, max_col]] = A_aug[:, [max_col, k]]
            col_perm[[k, max_col]] = col_perm[[max_col, k]]
        end

        for i in k+1:n
            factor = A_aug[i, k] / A_aug[k, k]
            A_aug[i, k:end] -= factor * A_aug[k, k:end]
        end
    end

    x = zeros(n)
    for j in n:-1:1
        x[j] = (A_aug[j, end] - A_aug[j, j+1:end-1]' * x[j+1:end]) / A_aug[j, j]
    end

    result = zeros(n)
    for i in 1:n
        result[col_perm[i]] = x[i]
    end

    return result
end



function relative_error(x_original, x_found)
    return norm(x_original - x_found) / norm(x_original)
end

x_classic = gauss_classic(A, f)
x_by_rows = gauss_by_rows(A, f)
x_by_columns = gauss_by_columns(A, f)
x_combined = gauss_combined(A, f)

error_classic = relative_error(x_original, x_classic)
error_by_rows = relative_error(x_original, x_by_rows)
error_by_columns = relative_error(x_original, x_by_columns)
error_combined = relative_error(x_original, x_combined)

println("Классический метод (оценка): $error_classic")
println("Метод по строкам (оценка): $error_by_rows")
println("Метод по столбцам (оценка): $error_by_columns")
println("Комбинированный метод (оценка): $error_combined")

# println("Оригинальный вектор x: $x_original")
# println("Классический метод: $x_classic")
# println("Метод по строкам: $x_by_rows")
# println("Метод по столбцам: $x_by_columns")
# println("Комбинированный метод: $x_combined")