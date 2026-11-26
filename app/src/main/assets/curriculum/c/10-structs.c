#include <stdio.h>

struct Student {
    int id;
    double score;
};

int main(void) {
    struct Student student = {1, 82.5};

    printf("Student %d scored %.1f\n", student.id, student.score);
    struct Student team[2] = {{2, 74.0}, {3, 91.5}};
    printf("Second member: %d\n", team[1].id);
    return 0;
}
/* expect:
Student 1 scored 82.5
Second member: 3
*/
