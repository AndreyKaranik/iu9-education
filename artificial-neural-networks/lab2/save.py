from tensorflow.keras.datasets import mnist
import matplotlib.pyplot as plt
import numpy as np

N_SAMPLES  = 5000
N_TESTS = 1000
(train_X, train_Y), (test_X, test_Y) = mnist.load_data()
train_X, train_Y = train_X[:N_SAMPLES], train_Y[:N_SAMPLES]
test_X, test_Y = test_X[:N_TESTS], test_Y[:N_TESTS]
train_X = train_X.reshape(train_X.shape[0], 28**2) / 255.0
test_X = test_X.reshape(test_X.shape[0], 28**2) / 255.0
train_X.shape, train_Y.shape, test_X.shape, test_Y.shape

k = 1
ReLU = lambda x: np.maximum(0, k*x)
dReLU = lambda x: np.where(x > 0, k, 0)

def softmax(x):
    shiftx = x - np.max(x)
    exps = np.exp(shiftx)
    return exps / np.sum(exps)

def dsoftmax(x):
    s = softmax(x)
    return np.diagflat(s) - np.outer(s, s)

def MSE(y, y_ref):
    return 0.5 * np.mean((y - y_ref)**2)

def dMSE(y, y_ref):
    delta = y - y_ref
    return delta / delta.size

def KL_divergence(y, y_ref):
    return np.mean(y_ref * np.log((y_ref + 1e-9) / (y + 1e-9)))

def KL_divergence_derivative(y, y_ref):
    return -y_ref / (y + 1e-9)

def categorical_cross_entropy(y, y_ref):
    return -np.mean(y_ref * np.log(y + 1e-9))

def categorical_cross_entropy_derivative(y, y_ref):
   return y - y_ref

class Perceptron:
    def __init__(self, input_neurons, hidden_neurons, output_neurons,  hidden_layers_count, epochs=100, learning_rate=1e-3):
        self.initial_weights = []
        self.initial_biases = []
        self.activation_functions = []
        self.activation_derivatives = []
        self.epochs = epochs
        self.learning_rate = learning_rate
        layers = [input_neurons] + [hidden_neurons] * hidden_layers_count + [output_neurons]
        for i in range(1, len(layers)):
            W = np.random.randn(layers[i - 1], layers[i]) * 0.01
            b = np.zeros(layers[i])
            self.initial_weights.append(W)
            self.initial_biases.append(b)
            self.activation_functions.append(ReLU)
            self.activation_derivatives.append(dReLU)
        self.activation_functions[-1] = softmax
        self.activation_derivatives[-1] = dsoftmax

    def set_learning_rate(self, learning_rate):
        self.learning_rate = learning_rate

    def set_epochs(self, epochs):
        self.epochs = epochs

    def predict(self, x):
        activation = x
        for W, b, f in zip(self.weights, self.biases, self.activation_functions):
            activation = f(np.dot(activation, W) + b)

        return int(np.argmax(activation))
    
    def forward_and_backward(self, x, y_ref, loss_func, dloss_func):
        grads_w = [None] * len(self.weights)
        grads_b = [None] * len(self.biases)
        activations = [x]
        inputs = []
        for (W, b, f) in zip(self.weights, self.biases, self.activation_functions):
            weighted_sum = np.dot(activations[-1], W) + b
            inputs.append(weighted_sum)
            activations.append(f(weighted_sum))

        y = activations[-1]
        loss = loss_func(y, y_ref)
        delta_loss = dloss_func(y, y_ref)
        dZ = np.dot(delta_loss, dsoftmax(inputs[-1]))
        grads_w[-1] = np.outer(activations[-2], dZ)
        grads_b[-1] = dZ

        for i in range(len(self.weights) - 2, -1, -1):
            dA = np.dot(dZ, self.weights[i + 1].T)
            dZ = dA * self.activation_derivatives[i](inputs[i])
            grads_w[i] = np.outer(activations[i], dZ)
            grads_b[i] = dZ

        return grads_w, grads_b, loss
    
    def validate(self, X, Y, loss_func):
        correct_predictions = 0
        total_loss = 0.0
        for (x, y_ref) in zip(X, Y):
            activation = x
            for W, b, f in zip(self.weights, self.biases, self.activation_functions):
                activation = f(np.dot(activation, W) + b)
            total_loss +=  loss_func(activation, y_ref)

            if int(np.argmax(activation)) == np.argmax(y_ref):
                correct_predictions += 1
        return total_loss / len(X), correct_predictions / len(X)

    def train(self, train_X, train_Y, validate_X, validate_Y, loss_func, dloss_func):
        train_one_hot = np.array([np.array([int(i == y) for i in range(10)]) for y in train_Y])
        validate_one_hot = np.array([np.array([int(i == y) for i in range(10)]) for y in validate_Y])
        self.weights = [np.copy(w) for w in self.initial_weights]
        self.biases = [np.copy(b) for b in self.initial_biases]
        loss, accuracy = self.validate(validate_X, validate_one_hot, loss_func)
        losses = [loss]
        accuracies = [accuracy]
        for epoch in range(self.epochs):
            for (x, y_ref) in zip(train_X, train_one_hot):
                weight_grads, bias_grads, loss = self.forward_and_backward(
                    x, 
                    y_ref,
                    loss_func, 
                    dloss_func
                )

                for i in range(len(self.weights)):
                    self.weights[i] -= weight_grads[i] * self.learning_rate
                    self.biases[i] -= bias_grads[i] * self.learning_rate
            loss, accuracy = self.validate(validate_X, validate_one_hot, loss_func)
            losses.append(loss)
            accuracies.append(accuracy)

        return losses, accuracies

INPUT_SIZE = 28**2
OUTPUT_NEURONS = 10

def experiment(num_neurons, num_layers, epochs, learging_rate):
    epochs_axis = np.arange(epochs + 1)

    perceptron = Perceptron(INPUT_SIZE, num_neurons, OUTPUT_NEURONS, num_layers, epochs, learging_rate)

    mse_loss, mse_accuracy = perceptron.train(train_X, train_Y, test_X, test_Y, MSE, dMSE)

    cross_entropy_loss, cross_entropy_accuracy = perceptron.train(train_X, train_Y, test_X, test_Y,categorical_cross_entropy, categorical_cross_entropy_derivative)

    KL_divergence_loss, KL_divergence_accuracy = perceptron.train(train_X, train_Y, test_X, test_Y, KL_divergence, KL_divergence_derivative)
    
    plt.figure()
    plt.title(f"Зависимость функции потерь от количества эпох, скрытых слоев: {num_layers}, нейронов: {num_neurons}")
    plt.xlabel("Количество эпох")
    plt.ylabel("Значение функции потерь")
    plt.plot(epochs_axis, mse_loss, label = "MSE")
    plt.plot(epochs_axis, cross_entropy_loss, label = "Categorical cross entropy")
    plt.plot(epochs_axis, KL_divergence_loss, label = "KL divergence")
    plt.legend()
    plt.show()

    plt.figure()
    plt.title(f"Зависимость точности от количества эпох, скрытых слоев: {num_layers}, нейронов: {num_neurons}")
    plt.xlabel("Количество эпох")
    plt.ylabel("Точность")
    plt.plot(epochs_axis, mse_accuracy, label = "MSE")
    plt.plot(epochs_axis, cross_entropy_accuracy, label = "Categorical cross entropy")
    plt.plot(epochs_axis, KL_divergence_accuracy, label = "KL divergence")
    plt.legend()
    plt.show()

    return

N_EPOCHS = 10
LEARNING_RATE = 0.01
num_layers_range = [1, 2, 3, 4]

for num_layers in num_layers_range:
    experiment(64, num_layers, N_EPOCHS, LEARNING_RATE)     

num_neurons_range = [32, 64, 128]
for neurons_num in num_layers_range:
    experiment(neurons_num, 3, N_EPOCHS, LEARNING_RATE)     
  