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

// double F(double x1, double x2) {
//     return x1 * x1 * x1 * x1 + x2 * x2 + log(1 + 0.1 * x1 * x1 * x2 * x2) + 0.15 * x1;
// }

// double derivativeX1(double x1, double x2) {
//     return 4 * x1 * x1 * x1 + (2 * x2 * x2 * x1) / (10 + x2 * x2 * x1 * x1) + 0.15;
// }

// double derivativeX2(double x1, double x2) {
//     return 2 * x2 + (2 * x1 * x1 * x2) / (10 + x1 * x1 * x2 * x2);
// }

// int main(int argc, char *argv[]) {

//     return 0;
// }


class vector
{
    public:
    double x1 = 0;
    double x2 = 0;
    vector() {}
    vector(double x1, double x2) {
        this->x1 = x1;
        this->x2 = x2;
    }
};
 
double fx(vector x)
{
   return x.x1 * x.x1 * x.x1 * x.x1 + x.x2 * x.x2 + log(1 + 0.1 * x.x1 * x.x1 * x.x2 * x.x2) + 0.15 * x.x1;
}

vector gradient(vector x)
{
   vector grad;
 
   grad.x1 = 4 * x.x1 * x.x1 * x.x1 + (2 * x.x2 * x.x2 * x.x1) / (10 + x.x2 * x.x2 * x.x1 * x.x1) + 0.15;
   grad.x2 = 2 * x.x2 + (2 * x.x1 * x.x1 * x.x2) / (10 + x.x1 * x.x1 * x.x2 * x.x2);
 
   return grad;
}
 
double MakeSimplefx(double x, vector grad, vector xj)
{
   vector buffer;
 
   buffer.x1 = xj.x1 - x * grad.x1;
   buffer.x2 = xj.x2 - x * grad.x2;
 
   return fx(buffer);
}

double GoldenSelection(double a, double b, double eps, vector gradient, vector x)
{
   const double fi = 1.6180339887;
   double x1, x2;
   double y1, y2;
 
   x1 = b-((b-a) / fi);
   x2 = a+((b-a) / fi);
   y1 = MakeSimplefx(x1, gradient,x);
   y2 = MakeSimplefx(x2, gradient,x);
   while (std::abs(b-a)>eps)
   {
      if (y1<=y2)
      {
         b=x2;
         x2=x1;
         x1=b-((b-a)/fi);
         y2=y1;
         y1=MakeSimplefx(x1,gradient,x);
      }
      else
      {
         a=x1;
         x1=x2;
         x2=a+((b-a)/fi);
         y1=y2;
         y2=MakeSimplefx(x2,gradient,x);
      }
   }
 
   return (a+b)/2;
}

vector Calculate(vector x, vector gradient, double lambda)
{
   vector buffer;
 
   buffer.x1 = x.x1-lambda*gradient.x1;
   buffer.x2 = x.x2-lambda*gradient.x2;
 
   return buffer;
}
 
vector GradDown(vector x, double eps)
{
   vector current=x;
   vector last;
 
   do
   {
      last = current; //Запоминаем предыдущее значение
      vector grad = gradient(current); //Вычисляем градиент
      double lambda = GoldenSelection(0, 0.05, eps, grad, current); //Находим шаг вычислений методом золотого сечения
      current=Calculate(current,grad,lambda); //Вычисляем новое приближение
   } while(std::abs(fx(current) - fx(last))>eps); //Проверяем условие
 
   return current;
}

int main(int argc, char *argv[]) {
   vector x(0, 0);
   double eps = 0.001;
   return 0;
};