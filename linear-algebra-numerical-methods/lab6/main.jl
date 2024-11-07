R1 = 1000.0
R2 = 1.0
R3 = 1.0
R4 = 1.0
R5 = 1.0
R6 = 1.0
q = 9.0

A = [
    0 0 1;
    1 0 -1;
    (1/(R1 + R4) + 1/R2 + 1/(R3 + R5)) (-1/(R1 + R4) - 1/R2 - 1/(R3+R5) - 1/R6) 1/R6
]

b = [0, q, 0]

x = A \ b

x1, x2, x3 = x
println("Решение: x1 = $x1, x2 = $x2, x3 = $x3")

I0 = (x2 - x3) / R6
I1 = (x1 - x2) / (R1 + R4)
I2 = (x1 - x2) / R2
I3 = (x1 - x2) / (R3 + R5)

println("Токи:")
println("I0 = $I0")
println("I1 = $I1")
println("I2 = $I2")
println("I3 = $I3")