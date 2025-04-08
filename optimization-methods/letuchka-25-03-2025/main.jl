using Plots

# Определяем функцию для неравенств с ограничением x1 >= 0 и x2 >= 0
function region_condition(x1, x2)
    return -x1 + x2 >= -1 && x1 - 2 * x2 <= 1 && x1 >= 0 && x2 >= 0
end

# Создаем сетку для x1 и x2
x1_vals = LinRange(0, 3, 100)  # Ограничиваем x1 >= 0
x2_vals = LinRange(0, 3, 100)  # Ограничиваем x2 >= 0

# Массивы для хранения координат
valid_x1 = Float64[]
valid_x2 = Float64[]
valid_z = Float64[]

# Заполняем массивы точками, которые удовлетворяют неравенствам
for x1 in x1_vals
    for x2 in x2_vals
        if region_condition(x1, x2)
            push!(valid_x1, x1)
            push!(valid_x2, x2)
            push!(valid_z, 1)  # Присваиваем значение 1, если точка в области
        end
    end
end

# Строим 3D-график
scatter3d(valid_x1, valid_x2, valid_z, label="Valid Region", color=:green, alpha=0.5, size=(800, 600))

# Строим линии для неравенств
# Линия для -x1 + x2 = -1, т.е. x2 = x1 - 1
plot!(x1_vals, x1_vals .- 1, 1 .* ones(length(x1_vals)), label="-x1 + x2 = -1", color=:blue, linewidth=2)

# Линия для x1 - 2x2 = 1, т.е. x2 = (x1 - 1)/2
plot!(x1_vals, (x1_vals .- 1) ./ 2, 1 .* ones(length(x1_vals)), label="x1 - 2x2 = 1", color=:red, linewidth=2)

# Отображаем график
display(plot)