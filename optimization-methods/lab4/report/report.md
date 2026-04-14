---
year: 2025
author: "Караник А.А."
group: "ИУ9-82Б"
teacher: "Посевин Д.П."
subject: "Методы оптимизации"
name: "Поиск минимума унимодальной функции"
number: "4"
---

# Цель работы

Определить интервал, на котором функция является унимодальной, алгоритм определения унимодальности должен принимать на вход левую и правую точку отрезка и возвращать false — если функция на этом отрезке не унимодальная, в противном случае true.  
Реализовать поиск минимума унимодальной функции на полученном интервале методом прямого перебора, дихотомии (деление отрезка пополам), золотого сечения и Фибоначчи с заданной точностью по вариантам. Результат должен быть представлен на графике, точки минимизирующей последовательности должны быть выделены красным цветом, интервалы деления синим. 
Точность вычисления точки минимума должна варьироваться.
 

# Реализация

Исходный код:
```julia
using Printf
using Random
using Dates
using Plots

function f(x)
    return (x+1)^2 * exp(2*x);
end

function df(x)
    return 6 * x * exp(2*x) + 4*exp(2*x) + 2 * x^2 * exp(2*x)
end

function d2f(x)
    return 14 * exp(2*x) + 16 * x * exp(2*x) + 4 * x^2 * exp(2*x)
end

function brute_force_method(df, a, b, num_points=10)
    steps = []
    dx = (b - a) / num_points
    x_prev = a
    y_prev = df(a)

    for i in 1:num_points
        x = a + i * dx
        y = df(x)
        push!(steps, (x_prev, x))

        if y_prev * y <= 0
            return (x_prev + x) / 2, steps
        end

        x_prev, y_prev = x, y
    end

    return nothing, steps
end

function bisection_method(f, a, b)
    if f(a) * f(b) > 0
        return nothing, []
    end
    steps = []
    while abs(b - a) > 1e-3
        c = (a + b) / 2
        push!(steps, (a, b))
        if f(c) == 0
            return c, steps
        elseif f(a) * f(c) < 0
            b = c
        else
            a = c
        end
    end
    return (a + b) / 2, steps
end

function golden_section_method(f, a, b)
    if f(a) * f(b) > 0
        return nothing, []
    end

    φ = (1 + sqrt(5)) / 2
    steps = []
    
    x1 = b - (b - a) / φ
    x2 = a + (b - a) / φ
    
    while abs(b - a) > 1e-3
        push!(steps, (a, b))
        
        if abs(f(x1)) < abs(f(x2))
            b = x2
        else
            a = x1
        end
        
        x1 = b - (b - a) / φ
        x2 = a + (b - a) / φ
    end
    
    return (a + b) / 2, steps
end

function fibonacci_method(f, a, b, n=20)
    if f(a) * f(b) > 0
        return nothing, []
    end

    fib = [1, 1]
    for i in 3:n+1
        push!(fib, fib[end] + fib[end-1])
    end

    steps = []
    
    x1 = a + (b - a) * fib[n-1] / fib[n+1]
    x2 = a + (b - a) * fib[n] / fib[n+1]
    
    for k in 1:n-1
        push!(steps, (a, b))
        
        if abs(f(x1)) < abs(f(x2))
            b = x2
        else
            a = x1
        end
        
        if n-k-1 > 0
            x1 = a + (b - a) * fib[n-k-1] / fib[n-k+1]
            x2 = a + (b - a) * fib[n-k] / fib[n-k+1]
        end
    end
    
    return (a + b) / 2, steps
end

function find_critical_points(method, a, b, n)
    critical_points = []
    x = a
    while x < b
        x_next = x + (b - a) / n
        if x_next > b
            break
        end
        root, steps = method(df, x, x_next)
        if root !== nothing
            push!(critical_points, (root, steps))
        end
        x = x_next
    end
    return critical_points
end

function check_unimodality(method, a, b, n)
    t_start = now()
    crit_points = find_critical_points(method, a, b, n)
    elapsed_time = now() - t_start

    if isempty(crit_points)
        println("Метод $(method): нет критических точек, функция монотонна")
        return true, elapsed_time, crit_points
    end

    second_derivatives = [d2f(c[1]) for c in crit_points]

    println("Метод $(method):")
    println("Критические точки: ", [c[1] for c in crit_points])
    println("Значения второй производной: ", second_derivatives)

    if all(d -> d > 0, second_derivatives) || all(d -> d < 0, second_derivatives)
        println("Функция унимодальна (имеет единственный экстремум).")
        return true, elapsed_time, crit_points
    else
        println("Функция не унимодальна (есть и минимум, и максимум).")
        return false, elapsed_time, crit_points
    end
end

function plot_method(f, a, b, method_name, crit_points)
    x = a:0.1:b
    y = f.(x)
    
    p = plot(x, y, label="f(x)", xlabel="x", ylabel="f(x)", title="$method_name", linewidth=2, legend=false)

    ymin = minimum(y) - 1

    plot!(p, xlims=(a, b), ylims=(ymin, maximum(y) + 1))
    hline!(y=0, linestyle=:solid, linewidth=1)
    vline!(x=0, linestyle=:solid, linewidth=1)

    for (root, steps) in crit_points
        for step in steps
            plot!(p, [step[1], step[1]], [ymin, f(step[1])], color=:blue, linewidth=1, label="")
            plot!(p, [step[2], step[2]], [ymin, f(step[2])], color=:blue, linewidth=1, label="")
            midpoint = (step[1] + step[2]) / 2
            scatter!(p, [midpoint], [f(midpoint)], color=:red, markersize=3)
        end
        scatter!(p, [root], [f(root)], color=:red, label="Точка экстремума", markersize=4)
    end
    
    display(p)
end

methods = [brute_force_method, bisection_method, golden_section_method, fibonacci_method]
method_names = ["Метод прямого перебора", "Метод дихотомии", "Метод золотого сечения", "Метод Фибоначчи"]
a = -2.55
b = -0.5
results = Dict()

all_critical_points = Dict()

for (method, method_name) in zip(methods, method_names)
    println("\n==============================")
    println("Тест метода: ", method_name)
    unimodal, time_taken, crit_points = check_unimodality(method, a, b, 7)
    all_critical_points[method_name] = crit_points
    results[method] = time_taken
end

println("\n==============================")
println("Сравнение методов по времени выполнения:")
for (method, time) in results
    @printf "%s: %s\n" method time
end

for (method_name, crit_points) in all_critical_points
    plot_method(f, a, b, method_name, crit_points)
end
```

# Результаты

![результаты](1.png){width=10cm}

![результаты](2.png){width=10cm}

![результаты](3.png){width=10cm}

![результаты](4.png){width=10cm}

# Вывод

В процессе выполнения данной лабораторной работы были выполнены 4 метода поиска минимума функции: пополам, золотого сечения, Фибоначчи, прямого перебора. Перед применением методов функция проверена на унимодальность.
