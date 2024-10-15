import matplotlib.pyplot as plt

# Чтение данных из файлов
def read_loss_data(filename):
    epochs = []
    losses = []
    with open(filename, 'r') as f:
        for line in f:
            epoch, loss = map(float, line.split())
            epochs.append(epoch)
            losses.append(loss)
    return epochs, losses

# Получаем данные для всех функций активации
epochs_sigmoid, losses_sigmoid = read_loss_data('sigmoid_loss.txt')
epochs_tanh, losses_tanh = read_loss_data('tanh_loss.txt')
epochs_relu, losses_relu = read_loss_data('relu_loss.txt')

# Построение графиков
plt.plot(epochs_sigmoid, losses_sigmoid, label='Sigmoid')
plt.plot(epochs_tanh, losses_tanh, label='Tanh')
plt.plot(epochs_relu, losses_relu, label='ReLU')

plt.xlabel('Epochs')
plt.ylabel('Loss')
plt.title('Loss vs Epochs for different activation functions')
plt.legend()

# Отображение графика
plt.show()