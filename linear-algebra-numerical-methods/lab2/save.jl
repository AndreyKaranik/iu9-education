using LinearAlgebra
n = 3
A = randn(n, n)
x_original = randn(n)

f = A * x_original

function gauss_elimination(A, f)
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

x_found = gauss_elimination(A, f)

println("Найденный вектор x: $x_found")
println("Исходный вектор x: $x_original")