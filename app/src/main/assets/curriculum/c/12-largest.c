#include <stdio.h>

int largest(const int values[], int count) {
    if (count <= 0) {
        return 0;
    }
    int best = values[0];
    for (int i = 1; i < count; ++i) {
        if (values[i] > best) {
            best = values[i];
        }
    }
    return best;
}

int main(void) {
    int values[] = {4, 1, 9};
    printf("%d\n", largest(values, 3));
    return 0;
}
/* expect:
9
*/
