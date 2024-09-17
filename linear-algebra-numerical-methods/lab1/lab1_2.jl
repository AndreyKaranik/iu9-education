using PyPlot
u = range(0, stop=2π, length=50)
v = range(0, stop=π, length=50)
x = [cos(u[i]) * sin(v[j]) for i in 1:length(u), j in 1:length(v)]
y = [sin(u[i]) * sin(v[j]) for i in 1:length(u), j in 1:length(v)]
z = [cos(v[j]) for i in 1:length(u), j in 1:length(v)]
surf(x, y, z)
PyPlot.display_figs()
