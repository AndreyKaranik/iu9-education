using LinearAlgebra

function characteristic_polynomial(A::Matrix{T}) where T
    if A != A'
        error("Матрица должна быть симметричной.")
    end
    eigenvalues = eigen(A).values
    return eigenvalues
end

function calculate_eigenvectors(A::Matrix{T}) where T
    V = eigen(A).vectors
    return V
end

function check_vieta(A::Matrix{T}, eigenvalues::Vector{T}) where T
    n = size(A, 1)
    sum_eigenvalues = sum(eigenvalues)
    trace_A = tr(A)
    return abs(sum_eigenvalues - trace_A) < 1e-6
end

function check_gershgorin(A::Matrix{T}, eigenvalues::Vector{T}) where T
    n = size(A, 1)
    for i in 1:n
        center = A[i, i]
        radius = sum(abs(A[i, j]) for j in 1:n if j != i)
        for λ in eigenvalues
            if abs(λ - center) > radius
                return false
            end
        end
    end
    return true
end

function main(A::Matrix{Float64})

    if A != A'
        error("Матрица должна быть симметричной.")
    end
    
    eigenvalues = characteristic_polynomial(A)
    eigenvectorss = calculate_eigenvectors(A)

    if check_vieta(A, eigenvalues)
        println("Теорема Виета выполнена.")
    else
        println("Теорема Виета не выполнена.")
    end

    if check_gershgorin(A, eigenvalues)
        println("Теорема Гершгорина выполнена.")
    else
        println("Теорема Гершгорина не выполнена.")
    end

    println("Собственные значения: ", eigenvalues)
    println("Собственные векторы: ", eigenvectorss)
end

A = [4.0 1.0 2.0 3.0; 
     1.0 3.0 0.0 1.0; 
     2.0 0.0 2.0 0.0; 
     3.0 1.0 0.0 5.0]

main(A)