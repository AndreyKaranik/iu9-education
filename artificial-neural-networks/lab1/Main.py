import numpy as np
from matplotlib import pyplot as plt

data = np.zeros((10, 5, 4), dtype=int)

digits = [
    np.array([
        [0, 1, 1, 0],
        [1, 0, 0, 1],
        [1, 0, 0, 1],
        [1, 0, 0, 1],
        [0, 1, 1, 0]
        ]),
    np.array([
        [0, 1, 1, 0],
        [0, 0, 1, 0],
        [0, 0, 1, 0],
        [0, 0, 1, 0],
        [0, 0, 1, 0]
    ]),
    np.array([
        [0, 1, 1, 0],
        [1, 0, 0, 1],
        [0, 0, 1, 0],
        [0, 1, 0, 0],
        [1, 1, 1, 1]
    ]),
    np.array([
        [1, 1, 1, 0],
        [0, 0, 0, 1],
        [0, 1, 1, 0],
        [0, 0, 0, 1],
        [1, 1, 1, 0]
    ]),
    np.array([
        [0, 0, 1, 0],
        [0, 1, 1, 0],
        [1, 0, 1, 0],
        [1, 1, 1, 1],
        [0, 0, 1, 0]
    ]),
    np.array([
        [1, 1, 1, 1],
        [1, 0, 0, 0],
        [1, 1, 1, 0],
        [0, 0, 0, 1],
        [1, 1, 1, 0]
    ]),
    np.array([
        [0, 1, 1, 0],
        [1, 0, 0, 0],
        [1, 1, 1, 0],
        [1, 0, 0, 1],
        [0, 1, 1, 0]
    ]),
    np.array([
        [1, 1, 1, 1],
        [0, 0, 0, 1],
        [0, 0, 1, 0],
        [0, 1, 0, 0],
        [0, 1, 0, 0]
    ]),
    np.array([
        [0, 1, 1, 0],
        [1, 0, 0, 1],
        [0, 1, 1, 0],
        [1, 0, 0, 1],
        [0, 1, 1, 0]
    ]),
    np.array([
        [0, 1, 1, 0],
        [1, 0, 0, 1],
        [0, 1, 1, 1],
        [0, 0, 0, 1],
        [0, 1, 1, 0]
    ])
    ]

for i in range(10):
    data[i] = digits[i]

plt.figure(figsize=(10, 3))
for i in range(10):
    plt.subplot(2, 5, i + 1)
    plt.imshow(1-data[i], cmap='gray')
    plt.axis('off')
plt.show()

class Perceptron:
    def __init__(self, input_size, output_size, learning_rate=0.05, activation='sigmoid'):
        self.input_size = input_size
        self.output_size = output_size
        self.learning_rate = learning_rate
        self.weights = np.random.randn(input_size, output_size)
        self.bias = np.zeros(output_size)
        self.activation = activation
        self.losses = []
        self.accuracies = []

    def linear(self, x):    
        return x
    def sigmoid(self, x):
        return 1 / (1 + np.exp(-x))
    def tanh(self, x):
        return np.tanh(x)    
    def relu(self, x):
        return np.maximum(0, x)    
    
    def linear_derivative(self, x):
        return np.ones_like(x)
    def sigmoid_derivative(self, x):
        return x * (1 - x)
    def tanh_derivative(self, x):
        return 1 - np.tanh(x) ** 2
    def relu_derivative(self, x):
        return np.where(x > 0, 1, 0)
    
    def activate(self, x):
        if self.activation == 'sigmoid':
            return self.sigmoid(x)
        elif self.activation == 'linear':
            return self.linear(x)
        elif self.activation == 'tanh':
            return self.tanh(x)
        elif self.activation == 'relu':
            return self.relu(x)
        else:
            raise ValueError("Unknown activation function")
    def activate_derivative(self, x):
        if self.activation == 'sigmoid':
            return self.sigmoid_derivative(x)
        elif self.activation == 'linear':
            return self.linear_derivative(x)
        elif self.activation == 'tanh':
            return self.tanh_derivative(x)
        elif self.activation == 'relu':
            return self.relu_derivative(x)
        else:
            raise ValueError("Unknown activation function")
    def mse_derivative(self, output, target):
        return 2 * (output - target)
    def train(self, X, y, n_epochs):
        for epoch in range(n_epochs):
            total_loss = 0
            correct_predictions = 0
            for i in range(len(X)):
                x = X[i].flatten()
                target = np.zeros(self.output_size)
                target[y[i]] = 1

                output = self.activate(np.dot(x, self.weights) + self.bias)

                error = (target - output) ** 2
                total_loss += np.sum(error)
                mse_grad = self.mse_derivative(output, target)
                adjustment = self.learning_rate * mse_grad * self.activate_derivative(output)

                self.weights -= np.outer(x, adjustment)
                self.bias -= adjustment

                if np.argmax(output) == y[i]:
                    correct_predictions += 1

            self.losses.append(total_loss / len(X))
            self.accuracies.append(correct_predictions / len(X))
            
    def predict(self, X):
        x = X.flatten()
        output = self.activate(np.dot(x, self.weights) + self.bias)
        return np.argmax(output)

    def predict_1(self, X):
        x = X.flatten()
        output = self.activate(np.dot(x, self.weights) + self.bias)
        return output

labels = np.arange(10)

input_size = 5 * 4
output_size = 10

# Обучение с различными функциями активации
activations = ['sigmoid', 'linear', 'tanh', 'relu']

plt.figure(figsize=(12, 6))

for activation in activations:
    print(f"Training with activation function: {activation}")
    perceptron = Perceptron(input_size, output_size, activation=activation)
    perceptron.train(data, labels, n_epochs=1000)

    for i in range(10):
        prediction = perceptron.predict(data[i])
        pred_proba = perceptron.predict_1(data[i])
        print(f'True: {labels[i]} Pred: {prediction} Proba\n {pred_proba}')

    # Построение графика loss
    plt.subplot(1, 2, 1)
    plt.plot(perceptron.losses, label=f'{activation}')

    # Построение графика accuracy
    plt.subplot(1, 2, 2)
    plt.plot(perceptron.accuracies, label=f'{activation}')

# Настройка графиков
plt.subplot(1, 2, 1)
plt.xlabel('Epochs')
plt.ylabel('Loss')
plt.title('Loss vs Epochs')
plt.legend()

plt.subplot(1, 2, 2)
plt.xlabel('Epochs')
plt.ylabel('Accuracy')
plt.title('Accuracy vs Epochs')
plt.legend()

plt.tight_layout()
plt.show()
