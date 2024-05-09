/**
 * Laboratory work: 7
 * Discipline: Numerical methods
 * Copyright (c) 2024, Andrey Karanik
*/

#include <iostream>
#define _USE_MATH_DEFINES
#include <cmath>
#include <iomanip>

float* solve(float *a, float *b, float *c, float *d, int n) {
    float *alpha = new float[n - 1];
    float *beta = new float[n - 1];
    float *x = new float[n];
    // for (int i = 1; i < n - 1; i++) {
    //     if (std::fabs(b[i]) < std::fabs(a[i - 1]) + std::fabs(c[i])) {
    //         std::cout << "The condition is not satisfied." << std::endl;
    //         break;
    //     }
    //     if (std::fabs(b[i]) / (std::fabs(c[i])) < 1) {
    //         std::cout << "The condition is not satisfied." << std::endl;
    //         break;
    //     }
    //     if (std::fabs(b[i]) / (std::fabs(a[i - 1])) < 1) {
    //         std::cout << "The condition is not satisfied." << std::endl;
    //         break;
    //     }
    // }
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

    for (int i = 0; i < n - 1; i++) {
        std::cout << alpha[i] << " ";
    }
    std::cout << std::endl;
    for (int i = 0; i < n - 1; i++) {
        std::cout << beta[i] << " ";
    }
    std::cout << std::endl;


    delete[] alpha;
    delete[] beta;

    return x;
}

float calculateSplineValue(float x, float *a, float *b, float *c, float *d, float *_x, float left, float h) {
    int i = (x - left) / h;
    float difference = x - _x[i];
    return a[i] + b[i] * difference + c[i] * difference * difference + d[i] * difference * difference * difference;
}

int main(int argc, char *argv[]) {

    int n = 10;
    
    float left = 0.25;
    float right = 4.0;

    float *x = new float[n + 1];
    float *y = new float[n + 1];
    
    float *a = new float[n + 1];
    float *b = new float[n + 1]; 
    float *c = new float[n + 1];
    float *d = new float[n + 1];
    float *spline = new float[n + 1];
    float *delta = new float[n + 1];
    
    float h = (right - left) / (float)n;
    
    x[0] = left;
    for (int i = 1; i <= n; i++) {
        x[i] = x[i - 1] + h;
    }

    for (int i = 0; i <= n; i++) {
        y[i] = log(4 * x[i]) / x[i];
    }
 
    float *diag_a = new float[n - 2];
    float *diag_b = new float[n - 1];
    float *diag_c = new float[n - 2];
    float *free_d = new float[n - 1];

    for (int i = 0; i < n - 2; i++) {
        diag_a[i] = 1.0f;
        diag_c[i] = 1.0f;
    }
    
    for (int i = 0; i < n - 1; i++) {
        diag_b[i] = 4.0f;
    }

    for (int i = 0; i < n - 1; i++) {
        free_d[i] = (y[i + 1] - 2.0f * y[i] + y[i - 1]) / (h * h);
    }

    float* solution = solve(diag_a, diag_b, diag_c, free_d, n - 1);
    for (int i = 1; i <= n - 1; i++) {
        c[i] = solution[i - 1];
    }

    c[n] = 0;
    c[0] = 0;

    for (int i = 0; i <= n; i++) {
        a[i] = y[i];
    }
    
    for (int i = 0; i <= n - 2; i++) {
        b[i] = ((y[i + 1] - y[i]) / h) * (h / 3.0f) * (c[i + 1] + 2.0f * c[i]);
        d[i] = (c[i + 1] - c[i]) / (3.0f * h);
    }

    b[n - 1] = ((y[n] - y[n - 1]) / h) * (2.0f / 3.0f) * h * c[n - 1];
    d[n - 1] = c[n] / (3.0f * h);

    for (int i = 0; i <= n - 1; i++) {
        spline[i] = calculateSplineValue(x[i], a, b, c, d, x, left, h);
    }

    std::cout << "x: ";
    for (int i = 0; i <= n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << x[i] << " ";
    }
    std::cout << std::endl;

    std::cout << "S(x): ";
    for (int i = 0; i < n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << spline[i] << " ";
    }
    std::cout << std::endl;
    
    std::cout << "y(x): ";
    for (int i = 0; i <= n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << y[i] << " ";
    }
    std::cout << std::endl;

    std::cout << "|S(x)-y(x)|: ";
    for (int i = 0; i < n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << fabs(spline[i] - y[i]) << " ";
    }
    std::cout << std::endl;

    for (int i = 1; i <= n - 1; i++) {
        x[i] = left + (i - 0.5) * h;
    }

    for (int i = 0; i <= n - 1; i++) {
        y[i] = log(4 * x[i]) / x[i];
    }

    for (int i = 0; i <= n - 1; i++) {
        spline[i] = calculateSplineValue(x[i], a, b, c, d, x, left, h);
    }
    
    for (int i = 0; i <= n - 1; i++) {
        delta[i] = fabs(y[i] - spline[i]);
    }

    std::cout << "x: ";
    for (int i = 0; i <= n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << x[i] << " ";
    }
    std::cout << std::endl;

    std::cout << "S(x): ";
    for (int i = 0; i < n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << spline[i] << " ";
    }
    std::cout << std::endl;
    
    std::cout << "y(x): ";
    for (int i = 0; i <= n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << y[i] << " ";
    }
    std::cout << std::endl;

    std::cout << "|S(x)-y(x)|: ";
    for (int i = 0; i < n; i++) {
        std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << fabs(spline[i] - y[i]) << " ";
    }
    std::cout << std::endl;

    // std::cout << "a: ";
    // for (int i = 0; i <= n - 1; i++) {
    //     std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << a[i] << " ";
    // }
    // std::cout << std::endl;

    // std::cout << "b: ";
    // for (int i = 0; i <= n - 1; i++) {
    //     std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << b[i] << " ";
    // }
    // std::cout << std::endl;

    // std::cout << "c: ";
    // for (int i = 0; i <= n - 1; i++) {
    //     std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << c[i] << " ";
    // }
    // std::cout << std::endl;

    // std::cout << "d: ";
    // for (int i = 0; i <= n - 1; i++) {
    //     std::cout << std::fixed << std::setprecision(std::numeric_limits<float>::max_digits10) << d[i] << " ";
    // }
    // std::cout << std::endl;


    delete[] x;
    delete[] y;
    delete[] a;
    delete[] b;
    delete[] c;
    delete[] d;
    delete[] solution;
    delete[] spline;
    delete[] delta;
    
    return 0;
}