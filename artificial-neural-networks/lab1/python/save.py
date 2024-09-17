import numpy as np
import matplotlib.pyplot as plt

# Линейная функция активации
def linear(x):
    return x

# Сигмоида
def sigmoid(x):
    return 1 / (1 + np.exp(-x))

# Гиперболический тангенс
def tanh(x):
    return np.tanh(x)

# ReLU
def relu(x):
    return np.maximum(0, x)

# Производные для обучения
def linear_derivative(x):
    return np.ones_like(x)

def sigmoid_derivative(x):
    return sigmoid(x) * (1 - sigmoid(x))

def tanh_derivative(x):
    return 1 - np.tanh(x)**2

def relu_derivative(x):
    return np.where(x > 0, 1, 0)

class Perceptron:
    def __init__(self, input_size, activation_func, activation_derivative):
        # self.weights = np.random.randn(input_size)
        self.weights = np.random.uniform(1, 2, size=input_size)
        self.bias = np.random.randn()
        self.activation_func = activation_func
        self.activation_derivative = activation_derivative

    def predict(self, inputs):
        linear_output = np.dot(inputs, self.weights) + self.bias
        # print('output', linear_output, '\n', '\n')
        return self.activation_func(linear_output)

    def train(self, inputs, target, learning_rate):
        prediction = self.predict(inputs)
        error = target - prediction
        gradient = error * self.activation_derivative(prediction)
        
        # Обновление весов
        self.weights += learning_rate * gradient * inputs
        self.bias += learning_rate * gradient
        
        return error**2  # Среднеквадратичная ошибка

def generate_data():
    data = {
        '0': [0, 1, 1, 0,
              1, 0, 0, 1,
              1, 0, 0, 1,
              1, 0, 0, 1,
              0, 1, 1, 0],
        
        '1': [0, 0, 1, 0,
              0, 1, 1, 0,
              0, 0, 1, 0,
              0, 0, 1, 0,
              1, 1, 1, 1],
        
        '2': [1, 1, 1, 1,
              0, 0, 0, 1,
              1, 1, 1, 1,
              1, 0, 0, 0,
              1, 1, 1, 1],
        
        '3': [1, 1, 1, 0,
              0, 0, 1, 1,
              0, 1, 1, 0,
              0, 0, 1, 1,
              1, 1, 1, 0],
        
        '4': [0, 0, 1, 0,
              0, 1, 1, 0,
              1, 1, 1, 1,
              0, 0, 1, 0,
              0, 0, 1, 0],
        
        '5': [1, 1, 1, 1,
              1, 0, 0, 0,
              1, 1, 1, 1,
              0, 0, 0, 1,
              1, 1, 1, 1],
        
        '6': [0, 1, 1, 0,
              1, 0, 0, 0,
              1, 1, 1, 0,
              1, 0, 0, 1,
              0, 1, 1, 0],
        
        '7': [1, 1, 1, 1,
              0, 0, 1, 0,
              0, 1, 0, 0,
              0, 1, 0, 0,
              0, 1, 0, 0],
        
        '8': [0, 1, 1, 0,
              1, 0, 0, 1,
              0, 1, 1, 0,
              1, 0, 0, 1,
              0, 1, 1, 0],
        
        '9': [0, 1, 1, 0,
              1, 0, 0, 1,
              0, 1, 1, 1,
              0, 0, 1, 0,
              0, 1, 1, 0],

        'А': [0, 1, 1, 0,
              1, 0, 0, 1,
              1, 1, 1, 1,
              1, 0, 0, 1,
              1, 0, 0, 1],
        
        # 'Б': [1, 1, 1, 0,
        #       1, 0, 0, 0,
        #       1, 1, 1, 0,
        #       1, 0, 0, 1,
        #       1, 1, 1, 0],
        
        # 'В': [1, 1, 1, 0,
        #       1, 0, 0, 1,
        #       1, 1, 1, 0,
        #       1, 0, 0, 1,
        #       1, 1, 1, 0],
        
        # 'Г': [1, 1, 1, 1,
        #       1, 0, 0, 0,
        #       1, 0, 0, 0,
        #       1, 0, 0, 0,
        #       1, 0, 0, 0],
        
        # 'Д': [0, 0, 1, 0,
        #       0, 1, 0, 1,
        #       1, 1, 1, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1],
        
        # 'Е': [1, 1, 1, 0,
        #       1, 0, 0, 0,
        #       1, 1, 1, 0,
        #       1, 0, 0, 0,
        #       1, 1, 1, 0],
        
        # 'Ё': [0, 1, 1, 0,
        #       1, 0, 0, 0,
        #       1, 1, 1, 0,
        #       1, 0, 0, 0,
        #       1, 1, 1, 0],
        
        # 'Ж': [1, 0, 1, 0,
        #       0, 1, 0, 1,
        #       1, 0, 1, 0,
        #       0, 1, 0, 1,
        #       1, 0, 1, 0],
        
        # 'З': [1, 1, 1, 0,
        #       0, 0, 1, 1,
        #       0, 1, 1, 1,
        #       0, 0, 1, 1,
        #       1, 1, 1, 0],
        
        # 'И': [1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       1, 0, 1, 1,
        #       1, 1, 0, 1,
        #       1, 0, 0, 1],
        
        # 'Й': [1, 1, 1, 1,
        #       1, 0, 0, 1,
        #       1, 0, 1, 1,
        #       1, 1, 0, 1,
        #       1, 0, 0, 1],
        
        # 'К': [1, 0, 1, 0,
        #       1, 1, 0, 0,
        #       1, 1, 0, 0,
        #       1, 0, 1, 0,
        #       1, 0, 1, 0],
        
        # 'Л': [0, 1, 1, 1,
        #       0, 1, 0, 1,
        #       0, 1, 0, 1,
        #       0, 1, 0, 1,
        #       1, 0, 0, 1],
        
        # 'М': [1, 0, 0, 1,
        #       1, 1, 1, 1,
        #       1, 1, 1, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1],
        
        # 'Н': [1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       1, 1, 1, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1],
        
        # 'О': [1, 1, 1, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       1, 1, 1, 1],
        
        # 'П': [1, 1, 1, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       1, 0, 0, 1],
        
        # 'Р': [1, 1, 1, 0,
        #       1, 0, 0, 1,
        #       1, 1, 1, 0,
        #       1, 0, 0, 0,
        #       1, 0, 0, 0],

        # 'С': [0, 1, 1, 0,
        #       1, 0, 0, 0,
        #       1, 0, 0, 0,
        #       1, 0, 0, 0,
        #       0, 1, 1, 0],
        
        # 'Т': [1, 1, 1, 1,
        #       0, 0, 1, 0,
        #       0, 0, 1, 0,
        #       0, 0, 1, 0,
        #       0, 0, 1, 0],
        
        # 'У': [1, 0, 0, 1,
        #       0, 1, 0, 1,
        #       0, 0, 1, 0,
        #       0, 0, 1, 0,
        #       1, 1, 0, 0],
        
        # 'Ф': [0, 1, 1, 0,
        #       1, 0, 0, 1,
        #       1, 1, 1, 1,
        #       0, 1, 1, 0,
        #       0, 1, 1, 0],
        
        # 'Х': [1, 0, 0, 1,
        #       0, 1, 1, 0,
        #       0, 1, 1, 0,
        #       0, 1, 1, 0,
        #       1, 0, 0, 1],
        
        # 'Ц': [1, 0, 1, 0,
        #       1, 0, 1, 0,
        #       1, 0, 1, 0,
        #       1, 1, 1, 1,
        #       0, 0, 0, 1],
        
        # 'Ч': [1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       0, 1, 1, 1,
        #       0, 0, 0, 1,
        #       0, 0, 0, 1],
        
        # 'Ш': [1, 1, 0, 1,
        #       1, 1, 0, 1,
        #       1, 1, 0, 1,
        #       1, 1, 0, 1,
        #       1, 1, 1, 1],
        
        # 'Щ': [1, 1, 1, 0,
        #       1, 1, 1, 0,
        #       1, 1, 1, 0,
        #       1, 1, 1, 0,
        #       1, 1, 1, 1],
        
        # 'Ь': [1, 0, 0, 0,
        #       1, 0, 0, 0,
        #       1, 1, 0, 0,
        #       1, 0, 1, 0,
        #       1, 1, 0, 0],
        
        # 'Ы': [1, 0, 0, 1,
        #       1, 0, 0, 1,
        #       1, 1, 0, 1,
        #       1, 0, 1, 1,
        #       1, 1, 0, 1],
        
        # 'Э': [0, 1, 1, 0,
        #       0, 0, 0, 1,
        #       0, 1, 1, 1,
        #       0, 0, 0, 1,
        #       0, 1, 1, 0],
        
        # 'Ю': [1, 0, 0, 0,
        #       1, 0, 1, 0,
        #       1, 1, 0, 1,
        #       1, 0, 1, 0,
        #       1, 0, 0, 0],
        
        # 'Я': [0, 0, 1, 1,
        #       0, 1, 0, 1,
        #       0, 1, 1, 1,
        #       0, 1, 0, 1,
        #       1, 0, 0, 1]
    }
    return data

def generate_targets():
    targets = {
        '0': 0.0,
        '1': 0.01,
        '2': 0.02,
        '3': 0.03,
        '4': 0.04,
        '5': 0.05,
        '6': 0.06,
        '7': 0.07,
        '8': 0.08,
        '9': 0.09,
        'А': 0.1,
        # 'Б': 11,
        # 'В': 12,
        # 'Г': 13,
        # 'Д': 14,
        # 'Е': 15,
        # 'Ё': 16,
        # 'Ж': 17,
        # 'З': 18,
        # 'И': 19,
        # 'Й': 20,
        # 'К': 21,
        # 'Л': 22,
        # 'М': 23,
        # 'Н': 24,
        # 'О': 25,
        # 'П': 26,
        # 'Р': 27,
        # 'С': 28,
        # 'Т': 29,
        # 'У': 30,
        # 'Ф': 31,
        # 'Х': 32,
        # 'Ц': 33,
        # 'Ч': 34,
        # 'Ш': 35,
        # 'Щ': 36,
        # 'Ь': 37,
        # 'Ы': 38,
        # 'Э': 39,
        # 'Ю': 40,
        # 'Я': 41
    }
    return targets

# Данные
data = generate_data()

# Подготовим метки
targets = generate_targets()

# Гиперпараметры
learning_rate = 0.01
epochs = 100

# Функции активации
activation_funcs = {
    'Linear': (linear, linear_derivative),
    'Sigmoid': (sigmoid, sigmoid_derivative),
    'Tanh': (tanh, tanh_derivative),
    'ReLU': (relu, relu_derivative)
}

# Хранение ошибок
errors_by_activation = {}

# Обучаем модель с каждой функцией активации
for name, (activation_func, activation_derivative) in activation_funcs.items():
    perceptron = Perceptron(input_size=20, activation_func=activation_func, activation_derivative=activation_derivative)
    errors = []

    for epoch in range(epochs):
        total_error = 0
        for char, inputs in data.items():
            inputs = np.array(inputs)
            target = targets[char]
            error = perceptron.train(inputs, target, learning_rate)
            total_error += error
            # break
        
        errors.append(total_error / len(data))

    errors_by_activation[name] = errors
    print(perceptron.predict(data['А']))

plt.figure(figsize=(10, 6))

for name, errors in errors_by_activation.items():
    plt.plot(errors[:1000], label=name)

plt.title('Зависимость функции потерь от числа эпох')
plt.xlabel('Эпохи')
plt.ylabel('Средняя ошибка')
plt.legend()
plt.show()