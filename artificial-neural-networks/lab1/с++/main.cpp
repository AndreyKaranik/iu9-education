#include <iostream>
#include <vector>
#include <cmath>
#include <random>
#include <map>
#include <fstream> // Для записи данных в файл

using namespace std;

// Определяем активационные функции
enum ActivationFunction { LINEAR, SIGMOID, TANH, RELU };

// Функции активации
double linear(double x) {
    return x;
}

double sigmoid(double x) {
    return 1.0 / (1.0 + exp(-x));
}

double tanh_act(double x) {
    return tanh(x);
}

double relu(double x) {
    return x > 0 ? x : 0;
}

// Производные функций активации
double sigmoid_derivative(double x) {
    return sigmoid(x) * (1 - sigmoid(x));
}

double tanh_derivative(double x) {
    return 1 - pow(tanh(x), 2);
}

double relu_derivative(double x) {
    return x > 0 ? 1 : 0;
}

// Персептрон
class Perceptron {
private:
    vector<double> weights;
    double bias;
    ActivationFunction act_func;
    double (*activation)(double);      // Указатель на функцию активации
    double (*activation_derivative)(double); // Указатель на производную

public:
    Perceptron(int input_size, ActivationFunction af) : bias(0.0), act_func(af) {
        // Инициализация весов случайными значениями
        random_device rd;
        mt19937 gen(rd());
        uniform_real_distribution<> dis(-1, 1);
        
        weights.resize(input_size);
        for (double &w : weights) {
            w = dis(gen);
        }
        
        // Установка функции активации и её производной
        switch (af) {
            case LINEAR:
                activation = linear;
                activation_derivative = [](double) { return 1.0; }; // Производная линейной функции
                break;
            case SIGMOID:
                activation = sigmoid;
                activation_derivative = sigmoid_derivative;
                break;
            case TANH:
                activation = tanh_act;
                activation_derivative = tanh_derivative;
                break;
            case RELU:
                activation = relu;
                activation_derivative = relu_derivative;
                break;
        }
    }

    // Прямой проход
    double predict(const vector<int>& inputs) {
        double sum = bias;
        for (size_t i = 0; i < weights.size(); ++i) {
            sum += weights[i] * inputs[i];
        }
        return activation(sum);
    }

    // Обучение
    void train(const vector<int>& inputs, double target, double learning_rate = 0.1) {
        double output = predict(inputs);
        double error = target - output;

        // Коррекция весов и смещения
        for (size_t i = 0; i < weights.size(); ++i) {
            weights[i] += learning_rate * error * activation_derivative(output) * inputs[i];
        }
        bias += learning_rate * error * activation_derivative(output);
    }

    // Расчет ошибки (например, среднеквадратичной)
    double calculate_loss(const vector<int>& inputs, double target) {
        double output = predict(inputs);
        double error = target - output;
        return error * error; // MSE
    }
};

// Пример данных для цифр и букв (5x4)
map<char, vector<int>> generate_data() {
    map<char, vector<int>> data;
    
    // Пример цифр 0 и 1, где 1 - заполненные ячейки, 0 - пустые
    data['0'] = {
        1, 1, 1, 1,
        1, 0, 0, 1,
        1, 0, 0, 1,
        1, 0, 0, 1,
        1, 1, 1, 1
    };
    
    data['1'] = {
        0, 0, 1, 0,
        0, 1, 1, 0,
        0, 0, 1, 0,
        0, 0, 1, 0,
        0, 1, 1, 1
    };
    
    // Пример буквы "А" и "Б"
    data['A'] = {
        0, 1, 1, 0,
        1, 0, 0, 1,
        1, 1, 1, 1,
        1, 0, 0, 1,
        1, 0, 0, 1
    };
    
    data['B'] = {
        1, 1, 1, 1,
        1, 0, 0, 0,
        1, 1, 1, 0,
        1, 0, 0, 1,
        1, 1, 1, 0
    };
    
    return data;
}

int main() {
    // Генерируем данные
    auto data = generate_data();
    
    // Создаем персептрон для каждой функции активации
    Perceptron perceptron_sigmoid(20, SIGMOID);
    Perceptron perceptron_tanh(20, TANH);
    Perceptron perceptron_relu(20, RELU);
    
    // Открываем файлы для записи результатов
    ofstream sigmoid_loss_file("sigmoid_loss.txt");
    ofstream tanh_loss_file("tanh_loss.txt");
    ofstream relu_loss_file("relu_loss.txt");
    
    // Тренируем персептрон на цифрах и буквах
    for (int epoch = 0; epoch < 1000; ++epoch) {
        double sigmoid_loss = 0.0;
        double tanh_loss = 0.0;
        double relu_loss = 0.0;
        
        for (const auto& [key, input] : data) {
            // Цели (например, 0 и 1 для бинарной классификации)
            double target = (key == '0' || key == 'A') ? 1.0 : 0.0;
            
            // Обучение
            perceptron_sigmoid.train(input, target);
            perceptron_tanh.train(input, target);
            perceptron_relu.train(input, target);
            
            // Вычисление ошибки для текущей эпохи
            sigmoid_loss += perceptron_sigmoid.calculate_loss(input, target);
            tanh_loss += perceptron_tanh.calculate_loss(input, target);
            relu_loss += perceptron_relu.calculate_loss(input, target);
        }
        
        // Записываем среднюю ошибку для каждой функции активации в файлы
        sigmoid_loss_file << epoch << " " << sigmoid_loss / data.size() << endl;
        tanh_loss_file << epoch << " " << tanh_loss / data.size() << endl;
        relu_loss_file << epoch << " " << relu_loss / data.size() << endl;
    }
    
    // Закрываем файлы
    sigmoid_loss_file.close();
    tanh_loss_file.close();
    relu_loss_file.close();
    
    return 0;
}