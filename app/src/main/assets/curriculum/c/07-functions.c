#include <stdio.h>

int square(int value) {
    return value * value;
}

int maximum(int a, int b) {
    return a > b ? a : b;
}

int main(void) {
    printf("%d\n", square(6));
    printf("%d\n", maximum(12, 27));
    return 0;
}
/* expect:
36
27
*/
