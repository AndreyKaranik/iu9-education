#include <iostream>
#include <vector>
#include <cmath>
#include <fstream>
#include <random>
#include <string>

typedef std::vector<double> Vector;
typedef std::vector<Vector> Matrix;

// Линейная функция активации
double linear(double x) {
    return x;
}

// Сигмоида
double sigmoid(double x) {
    return 1.0 / (1.0 + exp(-x));
}

// Гиперболический тангенс
double tanh(double x) {
    return std::tanh(x);
}

// ReLU
double relu(double x) {
    return std::max(0.0, x);
}

// Выбор функции активации
double activate(double x, const std::string& activation) {
    if (activation == "linear") return linear(x);
    else if (activation == "sigmoid") return sigmoid(x);
    else if (activation == "tanh") return tanh(x);
    else if (activation == "relu") return relu(x);
    else return linear(x); // По умолчанию линейная
}

// Персептрон
class Perceptron {
public:
    // Конструктор для создания персептрона с N входами и M выходами
    Perceptron(int input_size, int output_size, const std::string& activation_function = "sigmoid")
        : weights(output_size, Vector(input_size)), activation(activation_function) {
        std::random_device rd;
        std::mt19937 gen(rd());
        std::uniform_real_distribution<> dis(-1, 1);

        // Инициализация весов случайными значениями
        for (auto& row : weights) {
            for (auto& w : row) {
                w = dis(gen);
            }
        }
    }

    // Прямое распространение (возвращает вектор вероятностей)
    Vector forward(const Vector& input) {
        Vector output(weights.size());
        for (int i = 0; i < weights.size(); ++i) {
            double sum = 0.0;
            for (int j = 0; j < input.size(); ++j) {
                sum += input[j] * weights[i][j];
            }
            output[i] = activate(sum, activation);
        }
        return output;
    }

    // Обучение методом градиентного спуска
    void train(const Matrix& X, const Matrix& y, int epochs, double lr, const std::string& filename) {
        std::ofstream loss_file(filename);

        for (int epoch = 0; epoch < epochs; ++epoch) {
            double total_loss = 0.0;
            for (int i = 0; i < X.size(); ++i) {
                Vector prediction = forward(X[i]);
                Vector error(y[i].size());

                // Подсчет ошибки и обновление весов для каждого выхода
                for (int k = 0; k < y[i].size(); ++k) {
                    error[k] = y[i][k] - prediction[k];
                    total_loss += error[k] * error[k]; // MSE (Среднеквадратичная ошибка)

                    // Обновление весов
                    for (int j = 0; j < weights[k].size(); ++j) {
                        weights[k][j] += lr * error[k] * X[i][j];
                    }
                }
            }
            total_loss /= (X.size() * y[0].size());
            loss_file << epoch << "," << total_loss << std::endl;
        }
        loss_file.close();
    }

private:
    Matrix weights; // Матрица весов
    std::string activation;
};

int main() {
    // Искусственные данные (например, представления цифр 0 и 1 в виде 5x4)
    Matrix X = {
        {0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0}, // Пример цифры 0
        {0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1}, // Пример цифры 1
        // Добавьте больше примеров для цифр и букв
    };

    // Метки данных: один вектор для каждой цифры/буквы, где правильная категория 1, остальные 0
    Matrix y = {
        {1, 0}, // Метка для цифры 0
        {0, 1}, // Метка для цифры 1
        // Добавьте больше меток для остальных цифр и букв
    };

    // Список функций активации и соответствующие файлы для сохранения результатов
    std::vector<std::string> activations = {"linear", "sigmoid"}; //, "tanh", "relu"};
    std::vector<std::string> filenames = {
        "loss_data_linear.csv",
        "loss_data_sigmoid.csv",
        "loss_data_tanh.csv",
        "loss_data_relu.csv"
    };
    for (int i = 0; i < activations.size(); ++i) {
        std::cout << "start\n";
        std::cout << "Training with activation: " << activations[i] << std::endl;
        Perceptron p(20, 2, activations[i]); // 20 входов и 10 выходов
        p.train(X, y, 100, 0.01, filenames[i]);
        std::cout << "end\n";
    }

    Perceptron p(20, 2, "linear");
    Vector result = p.forward(X[0]);
    for (auto val : result) {
        std::cout << val << " ";
    }
    

    return 0;
}