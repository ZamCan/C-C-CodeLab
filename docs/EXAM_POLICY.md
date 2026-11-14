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
