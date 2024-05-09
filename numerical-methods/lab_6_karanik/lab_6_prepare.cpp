/**
 * Laboratory work: 6
 * Discipline: Numerical methods
 * Copyright (c) 2024, Andrey Karanik
*/

#include <iostream>
#define _USE_MATH_DEFINES
#include <cmath>
#include <iomanip>
#include <vector>

double F(double x) {
   double a = 2;
   double b = 0;
   double c = -9;
   double d = 1;

   return a * x * x * x + b * x * x + c * x + d;
}

double derivativeX(double x) {
   double a = 2;
   double b = 0;
   double c = -9;
   return 3 * a * x * x + 2 * b * x + c;
}

double derivativeXX(double x) {
   double a = 2;
   double b = 0;
   return 6 * a * x + 2 * b;
}

// Метод деления отрезка пополам
double method1(double a, double b, double eps) {
   double k = 0;
   while (b - a > 2 * eps) {
      double x = (a + b) / 2.0;

      if (F(a) * F(x) < 0) {
         b = x;
      } else {
         a = x;
      }
      k++;
   }

   std::cout << " (Number of approximations = " << k << ") ";

   return (a + b) / 2.0;
}

int sgn(double x) {
   if (x > 0) {
      return 1;
   } else if (x == 0) {
      return 0;
   }
   return -1;
}

// Метод Ньютона
double method2(double a, double b, double eps) {
   double xk;
   if (F(a) * derivativeXX(a) > 0) {
      xk = a;
   } else {
      xk = b;
   }

   double xk1 = xk;

   double k = 0;

   do {
      xk = xk1;
      xk1 = xk - F(xk) / derivativeX(xk);
      k++;
   } while (F(xk1) * F(xk1 + sgn(xk1 - xk) * eps) >= 0);

   std::cout << " (Number of approximations = " << k << ") ";


   return xk1;
}

int main(int argc, char *argv[]) {

   int n = 3;

   double* a = new double[n] {-2.5, 0.05, 1.5};
   double* b = new double[n] {-2, 0.15, 2.5};

   for (int i = 0; i < n; i++) {
      std::cout << "i=" << i << ", a=" << a[i] << ", b=" << b[i] << std::endl;
      std::cout << "Method of dividing segments in half: ";
      std::cout << method1(a[i], b[i], 0.001) << std::endl;
      std::cout << "Newton's method: ";
      std::cout << method2(a[i], b[i], 0.001) << std::endl;
      std::cout << std::endl;
   }

   delete[] a;
   delete[] b;
   return 0;
};