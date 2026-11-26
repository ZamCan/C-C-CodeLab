#include <stdio.h>

int main(void) {
    FILE *file = fopen("results.txt", "w");
    if (file == NULL) {
        puts("Could not open file");
        return 1;
    }
    if (fprintf(file, "1,82.5\n") < 0) {
        puts("Write failed");
        fclose(file);
        return 1;
    }
    if (fclose(file) != 0) {
        puts("Close failed");
        return 1;
    }
    puts("Record saved");
    return 0;
}
/* expect:
Record saved
*/
