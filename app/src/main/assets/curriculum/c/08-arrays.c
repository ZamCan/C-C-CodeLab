#include <stdio.h>

int main(void) {
    int marks[] = {70, 80, 90};
    int count = 3;
    int total = 0;
    char name[32] = "Ada";

    for (int i = 0; i < count; ++i) {
        total += marks[i];
    }
    printf("%s: average %.1f\n", name, (double) total / count);
    printf("last valid index: %d\n", count - 1);
    return 0;
}
/* expect:
Ada: average 80.0
last valid index: 2
*/
