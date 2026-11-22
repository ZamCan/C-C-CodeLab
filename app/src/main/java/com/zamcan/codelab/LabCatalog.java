package com.zamcan.codelab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Original worked examples that open in the real offline compiler. */
public final class LabCatalog {
    private LabCatalog() {}

    public static final class Lab {
        public final String title, summary, source;
        public final boolean cpp;
        Lab(String title, String summary, boolean cpp, String... lines) {
            this.title = title;
            this.summary = summary;
            this.cpp = cpp;
            this.source = String.join("\n", lines) + "\n";
        }
    }

    public static List<Lab> math() {
        List<Lab> labs = new ArrayList<>();
        labs.add(new Lab("Compound interest", "Start at 1000 and grow by 10% until the amount is greater than 2000. Trace the loop condition and the first crossing.", false,
                "#include <stdio.h>", "", "int main(void) {", "    double amount = 1000.0;", "    int years = 0;",
                "    while (amount <= 2000.0) {", "        amount *= 1.10;", "        ++years;", "    }",
                "    printf(\"Year %d: %.2f\\n\", years, amount);", "    return 0;", "}"));
        labs.add(new Lab("Celsius to Fahrenheit", "Use floating-point arithmetic: F = (9.0 / 5.0) * C + 32. Compare 9 / 5 with 9.0 / 5.0.", false,
                "#include <stdio.h>", "", "int main(void) {", "    double celsius = 0.0;",
                "    double fahrenheit = (9.0 / 5.0) * celsius + 32.0;",
                "    printf(\"%.1f C = %.1f F\\n\", celsius, fahrenheit);", "    return 0;", "}"));
        labs.add(new Lab("Number systems", "Repeated division by a base produces remainders. Reverse them to read the result.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int value = 13;", "    int base = 2;",
                "    char digits[33];", "    int count = 0;", "    if (value == 0) {", "        puts(\"0\");",
                "        return 0;", "    }", "    while (value > 0) {", "        int remainder = value % base;",
                "        digits[count++] = (char)('0' + remainder);", "        value /= base;", "    }",
                "    while (count > 0) putchar(digits[--count]);", "    putchar('\\n');", "    return 0;", "}"));
        labs.add(new Lab("Grade policy", "Validate the score before applying the policy. Test -1, 0, 49, 50, 100 and 101.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int score = 72;",
                "    if (score < 0 || score > 100) {", "        puts(\"Invalid score\");",
                "    } else if (score >= 50) {", "        puts(\"Pass\");", "    } else {",
                "        puts(\"Retry\");", "    }", "    return 0;", "}"));
        labs.add(new Lab("Linear search", "Search a small array and report an index or -1. Never use the not-found value as a subscript.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int values[] = {4, 8, 15, 16, 23, 42};",
                "    int target = 15;", "    int found = -1;", "    for (int i = 0; i < 6; ++i) {",
                "        if (values[i] == target) {", "            found = i;", "            break;", "        }",
                "    }", "    printf(\"Index: %d\\n\", found);", "    return 0;", "}"));
        labs.add(new Lab("Euclid's algorithm", "The greatest common divisor is unchanged when the larger number is replaced by its remainder. Trace why the loop terminates.", false,
                "#include <stdio.h>", "", "int gcd(int a, int b) {", "    while (b != 0) {", "        int remainder = a % b;", "        a = b;", "        b = remainder;", "    }", "    return a;", "}", "", "int main(void) {", "    printf(\"gcd(1071, 462) = %d\\n\", gcd(1071, 462));", "    return 0;", "}"));
        labs.add(new Lab("Counting primes", "A prime greater than 1 has no positive divisor other than 1 and itself. Test candidates with a loop and count the matches.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int count = 0;", "    for (int candidate = 2; candidate <= 50; ++candidate) {", "        int prime = 1;", "        for (int divisor = 2; divisor * divisor <= candidate; ++divisor) {", "            if (candidate % divisor == 0) {", "                prime = 0;", "                break;", "            }", "        }", "        if (prime) ++count;", "    }", "    printf(\"Primes up to 50: %d\\n\", count);", "    return 0;", "}"));
        labs.add(new Lab("Matrix multiplication", "A product entry is a dot product of one row and one column. Check dimensions before multiplying.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int a[2][2] = {{1, 2}, {3, 4}};", "    int b[2][2] = {{5, 6}, {7, 8}};", "    int result[2][2] = {{0, 0}, {0, 0}};", "    for (int row = 0; row < 2; ++row) {", "        for (int column = 0; column < 2; ++column) {", "            for (int k = 0; k < 2; ++k) {", "                result[row][column] += a[row][k] * b[k][column];", "            }", "        }", "    }", "    for (int row = 0; row < 2; ++row) {", "        printf(\"%d %d\\n\", result[row][0], result[row][1]);", "    }", "    return 0;", "}"));
        labs.add(new Lab("Average and range", "Accumulate a sum while tracking the smallest and largest values. The range is maximum minus minimum.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int values[] = {14, 7, 21, 3, 18};", "    int count = 5;", "    int sum = 0;", "    int smallest = values[0];", "    int largest = values[0];", "    for (int i = 0; i < count; ++i) {", "        sum += values[i];", "        if (values[i] < smallest) smallest = values[i];", "        if (values[i] > largest) largest = values[i];", "    }", "    printf(\"mean %.2f, range %d\\n\", (double)sum / count, largest - smallest);", "    return 0;", "}"));
        labs.add(new Lab("Recursive factorial", "A recursive function needs a base case and a smaller argument at every step. Compare 5! with the iterative product.", false,
                "#include <stdio.h>", "", "long factorial(int n) {", "    if (n <= 1) return 1;", "    return n * factorial(n - 1);", "}", "", "int main(void) {", "    printf(\"5! = %ld\\n\", factorial(5));", "    return 0;", "}"));
        labs.add(new Lab("Bubble sort", "Repeated adjacent swaps move the largest remaining value to the end. Print after each pass to see the invariant.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int values[] = {5, 1, 4, 2, 8};", "    int count = 5;", "    for (int pass = 0; pass < count - 1; ++pass) {", "        for (int i = 0; i < count - 1 - pass; ++i) {", "            if (values[i] > values[i + 1]) {", "                int temporary = values[i];", "                values[i] = values[i + 1];", "                values[i + 1] = temporary;", "            }", "        }", "    }", "    for (int i = 0; i < count; ++i) printf(\"%d \", values[i]);", "    putchar('\\n');", "    return 0;", "}"));
        labs.add(new Lab("Count vowels", "Iterate over a null-terminated string. Compare one character at a time and stop at the terminator.", false,
                "#include <stdio.h>", "#include <ctype.h>", "", "int main(void) {", "    const char *text = \"Mathematics into code\";", "    int vowels = 0;", "    for (int i = 0; text[i] != '\\0'; ++i) {", "        char lower = (char)tolower((unsigned char)text[i]);", "        if (lower == 'a' || lower == 'e' || lower == 'i' || lower == 'o' || lower == 'u') ++vowels;", "    }", "    printf(\"Vowels: %d\\n\", vowels);", "    return 0;", "}"));
        labs.add(new Lab("A small C++ class", "A class groups state and behaviour. The constructor establishes the invariant; deposit preserves it.", true,
                "#include <iostream>", "", "class Counter {", "public:", "    explicit Counter(int start) : value(start) {}", "    void add(int amount) { value += amount; }", "    int value() const { return value; }", "private:", "    int value;", "};", "", "int main() {", "    Counter counter(10);", "    counter.add(5);", "    std::cout << \"Count: \" << counter.value() << '\\n';", "    return 0;", "}"));
        return Collections.unmodifiableList(labs);
    }

    public static List<Lab> code() {
        List<Lab> labs = new ArrayList<>();
        labs.add(new Lab("Hello, CodeLab", "A complete hosted C program: header, entry point, statement and exit status.", false,
                "#include <stdio.h>", "", "int main(void) {", "    puts(\"Hello, CodeLab!\");", "    return 0;", "}"));
        labs.add(new Lab("FizzBuzz", "Practise a loop, integer remainder and branching in one small program.", false,
                "#include <stdio.h>", "", "int main(void) {", "    for (int i = 1; i <= 20; ++i) {",
                "        if (i % 15 == 0) puts(\"FizzBuzz\");", "        else if (i % 3 == 0) puts(\"Fizz\");",
                "        else if (i % 5 == 0) puts(\"Buzz\");", "        else printf(\"%d\\n\", i);", "    }",
                "    return 0;", "}"));
        labs.add(new Lab("Average with a function", "Separate the calculation from printing. Guard an empty input before dividing.", false,
                "#include <stdio.h>", "", "double average(const int values[], int count) {",
                "    if (count <= 0) return 0.0;", "    int sum = 0;", "    for (int i = 0; i < count; ++i) sum += values[i];",
                "    return (double)sum / count;", "}", "", "int main(void) {", "    int marks[] = {72, 84, 91};",
                "    printf(\"Average: %.2f\\n\", average(marks, 3));", "    return 0;", "}"));
        labs.add(new Lab("Swap through pointers", "A function receives addresses, so it can update the caller's two integers.", false,
                "#include <stdio.h>", "", "void swap(int *left, int *right) {", "    int temporary = *left;",
                "    *left = *right;", "    *right = temporary;", "}", "", "int main(void) {", "    int a = 3, b = 8;",
                "    swap(&a, &b);", "    printf(\"a=%d b=%d\\n\", a, b);", "    return 0;", "}"));
        labs.add(new Lab("A student record", "Group related fields in a struct, then pass a value to a printing function.", false,
                "#include <stdio.h>", "", "struct Student {", "    int id;", "    double score;", "};", "",
                "void print_student(struct Student student) {", "    printf(\"Student %d: %.1f\\n\", student.id, student.score);",
                "}", "", "int main(void) {", "    struct Student ada = {1, 92.5};", "    print_student(ada);",
                "    return 0;", "}"));
        labs.add(new Lab("C++ vector average", "Use a range-based loop, std::vector and a const reference to read without copying.", true,
                "#include <iostream>", "#include <vector>", "", "double average(const std::vector<int>& values) {",
                "    if (values.empty()) return 0.0;", "    int sum = 0;", "    for (int value : values) sum += value;",
                "    return static_cast<double>(sum) / values.size();", "}", "", "int main() {",
                "    std::vector<int> marks{72, 84, 91};", "    std::cout << \"Average: \" << average(marks) << '\\n';",
                "    return 0;", "}"));
        labs.add(new Lab("File-safe parsing", "Read a line, validate its length, and process input without trusting its size.", false,
                "#include <stdio.h>", "#include <string.h>", "", "int main(void) {", "    char line[64];",
                "    if (!fgets(line, sizeof line, stdin)) return 1;", "    line[strcspn(line, \"\\n\")] = '\\0';",
                "    printf(\"Received %zu characters\\n\", strlen(line));", "    return 0;", "}"));
        labs.add(new Lab("C++ map lookup", "Associate a course code with a title and handle a missing key explicitly.", true,
                "#include <iostream>", "#include <map>", "#include <string>", "", "int main() {",
                "    std::map<std::string, int> credits{{\"C\", 4}, {\"CPP\", 5}};",
                "    auto found = credits.find(\"CPP\");", "    if (found != credits.end()) std::cout << found->second << '\\n';",
                "    return 0;", "}"));
        labs.add(new Lab("Binary search", "Maintain a sorted range and shrink the interval after every comparison.", false,
                "#include <stdio.h>", "", "int main(void) {", "    int values[] = {2, 4, 8, 16, 32};", "    int low = 0, high = 4, target = 16, found = -1;",
                "    while (low <= high) {", "        int middle = low + (high - low) / 2;", "        if (values[middle] == target) { found = middle; break; }",
                "        if (values[middle] < target) low = middle + 1; else high = middle - 1;", "    }", "    printf(\"Index: %d\\n\", found);", "    return 0;", "}"));
        labs.add(new Lab("C++ exception boundary", "Use a narrow try block for conversion and report invalid input clearly.", true,
                "#include <iostream>", "#include <string>", "", "int main() {", "    try {", "        int value = std::stoi(\"42\");", "        std::cout << value << '\\n';",
                "    } catch (const std::exception& error) {", "        std::cerr << error.what() << '\\n';", "        return 1;", "    }", "    return 0;", "}"));
        return Collections.unmodifiableList(labs);
    }
}
