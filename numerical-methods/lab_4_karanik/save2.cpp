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

double* solve(double *a, double *b, double *c, double *d, int n) {
    double *alpha = new double[n - 1];
    double *beta = new double[n - 1];
    double *x = new double[n];

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
        std::cout << "ERROR: b[0] = 0" << std::endl;
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

double f(double x) {
    return 16 * x * exp(2 * x);
}

double y(double x) {
    return exp(-2 * x) * (exp(4 * x) * (2 * x * x - x + 1) - 1);
}

int main(int argc, char *argv[]) {

    double a = 0;
    double b = 1;
    int n = 10;
    double p = 0;
    double q = -4;

    double h = (b - a) / n;
    double* xi = new double[n + 1];
    double* yi = new double[n + 1];
    double* ai = new double[n - 2];
    double* bi = new double[n - 1];
    double* ci = new double[n - 2];
    double* di = new double[n - 1];

    for (int i = 0; i <= n; i++) {
        xi[i] = a + i * h;
    }

    yi[0] = y(0);
    yi[n] = y(1);

    for (int i = 0; i < n - 2; i++) {
        ai[i] = 1 - (h / 2) * p;
        ci[i] = 1 + (h / 2) * p;
    }

    for (int i = 0; i < n - 1; i++) {
        bi[i] = h * h * q - 2;
    }

    di[0] = h * h * f(xi[1]) - yi[0] * (1 - (h / 2) * p);
    di[n - 2] = h * h * f(xi[n - 1]) - yi[n] * (1 + (h / 2) * p);

    for (int i = 1; i < n - 2; i++) {
        di[i] = h * h * f(xi[i + 1]);
    }

    double* solution = solve(ai, bi, ci, di, n - 1);

    for (int i = 1; i < n; i++) {
        yi[i] = solution[i - 1];
    }

    std::cout << "Numerical solution (xi, yi), i = 0, ..., n." << std::endl;

    for (int i = 0; i <= n; i++) {
        std::cout << "(" << xi[i] << ", " << yi[i] << ")" << std::endl;
    }

    std::cout << std::endl;

    double error = 0;
    for (int i = 0; i <= n; i++) {
        if (error < fabs(y(xi[i]) - yi[i])) {
            error = fabs(y(xi[i]) - yi[i]);
        }
    }

    std::cout << "Error of numerical solution: " << error << std::endl;

    return 0;
}