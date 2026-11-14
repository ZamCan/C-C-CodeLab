"""Author original, portable console project references and offline teaching guides."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
DEST=ROOT/'app/src/main/assets/projects'
DEST.mkdir(parents=True,exist_ok=True)
sources={
'calculator':r'''#include <stdio.h>
#include <math.h>

int calculate(double left, char operation, double right, double *result) {
    switch (operation) {
        case '+': *result = left + right; break;
        case '-': *result = left - right; break;
        case '*': *result = left * right; break;
        case '/': if (right == 0) return 0; *result = left / right; break;
        default: return 0;
    }
    return isfinite(*result);
}

int main(void) {
    double left, right, result;
    char operation, extra;
    char line[256];
    if (!fgets(line, sizeof line, stdin) ||
        sscanf(line, "%lf %c %lf %c", &left, &operation, &right, &extra) != 3 ||
        !isfinite(left) || !isfinite(right)) {
        fputs("Invalid input\n", stderr);
        return 1;
    }
    if (!calculate(left, operation, right, &result)) {
        fputs("Invalid operation or result\n", stderr);
        return 2;
    }
    printf("%.6g\n", result);
    return 0;
}
''',
'atm':r'''#include <stdio.h>
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
''',
'login':r'''#include <stdio.h>
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
''',
'grades':r'''#include <stdio.h>
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
''',
'library':r'''#include <stdio.h>
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
'''
}
metadata={
'calculator':('Calculator','Read one line → parse two finite operands and an operator → validate the operation → compute → print or report a diagnostic.','1. Implement addition in a function.\n2. Add subtraction and multiplication.\n3. Reject zero divisors and non-finite results.\n4. Reject extra tokens and test all four operations.','2 + 3\n','5\n','The pointer result is an output parameter. switch selects one operation. A return of 0 from calculate means failure; main maps that to exit status 2. %lf is required when scanf stores into double; %.6g chooses a concise printed representation. Remove the zero check to observe why validation belongs before division.'),
'atm':('ATM simulator','Read command → parse positive integer minor units → check balance/overflow invariant → change balance only on success → print result → repeat.','1. Display a fictional balance.\n2. Add positive deposits.\n3. Reject overdrafts and negative amounts.\n4. Check addition overflow.\n5. Trace state after rejected operations.','deposit 1000\nwithdraw 400\nbalance\nwithdraw 1001\nbalance\nquit\n','Deposited 1000\nWithdrew 400\nBalance 600\nRejected\nBalance 600\n','balance is always nonnegative. amount <= LONG_MAX - balance checks addition before doing it. strcmp returns zero for equal strings. Rejected operations must not mutate balance. The ledger is session-only; persist an auditable transaction history as an extension. Never connect this teaching exercise to real money.'),
'login':('Educational login','Register one memory-only account → validate input → compare demonstration credentials → establish or reject session → logout → clear session on process exit.','1. Model signed-out and signed-in states.\n2. Register one demonstration account.\n3. Require an eight-character demonstration password.\n4. Limit unsuccessful attempts to three.\n5. Implement logout and test restart.','register ada example123\nlogin ada wrongpass\nlogin ada example123\nstatus\nlogout\nstatus\nquit\n','Registered for this session\nInvalid credentials\nLogged in\nAuthenticated demo session\nLogged out\nSigned out\n','This intentionally limited model teaches state transitions, not secure authentication. Passwords exist only in memory; there is no password file. Ordinary string comparison and memset are not cryptographic security. Production systems need reviewed password hashing, salts, identity/session protections, and secure transport. Never reuse a real password here.'),
'grades':('Student results','Parse command → validate unique positive ID and finite score 0–100 → update collection → calculate average or lookup → save validated records → validate complete input before load replacement.','1. Add a student record.\n2. Reject duplicates and invalid scores.\n3. Search by ID and calculate average.\n4. Save through a temporary file.\n5. Validate load into a separate collection before replacing current state.','average\nadd 1 80\nadd 2 60\nadd 1 90\naverage\nfind 1\nsave\nload\nquit\n','No records\nAdded\nAdded\nRejected\nAverage 70.00\n1 80.00 Pass\nSaved\nLoaded\n','The illustrative pass threshold is 50, not an institutional GPA policy. structs group IDs and scores. The array capacity is 100; count is the number of initialized records. A separate incoming array makes failed loads non-destructive. Division occurs only when count is positive.'),
'library':('Library management','Parse command → find unique book ID → enforce borrowing transition → report state → persist catalog → validate full saved catalog before replacement.','1. Create a catalog with unique positive IDs.\n2. Search by ID.\n3. Borrow only available books.\n4. Return only borrowed books.\n5. Save/load and test invalid storage.','add 1 C_Fundamentals\nborrow 1\nborrow 1\nfind 1\nreturn 1\nsave\nload\nfind 1\nquit\n','Added\nBorrowed\nRejected\n1 C_Fundamentals Borrowed\nReturned\nSaved\nLoaded\n1 C_Fundamentals Available\n','Each book has a unique ID, a one-token title (use underscores for spaces), and a boolean-like borrowing state. Search returns -1 for missing records, never an array index. Only 0 or 1 is accepted from storage. Extend with a defined date model before adding overdue calculations.')
}
manifest=[]
for key,source in sources.items():
    title,algorithm,milestones,stdin,stdout,anatomy=metadata[key]
    # The procedural reference is valid in C11 and C++17. C++ idiomatic alternatives are a later lesson.
    for extension in ('c','cpp'):
        (DEST/f'{key}.{extension}').write_text(source)
        (DEST/f'{key}-starter.{extension}').write_text('/* '+title+' — independent exercise.\nRead the guide and implement one milestone at a time.\n*/\n#include <stdio.h>\n\nint main(void) {\n    /* TODO: validate input, implement rules, and print results. */\n    return 0;\n}\n')
    guide=f'''{title}

SPECIFICATION
{algorithm}

ALGORITHM / TEXT FLOWCHART
Start → read bounded input → validate → apply one domain operation → report → repeat or exit.
Invalid input → diagnostic/rejection → no invalid state update.

MILESTONES
{milestones}

CODE ANATOMY
{anatomy}

INPUT CONTRACT
These bounded console exercises accept small, representable numbers and whitespace-separated commands. Use the commands in the test. Text tokens cannot contain spaces. Numeric scanf conversion is not a hardened parser for arbitrarily huge values: replace it with strtol/strtod plus end-pointer/range checking as a robustness extension. The reference is procedural code valid as both C11 and C++17; it is not an idiomatic object-oriented C++ solution.

EXPECTED SAMPLE INPUT (NOT LIVE OUTPUT)
{stdin}
EXPECTED SAMPLE OUTPUT (NOT LIVE OUTPUT)
{stdout}
DEBUGGING EXERCISE
Temporarily remove one boundary check. Predict the incorrect state or output, add a failing test, then restore the check. Never use real credentials, financial balances, or private records.

INDEPENDENT TASK
Begin with the starter, write your algorithm, then implement each milestone. Compare actual compiler output with the expected transcript. The reference solution is available separately. Android execution is unavailable until the toolchain gate is passed; host verification does not change that.

LIMITS
Console programs only, not Android APKs or GUI generators. No static interface is represented as program output. Files belong in an isolated project working directory. These teaching references need further hardening and review before reuse outside the academy.
'''
    (DEST/f'{key}.txt').write_text(guide)
    manifest.append(dict(id=key,title=title,stdin=stdin,stdout=stdout))
(DEST/'tests.json').write_text(json.dumps(manifest,indent=2)+'\n')
print('Wrote five guides, ten reference files, ten starters, and sample-output test cases.')
