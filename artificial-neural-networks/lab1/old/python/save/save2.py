import numpy as np
import matplotlib.pyplot as plt

def linear(x):
    return x

def sigmoid(x):
    return 1 / (1 + np.exp(-x))

def tanh(x):
    return np.tanh(x)

def relu(x):
    return np.maximum(0, x)

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
        self.weights = np.random.randn(input_size)
        #self.weights = np.random.uniform(1, 2, size=input_size)
        self.bias = np.random.randn()
        self.activation_func = activation_func
        self.activation_derivative = activation_derivative

    def predict(self, inputs):
        linear_output = np.dot(inputs, self.weights) + self.bias
        return self.activation_func(linear_output)

    def train(self, inputs, target, learning_rate):
        prediction = self.predict(inputs)
        error = target - prediction
        gradient = error * self.activation_derivative(prediction)
        self.weights += learning_rate * gradient * inputs
        self.bias += learning_rate * gradient
        
        return error**2

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
        
        'Б': [1, 1, 1, 0,
              1, 0, 0, 0,
              1, 1, 1, 0,
              1, 0, 0, 1,
              1, 1, 1, 0],
        
        'В': [1, 1, 1, 0,
              1, 0, 0, 1,
              1, 1, 1, 0,
              1, 0, 0, 1,
              1, 1, 1, 0],
        
        'Г': [1, 1, 1, 1,
              1, 0, 0, 0,
              1, 0, 0, 0,
              1, 0, 0, 0,
              1, 0, 0, 0],
        
        'Д': [0, 0, 1, 0,
              0, 1, 0, 1,
              1, 1, 1, 1,
              1, 0, 0, 1,
              1, 0, 0, 1],
        
        'Е': [1, 1, 1, 0,
              1, 0, 0, 0,
              1, 1, 1, 0,
              1, 0, 0, 0,
              1, 1, 1, 0],
        
        'Ё': [0, 1, 1, 0,
              1, 0, 0, 0,
              1, 1, 1, 0,
              1, 0, 0, 0,
              1, 1, 1, 0],
        
        'Ж': [1, 0, 1, 0,
              0, 1, 0, 1,
              1, 0, 1, 0,
              0, 1, 0, 1,
              1, 0, 1, 0],
        
        'З': [1, 1, 1, 0,
              0, 0, 1, 1,
              0, 1, 1, 1,
              0, 0, 1, 1,
              1, 1, 1, 0],
        
        'И': [1, 0, 0, 1,
              1, 0, 0, 1,
              1, 0, 1, 1,
              1, 1, 0, 1,
              1, 0, 0, 1],
        
        'Й': [1, 1, 1, 1,
              1, 0, 0, 1,
              1, 0, 1, 1,
              1, 1, 0, 1,
              1, 0, 0, 1],
        
        'К': [1, 0, 1, 0,
              1, 1, 0, 0,
              1, 1, 0, 0,
              1, 0, 1, 0,
              1, 0, 1, 0],
        
        'Л': [0, 1, 1, 1,
              0, 1, 0, 1,
              0, 1, 0, 1,
              0, 1, 0, 1,
              1, 0, 0, 1],
        
        'М': [1, 0, 0, 1,
              1, 1, 1, 1,
              1, 1, 1, 1,
              1, 0, 0, 1,
              1, 0, 0, 1],
        
        'Н': [1, 0, 0, 1,
              1, 0, 0, 1,
              1, 1, 1, 1,
              1, 0, 0, 1,
              1, 0, 0, 1],
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
        'Б': 0.11,
        'В': 0.12,
        'Г': 0.13,
        'Д': 0.14,
        'Е': 0.15,
        'Ё': 0.16,
        'Ж': 0.17,
        'З': 0.18,
        'И': 0.19,
        'Й': 0.20,
        'К': 0.21,
        'Л': 0.22,
        'М': 0.23,
        'Н': 0.24,
    }
    return targets

data = generate_data()

targets = generate_targets()

learning_rate = 0.01
epochs = 10000

activation_funcs = {
    'Linear': (linear, linear_derivative),
    'Sigmoid': (sigmoid, sigmoid_derivative),
    'Tanh': (tanh, tanh_derivative),
    'ReLU': (relu, relu_derivative)
}

errors_by_activation = {}

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
        # print(total_error)
        errors.append(total_error / len(data))

    errors_by_activation[name] = errors
    print(perceptron.predict(data['9']))

plt.figure(figsize=(10, 6))

for name, errors in errors_by_activation.items():
    plt.plot(errors[:50], label=name)

plt.title('Зависимость функции потерь от числа эпох')
plt.xlabel('Эпохи')
plt.ylabel('Средняя ошибка')
plt.legend()
plt.show()