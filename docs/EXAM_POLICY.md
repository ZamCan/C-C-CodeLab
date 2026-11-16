# Certificate exam engine — design specification

- Certificate exams are online and server-authorized. Practice quizzes and flash questions are offline and free.
- 25 questions per attempt. Ten supported question styles: multiple choice, true/false, output prediction, error identification, code completion, code ordering, debugging, trace tables, algorithm selection, and practical coding.
- Proposed mode: select two styles for each 25-question exam; balance course objectives and difficulty. Do not let the selected styles prevent testing required skills.
- Server securely selects from a large tagged bank, applies per-learner exposure constraints, and records question identifiers and randomized parameter seeds.
- User-requested repeat probability 0.01 is a *target*, not a mathematically enforceable guarantee with finite banks and unlimited retakes. Measure both question overlap and probability of any repeated question; fail closed or relax only under explicit policy when insufficient fresh questions remain.
- Exam timer is server authoritative. Leaving or force-closing cannot be prevented; reopening resumes the same attempt without resetting its deadline. On expiry submit/expire according to policy. Abandonment and cooldown are auditable.
- No answer keys or hidden certificate exam bank in APK. Offline quizzes are distinct.
- For executable coding challenges, require a dedicated isolated grading runtime; Workers orchestrate but do not run arbitrary native student binaries.
- Certificate issue requires trusted identity/eligibility checks, passing assessment evidence, rate limits, idempotent issuance, status records, and protected signing keys.
- Accessibility accommodations and connectivity-failure policy must be explicit; do not penalize accidental interruptions without an appeal/review route.

## Confirmed learner policy — 2026-11-16

The final pass mark is **70% or above**. Before starting, the learner supplies three official-name components and confirms they match their official certificates. The server requires verified identity agreement and stores an immutable attempt name. After passing, the learner re-enters and confirms those names before issuance. Corrections require identity review; issued credentials are never silently edited.

Separate C17 and C++17 assessments must measure independent programming, not just recognition of syntax. C covers explicit lifetime, arrays, pointers, structs, checked input and C library behavior. C++ adds references, const correctness, RAII, value semantics, containers, algorithms and ownership. Do not teach malloc/free as idiomatic C++ ownership or permit C++-only syntax in C tasks.

Target academic review blueprint: 70% practical work and 30% theory, with independent practical competence required for eligibility. This weighting is a proposed assessment design, not a currently deployed grader. Existing 25 selected-answer questions award four points each: 68% fails and 72% passes under the exact 70% threshold. A secure weighted practical engine is still a release gate; never report client-supplied compiler success as trusted evidence.

Practice should scaffold worked example → modify → diagnose → independent implementation → unseen inputs. Optional brief humor must explain a real misconception (for example, “The compiler cannot read minds; give this variable a declaration.”), remain skippable, avoid timed delays and never obscure diagnostic feedback. No course can guarantee perfect coding; measure independent competence with concrete outcomes.
