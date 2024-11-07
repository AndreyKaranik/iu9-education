import numpy as np

a = 100
b = 1
n = 2
f0 = 0

def rosenbrock(x):
    return sum(a * ((x[i]**2 - x[i + 1]))**2 + b * ((x[i] - 1))**2 + f0 for i in range(len(x) - 1))

def rosenbrock_grad(x):
    grad = np.zeros_like(x)
    grad[0] = -400 * x[0] * (x[1] - x[0]**2) - 2 * (1 - x[0])
    grad[-1] = 200 * (x[-1] - x[-2]**2)
    for i in range(1, len(x) - 1):
        grad[i] = 200 * (x[i] - x[i-1]**2) - 400 * x[i] * (x[i+1] - x[i]**2) - 2 * (1 - x[i])
    return grad

def conjugate_gradient_fr(x0, tol=1e-6, max_iter=1000):
    x = x0
    grad = rosenbrock_grad(x)
    d = -grad
    for i in range(max_iter):
        alpha = line_search(rosenbrock, x, d)
        x_new = x + alpha * d
        grad_new = rosenbrock_grad(x_new)

        if np.linalg.norm(grad_new) < tol:
            break
        
        beta = np.dot(grad_new, grad_new) / np.dot(grad, grad)
        d = -grad_new + beta * d
        x, grad = x_new, grad_new
    return x, rosenbrock(x)

# Метод сопряжённых градиентов (Полака-Рибьера)
def conjugate_gradient_pr(x0, tol=1e-6, max_iter=1000):
    x = x0
    grad = rosenbrock_grad(x)
    d = -grad
    for i in range(max_iter):
        alpha = line_search(rosenbrock, x, d)
        x_new = x + alpha * d
        grad_new = rosenbrock_grad(x_new)
        
        # Условие выхода
        if np.linalg.norm(grad_new) < tol:
            break
        
        # Коэффициент Полака-Рибьера
        beta = np.dot(grad_new, grad_new - grad) / np.dot(grad, grad)
        d = -grad_new + beta * d
        x, grad = x_new, grad_new
    return x, rosenbrock(x)

# Линейный поиск для нахождения подходящего шага alpha
def line_search(f, x, d, alpha=1.0, tau=0.5, c=1e-4):
    while f(x + alpha * d) > f(x) + c * alpha * np.dot(rosenbrock_grad(x), d):
        alpha *= tau
    return alpha

# Начальная точка и параметры
n = 5  # Размерность пространства
x0 = np.random.randn(n)  # Начальная точка
tol = 1e-6

# Запуск метода Флетчера-Ривза
min_fr, f_min_fr = conjugate_gradient_fr(x0, tol)
print("Метод Флетчера-Ривза:")
print("Минимум функции:", f_min_fr)
print("Точка минимума:", min_fr)

# Запуск метода Полака-Рибьера
min_pr, f_min_pr = conjugate_gradient_pr(x0, tol)
print("\nМетод Полака-Рибьера:")
print("Минимум функции:", f_min_pr)
print("Точка минимума:", min_pr)