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
    sum_check = abs(sum_eigenvalues - trace_A) < 1e-6

    product_eigenvalues = prod(eigenvalues)
    det_A = det(A)
    product_check = abs(product_eigenvalues - det_A) < 1e-6

    return sum_check && product_check
end

function check_gershgorin(A::Matrix{T}, eigenvalues::Vector{T}) where T
    n = size(A, 1)
    for λ in eigenvalues
        belongs_to_circle = false
        for i in 1:n
            center = A[i, i]
            radius = sum(abs(A[i, j]) for j in 1:n if j != i)
            if abs(λ - center) <= radius
                belongs_to_circle = true
                break
            end
        end
        if !belongs_to_circle
            return false
        end
    end
    return true
end

function check_orthogonality(vectors::Matrix{T}) where T
    n = size(vectors, 2)
    for i in 1:n
        for j in i+1:n
            if abs(vectors[:, i] ⋅ vectors[:, j]) > 1e-6
                return false
            end
        end
    end
    return true
end

function random_symmetric_matrix(n::Int)
    A = randn(n, n)
    return (A + A') / 2
end

function main(A::Matrix{Float64})
    if A != A'
        error("Матрица должна быть симметричной.")
    end
    
    eigenvalues = characteristic_polynomial(A)
    eigenvecs = calculate_eigenvectors(A)
    
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
    
    if check_orthogonality(eigenvecs)
        println("Собственные векторы ортогональны.")
    else
        println("Собственные векторы не ортогональны.")
    end
    
    println("Собственные значения: ", eigenvalues)
    println("Собственные векторы: ", eigenvecs)
end

A = [4.0 1.0 2.0 3.0; 
     1.0 3.0 0.0 1.0; 
     2.0 0.0 2.0 0.0; 
     3.0 1.0 0.0 5.0]

println("Матрица: A")
main(A)
random_matrix = random_symmetric_matrix(4)
println("Рандомная матрица: (...)")
main(random_matrix)