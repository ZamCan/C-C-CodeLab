# C/C++ CodeLab — Deep curriculum and lesson experience (2026-11-15)

## Reference screens and originality
The user supplied seven screenshots illustrating: a white exam-instructions screen with a teacher illustration and prominent blue-violet Start Test action; a success screen with accuracy and completion time; segmented progress rails; end-of-course story slides; and a certificate transition. Treat these as **interaction and layout references**, not assets to reproduce. Do not copy characters, third-party marks, text, or impersonate Bjarne Stroustrup. Create original, accessible CodeLab illustrations and original instructor voice. Avoid inaccurate claims of certificate issuance before the trusted backend has issued one.

## Curriculum data model
Subject → Track (C17/C++17) → Course → Topic → **3–5 modules per topic** → staged lessons → code labs → checkpoints → topic assessment → mastery. A module may contain multiple lessons; never compress a complex concept into one definition card. Store content in versioned structured offline assets, not a single giant UI string. Include stable IDs, prerequisites, objective tags, estimated effort, language standard, runnable examples, expected stdout/stderr/exit code, misconceptions, tests, and source references. Keep navigation resumable and searchable. Allow optional deeper reading without overwhelming the primary stage.

## Mandatory lesson anatomy
Every unfamiliar syntax element: name and lexical role; definition and motivation; valid position/context; what happens if moved, removed or changed; C versus C++ behavior; small runnable example; predicted output; compiler errors with actual diagnostic when available; editable experiment; guided exercise; independent task; answer explanation and mastery checkpoint. Distinguish compile-time, link-time and runtime behavior. Explain memory and types accurately without claiming platform-specific sizes are universal. Never simulate compilation results as live output.

## Course map (each topic contains 3–5 substantive modules)
**Shared Foundations**
1. Computing and programs: computer/OS, source vs machine code, algorithms, inputs/process/outputs, real problem formulation (4).
2. Problem solving: decomposition, pseudocode, flowcharts, decision tables, tracing and testing (5).
3. C/C++ origins and toolchains: standards, source/header, preprocessor, compiler, linker, executable and terminal (4).
4. First programs: file anatomy, headers, main, braces, statements, stdout, return and diagnostics (5).
5. Number systems and mathematical computing: binary/octal/decimal/hex, arithmetic, integer overflow, interest and temperature conversion (4).

**C17 Track**
6. Variables and fundamental types: identifiers/declaration/initialization; integer/float/char/_Bool and signedness; conversions/precision; const/storage/scope; practical input/output (5).
7. Operators and expressions: arithmetic, relational/logical, precedence/associativity, increments, bitwise and safe evaluation (5).
8. Input/output and formatting: printf format strings, scanf pitfalls, fgets/strtol, validation, errors (4).
9. Decisions: if/else, nested conditions, switch, decision tables, validation and grade examples (5).
10. Loops: while/do-while/for, sentinels, counters, compound-interest >2000, nested loops and termination (5).
11. Arrays: indexing, declaration/initialization, bounds, traversal, 2D arrays and searching/sorting (5).
12. Strings: char arrays, terminators, length, input, comparison, buffers and safe formatting (5).
13. Functions: prototypes, definitions/calls, parameters/return, pass-by-value, recursion and modular testing (5).
14. Pointers and memory: address/dereference, pointer-array relationship, lifetime, allocation/free and safety (5).
15. Structures/enums/unions: data modeling, initialization, nested records, enums, tradeoffs (4).
16. Files and errors: text/binary files, fopen/fclose, error checks, parsing, persistence (4).
17. Multi-file C projects: headers, translation units, linkage, make/build flags, debugging and tests (5).

**C++17 Track**
18. C++ program structure and I/O: iostream, std::, main, streams, namespaces, compiler differences (4).
19. Types, variables and references: auto, const/constexpr, initialization forms, references, conversions and lifetime (5).
20. Expressions and control flow: conditions, loops, switch, range-for, algorithm tracing (4).
21. Strings, arrays and vectors: std::string, std::array, std::vector, bounds and iterators (5).
22. Functions: overloads, default arguments, pass-by-value/reference, templates introduction, recursion (5).
23. Classes and objects: fields, methods, constructors, encapsulation, invariants and access control (5).
24. Ownership and RAII: stack/heap, lifetime, smart pointers, move semantics, resource safety (5).
25. Standard library: algorithms, containers, iterators, maps/sets and complexity (5).
26. Inheritance and polymorphism: virtual functions, abstract interfaces, overrides, composition alternatives (4).
27. Exceptions, files and testing: error handling, streams, testing, debugging and build organization (5).
28. Generic programming and templates: function/class templates, deduction, constraints of C++17, reusable algorithms (4).

**Applied engineering and capstones**
29. C versus C++: standards, headers, strings, memory, linkage, compatibility, style and migration (4).
30. Algorithms and data structures: searching, sorting, complexity, recursion, stacks/queues and correctness (5).
31. Real-world program design: specification, validation, modularization, test cases, documentation and review (5).
32. Five guided projects: calculator, educational login, student results, fictional ATM, library system; each is a full multi-stage lab, not a one-screen tutorial (5).

## Example deep topic: Variables and Types (five modules)
1. Meaning and memory model: values, names, storage, declaration vs definition, naming rules; `int age = 20;`; tracing values and printout.
2. Initialization and assignments: `int x;` vs `int x = 0;`, uninitialized reads, assignments, multiple declarations, scope, compiler warnings.
3. Type families: signed/unsigned integer ranges, floating-point approximation, character encodings, booleans, `sizeof`, portable formatting; compare `5/2` and `5.0/2`.
4. Conversions and lifetime: integer promotions, narrowing, casts, const, local/global/static storage, C++ references/auto differences.
5. Practical program: temperature converter and student score calculator with input validation, tests, common diagnostics and independent extension.
Each module includes at least two runnable, verified examples (simple + applied), a guided modification and a checkpoint. Content depth is measured by correctness and independence, not word count.

## Example deep topic: Arrays (five modules)
1. Why collections; declaration, indexing and memory layout.
2. Initialization, length calculation and bounds; no runtime bounds checking for raw arrays.
3. Iteration and aggregation (sum/average/min/max).
4. 2D arrays, rows/columns and table examples.
5. Searching/sorting and passing arrays to functions; C++ vector alternatives, tests and error cases.

## Example deep topic: Functions (five modules)
1. Why functions, signatures, declarations and definitions.
2. Arguments, return types, void and control flow.
3. Scope, lifetime and pass-by-value versus C++ references.
4. Recursion, base cases, stack limits and tracing.
5. Header/module organization, testing and reusable mathematical routines.

## UX and assessments
- Reference-first light theme; equally polished dark mode; segmented progress; short controls Next/Back/Try/Check/Finish; original illustration only when pedagogically useful; accessible large text and screen readers.
- Topic learning remains offline and free. Module checkpoint feedback explains errors, allows review and supports resume. On completion show real progress, accuracy and time **derived from actual data**. Do not always show 100% or a fabricated speed.
- Online final exam: up to 10 reviewed variants per course, exactly 25 top-level questions; ten design families and multipart subparts counting as one question. Short but justified server-enforced duration, with accessibility accommodations and network appeal path. Do not copy screenshot rules that automatically fail app switching, prevent back-navigation, or claim an arbitrary 12-minute limit for 25 complex questions.
- Distinguish **course completion** from **certificate earned**. A celebratory certificate screen appears only after successful online authenticated assessment and verifiable signed issuance; otherwise show eligibility / pending verification. No instructor impersonation.

## Acceptance evidence
For every topic: 3–5 modules, complete explanation metadata, 2+ verified runnable examples per module, at least one error/debugging example, editable exercise, quiz feedback, tests, C/C++ standard correctness and localization scaffolding. Verify on-device offline operation and content navigation before calling it complete. Track completion counts in delivery ledger.
