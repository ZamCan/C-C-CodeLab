#include <stdio.h>

void swap(int *a, int *b) {
    int temporary = *a;
    *a = *b;
    *b = temporary;
}

int main(void) {
    int value = 10;
    int *pointer = &value;

    *pointer = 20;
    printf("%d\n", value);

    int left = 3;
    int right = 7;
    swap(&left, &right);
    printf("%d %d\n", left, right);
    return 0;
}
/* expect:
20
7 3
*/
