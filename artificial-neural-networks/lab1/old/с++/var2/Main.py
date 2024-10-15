import matplotlib.pyplot as plt
import csv

def load_loss_data(filename):
    epochs = []
    losses = []
    with open(filename, 'r') as file:
        reader = csv.reader(file)
        for row in reader:
            epochs.append(int(row[0]))
            losses.append(float(row[1]))
    return epochs, losses

# Загрузка данных для каждой функции активации
epochs_linear, losses_linear = load_loss_data('loss_data_linear.csv')
epochs_sigmoid, losses_sigmoid = load_loss_data('loss_data_sigmoid.csv')
epochs_tanh, losses_tanh = load_loss_data('loss_data_tanh.csv')
epochs_relu, losses_relu = load_loss_data('loss_data_relu.csv')

# Построение графиков
plt.plot(epochs_linear, losses_linear, label='Linear Activation', color='blue')
plt.plot(epochs_sigmoid, losses_sigmoid, label='Sigmoid Activation', color='green')
plt.plot(epochs_tanh, losses_tanh, label='Tanh Activation', color='red')
plt.plot(epochs_relu, losses_relu, label='ReLU Activation', color='purple')

# Настройка графика
plt.xlabel('Epochs')
plt.ylabel('Loss')
plt.title('Loss vs Epochs for Different Activation Functions')
plt.legend()
plt.grid(True)

# Отображение графика
plt.show()
