/**
 * Laboratory work: 1
 * Discipline: Numerical methods
 * Copyright (c) 2024, Karanik Andrey
*/

#include <iostream>
#include <cmath>

int main(int argc, const char * argv[]) {
    int n = 0;
    
    std::cout << "Enter size of matrix \n";
    std::cin >> n;

    float *a = new float[n-1];
    float *b = new float[n];
    float *c = new float[n-1];
    float *d = new float[n];
    float *alpha = new float[n - 1];
    float *beta = new float[n - 1];
    float *x = new float[n];
    float *expectedX = new float[n];
    
    std::cout << "Enter lower diagonal: \n";
    for (int i = 0; i < n - 1; i++) {
        std::cin >> a[i];
    }

    std::cout << "Enter main diagonal: \n";
    for (int i = 0; i < n; i++) {
        std::cin >> b[i];
    }

    std::cout << "Enter upper diagonal: \n";
    for (int i = 0; i< n - 1; i++) {
        std::cin >> c[i];
    }
    
    std::cout << "Enter d: \n";
    for (int i = 0; i < n; i++) {
        std::cin >> d[i];
    }

    std::cout << "Enter x: \n";
    for (int i = 0; i < n; i++) {
        std::cin >> expectedX[i];
    }

    for (int i = 1; i < n - 1; i++) {
        if (std::abs(b[i]) >= std::abs(a[i - 1]) + std::abs(c[i])) {
            std::cout << "The condition is not satisfied." << std::endl;
        }
        if (std::abs(b[i]) / (std::abs(c[i])) >= 1) {
            std::cout << "The condition is not satisfied." << std::endl;
        }
        if (std::abs(b[i]) / (std::abs(a[i - 1])) >= 1) {
            std::cout << "The condition is not satisfied." << std::endl;
        }
    }
    
    if (b[0] != 0) {
        alpha[0] = -c[0] / b[0];
    } else {
        std::cout << "ERROR: b[0] = 0";
        return 1;
    }

    beta[0] = d[0] / b[0];
    
    for (int i = 1; i < n - 1; i++) {
        alpha[i] = -(c[i]) / (alpha[i - 1] * a[i - 1] + b[i]);
        beta[i] = (d[i] - a[i - 1] * beta[i - 1]) / (alpha[i - 1] * a[i - 1] + b[i]);
    }
    beta[n - 1] = (d[n - 1] - a[n - 2] * beta[n - 2]) / (alpha[n - 2] * a[n - 2] + b[n - 1]);

    x[n - 1] = beta[n - 1];
    
    for (int i = n - 2; i >= 0; i--) {
        x[i] = alpha[i] * x[i + 1] + beta[i];
    }

    for (int i = 0; i < n; i++) {
        std::cout << "x[" << i << "] = " << x[i] << std::endl;
    }

    std::cout << std::endl;

    for (int i = 0; i < n; i++) {
        std::cout << "e[" << i << "] = " << x[i] - expectedX[i] << std::endl;
    }
    
    return 0;
}