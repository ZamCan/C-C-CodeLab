#include <stdio.h>
#include <string.h>

/* Educational demonstration: memory-only credentials, never written to disk.
   Not production authentication; no secure hashing or constant-time comparison. */
int main(void) {
    char username[32] = "", password[64] = "";
    char line[256], command[16], user[32], pass[64], extra;
    int registered = 0, logged_in = 0, failures = 0;
    while (fgets(line, sizeof line, stdin)) {
        int count = sscanf(line, "%15s %31s %63s %c", command, user, pass, &extra);
        if (count == 1 && strcmp(command, "quit") == 0) break;
        if (count == 3 && strcmp(command, "register") == 0 && !registered && strlen(pass) >= 8) {
            strcpy(username, user); strcpy(password, pass); registered = 1;
            puts("Registered for this session");
        } else if (count == 3 && strcmp(command, "login") == 0 && registered && failures < 3) {
            if (strcmp(username, user) == 0 && strcmp(password, pass) == 0) {
                logged_in = 1; failures = 0; puts("Logged in");
            } else { ++failures; puts("Invalid credentials"); }
        } else if (count == 1 && strcmp(command, "logout") == 0 && logged_in) {
            logged_in = 0; puts("Logged out");
        } else if (count == 1 && strcmp(command, "status") == 0) {
            puts(logged_in ? "Authenticated demo session" : "Signed out");
        } else { puts("Rejected"); }
        memset(pass, 0, sizeof pass);
        memset(line, 0, sizeof line);
    }
    /* Ordinary memset is illustrative, not a guarantee of secure memory erasure. */
    memset(password, 0, sizeof password);
    return 0;
}
