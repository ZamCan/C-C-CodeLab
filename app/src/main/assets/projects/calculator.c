#include <stdio.h>
#include <math.h>

int calculate(double left, char operation, double right, double *result) {
    switch (operation) {
        case '+': *result = left + right; break;
        case '-': *result = left - right; break;
        case '*': *result = left * right; break;
        case '/': if (right == 0) return 0; *result = left / right; break;
        default: return 0;
    }
    return isfinite(*result);
}

int main(void) {
    double left, right, result;
    char operation, extra;
    char line[256];
    if (!fgets(line, sizeof line, stdin) ||
        sscanf(line, "%lf %c %lf %c", &left, &operation, &right, &extra) != 3 ||
        !isfinite(left) || !isfinite(right)) {
        fputs("Invalid input\n", stderr);
        return 1;
    }
    if (!calculate(left, operation, right, &result)) {
        fputs("Invalid operation or result\n", stderr);
        return 2;
    }
    printf("%.6g\n", result);
    return 0;
}
