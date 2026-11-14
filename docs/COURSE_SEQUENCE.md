# C/C++ CodeLab — binding academic course sequence

User decision 2026-11-14: Begin with algorithmic thinking, explicitly relate algorithms to computers, explain representations in depth, conclude with pseudocode, transition to programming languages, introduce C/C++ formally (definitions and history), then open separate C and C++ course maps. Algorithm foundation is skippable; formal C/C++ core follows Continue/Next with completion checkpoints and revision access.

## Entry navigation
Home → Learning Academy → **Start C/C++ journey**.
A. **Algorithms foundation** (optional; visible Skip to C/C++ at each section; skipping does not award mastery).
B. **Programming languages and C/C++ introduction** (required, sequenced).
C. **Choose pathway**: C first / C++ first / guided both. Required checkpoints within chosen pathway; no skip-ahead button, but allow resume and revisiting completed lessons. Never block leaving the app or browsing course outlines.

## Foundation lesson 1 — Algorithm
Learning outcome: explain algorithm, input, output, finite steps, unambiguity and effective execution; distinguish problem from solution and algorithm from implementation.
Start with a question: How can a computer decide if a student qualifies for a course?
Definition: an algorithm is a finite, precise sequence of effective steps to solve a specified problem or compute a result. Explain limitations and that an algorithm is not necessarily a computer program.
Relate to computer in plain language: **An algorithm describes the logic of a solution; a computer carries out that logic when it is expressed as executable instructions in a programming language.** Explain input → process → output and compiler role later. Use one-sentence summary before expanding academic details.

## Foundation lesson 2 — Representing algorithms
There is **no universal official count** of representations. Teach five practical pedagogical formats; note overlap between natural language and structured English:
1. Natural-language explanation (prose): eligibility determined by subject and grade criteria.
2. Numbered structured steps: start, collect subjects/grades, validate, test policy, announce result, stop.
3. Flowchart: Start oval → Input parallelogram → validation/decision diamond → action rectangle → Output parallelogram → End oval. Include Yes/No branches and one connected example.
4. Decision table: conditions, rules/columns, actions; distinguish multiple conditions and their combinations. Include at least one 3-condition case and avoid invented real admissions criteria. Use hypothetical eligibility rules or cite an exact verified TCU guideline when using real KIUT admissions.
5. Pseudocode **last**: a structured human-readable algorithm, not a standardized executable language; define INPUT, IF/ELSE, OUTPUT, ENDIF, indentation, conditions. Example:

    INPUT subject_eligible
    INPUT grades_sufficient
    INPUT application_complete
    IF subject_eligible AND grades_sufficient AND application_complete THEN
        OUTPUT "Eligible for review"
    ELSE
        OUTPUT "Requirements not met"
    ENDIF

Explain every token, branch, why this is not directly compiled, and that real admission decisions may involve other policies. Optional alternate examples: Celsius→Fahrenheit and annual compound interest.

## Transition lesson 3 — Programming languages
Define a programming language as a formal notation with syntax and semantics for expressing computational instructions/programs. Explain algorithms vs pseudocode vs source code, source files, compiler/linker/executable and output. Introduce syntax errors vs logic errors; no inaccurate claim that all languages compile identically.

## Required lesson 4 — Formal introduction to C and C++
- C: general-purpose procedural programming language developed by Dennis Ritchie at Bell Labs, primarily during 1969–1973, with strong ties to Unix. Discuss B/BCPL influences, systems programming, portability, compiled implementations, standardized C, and that C is not the same language as C++.
- C++: general-purpose multi-paradigm language developed by Bjarne Stroustrup, beginning with 'C with Classes' in 1979 and renamed C++ in 1983. Explain relationship to C, abstraction, RAII, generic and object-oriented programming, standards and compatibility limits. Avoid claiming every valid C program is valid C++.
- Compare C and C++ by purpose, idiomatic I/O, files, compilation modes, libraries, memory management and beginner code anatomy.
- Show accurate starter programs with each token explained; allow genuine experimentation only when real compiler engine exists, otherwise show clearly labeled static reference examples and disable Run.
- Include a short knowledge checkpoint and **Continue** button, not a Skip button.

## C pathway ordered modules
C01 Computer programs and toolchain; C02 first program, headers, main, printf; C03 tokens, indentation, comments and errors; C04 variables/types/constants; C05 operators and expressions; C06 scanf/input and validation; C07 conditions; C08 loops; C09 functions and scope; C10 arrays and strings; C11 pointers and memory; C12 structs/enums; C13 files and modular programs; C14 debugging/testing; C15 full mathematical and real-world problems; C16 independent final exercises.

## C++ pathway ordered modules
CPP01 C++ program anatomy and iostream; CPP02 syntax, names, namespaces and types; CPP03 expressions and input; CPP04 control flow; CPP05 functions and references; CPP06 strings and vectors; CPP07 arrays/pointers and safety; CPP08 classes, objects and encapsulation; CPP09 constructors, RAII and resource management; CPP10 inheritance and polymorphism; CPP11 templates and STL; CPP12 exceptions and error handling; CPP13 file I/O and multi-file builds; CPP14 debugging and testing; CPP15 C/C++ interoperability and differences; CPP16 independent final exercises.

## After core completion
Unlock five guided projects: calculator, educational login, student results, fictional ATM, library system. Certification is optional, online-only, independent of project laboratory. Do not treat skippable algorithm foundation as a certificate prerequisite unless separately approved.

## UI and content contract
Each lesson: outcome → relatable problem → precise definition → visual explanation → annotated example → trace/prediction → exercise → feedback → mastery checkpoint → Continue. Break into readable stages with accessible diagrams, dark/light syntax colors, optional deeper explanation, bookmarks and offline resume. Do not confuse a screen tap with demonstrated learning. Every symbol and code token: name, reason, position, constraints, alternative, error cases. Distinguish C and C++ syntax precisely.

## Authoritative research anchors (paraphrase originally; do not copy textbooks)
- MIT OCW Introduction to Algorithms syllabus: https://ocw.mit.edu/courses/6-046j-introduction-to-algorithms-sma-5503-fall-2005/pages/syllabus/
- MIT OCW 6.006 models of computation: https://live.ocw.mit.edu/courses/6-006-introduction-to-algorithms-fall-2011/6b9b20992d8c6a0f3f10a34ff7878aa9_MIT6_006F11_lec02.pdf
- Dennis Ritchie, The Development of the C Language: https://www.nokia.com/bell-labs/about/dennis-m-ritchie/chist.pdf
- University of Cambridge C/C++ lecture: https://www.cl.cam.ac.uk/teaching/0607/CandC%2B%2B/lecture1.pdf
- Bjarne Stroustrup, The C++ Programming Language; Kernighan & Ritchie, The C Programming Language; ISO language standards. Reference rather than reproduce copyrighted content.

## Agent implementation tasks
- [ ] Create structured course metadata and prerequisite/skip flags, with migration-safe local progress.
- [ ] Implement all five algorithm representation lessons, diagram/table and pseudocode components.
- [ ] Implement required programming language and C/C++ history introduction.
- [ ] Add sequential navigation, resume, optional foundation skip without false completion.
- [ ] Populate deep C/C++ course tracks with reviewed original lessons, code, checkpoints and tests.
- [ ] Test all routes, prerequisite boundaries, offline behavior and accessibility on Android.
