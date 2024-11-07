using LinearAlgebra

function check_vieta(A::Matrix{Float64}, eigenvalues::Vector{Float64})
    n = size(A, 1)

    sum_eigenvalues = sum(eigenvalues)
    trace_A = tr(A)
    sum_check = abs(sum_eigenvalues - trace_A) < 1e-6

    product_eigenvalues = prod(eigenvalues)
    det_A = det(A)
    product_check = abs(product_eigenvalues - det_A) < 1e-6

    return sum_check && product_check
end

function check_gershgorin(A::Matrix{Float64}, eigenvalues::Vector{Float64})
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

function check_orthogonality(vectors::Matrix{Float64})
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

function danilevsky(A::Matrix{Float64})
    n = size(A, 1)
    D = copy(A)  # Копируем исходную матрицу
    Bs = I  # Начальная матрица подобия как единичная
    for i in 1:n-1
        B = Matrix{Float64}(I, n, n)  # Инициализируем B как единичную матрицу
        B[n-i, :] = -D[n-i+1, :] ./ D[n-i+1,n-i]  # Заполнение строк B
        B[n-i, n-i] = 1 / D[n-i+1, n-i]  # Заполнение диагонального элемента

        C = D * B  # Применяем преобразование
        B_inv = Matrix{Float64}(I, n, n)  # Обратная матрица B
        B_inv[n-i, :] = D[n-i+1, :]  # Заполняем обратную матрицу
        D = B_inv * C  # Обновляем D
        Bs *= B  # Обновляем матрицу подобия
    end
    return D, Bs  # Возвращаем D (матрица Фробениуса) и Bs (матрица подобия)
end

function check_vieta(A::Matrix{Float64}, eigenvalues::Vector{Float64})
    n = size(A, 1)

    sum_eigenvalues = sum(eigenvalues)
    trace_A = tr(A)
    sum_check = abs(sum_eigenvalues - trace_A) < 1e-6

    product_eigenvalues = prod(eigenvalues)
    det_A = det(A)
    product_check = abs(product_eigenvalues - det_A) < 1e-6

    return sum_check && product_check
end

function random_symmetric_matrix(n::Int)
    A = randn(n, n)
    return (A + A') / 2
end

function main(A::Matrix{Float64})
    if A != A'
        error("Матрица должна быть симметричной.")
    end

    D, Bs = danilevsky(A)
    
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