/**
 * Laboratory work: 2
 * Discipline: Numerical methods
 * Copyright (c) 2024, Andrey Karanik
*/

#include <iostream>
#define _USE_MATH_DEFINES
#include <cmath>
#include <iomanip>

float a = 0.25f;
float b = 4.0f;
float eps = 0.001f;

float f(float x) {
    return logf(4*x) / x;
}

float rectangleMethod(float h, int n) {
    float sum = 0;
    for (int i = 0; i < n; i++) {
        float x = a + i * h;
        sum += f(x + h / 2.0f);
    }
    return h * sum;
}

float simpsonMethod(float h, int n) {
    float result = 0;
    result = f(a) + f(b);
    for (int i = 1; i < n; i += 2) {
        float x1 = a + i * h;
        float x2 = a + (i + 1) * h;
        result += 4 * f(x1);
        if (i <= n - 2) {
            result += 2 * f(x2);
        }
    }
    result *= h / 3.0f;
    return result;
}

float trapezoidMethod(float h, int n) {
    float result = 0;

    result += (f(a) + f(b)) / 2.0f;
 
    for (int i = 1; i < n; i++) {
        result += f(a + i * h);
    }

    result *= h;

    return result;
}

int main(int argc, char *argv[]) {
    int n = 2;

    int n1 = 0;
    int n2 = 0;
    int n3 = 0;

    float h1 = 0;
    float h2 = 0;
    float h3 = 0;

    float r1 = 0;
    float r2 = 0;
    float r3 = 0;

    float integral1 = 0;
    float integral2 = 0;
    float integral3 = 0;

    bool check1 = false;
    bool check2 = false;
    bool check3 = false;

    while (true) {
        float h = (b - a) / (float)n;
        float delta = 0;

        if (!check1) {
            integral1 = rectangleMethod(h / 2.0f, 2 * n);
            r1 = fabs(rectangleMethod(h, n) - integral1) / (4.0f - 1.0f);
            check1 = r1 <= eps;
            if (check1) {
                h1 = h;
                n1 = n;
            }
        }
        
        if (n % 2 == 0) {
            if (!check2) {
                integral2 = simpsonMethod(h / 2.0f, 2 * n);
                r2 = fabs(simpsonMethod(h, n) - integral2) / (16.0f - 1.0f);
                check2 = r2 <= eps;
                if (check2) {
                    h2 = h;
                    n2 = n;
                }
            }
        }

        if (!check3) {
            integral3 = trapezoidMethod(h / 2.0f, 2 * n);
            r3 = fabs(trapezoidMethod(h, n) - integral3) / (4.0f - 1.0f);
            check3 = r3 <= eps;
            if (check3) {
                h3 = h;
                n3 = n;
            }
        }

        if (check1 && check2 && check3) {
            break;
        }

        n++;
    }

    std::cout << "n: " << n1 << std::endl;
    std::cout << "rectangleMethod(h/2): " << integral1 << std::endl;
    std::cout << "R: " << r1 << std::endl;
    std::cout << "rectangleMethod(h/2) + R: " << integral1 + r1 << std::endl;

    std::cout << std::endl;

    std::cout << "n: " << n2 << std::endl;
    std::cout << "simpsonMethod(h/2): " << integral2 << std::endl;
    std::cout << "R: " << r2 << std::endl;
    std::cout << "simpsonMethod(h/2) + R: " << integral2 + r2 << std::endl;

    std::cout << std::endl;

    std::cout << "n: " << n3 << std::endl;
    std::cout << "trapezoidMethod(h/2): " << integral3 << std::endl;
    std::cout << "R: " << r3 << std::endl;
    std::cout << "trapezoidMethod(h/2) + R: " << integral3 + r3 << std::endl;

    return 0;
}