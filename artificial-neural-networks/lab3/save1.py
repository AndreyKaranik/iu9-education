import random
import numpy as np
from copy import deepcopy
from scipy.optimize import fmin
from typing import Callable, Tuple, Union

def rozenbrok_func(a, b, n, f0, x):
    res = f0
    for i in range(0, n - 1):
        res += a * (x[i]**2 - x[i + 1])**2 + b * (x[i] - 1)**2
    return res

def find_gradient(a, b, n, x):
    grad = [0.0 for _ in range(0, n)]
    grad[0] = 2 * a * (x[0]**2 - x[1]) * 2 * x[0] + 2 * b * (x[0] - 1)
    for i in range(1, n - 1):
        grad[i] = 2 * a * (x[i - 1]**2 - x[i]) * (-1) + 2 * a * (x[i]**2 - x[i + 1]) * 2 * x[i] + 2 * b * (x[i] - 1)
    grad[n - 1] = 2 * a * (x[n - 2]**2 - x[n - 1]) * (-1)
    return grad

def _order(x: np.ndarray, fx: np.ndarray) -> Tuple[np.ndarray, np.ndarray]:
    indices = np.argsort(fx)
    return x[indices], fx[indices]

def optimize_1d(
    fun: Callable[[float], float],
    x0: float,
    maxiter: Union[int, None] = None,
    initial_simplex: Union[np.ndarray, None] = None
) -> Tuple[float, float]:
    if initial_simplex is not None:
        if initial_simplex.ndim != 1 or len(initial_simplex) != 2:
            raise ValueError("initial_simplex must be a 1D array of length 2 for 1D optimization.")
        x = initial_simplex.copy()
    else:
        h = 0.05 if x0 != 0 else 0.00025
        x = np.array([x0, x0 + h])

    if maxiter is None:
        maxiter = 200

    alpha = 1.0
    gamma = 2.0
    rho = 0.5
    sigma = 0.5

    fx = np.array([fun(x[0]), fun(x[1])])
    x, fx = _order(x, fx)

    for _ in range(maxiter):
        xo = x[0]
        xr = xo + alpha * (xo - x[1])
        fxr = fun(xr)

        if fx[0] <= fxr < fx[1]:
            x[1] = xr
            fx[1] = fxr
        elif fxr < fx[0]:
            xe = xo + gamma * (xr - xo)
            fxe = fun(xe)
            if fxe < fxr:
                x[1] = xe
                fx[1] = fxe
            else:
                x[1] = xr
                fx[1] = fxr
        else:
            if fxr < fx[1]:
                xc = xo + rho * (xr - xo)
                fxc = fun(xc)
                if fxc < fxr:
                    x[1] = xc
                    fx[1] = fxc
            else:
                xc = xo + rho * (x[1] - xo)
                fxc = fun(xc)
                if fxc < fx[1]:
                    x[1] = xc
                    fx[1] = fxc
                else:
                    x[1] = x[0] + sigma * (x[1] - x[0])
                    fx[1] = fun(x[1])

        x, fx = _order(x, fx)

        if abs(fx[1] - fx[0]) < 1e-6:
            break

    return x[0], fx[0]


def Fletcher_Rivz(a, b, x, n=2, eps1=1e-6, delta2=1e-6, eps2=1e-6, M=10000):
    argmin_xs = [None for _ in range(n_arg)]
    argmin_grad = [None for _ in range(n_arg)]

    res = []
    iters = []

    def argmin_fn(a):
        n_xs = [None for _ in range(0, len(argmin_xs))]
        for i in range(0, len(argmin_xs)):
            n_xs[i] = argmin_xs[i] - a * argmin_grad[i]
        return rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, n_xs)

    def get_argmin(xs, grad):
        for i in range(0, len(xs)):
            argmin_xs[i] = xs[i]
            argmin_grad[i] = grad[i]

        p = optimize_1d(argmin_fn, x0=0)[0]
        return p
    
    k = 0
    prev_x = [0 for _ in range(n)]
    d_k = 0
    d_k_1 = 0
    w_k = 0
    while True:
        grad = np.array(find_gradient(a, b, n, x), dtype=float)
        if np.linalg.norm(grad) < eps1:
            return x, k, res, iters
        if k >= M:
            return x, k, res, iters

        if k == 0:
          d_k = -grad
        else:
          tmp1 = (np.linalg.norm(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, x)))**2
          tmp2 = (np.linalg.norm(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, prev_x)))**2
          w_k = tmp1 / tmp2
          d_k = -grad + w_k * d_k_1

        arg = get_argmin(x, d_k)
        prev_x = deepcopy(x)
        x = x - arg * d_k
        res.append(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, x))

        if (np.linalg.norm(abs(x - prev_x)) < delta2) and (abs(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, x) - rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, prev_x)) < eps2):
            return x, k, res, iters

        iters.append(k)
        k += 1


def Polak_Ribyer(a, b, x, n=2, eps1=1e-7, delta2=1e-7, eps2=1e-7, M=10000):
    argmin_xs = [None for _ in range(n_arg)]
    argmin_grad = [None for _ in range(n_arg)]

    res = []
    iters = []

    def argmin_fn(a):
        n_xs = [None for _ in range(0, len(argmin_xs))]
        for i in range(0, len(argmin_xs)):
            n_xs[i] = argmin_xs[i] - a * argmin_grad[i]
        return rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, n_xs)

    def get_argmin(xs, grad):
        for i in range(0, len(xs)):
            argmin_xs[i] = xs[i]
            argmin_grad[i] = grad[i]

        p = optimize_1d(argmin_fn, x0=0)[0]

        return p

    # k - число итераций
    k = 0
    prev_x = [0 for _ in range(n)]
    prev_grad = [0 for _ in range(n)]
    d_k = 0
    d_k_1 = 0
    w_k = 0
    while True:
        # вычисляем градиент
        grad = np.array(find_gradient(a, b, n, x), dtype=float)
        # проверяем выполнение критерия окончания
        # Построение последовательности {x^k} заканчивается, когда либо ||grad(f(x))|| < eps1
        if np.linalg.norm(grad) < eps1:
            return x, k, res, iters
        # проверяем выполнение неравенства k >= M
        # Или построение последовательности {x^k} заканчивается, когда k>=M
        if k >= M:
            return x, k, res, iters

        if k > 0:
            grad_diff = np.array(grad) - np.array(prev_grad)
            if np.dot(prev_grad, prev_grad) != 0:
                beta_k = np.dot(grad_diff, grad_diff) / np.dot(prev_grad, prev_grad)
                if np.isfinite(beta_k):
                    d_k = -grad + beta_k * d_k_1
                else:
                    d_k = -grad  # Если beta_k некорректен, восстанавливаем исходное направление
            else:
                d_k = -grad  # Если предыдущее скалярное произведение равно нулю, используем исходное направление
        else:
            d_k = -grad  # Для первого шага

        # вычисляем a_k = Argmin f(x_k + a_k d_k)
        arg = get_argmin(x, d_k)
        # сохраняем x_k
        prev_x = deepcopy(x)
        # вычисляем x_k+1
        x = x - arg * d_k
        res.append(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, x))

        # Или построение последовательности {x^k} заканчивается, когда одновременно
        # выполнены два неравенства ||x_k+1 - x_k|| < delta2 и ||f(x_k+1) - f(x_k)|| < eps2
        if (np.linalg.norm(abs(x - prev_x)) < delta2) and (abs(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, x) - rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, prev_x)) < eps2):
            return x, k, res, iters

        iters.append(k)
        k += 1


#Квазиньютоновский метод Девидона-Флетчера-Пауэлла
def DFP(a, b, x, n=2, eps1=1e-7, delta2=1e-7, eps2=1e-7, M=10000):
    argmin_xs = [None for _ in range(n_arg)]
    argmin_grad = [None for _ in range(n_arg)]

    res = []
    iters = []

    def argmin_fn(a):
        n_xs = [None for _ in range(0, len(argmin_xs))]
        for i in range(0, len(argmin_xs)):
            n_xs[i] = argmin_xs[i] - a * argmin_grad[i]
        return rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, n_xs)

    def get_argmin(xs, grad):
        for i in range(0, len(xs)):
            argmin_xs[i] = xs[i]
            argmin_grad[i] = grad[i]
    
        p = optimize_1d(argmin_fn, x0=0)[0]

        return p

    # матрица G_0
    G = np.eye(n)
    # k - число итераций
    k = 0
    prev_x = [0 for _ in range(n)]
    prev_grad = [0 for _ in range(n)]

    while True:
        # вычисляем градиент
        grad = np.array(find_gradient(a, b, n, x), dtype=float)
        # проверяем выполнение критерия окончания
        # Построение последовательности {x^k} заканчивается, когда либо ||grad(f(x))|| < eps1
        if np.linalg.norm(grad) < eps1:
            return x, k, res, iters
        # проверяем выполнение неравенства k >= M
        # Или построение последовательности {x^k} заканчивается, когда k>=M
        if k >= M:
            return x, k, res, iters

        if k >= 1:
            # вычисляем Δg_k
            delta_g = grad - prev_grad
            # вычисляем Δx_k
            delta_x = x - prev_x
            # вычисляем ΔG_k
            delta_G = np.outer(delta_x, delta_x) / np.dot(delta_x, delta_g) - np.outer(G @ delta_g, G @ delta_g) / np.dot(delta_g, G @ delta_g)
            # вычисляем G_k+1 = G_k + ΔG_k
            G += delta_G
        # вычисляем d_k
        d = G @ grad
        # вычисляем a_k = Argmin f(x_k + a_k d_k)
        arg = get_argmin(x, d)
        # сохраняем x_k
        prev_x = deepcopy(x)
        # вычисляем x_k+1
        x = x - arg * d
        prev_grad = deepcopy(grad)
        res.append(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, x))

        # Или построение последовательности {x^k} заканчивается, когда одновременно
        # выполнены два неравенства ||x_k+1 - x_k|| < delta2 и ||f(x_k+1) - f(x_k)|| < eps2
        if (np.linalg.norm(abs(x - prev_x)) < delta2) and (abs(rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, x) - rozenbrok_func(a_arg, b_arg, n_arg, f0_arg, prev_x)) < eps2):
            return x, k, res, iters

        iters.append(k)
        k += 1


# Метод Левенберга-Марквардта
def LM(xs, eps1=1e-7, mu_k=10000, M=10000):
    # Функция подсчета матрицы Гессе
    def find_H(a, b, n, x):
        length = len(x)
        H = [[0.0 for _ in range(0, length)] for _ in range(0, length)]
        H[0][0] = 12 * a * x[0]**2 - 4 * a * x[1] + 2 * b
        H[0][1] = -4 * a * x[0]

        for i in range(1, n - 1):
            H[i][i - 1] = -4 * a * x[i - 1]
            H[i][i] = 12 * a * x[i]**2 - 4 * a * x[i + 1] + 2 * b + 2 * a
            H[i][i + 1] = -4 * a * x[i]

        H[n - 1][n - 2] = -4 * a * x[n - 2]
        H[n - 1][n - 1] = 2 * a
        return H

    iters = []
    res = []
    # Построение последовательности {x^k} заканчивается, когда либо k>=M
    # k - число итераций
    for k in range(0, M):
        iters.append(k)
        # Вычисляем градиент
        grad = find_gradient(a_arg , b_arg , n_arg , xs)
        # ЛИБО построение последовательности {x^k} заканчивается, когда ||grad(f(x))|| < eps1
        if np.linalg.norm(grad) < eps1:
            break
        # Вычисляем матрицу Гессе
        H = find_H(a_arg , b_arg , n_arg , xs)
        # Вычисляем матрицу H(x)+mu*E
        ll = len(xs)
        mat = np.zeros((ll, ll))
        for i in range(0, ll):
            for j in range(0, ll):
                if i == j:
                    mat[i][j] = H[i][j] + mu_k
                else:
                    mat[i][j] = H[i][j]
        # Вычисляем матрицу (H(x)+mu*E)^-1
        mat_inv = np.linalg.inv(mat)
        # Вычисляем d
        d = [0.0 for _ in range(0, ll)]
        # Сохраняем x^k
        xs_prev = deepcopy(xs)
        # Вычисляем x^k+1
        for i in range(0, ll):
            tmp = 0.0
            for j in range(0, ll):
                tmp += mat_inv[i][j] * grad[j]
            d[i] = tmp
            xs[i] -= d[i]
        # Проверяем условие f(x^k+1)<f(x^k)
        if rozenbrok_func(a_arg , b_arg , n_arg , f0_arg , xs) < rozenbrok_func(a_arg , b_arg , n_arg , f0_arg , xs_prev):
            mu_k = mu_k / 2
        else:
            mu_k = mu_k * 2
        res.append(rozenbrok_func(a_arg , b_arg , n_arg , f0_arg , xs))

    res.append(rozenbrok_func(a_arg , b_arg , n_arg , f0_arg , xs))
    return xs, iters, res

a_arg = 300
b_arg = 5
n_arg = 2
f0_arg = 15

x0 = [random.uniform(-2.0, 2.0) for _ in range(0, n_arg)]

print(f'n={n_arg}\na={a_arg}\nb={b_arg}\nf0={f0_arg}')

# Метод сопряженных градиентов Флетчера-Ривза
x, k, res1, iters = Fletcher_Rivz(a_arg, b_arg, deepcopy(x0))
print(f'\nМетод Флетчера-Ривза')
print(f'Кол-во итераций: {k}')
print(f'x* = {x}')
print(f'f(x*)= {res1[len(res1)-1]}')

# Метод сопряженных градиентов Полака-Рибьера
x, k, res2, iters = Polak_Ribyer(a_arg, b_arg, deepcopy(x0))
print(f'\nМетод Полака-Рибьера')
print(f'Кол-во итераций: {k}')
print(f'x* = {x}')
print(f'f(x*)= {res2[len(res2)-1]}')

# Квазиньютоновский метод Девидона-Флетчера-Пауэлла
x, k, res3, iters = DFP(a_arg, b_arg, deepcopy(x0))
print(f'\nМетод Девидона-Флетчера-Пауэлла')
print(f'Кол-во итераций: {k}')
print(f'x* = {x}')
print(f'f(x*)= {res3[len(res3)-1]}')

# Метод Левенберга-Марквардта
x, k, res4 = LM(deepcopy(x0))
print(f'\nМетод Левенберга-Марквардта')
print(f'Кол-во итераций: {len(res4)-1}')
print(f'x* = {x}')
print(f'f(x*)= {res4[len(res4)-1]}')