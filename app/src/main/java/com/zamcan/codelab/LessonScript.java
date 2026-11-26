package com.zamcan.codelab;

/**
 * Authored C course. Each lesson is a connected sequence of beats, not a template of screens:
 * a question opens the idea, a (clearly fictionalised) dialogue places it in history, a definition
 * names it precisely, a diagram and a verified example make it concrete, and prediction or check
 * beats test understanding before the recap. Code is loaded from bundled assets so the exact
 * program shown is the program compiled by {@code tools/verify_curriculum_examples.py}.
 */
final class LessonScript {
    private LessonScript() {}

    /** A complete lesson: metadata plus the ordered beats the player presents. */
    static final class Lesson {
        final String title;
        final String subtitle;
        final int minutes;
        final String codeAsset;
        final Beat[] beats;
        private final String id;

        Lesson(String id, String title, String subtitle, int minutes, String codeAsset, Beat... beats) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.minutes = minutes;
            this.codeAsset = codeAsset;
            this.beats = beats;
        }

        /** Stable preference/database key, independent of the visible title. */
        String id() { return id; }

        int scoringBeats() {
            int count = 0;
            for (Beat beat : beats) if (beat.scored()) count++;
            return count;
        }
    }

    static Lesson[] lessons(boolean cpp) {
        return cpp ? LessonScriptCpp.LESSONS : C;
    }

    private static String c(String file) { return "curriculum/c/" + file; }

    static final Lesson[] C = {
        new Lesson("meet-c", "Meet C", "Why a 1970s systems language still runs the world", 7, c("01-meet-c.c"),
            Beat.hook("""
                    Every phone call, car engine controller and web request you used today probably \
                    passed through code written in C. A language designed for a telephone company's \
                    computers in the early 1970s should be obsolete. Why is it not?"""),
            Beat.say("Dennis Ritchie · imagined dialogue", """
                    I did not set out to design a famous language. Ken Thompson and I needed to build an \
                    operating system, and the tools we had were tied to one machine. C began as a \
                    practical answer: enough structure to write readable programs, little enough \
                    machinery to stay close to the hardware."""),
            Beat.explain("""
                    By 1972, Bell Labs had B, a language derived from BCPL. B was small and elegant, \
                    but it assumed a machine where a word addressed a word. Newer computers addressed \
                    bytes, so C gained types and byte-level memory access. That practical revision is \
                    why C could express an operating system and still be portable across machines."""),
            Beat.visual("lineage", "C did not appear from nowhere: BCPL shaped B, B shaped C, and C later shaped C++."),
            Beat.define("""
                    C is a general-purpose, statically typed, compiled programming language. Source \
                    text is translated ahead of time into machine instructions for a target; it is not \
                    interpreted line by line."""),
            Beat.visual("pipeline", "The compiler translates each source file into an object file; the linker combines them into a program."),
            Beat.code(c("01-meet-c.c"), "Hello, CodeLab!"),
            Beat.predict("A file called hello.c has just been compiled. What exists before the linker runs?",
                    new String[]{"An object file containing machine code", "The final executable", "Nothing until the program starts"},
                    0,
                    "Compilation produces an object file (plus diagnostics). Linking combines objects and libraries, resolving symbols such as printf, into the final program."),
            Beat.explain("""
                    Separation of steps matters when a program breaks. A compiler diagnostic means \
                    the source violates the language rules. A linker error means a name was declared \
                    but never defined. A wrong answer at runtime means the logic, not the build, is at \
                    fault. Naming the stage tells you where to look."""),
            Beat.check("Which statement is accurate?",
                    new String[]{"C is compiled and gives direct access to memory", "C is interpreted by the browser", "C is a subset of C++"},
                    0,
                    "C is compiled and exposes memory through pointers and addresses. Valid C is not automatically valid C++, and C is not interpreted."),
            Beat.tryIt("Open the example in the compiler. Change the greeting, run it, then remove the semicolon after printf and read the diagnostic.", c("01-meet-c.c")),
            Beat.reflect("""
                    You now know what C is for, where it came from and how a build proceeds. Next: the \
                    smallest complete C program, one symbol at a time.""")),

        new Lesson("first-program", "Your first program", "Every character in Hello, CodeLab!", 8, c("02-first-program.c"),
            Beat.hook("A program that prints one line contains about nine punctuation marks you did not invent. What is each one doing?"),
            Beat.code(c("02-first-program.c"), "C was created at Bell Labs.\nIt is still used in operating systems today."),
            Beat.define("A C program is a set of declarations and function definitions. Execution begins at main, and a statement is the smallest unit that performs an action."),
            Beat.explain("""
                    `#include <stdio.h>` asks the preprocessor to make declarations from the standard \
                    input/output header visible. `<stdio.h>` names a standard header; replacing it with \
                    a missing name stops the build. `int main(void)` defines the entry point: it returns \
                    an int to the operating system and takes no parameters. Braces group the body."""),
            Beat.explain("""
                    `printf` is a library function that writes formatted text to standard output. The \
                    text `"C was created...\\n"` is a string literal; `\\n` is a newline escape, not two \
                    characters. A semicolon ends a statement. `return 0;` reports success: zero means \
                    the program completed normally, and a shell sees any non-zero value as failure."""),
            Beat.check("Which part is the entry point of a C program?",
                    new String[]{"main", "printf", "#include", "return"},
                    0,
                    "Execution begins at main. printf is called from main; #include is a preprocessor directive and return ends main."),
            Beat.explain("""
                    Common first mistakes are real diagnostics, not mysteries. Omitting a semicolon gives \
                    an error such as `expected ';' before ...`. A misspelled `prntf` gives an implicit \
                    declaration warning, and on modern compilers an error. Writing `#include <stdio>` \
                    instead of `<stdio.h>` usually produces 'file not found'. Read the first diagnostic \
                    and the cited line first."""),
            Beat.tryIt("Open the example in the compiler. Change the first line, then add a third printf. Run it and compare the output with your prediction.", c("02-first-program.c")),
            Beat.reflect("A C program is declarations plus a main function plus statements. Nothing is hidden: every symbol has a name, a meaning and a consequence if moved.")),

        new Lesson("types", "Variables and types", "What a name stores, and how many bytes it takes", 8, c("03-types.c"),
            Beat.hook("If a program remembers that you completed 10 lessons, where does the 10 physically live at runtime?"),
            Beat.define("""
                    A variable is a named object with a type. The type fixes which values it can hold, \
                    which operations apply, and how many bytes of storage it occupies."""),
            Beat.explain("""
                    `int` holds whole numbers; on this target it is four bytes. `double` holds \
                    floating-point approximations with more range and precision than `float`. `char` \
                    holds one small integer value, commonly a character code. Declaring `int lessons = 10;` \
                    reserves storage and initialises it in one step. Reading an uninitialised local \
                    variable is undefined behaviour, not 'zero by default'."""),
            Beat.visual("memory", "A declaration reserves a named box of a fixed size. The value is the contents; the address is where the box sits."),
            Beat.code(c("03-types.c"), "10 lessons, 2.5 hours, grade A\nint uses 4 bytes on this target"),
            Beat.predict("What does 7 / 2 evaluate to when both operands are int?",
                    new String[]{"3", "3.5", "4"},
                    0,
                    "Integer division discards the fractional part. Use double operands (7.0 / 2) when a fractional result is required."),
            Beat.explain("""
                    Floating point is approximate: values such as 0.1 have no exact binary fraction. \
                    That is why money is usually stored in integer minor units, or handled with an \
                    explicit rounding policy. `sizeof` reports the size in bytes for the current target; \
                    it is not a universal constant across every machine."""),
            Beat.check("Which declaration is safest for a value that must never change?",
                    new String[]{"const int days = 7;", "int days = 7;", "auto days;", "static days;"},
                    0,
                    "const makes the intent explicit and lets the compiler reject accidental reassignment."),
            Beat.tryIt("Add a `const double passMark = 50.0;` and print it with %.1f. Then try assigning to it and read the compiler's exact message.", c("03-types.c")),
            Beat.reflect("Types are contracts about representation and operations. Choosing them deliberately prevents whole classes of bugs before the program runs.")),

        new Lesson("input", "Input and arithmetic", "Reading values safely, and what scanf really returns", 8, c("04-input.c"),
            Beat.hook("A calculator that can only add numbers written into its source is a demonstration, not a tool. What changes when input arrives from a user?"),
            Beat.explain("""
                    `scanf("%d", &count)` reads a formatted integer. The `&` supplies the address of \
                    `count`, because scanf must write into the caller's variable. It returns the number \
                    of successful conversions, so `!= 1` detects missing or malformed input before the \
                    value is used."""),
            Beat.define("Standard input is a byte stream supplied by the environment. Reading it is an operation that can succeed, fail or reach end of input, and the program must distinguish those outcomes."),
            Beat.visual("io", "Input flows from the keyboard stream into a typed object; the program then transforms it and writes to standard output."),
            Beat.code(c("04-input.c"), "Enter a count: Double: 42"),
            Beat.predict("The user types `abc`. What does `scanf(\"%d\", &count)` return?",
                    new String[]{"0", "1", "-1", "EOF every time"},
                    0,
                    "No conversion succeeded, so the return value is 0. EOF is returned when input ends before any conversion; conflating the two hides genuine errors."),
            Beat.explain("""
                    Arithmetic follows the operand types: `+ - * /` behave as expected, and `%` is the \
                    integer remainder. Division by zero is undefined for integers and must be guarded \
                    explicitly. Overflow of a signed integer is also undefined behaviour; check limits \
                    before multiplying large values."""),
            Beat.check("Which expression evaluates to 3.5?",
                    new String[]{"7.0 / 2.0", "7 / 2", "(double) (7 / 2)"},
                    0,
                    "Casting after the division is too late: 7 / 2 already produced 3. At least one operand must be floating point before the division."),
            Beat.tryIt("Ask for two integers, reject a zero divisor, then print the quotient and remainder. Test 7 and 2, then 7 and 0.", c("04-input.c")),
            Beat.reflect("Input is untrusted data. Validate the conversion count, then the domain (range, non-zero divisor), and only then compute.")),

        new Lesson("decisions", "Decisions", "if, else and the difference between = and ==", 7, c("05-decisions.c"),
            Beat.hook("A grading rule sounds trivial — 50 or more passes — yet it has three distinct cases once invalid marks exist. How does a program express a rule?"),
            Beat.define("""
                    Selection is a control structure that executes a block only when a condition is true. \
                    In C a condition is an integer expression: zero is false and any non-zero value is true."""),
            Beat.code(c("05-decisions.c"), "Pass"),
            Beat.explain("""
                    `if` tests the first condition; `else if` tests the next only when earlier tests \
                    failed; `else` covers the remainder. `||` is logical OR and `&&` is logical AND; both \
                    short-circuit, so `score >= 0 && score <= 100` is safe even if later work is costly. \
                    Braces are optional for a single statement, but keeping them prevents a later edit \
                    from silently changing which statements the branch owns."""),
            Beat.visual("branch", "One condition, two or more paths. Exactly one branch of an if / else-if / else chain executes."),
            Beat.predict("With score = 49, what does the example print?",
                    new String[]{"Keep practicing", "Pass", "Invalid score"},
                    0,
                    "49 is a valid score but below 50, so the final else runs. The value 49 is not invalid: validity is a separate range test."),
            Beat.check("Which mistake changes a comparison into an assignment?",
                    new String[]{"if (score = 50)", "if (score == 50)", "if (score >= 50)"},
                    0,
                    "A single = assigns and yields the assigned value; == compares. Enable compiler warnings so this is caught."),
            Beat.tryIt("Add a distinction: 70 or more prints Distinction, 50–69 prints Pass, below 50 prints Retry. Test 0, 49, 50, 69, 70 and 100.", c("05-decisions.c")),
            Beat.reflect("Write the rule first in words, then as conditions ordered from most specific to least. Ambiguous order is a logic bug, not a syntax bug.")),

        new Lesson("loops", "Loops and tracing", "Repetition, progress and off-by-one errors", 8, c("06-loops.c"),
            Beat.hook("Adding the numbers 1 to 100 by hand is slow and error-prone. Writing 100 statements is worse. What does a loop actually guarantee?"),
            Beat.define("""
                    Iteration repeats a block while a control condition allows it. The loop must move \
                    toward a state where the condition becomes false, or it will not terminate."""),
            Beat.explain("""
                    `for (int i = 1; i <= 5; ++i)` groups three things: initialisation, a condition tested \
                    before every iteration, and an update after every body execution. `while` is \
                    preferable when the number of repetitions is unknown. `do ... while` runs the body at \
                    least once."""),
            Beat.visual("loop", "Trace the state, not the source: i and total after every iteration reveal whether the loop makes progress."),
            Beat.code(c("06-loops.c"), "i=1 total=1\ni=2 total=3\ni=3 total=6\ni=4 total=10\ni=5 total=15"),
            Beat.predict("How many times does the body run for `for (int i = 0; i < 3; ++i)`?",
                    new String[]{"3", "4", "2"},
                    0,
                    "The body runs for i equal to 0, 1 and 2; when i becomes 3 the condition fails, so it never runs for 3."),
            Beat.explain("""
                    A trace table is the cheapest debugger. Write one column per variable and one row per \
                    iteration. If a total is off by one, the trace usually shows the boundary. If the \
                    loop never ends, the trace shows a variable that never changes or a condition that \
                    can never fail."""),
            Beat.check("Which loop is guaranteed to run its body at least once?",
                    new String[]{"do { } while (condition);", "while (condition) { }", "for (; condition; ) { }"},
                    0,
                    "do-while tests after the body. The other two test before, so a false condition skips the body."),
            Beat.tryIt("Compute the sum from 1 to 10, then rewrite it with while. Trace both and confirm the totals match.", c("06-loops.c")),
            Beat.reflect("State the invariant of a loop in one sentence: what stays true after each iteration? If you cannot, simplify the loop.")),

        new Lesson("functions", "Functions and the call stack", "Named operations, parameters and return values", 8, c("07-functions.c"),
            Beat.hook("A long main function works until someone else must read it. What does a function buy you besides shorter code?"),
            Beat.define("""
                    A function is a callable unit defined by its name, parameter types, return type and \
                    body. The parameter list and return type form a contract that callers rely on."""),
            Beat.explain("""
                    `int square(int value)` promises to accept one int and return an int. Ordinary C \
                    arguments are passed by value: the parameter is a copy, so changing it cannot change \
                    the caller's variable. To modify the caller's object, pass its address and dereference \
                    the pointer. Declare a function before calling it, or provide a prototype."""),
            Beat.visual("stack", "Each call gets a frame holding its parameters and locals. Frames unwind in reverse order when functions return."),
            Beat.code(c("07-functions.c"), "36\n27"),
            Beat.predict("`int x = 3; square(x);` — what is x afterwards?",
                    new String[]{"3", "9", "Undefined"},
                    0,
                    "square receives a copy of x. Only the copy is multiplied; x itself is unchanged."),
            Beat.explain("""
                    Small functions with one responsibility are easy to test and reuse. A function named \
                    `maximum` that also prints, validates and stores results is hard to reason about. \
                    Prefer returning a value and let the caller decide how to display it."""),
            Beat.check("What does a function's return type specify?",
                    new String[]{"The type of value the caller receives", "The type of its first parameter", "How long it may run"},
                    0,
                    "The return type is part of the contract. Mismatches are compile-time diagnostics, not runtime guesses."),
            Beat.tryIt("Add `int minimum(int a, int b)`, call it, and test equal, negative and positive pairs.", c("07-functions.c")),
            Beat.reflect("Decompose by responsibility, not by line count. A function should have one reason to exist and one obvious way to call it.")),

        new Lesson("arrays", "Arrays and strings", "Indexing from zero, and the terminator you must remember", 8, c("08-arrays.c"),
            Beat.hook("Five marks stored in five separate variables cannot be processed by a loop. What does an array add, and what does it refuse to protect you from?"),
            Beat.define("""
                    An array is a fixed-size, contiguous sequence of elements of one type. A C string is \
                    a character array terminated by a null character, '\\0'."""),
            Beat.explain("""
                    Indices begin at zero: an array of three elements has valid indices 0, 1 and 2. C does \
                    not check bounds at runtime — reading marks[3] is undefined behaviour that may appear \
                    to work. `char name[32] = "Ada"` stores four characters: 'A', 'd', 'a' and the \
                    terminating '\\0'. The terminator is what lets printf's %s know where the text ends."""),
            Beat.visual("array", "One contiguous block, fixed element size, index arithmetic. Index 0 is the first element, not the first plus one."),
            Beat.code(c("08-arrays.c"), "Ada: average 80.0\nlast valid index: 2"),
            Beat.predict("`int a[5];` — what is the last valid index?",
                    new String[]{"4", "5", "0"},
                    0,
                    "Five elements occupy indices 0 through 4. Accessing a[5] is outside the array, even though the compiler may accept it."),
            Beat.explain("""
                    Because bounds are not checked, prefer bounded input such as `fgets` over `gets`, \
                    which cannot be used safely and was removed from the language. When passing an array \
                    to a function, pass the element count as well: the array decays to a pointer, so \
                    `sizeof` inside the function gives the pointer's size, not the array's."""),
            Beat.check("Why must a C string reserve one extra character?",
                    new String[]{"For the terminating null character", "For the newline", "For a length prefix"},
                    0,
                    "The null terminator marks the end. Without space for it, printing the string reads past the intended data."),
            Beat.tryIt("Compute the average of three marks with a function that also receives the count. Then deliberately print marks[3] and observe that the compiler stays silent.", c("08-arrays.c")),
            Beat.reflect("An array is a promise about layout, not a safety net. Track the length yourself and treat every index as a claim that needs justification.")),

        new Lesson("pointers", "Pointers", "Addresses, dereferencing and the cost of an invalid pointer", 9, c("09-pointers.c"),
            Beat.hook("A function needs to change the caller's variable, or a large structure must not be copied. C's answer is an address. What can go wrong?"),
            Beat.define("""
                    A pointer is an object whose value is the address of another object or function, or a \
                    null pointer value. `&` obtains an address; `*` dereferences a valid pointer."""),
            Beat.explain("""
                    `int *pointer = &value;` stores the address of value. `*pointer = 20;` writes through \
                    that address, so value becomes 20. The type matters: `int *` means 'address of an int', \
                    and dereferencing reads or writes an int at that address. A pointer with no valid \
                    target must be set to NULL and checked before use."""),
            Beat.visual("pointer", "Two boxes: one holds the value, the other holds the address of the first. Dereferencing follows the arrow."),
            Beat.code(c("09-pointers.c"), "20\n7 3"),
            Beat.predict("What does the example print for value after `*pointer = 20;`?",
                    new String[]{"20", "10", "The address of value"},
                    0,
                    "pointer refers to value, so writing through *pointer changes value itself. Printing value shows 20."),
            Beat.explain("""
                    Three pointer faults dominate: dereferencing NULL, using an uninitialised pointer, and \
                    using a pointer to an object whose lifetime ended (a dangling pointer). Returning the \
                    address of a local variable is dangling: the storage is gone when the function \
                    returns. Dynamically allocated memory must be freed exactly once, and never used \
                    afterwards."""),
            Beat.check("Which pointer is safe to dereference?",
                    new String[]{"One that points to an object still alive", "A null pointer", "A pointer to a returned local variable"},
                    0,
                    "Only a pointer to a live object of the correct type is safe. Null and dangling pointers are the two classic failures."),
            Beat.tryIt("Write a function that doubles an int through a pointer. Call it, print before and after, then set the pointer to NULL and guard against it.", c("09-pointers.c")),
            Beat.reflect("Pointers are not inherently dangerous; unchecked assumptions about lifetime and validity are. Write down who owns each object and how long it lives.")),

        new Lesson("structs", "Structures", "Grouping related data under one type", 7, c("10-structs.c"),
            Beat.hook("A student record has an id and a score. Two parallel arrays drift out of step; five fields become unmanageable. What groups them?"),
            Beat.define("""
                    A structure is a user-defined aggregate type whose members are accessed by name. Each \
                    member has its own type and offset inside the structure."""),
            Beat.explain("""
                    `struct Student { int id; double score; };` declares a type. `struct Student student = \
                    {1, 82.5};` creates an object and initialises its members in declaration order. Access \
                    uses the dot operator: `student.id`. Structures can be array elements, function \
                    parameters and return values, and they copy as a whole."""),
            Beat.visual("struct", "Named members with different sizes, laid out as one object. The compiler decides padding, so sizeof may exceed the sum of members."),
            Beat.code(c("10-structs.c"), "Student 1 scored 82.5\nSecond member: 3"),
            Beat.predict("How is the score of `team[1]` accessed?",
                    new String[]{"team[1].score", "team[1]->score", "team.score[1]"},
                    0,
                    "Array indexing first yields a structure value, then the dot operator selects a member. The arrow operator is for pointers to structures."),
            Beat.check("Which is a real advantage of a structure over parallel arrays?",
                    new String[]{"Related fields stay together and copy as one value", "It checks array bounds", "It stores data on disk automatically"},
                    0,
                    "A structure keeps fields associated and can be passed, returned and copied as a unit. It does not add bounds checking or persistence."),
            Beat.tryIt("Add a `char name[24]` member, initialise it, and print all three fields. Then create an array of two students and print the second one.", c("10-structs.c")),
            Beat.reflect("Model the data first. When the same group of fields keeps travelling together, it is usually a type waiting to be named.")),

        new Lesson("files", "Files and errors", "Persistence, checked operations and honest failure", 8, c("11-files.c"),
            Beat.hook("A program that forgets everything at exit cannot keep a record of results. What does writing a file actually require you to check?"),
            Beat.explain("""
                    `fopen` returns a stream or NULL. NULL is not an empty file: it means the open failed, \
                    so using the stream would be invalid. `fprintf` returns the number of characters \
                    written or a negative value on failure. `fclose` flushes buffered data and reports \
                    whether that succeeded. Every one of those results is part of the program's \
                    correctness."""),
            Beat.define("""
                    A file is a persistent sequence of bytes managed through a stream interface. The \
                    program is responsible for checking that each operation succeeded."""),
            Beat.code(c("11-files.c"), "Record saved"),
            Beat.predict("What does `fopen(\"results.txt\", \"w\")` do if the file already exists?",
                    new String[]{"Truncates it and opens for writing", "Appends to it", "Returns NULL"},
                    0,
                    "Mode \"w\" truncates existing contents. Use \"a\" to append, and consider writing to a temporary file then renaming when a crash must not destroy the previous record."),
            Beat.visual("files", "Open, check, write, check, close, check. Each step can fail for a different reason: permissions, space, or a full disk."),
            Beat.check("What must be checked immediately after fopen?",
                    new String[]{"Whether it returned NULL", "Whether the filename is lowercase", "Whether the file is empty"},
                    0,
                    "NULL means the open failed. A valid stream may still refer to an empty file, which is a different state."),
            Beat.explain("""
                    Persisted data is untrusted on the way back in. A stored record may be truncated, \
                    edited or written by another version of the program. Parse defensively, validate \
                    ranges, and never store plaintext passwords: a file is not a security boundary."""),
            Beat.tryIt("Write two records, close the file, reopen it for reading and print the lines. Handle the open failure path explicitly.", c("11-files.c")),
            Beat.reflect("Robust I/O is mostly honest bookkeeping: check each return value and decide what the program should do when it fails.")),

        new Lesson("debugging", "Testing and debugging", "Compiling is not the same as being correct", 8, c("12-largest.c"),
            Beat.hook("This program compiles without warnings and prints a number. Is it correct? Only a specification and tests can answer that."),
            Beat.define("""
                    Debugging is the systematic investigation and correction of a fault that causes \
                    incorrect behaviour. Testing is the process of comparing observed behaviour with a \
                    specification."""),
            Beat.explain("""
                    Begin with inputs, expected outputs and invariants. Test normal cases, boundaries and \
                    invalid input. For `largest`, the boundaries are an empty array, one element, and \
                    equal elements. Compiler diagnostics catch language violations; they cannot know \
                    that your algorithm is wrong."""),
            Beat.visual("debug", "A defect travels from specification to design to code to runtime. Test at each stage to stop it early and cheaply."),
            Beat.code(c("12-largest.c"), "9"),
            Beat.predict("What does `largest(values, 0)` return in this example, and is that a satisfying answer?",
                    new String[]{"0, but the caller cannot distinguish 'empty' from 'largest is 0'", "It crashes, which is always correct", "It returns the first element"},
                    0,
                    "Returning 0 for an empty input silently conflates two meanings. A robust design signals the empty case explicitly, for example with a success flag or an error return."),
            Beat.check("Does successful compilation prove a program meets its specification?",
                    new String[]{"No — behaviour must be tested", "Yes, if warnings are enabled", "Yes, for C only"},
                    0,
                    "Compilation checks language rules. Whether the program computes the right answer is a separate claim that requires tests."),
            Beat.explain("""
                    When a test fails, reduce the case before changing code. Print the inputs, the state \
                    before the failing step, and the state after. A minimal reproduction turns guesswork \
                    into a specific question. Then fix the cause, not the symptom, and keep the test."""),
            Beat.tryIt("Move the maximum search into main for a moment and watch it still compile. Then add the empty-array test and make the function report failure honestly.", c("12-largest.c")),
            Beat.reflect("""
                    You have finished the C foundations: programs, types, input, decisions, loops, \
                    functions, arrays, pointers, structures, files and testing. Guided projects are now \
                    unlocked, and the C++ track builds directly on this ground.""")),
    };
}
