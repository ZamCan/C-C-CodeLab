#include <stdio.h>

int main(void) {
    int total = 0;

    for (int i = 1; i <= 5; ++i) {
        total += i;
        printf("i=%d total=%d\n", i, total);
    }
    return 0;
}
/* expect:
i=1 total=1
i=2 total=3
i=3 total=6
i=4 total=10
i=5 total=15
*/
