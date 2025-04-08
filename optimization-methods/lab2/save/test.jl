using CSV
using DataFrames

function save_trajectory_to_file(traj, filename)
    CSV.write(filename, DataFrame(x1=[point[1] for point in traj], x2=[point[2] for point in traj]))
end

# Пример траектории (используйте свою реальную траекторию)
traj_cd = [[40.0, -50.0], [10.0, -10.0], [1.0, 1.0]]  # Траектория для покоординатного спуска

# Сохраняем траекторию в файл
save_trajectory_to_file(traj_cd, "trajectory_cd.csv")