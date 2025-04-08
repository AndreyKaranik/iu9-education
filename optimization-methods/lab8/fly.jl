using LinearAlgebra

x = [0.72, -0.008]

B = I(2)

y = [-8.624, -35.488]
s = [-0.28, -1.008]

rho = 1 / dot(y, s)
B = (I - rho * s * y') * B * (I - rho * y * s') + rho * s * s'

print(B)