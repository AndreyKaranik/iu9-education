import numpy as np
import time
import matplotlib.pyplot as plt

a, b, f0 = 500, 5, 15

def F(x):
    return a * (x[0]**2 - x[1])**2 + b * (x[0] - 1)**2 + f0

def dF(x):
    return np.array([2 * a * (x[0]**2 - x[1]) * 2 * x[0] + 2 * b * (x[0] - 1), -2 * a * (x[0]**2 - x[1])])

def gradient_descent(func, grad_func, x0, eps1=1e-6, eps2=1e-16, max_iters=10000):
    alpha = 0.001
    x = x0
    iter = 0
    second_time = False
    for i in range(max_iters):
        grad = grad_func(x)
        if np.any(np.isnan(grad)):
            break
        prev_x = x.copy()
        x -= alpha * grad
        if np.linalg.norm(x - prev_x) < eps1 and abs(func(x) - func(prev_x)) < eps2:
            if second_time:
                break
            else:
                second_time = True
        else:
            second_time = False
        iter += 1
    return x, iter

def fletcher_reeves(func, grad_func, x0, eps1=1e-6, eps2=1e-16, max_iters=10000):
    alpha = 1e-6
    x = x0
    d = -grad_func(x)
    grad_prev = grad_func(x)
    iter = 0
    second_time = False
    for i in range(max_iters):
        grad = grad_func(x)
        if np.any(np.isnan(grad)):
            break
        prev_x = x.copy()
        x += alpha * d
        if np.linalg.norm(x - prev_x) < eps1 and abs(func(x) - func(prev_x)) < eps2:
            if second_time:
                break
            else:
                second_time = True
        else:
            second_time = False
        grad_new = grad_func(x)
        denominator = np.dot(grad_prev, grad_prev)
        if denominator == 0:
            beta = 0
        else:
            beta = np.dot(grad_new, grad_new) / denominator
        d = -grad_new + beta * d
        grad_prev = grad_new
        iter += 1
    return x, iter

def polak_ribiere(func, grad_func, x0, eps1=1e-6, eps2=1e-16, max_iters=100000):
    alpha = 1e-6
    x = x0
    grad_prev = grad_func(x)
    d = -grad_prev
    iter = 0
    second_time = False
    step_count = 0

    for i in range(max_iters):
        grad = grad_func(x)
        if np.any(np.isnan(grad)):
            break
        prev_x = x.copy()
        x += alpha * d
        if np.linalg.norm(x - prev_x) < eps1 and abs(func(x) - func(prev_x)) < eps2:
            if second_time:
                break
            else:
                second_time = True
        else:
            second_time = False
        grad_new = grad_func(x)
        y = grad_new - grad_prev
        denominator = np.dot(grad_prev, grad_prev)
        if denominator == 0:
            beta = 0
        else:
            beta = np.dot(grad_new, y) / denominator
        d = -grad_new + beta * d
        grad_prev = grad_new
        iter += 1
        step_count += 1

        if step_count == 2:
            step_count = 0
            x0 = x
            grad_prev = grad_func(x0)
            d = -grad_prev
            second_time = False

    return x, iter

def dfp_method(func, grad_func, x0, eps1=1e-6, eps2=1e-16, max_iters=20000):
    n = len(x0)
    H = np.eye(n)
    x = x0
    iter = 0
    for i in range(max_iters):
        grad = grad_func(x)
        if np.any(np.isnan(grad)):
            break
        p = -np.dot(H, grad)
        alpha = 0.01
        prev_x = x.copy()
        x += alpha * p
        if np.linalg.norm(x - prev_x) < eps1 and abs(func(x) - func(prev_x)) < eps2:
            break
        s = alpha * p
        y = grad_func(x) - grad
        A = np.outer(s, s) / np.dot(s, y)
        B = np.dot(np.dot(np.dot(H, y), y.T), H) / np.dot(y.T, np.dot(H, y))
        H += A - B
        iter += 1
    return x, iter

def levenberg_marquardt(func, grad_func, x0, eps1=1e-6, eps2=1e-16, max_iters=10000):
    n = len(x0)
    x = x0
    alpha = 1
    iter = 0
    for i in range(max_iters):
        grad = grad_func(x)
        if np.any(np.isnan(grad)):
            break
        jac = dF(x)
        hessian = np.dot(jac.T, jac) + alpha * np.eye(n)
        try:
            step = np.linalg.solve(hessian, -grad)
        except np.linalg.LinAlgError:
            break
        new_x = x + step
        if np.linalg.norm(step) < eps1 and abs(func(x) - func(new_x)) < eps2:
            break
        if func(new_x) < func(x):
            alpha /= 2
            x = new_x
        else:
            alpha *= 2
        iter += 1
    return x, iter

methods = [
    # {'name': "Метод наискорейшего градиентного спуска", 'func': gradient_descent, 'x0': np.array([0.75, 0.75])},
    {'name': "Метод Флетчера-Ривза", 'func': fletcher_reeves, 'x0': np.array([2.0, 0.0])},
    {'name': "Метод Полака-Рибьера", 'func': polak_ribiere, 'x0': np.array([1.00001, 1.00001])},
    {'name': "Метод Девидона-Флетчера-Пауэлла", 'func': dfp_method, 'x0': np.array([0.99, 0.99])},
    {'name': "Метод Левенберга-Марквардта", 'func': levenberg_marquardt, 'x0': np.array([0.0, 0.0])}
]

for method in methods:
    start_time = time.time()
    print(f"\n{method['name']}:")
    result, iterations = method['func'](F, dF, method['x0'])
    print("Минимум функции:", F(result))
    print("Точка минимума функции:", result)
    print("Время выполнения:", time.time() - start_time, "c")

xs = np.linspace(-4, 4, 400)
ys = np.linspace(-4, 4, 400)
X, Y = np.meshgrid(xs, ys)
Z = np.array([[F([x, y]) for x in xs] for y in ys])

plt.figure(figsize=(10, 8))
plt.contour(X, Y, Z, levels=50, cmap='viridis')
plt.colorbar(label='Значение функции')
plt.xlabel('x1')
plt.ylabel('x2')
plt.title('Функция Розенброка')

for method in methods:
    result, _ = method['func'](F, dF, method['x0'])
    plt.plot(result[0], result[1], 'ro')
    plt.text(result[0], result[1], method['name'], fontsize=9, ha='right')

plt.show()