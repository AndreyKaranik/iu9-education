function cholesky_decomposition(A::Matrix{Float64})
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

A = [25.0 15.0 -5.0;
     15.0 18.0  0.0;
     -5.0  0.0 11.0]
    
L = cholesky_decomposition(A)

println("Матрица A:")
println(A)
println("Матрица L:")
println(L)
println("Матрица L':")
println(L')
println("Проверка A = L * L':")
println(L * L')