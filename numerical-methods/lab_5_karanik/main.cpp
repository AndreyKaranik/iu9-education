/**
 * Laboratory work: 5
 * Discipline: Numerical methods
 * Copyright (c) 2024, Andrey Karanik
*/

#include <iostream>
#define _USE_MATH_DEFINES
#include <cmath>
#include <iomanip>
#include <vector>

double F(double x1, double x2) {
    return x1 * x1 * x1 * x1 + x2 * x2 + log(1 + 0.1 * x1 * x1 * x2 * x2) + 0.15 * x1;
}

double derivativeX1(double x1, double x2) {
    return 4 * x1 * x1 * x1 + (2 * x2 * x2 * x1) / (10 + x2 * x2 * x1 * x1) + 0.15;
}

double derivativeX2(double x1, double x2) {
    return 2 * x2 + (2 * x1 * x1 * x2) / (10 + x1 * x1 * x2 * x2);
}

double derivativeX1X1(double x1, double x2) {
   return (0.2 * x2 * x2) / (1 + 0.1 * x2 * x2 * x1 * x1) + x1 * x1 * (12 - (0.04 * x2 * x2 * x2 * x2) / ((1 + 0.1 * x1 * x1 * x2 * x2) * (1 + 0.1 * x1 * x1 * x2 * x2)));
}

double derivativeX1X2(double x1, double x2) {
   return (40 * x1 * x2) / ((x1 * x1 * x2 * x2 + 10) * (x1 * x1 * x2 * x2 + 10));
}

double derivativeX2X2(double x1, double x2) {
   return (0.2 * x1 * x1) / (1 + 0.1 * x1 * x1 * x2 * x2) - (0.04 * x1 * x1 * x1 * x1 * x2 * x2) / ((1 + 0.1 * x1 * x1 * x2 * x2) * (1 + 0.1 * x1 * x1 * x2 * x2)) + 2;
}

int main(int argc, char *argv[]) {
   double eps = 0.001;

   int k = 0;
   double x1k = 0;
   double x2k = 1;

   double max;

   do {
      double phi1 = -(derivativeX1(x1k, x2k) * derivativeX1(x1k, x2k)) - (derivativeX2(x1k, x2k) * derivativeX2(x1k, x2k));
      double phi2 = derivativeX1X1(x1k, x2k) * derivativeX1(x1k, x2k) * derivativeX1(x1k, x2k) +
                     2 * derivativeX1X2(x1k, x2k) * derivativeX1(x1k, x2k) * derivativeX2(x1k, x2k) +
                     derivativeX2X2(x1k, x2k) * derivativeX2(x1k, x2k) * derivativeX2(x1k, x2k);
      double t = -(phi1 / phi2);
      double temp1 = x1k;
      double temp2 = x2k;
      x1k = temp1 - t * derivativeX1(temp1, temp2);
      x2k = temp2 - t * derivativeX2(temp1, temp2);
      k++;

      if (fabs(derivativeX1(x1k, x2k)) > fabs(derivativeX2(x1k, x2k))) {
         max = fabs(derivativeX1(x1k, x2k));
      } else {
         max = fabs(derivativeX2(x1k, x2k));
      }

   } while (max >= eps);

   std::cout << "minimum:" << std::endl;

   std::cout << x1k << " " << x2k << std::endl;

   std::cout << "analytical minimum:" << std::endl;

   std::cout << -0.334716 << " " << 0 << std::endl;

   std::cout << "step: " << std::endl;
   
   std::cout << k;

   return 0;
};