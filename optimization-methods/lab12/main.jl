using Images, Colors, Plots

# Определяем палитру цветов радуги
rainbow_palette = [RGB(1,0,0), RGB(1,0.65,0), RGB(1,1,0), 
                  RGB(0,1,0), RGB(0,0,1), RGB(0.29,0,0.51), RGB(0.93,0.51,0.93)]

# Функция для создания матрицы 3x3 из индексов палитры
function create_random_matrix()
    return rand(1:length(rainbow_palette), 3, 3)
end

# Эталонная матрица (красивый паттерн)
reference_matrix = [1 2 3;
                   4 5 6;
                   7 1 4]

# Преобразование матрицы в бинарное представление
function to_binary_matrix(matrix)
    return [bitstring(x-1)[end-2:end] for x in matrix]
end

# Кроссовер двух матриц
function crossover(parent1, parent2)
    child = copy(parent1)
    for i in 1:3, j in 1:3
        if rand() < 0.5
            child[i,j] = parent2[i,j]
        end
    end
    return child
end

# Мутация матрицы
function mutate(matrix, mutation_rate=0.1)
    result = copy(matrix)
    for i in 1:3, j in 1:3
        if rand() < mutation_rate
            result[i,j] = rand(1:length(rainbow_palette))
        end
    end
    return result
end

# Расстояние Хэмминга (булево)
function hamming_distance(m1, m2)
    return sum(m1 .!= m2)
end

# Евклидово расстояние
function euclidean_distance(m1, m2)
    return sqrt(sum((m1 .- m2).^2))
end

# Преобразование матрицы в изображение
function matrix_to_image(matrix)
    img = zeros(RGB, 3, 3)
    for i in 1:3, j in 1:3
        img[i,j] = rainbow_palette[matrix[i,j]]
    end
    return img
end

# Основной генетический алгоритм
function genetic_algorithm(start_matrix, ref_matrix; 
                         population_size=50, 
                         max_generations=100, 
                         epsilon=0.1)
    
    # Создаем начальную популяцию
    population = [mutate(start_matrix) for _ in 1:population_size]
    frames = [matrix_to_image(start_matrix)]
    
    for generation in 1:max_generations
        # Оценка приспособленности
        fitness = [hamming_distance(ind, ref_matrix) for ind in population]
        # Можно использовать euclidean_distance вместо hamming_distance
        
        # Проверка условия остановки
        best_idx = argmin(fitness)
        if fitness[best_idx] < epsilon
            break
        end
        
        # Селекция (турнирная)
        new_population = []
        for _ in 1:population_size
            parent1 = population[rand(1:population_size)]
            parent2 = population[rand(1:population_size)]
            child = crossover(parent1, parent2)
            child = mutate(child)
            push!(new_population, child)
        end
        
        population = new_population
        push!(frames, matrix_to_image(population[best_idx]))
    end
    
    return frames
end

# Основная программа
function main()
    # Создаем начальную случайную матрицу
    start_matrix = create_random_matrix()
    
    # Показываем эталонное изображение
    ref_img = matrix_to_image(reference_matrix)
    display(ref_img)
    println("Эталонное изображение")
    
    # Запускаем генетический алгоритм и создаем анимацию
    frames = genetic_algorithm(start_matrix, reference_matrix)
    
    # Создаем и сохраняем GIF
    anim = @animate for frame in frames
        plot(frame, axis=false, margin=0)
    end
    
    gif(anim, "evolution.gif", fps=5)
    println("GIF сохранен как evolution.gif")
end

# Запускаем программу
main()