/**
 * Laboratory work: 7
 * Discipline: Numerical methods
 * Copyright (c) 2024, Andrey Karanik
*/

#include <iostream>
#define _USE_MATH_DEFINES
#include <cmath>
#include <iomanip>
#include <vector>

struct complex {
    double Re = 0;
    double Im = 0;

    complex() {}
    complex(double re) { Re = re; }
    complex(double re, double im) { Re = re; Im = im; }

    friend complex operator+(const complex& A, const complex& B);
    friend complex operator-(const complex& A, const complex& B);
    friend complex operator*(const complex& A, const complex& B);

    friend complex operator!=(const complex& A, const complex& B);

    friend std::ostream& operator<<(std::ostream& stream, const complex& A);

    double abs() const { return sqrt(Re * Re + Im * Im); }
};

complex operator+(const complex& A, const complex& B) {
    return complex(A.Re + B.Re, A.Im + B.Im);
}

complex operator-(const complex& A, const complex& B) {
    return complex(A.Re - B.Re, A.Im - B.Im);
}

complex operator*(const complex& A, const complex& B) {
    return complex(A.Re * B.Re - A.Im * B.Im, A.Re * B.Im + A.Im * B.Re);
}

std::ostream& operator<<(std::ostream& stream, const complex& A) {
    stream << A.Re;
    if (A.Im >= 0) stream << "+";
    stream << A.Im << "j";
    return stream;
}

complex operator!=(const complex& A, const complex& B) {
    return ((A.Re == B.Re) && (A.Im == B.Im));
}

void FFT(std::vector<complex>& a) 
{
	int n = (int)a.size();
	if (n == 1) return;
 
	std::vector<complex> a0(n/2),  a1(n/2);
	for (int i=0, j=0; i<n; i+=2, ++j)
    {
		a0[j] = a[i];
		a1[j] = a[i+1];
	}

	FFT (a0);
	FFT (a1);
 
	double ang = 2 * M_PI / n ;
	complex w(1), wn(cos(ang), sin(ang));
	for (int i = 0; i < n / 2; ++i)
    {
		a[i] = a0[i] + w * a1[i];
		a[i + n / 2] = a0[i] - w * a1[i];
		w = w * wn; // Изменяем коэффициент
	}
}


double f(double x) {
    return (x - (int)x) * sin(2 * M_PI * x);
}

int main(int argc, char *argv[]) {
    
    int n = 128;

    std::vector<complex> vec;

    for (int i = 0; i < n; i++) {
        double x = i / n;
        vec.push_back(complex(f(x)));
        std::cout << f(x) << " ";
    }

    FFT(vec);
    for (int i = 0; i < n; i++) {
        std::cout << vec[i] << std::endl;
    }
    return 0;
};