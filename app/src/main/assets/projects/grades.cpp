#include <stdio.h>
#include <string.h>
#include <math.h>

struct Student { int id; double score; };
int find_student(const struct Student students[], int count, int id) {
    for (int i = 0; i < count; ++i) if (students[i].id == id) return i;
    return -1;
}
int main(void) {
    struct Student students[100];
    int count = 0;
    char line[256], command[16], extra;
    while (fgets(line, sizeof line, stdin)) {
        int id = 0; double score = 0;
        if (sscanf(line, "%15s", command) != 1) continue;
        if (strcmp(command, "quit") == 0) return 0;
        if (strcmp(command, "add") == 0 && sscanf(line, "%15s %d %lf %c", command, &id, &score, &extra) == 3 && id > 0 && isfinite(score) && score >= 0 && score <= 100 && count < 100 && find_student(students, count, id) < 0) {
            students[count].id = id; students[count++].score = score; puts("Added");
        } else if (strcmp(command, "find") == 0 && sscanf(line, "%15s %d %c", command, &id, &extra) == 2) {
            int index = find_student(students, count, id);
            if (index < 0) puts("Not found"); else printf("%d %.2f %s\n", id, students[index].score, students[index].score >= 50 ? "Pass" : "Retry");
        } else if (strcmp(command, "average") == 0) {
            double sum = 0; for (int i = 0; i < count; ++i) sum += students[i].score;
            if (count == 0) puts("No records"); else printf("Average %.2f\n", sum / count);
        } else if (strcmp(command, "save") == 0) {
            FILE *file = fopen("grades.tmp", "w"); int ok = file != NULL;
            if (file) { for (int i = 0; i < count; ++i) if (fprintf(file, "%d %.17g\n", students[i].id, students[i].score) < 0) ok = 0; if (fclose(file) != 0) ok = 0; }
            if (ok && rename("grades.tmp", "grades.txt") == 0) puts("Saved"); else puts("Save failed");
        } else if (strcmp(command, "load") == 0) {
            struct Student incoming[100]; int size = 0, ok = 1;
            FILE *file = fopen("grades.txt", "r");
            if (!file) ok = 0;
            else { while (fgets(line, sizeof line, file)) { if (size >= 100 || sscanf(line, "%d %lf %c", &id, &score, &extra) != 2 || id <= 0 || !isfinite(score) || score < 0 || score > 100 || find_student(incoming, size, id) >= 0) { ok = 0; break; } incoming[size].id = id; incoming[size++].score = score; } if (ferror(file)) ok = 0; if (fclose(file) != 0) ok = 0; }
            if (ok) { memcpy(students, incoming, (size_t)size * sizeof students[0]); count = size; puts("Loaded"); } else puts("Load failed; records unchanged");
        } else puts("Rejected");
    }
    return 0;
}
