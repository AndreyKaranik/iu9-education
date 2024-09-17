using PyPlot
x = range(-5, stop=5, length=1000);
y = range(-5, stop=5, length=1000);
z = @. (x^4) / 16 + (y / 4)'^4 - (x'^2 * y^2) / 8
surf(x, y, z)
PyPlot.display_figs()
