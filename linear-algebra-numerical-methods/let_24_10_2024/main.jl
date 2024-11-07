function cholesky_decomposition(A)
    n = size(A, 1)
    L = zeros(Float64, n, n)
    
    if A != A'
        throw(ArgumentError("Matrix must be symmetric"))
    end

    for i in 1:n
        for j in 1:i
            if i == j
                L[i, j] = sqrt(A[i, i] - (i > 1 ? sum(L[i, k]^2 for k in 1:(i-1)) : 0.0))
            else
                L[i, j] = (A[i, j] - (j > 1 ? sum(L[i, k] * L[j, k] for k in 1:(j-1)) : 0.0)) / L[j, j]
            end
        end
    end
    return L
end

# L * y = b
function forward_substitution(L, b)
    n = size(L, 1)
    y = zeros(Float64, n)

    for i in 1:n
        y[i] = (b[i] - (i > 1 ? sum(L[i, k] * y[k] for k in 1:(i-1)) : 0.0)) / L[i, i]
    end

    return y
end

# L' * x = y
function backward_substitution(LT, y)
    n = size(LT, 1)
    x = zeros(Float64, n)

    for i in n:-1:1
        x[i] = (y[i] - (i < n ? sum(LT[i, k] * x[k] for k in (i+1):n) : 0.0)) / LT[i, i]
    end

    return x
end

function solve_by_cholesky(A, b)
    L = cholesky_decomposition(A)
    y = forward_substitution(L, b)
    x = backward_substitution(L', y)

    println("Матрица A:")
    println(A)
    println("Матрица L:")
    println(L)
    println("Матрица L':")
    println(L')

    return x
end

A = [25.0 15.0 -5.0;
     15.0 18.0  0.0;
     -5.0  0.0 11.0]

b = [30.0, 18.0, 9.0]

x = solve_by_cholesky(A, b)
println("Решение системы x:")
println(x)
println("Проверка A * x = b:")
println(A * x)