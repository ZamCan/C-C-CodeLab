#include <stdio.h>
#include <string.h>

struct Book { int id; char title[64]; int borrowed; };
int find_book(const struct Book books[], int count, int id) {
    for (int i = 0; i < count; ++i) if (books[i].id == id) return i;
    return -1;
}
int main(void) {
    struct Book books[100]; int count = 0;
    char line[256], command[16], title[64], extra;
    while (fgets(line, sizeof line, stdin)) {
        int id = 0;
        if (sscanf(line, "%15s", command) != 1) continue;
        if (strcmp(command, "quit") == 0) return 0;
        if (strcmp(command, "add") == 0 && sscanf(line, "%15s %d %63s %c", command, &id, title, &extra) == 3 && id > 0 && count < 100 && find_book(books, count, id) < 0) {
            books[count].id = id; strcpy(books[count].title, title); books[count++].borrowed = 0; puts("Added");
        } else if ((strcmp(command, "borrow") == 0 || strcmp(command, "return") == 0 || strcmp(command, "find") == 0) && sscanf(line, "%15s %d %c", command, &id, &extra) == 2) {
            int i = find_book(books, count, id);
            if (i < 0) { puts("Not found"); continue; }
            if (strcmp(command, "find") == 0) printf("%d %s %s\n", id, books[i].title, books[i].borrowed ? "Borrowed" : "Available");
            else { int wanted = strcmp(command, "borrow") == 0; if (books[i].borrowed == wanted) puts("Rejected"); else { books[i].borrowed = wanted; puts(wanted ? "Borrowed" : "Returned"); } }
        } else if (strcmp(command, "save") == 0) {
            FILE *file = fopen("library.tmp", "w"); int ok = file != NULL;
            if (file) { for (int i = 0; i < count; ++i) if (fprintf(file, "%d %s %d\n", books[i].id, books[i].title, books[i].borrowed) < 0) ok = 0; if (fclose(file) != 0) ok = 0; }
            if (ok && rename("library.tmp", "library.txt") == 0) puts("Saved"); else puts("Save failed");
        } else if (strcmp(command, "load") == 0) {
            struct Book incoming[100]; int size = 0, ok = 1, borrowed;
            FILE *file = fopen("library.txt", "r"); if (!file) ok = 0;
            else { while (fgets(line, sizeof line, file)) { if (size >= 100 || sscanf(line, "%d %63s %d %c", &id, title, &borrowed, &extra) != 3 || id <= 0 || (borrowed != 0 && borrowed != 1) || find_book(incoming, size, id) >= 0) { ok = 0; break; } incoming[size].id = id; strcpy(incoming[size].title, title); incoming[size++].borrowed = borrowed; } if (ferror(file)) ok = 0; if (fclose(file) != 0) ok = 0; }
            if (ok) { memcpy(books, incoming, (size_t)size * sizeof books[0]); count = size; puts("Loaded"); } else puts("Load failed; catalog unchanged");
        } else puts("Rejected");
    }
    return 0;
}
