import numpy as np
import matplotlib.pyplot as plt

def newton_system(F, J, x0, err=0.01):
    x = x0
    i = 0
    while True:
        delta_x = np.linalg.solve(J(x), -F(x))
        x = x + delta_x
        if np.linalg.norm(delta_x) < err:
            return x, i + 1
        i += 1
def F(x):
    return np.array([
        np.cos(x[1] + 0.5) - x[0] - 2,
        np.sin(x[0]) - 2 * x[1] - 1
    ])

def J(x):
    return np.array([
        [-1, -np.sin(x[1] + 0.5)],
        [np.cos(x[0]), -2]
    ])

x0 = np.array([-1, -1])

solution, number = newton_system(F, J, x0)

print("Solution:", solution)
print("Iterations:", number)

