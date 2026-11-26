#include <stdio.h>

int main(void) {
    int lessons = 10;
    double hours = 2.5;
    char grade = 'A';

    printf("%d lessons, %.1f hours, grade %c\n", lessons, hours, grade);
    printf("int uses %zu bytes on this target\n", sizeof(int));
    return 0;
}
/* expect:
10 lessons, 2.5 hours, grade A
int uses 4 bytes on this target
*/
