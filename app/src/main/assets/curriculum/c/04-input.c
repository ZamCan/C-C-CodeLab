#include <stdio.h>

int main(void) {
    int count = 0;

    printf("Enter a count: ");
    if (scanf("%d", &count) != 1) {
        puts("Invalid input");
        return 1;
    }
    printf("Double: %d\n", count * 2);
    return 0;
}
/* expect:
Enter a count: Double: 42
*/
