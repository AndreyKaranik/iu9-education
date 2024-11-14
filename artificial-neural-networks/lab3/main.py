import random
import numpy as np
from copy import deepcopy
import copy
import math
import matplotlib.pyplot as plt
from scipy.optimize import fmin

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

def nelder_mead_1d(f, x_start, step=0.1, no_improve_thr=1e-6,
                   no_improv_break=10, max_iter=0,
                   alpha=1., gamma=2., rho=0.5, sigma=0.5):
    """
    Одномерный метод Нелдера-Мида для минимизации функции.
    
    Параметры:
    - f (function): функция для минимизации, должна возвращать скалярное значение
    - x_start (float): начальная точка
    - step (float): радиус вокруг начальной точки для инициализации
    - no_improve_thr (float): порог для выхода, если улучшение меньше этого значения
    - no_improv_break (int): выход после заданного количества итераций без улучшения
    - max_iter (int): максимальное число итераций, 0 - без ограничения
    - alpha, gamma, rho, sigma (float): параметры алгоритма

    Возвращает:
    - (float, float): лучшая найденная точка и значение функции в этой точке
    """

    # Инициализация симплекса (две точки в одномерном случае)
    x1 = x_start
    x2 = x_start + step
    f1 = f(x1)
    f2 = f(x2)
    
    # Упорядочиваем точки по значению функции
    if f2 < f1:
        x1, x2 = x2, x1
        f1, f2 = f2, f1

    prev_best = f1
    no_improv = 0
    iters = 0

    while True:
        # Сортируем и выбираем лучшую и худшую точки
        if f1 <= f2:
            best, worst = x1, x2
            best_score, worst_score = f1, f2
        else:
            best, worst = x2, x1
            best_score, worst_score = f2, f1

        # Проверка на сходимость
        if max_iter and iters >= max_iter:
            return best, best_score
        iters += 1

        if best_score < prev_best - no_improve_thr:
            no_improv = 0
            prev_best = best_score
        else:
            no_improv += 1

        if no_improv >= no_improv_break:
            return best, best_score

        # Центроид (в одномерном случае — просто лучшая точка)
        x0 = best

        # Операция отражения
        xr = x0 + alpha * (x0 - worst)
        rscore = f(xr)
        if best_score <= rscore < worst_score:
            worst, worst_score = xr, rscore
            continue

        # Операция расширения
        if rscore < best_score:
            xe = x0 + gamma * (xr - x0)
            escore = f(xe)
            if escore < rscore:
                worst, worst_score = xe, escore
            else:
                worst, worst_score = xr, rscore
            continue

        # Операция сжатия
        xc = x0 + rho * (worst - x0)
        cscore = f(xc)
        if cscore < worst_score:
            worst, worst_score = xc, cscore
            continue

        # Операция редукции (в одномерном случае: обе точки сжимаются к лучшей)
        x2 = best + sigma * (worst - best)
        f2 = f(x2)
        
        if f1 <= f2:
            x1, f1 = best, best_score
            x2, f2 = x2, f2
        else:
            x1, f1 = x2, f2
            x2, f2 = best, best_score

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

        p = nelder_mead_1d(argmin_fn, 0.0)[0]
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

        p = fmin(argmin_fn, [0.0])[0]

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
        p = fmin(argmin_fn, [0.0])[0]
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
            print(f'Took {k} iterations')
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


#вариант задания №7
a_arg = 300
b_arg = 25
n_arg = 2
f0_arg = 250

x0 = [random.uniform(-2.0, 2.0) for _ in range(0, n_arg)]

#Метод сопряженных градиентов Флетчера-Ривза
x, k, res1, iters = Fletcher_Rivz(a_arg, b_arg, deepcopy(x0))
print(f'\nметод Флетчера-Ривза\nn={n_arg}\na={a_arg}\nb={b_arg}\nf0={f0_arg}')
print(f'Took {k} iterations')
print(f'x* = {x}')
print(f'f(x*)= {res1[len(res1)-1]}')
# print(res1)
# plt.plot(iters, res1[1:])
# plt.title("Метод Флетчера-Ривза")
# plt.show()

# #метод сопряженных градиентов Полака-Рибьера
# x, k, res2, iters = Polak_Ribyer(a_arg, b_arg, deepcopy(x0))
# print(f'\nметод Полака-Рибьера\nn={n_arg}\na={a_arg}\nb={b_arg}\nf0={f0_arg}')
# print(f'Took {k} iterations')
# print(f'x* = {x}')
# print(f'f(x*)= {res2[len(res2)-1]}')
# print(res2)
# plt.plot(iters, res2[1:])
# plt.title("Метод Полака-Рибьера")
# plt.show()

# #Квазиньютоновский метод Девидона-Флетчера-Пауэлла
# x, k, res3, iters = DFP(a_arg, b_arg, deepcopy(x0))
# print(f'\nметод Девидона-Флетчера-Пауэлла\nn={n_arg}\na={a_arg}\nb={b_arg}\nf0={f0_arg}')
# print(f'Took {k} iterations')
# print(f'x* = {x}')
# print(f'f(x*)= {res3[len(res3)-1]}')
# print(res3)
# plt.plot(iters, res3[1:])
# plt.title("Метод Девидона-Флетчера-Пауэлла")
# plt.show()

# # Метод Левенберга-Марквардта
# x, iters, res4 = LM(deepcopy(x0))
# print(f'\nметод Левенберга-Марквардта\nn={n_arg}\na={a_arg}\nb={b_arg}\nf0={f0_arg}')
# print(f'Took {len(res4)-1} iterations')
# print(f'x* = {x}')
# print(f'f(x*)= {res4[len(res4)-1]}')
# print(res4)
# # plt.figure(figsize=(16, 12), dpi=80)
# plt.plot(iters[2:], res4[2:])
# plt.title("Метод Левенберга-Марквардта")
# plt.show()


# # Графики

# # plt.plot(res1, label='Метод Флетчера-Ривза')
# # plt.plot(res2, label='Метод Полака-Рибьера')
# plt.plot(res3, label='Метод Девидона-Флетчера-Пауэлла')
# plt.plot(res4, label='Метод Левенберга-Марквардта')
# plt.title("График зависимости значения функции от итераций для разных методов")
# plt.legend()
# plt.show()


# plt.plot(res1, label='Метод Флетчера-Ривза')
# plt.plot(res2, label='Метод Полака-Рибьера')
# # plt.plot(res3, label='Метод Девидона-Флетчера-Пауэлла')
# # plt.plot(res4, label='Метод Левенберга-Марквардта')
# plt.title("График зависимости значения функции от итераций для разных методов")
# plt.legend()
# plt.show()

# plt.plot(res1, label='Метод Флетчера-Ривза')
# plt.plot(res2, label='Метод Полака-Рибьера')
# plt.plot(res3, label='Метод Девидона-Флетчера-Пауэлла')
# plt.plot(res4, label='Метод Левенберга-Марквардта')
# plt.title("График зависимости значения функции от итераций для разных методов")
# plt.legend()
# plt.show()