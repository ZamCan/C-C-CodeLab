#include <stdio.h>
#include <string.h>
#include <limits.h>

/* Fictional integer minor units only. No real banking. */
int main(void) {
    long balance = 0;
    char line[128], command[16], extra;
    while (fgets(line, sizeof line, stdin)) {
        long amount = 0;
        int fields = sscanf(line, "%15s %ld %c", command, &amount, &extra);
        if (fields == 1 && strcmp(command, "quit") == 0) return 0;
        if (fields == 1 && strcmp(command, "balance") == 0) {
            printf("Balance %ld\n", balance);
        } else if (fields == 2 && amount > 0 && strcmp(command, "deposit") == 0 && amount <= LONG_MAX - balance) {
            balance += amount;
            printf("Deposited %ld\n", amount);
        } else if (fields == 2 && amount > 0 && strcmp(command, "withdraw") == 0 && amount <= balance) {
            balance -= amount;
            printf("Withdrew %ld\n", amount);
        } else {
            puts("Rejected");
        }
    }
    return 0;
}
