/**
 * Laboratory work: 1
 * Discipline: Numerical methods
 * Copyright (c) 2024, Andrey Karanik
*/

#include <iostream>
#include <cmath>
#include <iomanip>

int main(int argc, char *argv[]) {
    int n = 0;
    
    std::cout << "Enter size of matrix:" << std::endl;
    std::cin >> n;

    float *a = new float[n-1];
    float *b = new float[n];
    float *c = new float[n-1];
    float *d = new float[n];
    float *alpha = new float[n - 1];
    float *beta = new float[n - 1];
    float *x = new float[n];
    float *expectedX = new float[n];
    
    std::cout << "Enter lower diagonal:" << std::endl;
    for (int i = 0; i < n - 1; i++) {
        std::cin >> a[i];
    }

    std::cout << "Enter main diagonal:" << std::endl;
    for (int i = 0; i < n; i++) {
        std::cin >> b[i];
    }

    std::cout << "Enter upper diagonal:" << std::endl;
    for (int i = 0; i< n - 1; i++) {
        std::cin >> c[i];
    }
    
    std::cout << "Enter d:" << std::endl;
    for (int i = 0; i < n; i++) {
        std::cin >> d[i];
    }

    std::cout << "Enter expected x:" << std::endl;
    for (int i = 0; i < n; i++) {
        std::cin >> expectedX[i];
    }

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

    std::cout << std::endl;

    for (int i = 0; i < n; i++) {
        std::cout << "x[" << i << "] = " << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << x[i] << std::endl;
    }

    std::cout << std::endl;;


    for (int i = 0; i < n; i++) {
        std::cout << "error[" << i << "] = " << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << x[i] - expectedX[i] << std::endl;
    }

    delete[] a;
    delete[] b;
    delete[] c;
    delete[] d;
    delete[] alpha;
    delete[] beta;
    delete[] x;
    delete[] expectedX;
    
    return 0;
}