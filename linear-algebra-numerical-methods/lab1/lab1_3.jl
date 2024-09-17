using PyPlot
u = range(-2π, stop=2π, length=50);
v = range(-π, stop=π, length=50);
x = [cos(u[i]) * (cos(v[j] + 3)) for i in 1:length(u), j in 1:length(v)];
y = [sin(u[i]) * (cos(v[j] + 3)) for i in 1:length(u), j in 1:length(v)];
z = [u[i] + (sin(v[j])) for i in 1:length(u), j in 1:length(v)];
surf(x, y, z)
PyPlot.display_figs()
