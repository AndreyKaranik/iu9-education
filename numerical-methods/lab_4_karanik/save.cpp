/**
 * Laboratory work: 4
 * Discipline: Numerical methods
 * Copyright (c) 2024, Andrey Karanik
*/

#include <iostream>
#define _USE_MATH_DEFINES
#include <cmath>
#include <iomanip>
#include <vector>

float n = 10, a = 0, b = 1, A = 1, B = exp(1);
float p = 5;
float q = -4;

float f(float x) {
    return 2 * expf(x);
}

float* solve(float *a, float *b, float *c, float *d, int n) {
    float *alpha = new float[n - 1];
    float *beta = new float[n - 1];
    float *x = new float[n];

    for (int i = 1; i < n - 1; i++) {
        if (std::fabs(b[i]) < std::fabs(a[i - 1]) + std::fabs(c[i])) {
            std::cout << "The condition is not satisfied." << std::endl;
            break;
        }
        if (std::fabs(b[i]) / (std::fabs(c[i])) < 1) {
            std::cout << "The condition is not satisfied." << std::endl;
            break;
        }
        if (std::fabs(b[i]) / (std::fabs(a[i - 1])) < 1) {
            std::cout << "The condition is not satisfied." << std::endl;
            break;
        }
    }

    if (b[0] != 0) {
        alpha[0] = -c[0] / b[0];
    } else {
        std::cout << "ERROR: b[0] = 0";
        return x;
    }

    beta[0] = d[0] / b[0];

    for (int i = 1; i < n - 1; i++) {
        alpha[i] = -(c[i]) / (alpha[i - 1] * a[i - 1] + b[i]);
        beta[i] = (d[i] - a[i - 1] * beta[i - 1]) / (alpha[i - 1] * a[i - 1] + b[i]);
    }
    beta[n - 1] = (d[n - 1] - a[n - 2] * beta[n - 2]) / (alpha[n - 2] * a[n - 2] + b[n - 1]);

    x[n - 1] = beta[n - 1];

    std::cout << "";
    
    for (int i = n - 2; i >= 0; i--) {
        x[i] = alpha[i] * x[i + 1] + beta[i];
    }

    delete[] alpha;
    delete[] beta;

    return x;
}

int main(int argc, char *argv[]) {

    double h = (b - a) / n;
    double xi = a + h;
    std::vector<float> y1, y, di, ai, bi, ci;
    bi.push_back(q * h * h - 2);
    ci.push_back(1 + p * h / 2);
    di.push_back(f(xi) * h * h - A * (1 - p * h / 2));
    for (int i = 2; i < n - 1; i++) {
        xi = a + i * h;
        ai.push_back(1 - p * h / 2);
        bi.push_back(q * h * h - 2);
        ci.push_back(1 + p * h / 2);
        di.push_back(f(xi) * h * h);
    }
    xi = a + h * (n - 1);
    bi.push_back(q * h * h - 2);
    ai.push_back(1 - p * h / 2);
    di.push_back(f(xi) * h * h - B * (1 + p * h / 2)); ci.push_back(0);
    float* result = solve(&ai[0], &bi[0], &ci[0], &di[0], 9);

    for (int i = 0; i < 9; i++) {
        std::cout << result[i] << std::endl;
        std::cout << expf((i+1) / 10.0f) << std::endl;
        std::cout << std::endl;
    }
    

    return 0;
}