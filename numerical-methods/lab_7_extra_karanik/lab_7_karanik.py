import numpy as np
import matplotlib.pyplot as plt


def fft(x):
    N = len(x)
    
    if N <= 1:
        return x
    if N % 2 > 0:
        raise ValueError("Размер входного массива должен быть степенью двойки")

    even = fft(x[0::2])
    odd = fft(x[1::2])
    
    T = [np.exp(-2j * np.pi * k / N) * odd[k] for k in range(N // 2)]
    
    return [even[k] + T[k] for k in range(N // 2)] + [even[k] - T[k] for k in range(N // 2)]

N = 128
x = np.arange(N) / N
f_x = (x - np.floor(x)) * np.sin(2 * np.pi * x)

coeffs = np.array(fft(f_x)) / N

def trig_interpolation(xi):
    M = len(xi)
    interpol_values = np.zeros(M, dtype=complex)
    for k in range(N):
        term = coeffs[k] * np.exp(2j * np.pi * k * xi)
        interpol_values += term
    return interpol_values.real

y = 0.5 + np.arange(N) / N

interpol_values = trig_interpolation(y)

true_values = (y - np.floor(y)) * np.sin(2 * np.pi * y)


e = true_values - interpol_values
error = np.max(np.abs(e))
print("Погрешность:", error)

plt.plot(y, interpol_values, 'r', label='Интерполяция')
plt.plot(y, true_values, 'b--', label='Реальное значение')
plt.xlabel('y')
plt.ylabel('Значение функции')
plt.legend()
plt.title('Сравнение тригонометрической интерполяции и реальных значений')
plt.show()



