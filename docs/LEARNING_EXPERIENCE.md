# C/C++ CodeLab — premium learning experience and engagement engineering

## Purpose
The premium brand must continue through **every educational screen**. Optimize for understanding, confidence, sustained voluntary study and independent problem solving, not screen time alone. Avoid dark patterns, artificial scarcity, deceptive streaks, forced ads or celebratory animations without achievement.

## Design grammar
Use the brand palette in docs/VISUAL_BRAND.md, with consistent spacing (4/8/12/16/24dp), typography hierarchy, meaningful icons and restrained accent colors. Gold signals a milestone; blue signals exploration; violet signals practice; green signals demonstrated mastery; red indicates an error with an accessible textual explanation. Color never carries meaning alone. Preserve readable code contrast and comfortable 16sp+ lesson body text where possible. Dark and light themes must have equal quality. Avoid one long scrolling page, tiny controls, glass effects behind dense code, excessive cards and noisy motion.

## Learning screens and interactions
1. **Course map**: structured modules, prerequisites, completion states, estimated study time (optional), resume point, offline status, no artificial pressure. C and C++ pathways clearly distinct but comparative bridges available.
2. **Lesson overview**: what the student will be able to do, why the concept matters, a small real-world question, key prerequisites.
3. **Concept exploration**: concise readable chunks, tap-to-expand deeper explanations, real syntax annotations, original diagrams/flowcharts and clear definitions. Always offer deep academic explanation, not just bite-sized slogans.
4. **Code anatomy**: selectable tokens with 'name / role / why here / what if moved or removed / C vs C++ / common error / try it'. Explain indentation as readability, not syntax control in C/C++.
5. **Predict before run**: learner predicts stdout or error before revealing compiler output; distinguish prediction, expected sample output and actual execution.
6. **Guided experiment**: editable source, deliberate one-line changes, real compile/run if engine available, exact diagnostics, accessible explanation. If compiler unavailable, visibly disable Run and explain why; never simulate execution.
7. **Checkpoint**: 1–3 concept questions with explanatory feedback and retry, spaced review for weak concepts, optional hints without shame.
8. **Mastery summary**: show which concepts were demonstrated, recommended next topic, independent practice prompt; no fabricated mastery scores.
9. **Practice lab**: progressively open problems, optional hints, visible test expectations, student-controlled difficulty; save work offline.
10. **Project laboratory**: five post-course guided projects with stages specification → pseudocode/flowchart → design → implement → compile → debug → test → reflection. Static UI previews always labeled and separated from live terminal output.

## Pedagogical example — compound interest
Start with 1000, annual interest 10%, stop when amount > 2000. Ask learner to predict years, display a trace table, explain while loop and strict > versus >=, then run actual C and C++ versions and change interest rate. Explain floating point limitations and how to validate inputs. The learner should be able to write the program independently afterward.

## Pedagogical example — C and C++ main
Compare C `int main(void)` with C++ `int main()`; explain identifiers, return type, parameter list, function body, semicolons, preprocessor, headers, namespaces and output statements token by token. Allow tap-to-reveal reasons, compile-time error examples and genuine experiments.

## Learning analytics and engagement
Local-first event model: lesson_started, concept_explored, prediction_submitted, compile_attempted, feedback_viewed, checkpoint_completed, mastery_demonstrated, lesson_resumed. Do not upload private source code or sensitive analytics by default. Track meaningful progress and misconceptions, not merely time-on-screen. Recommend short breaks for extended study. Daily goals and streaks must be optional, forgiving and disableable. Offer offline bookmarks, continue-where-left-off, searchable concepts and revision plans.

## App structure and continuity
Home → Code Studio or Learning Academy. Within Learning Academy: C/C++ course selection, module map, lesson stages, exercises, quizzes, projects, optional online certification. Each screen has predictable Back, progress and resume behavior. Provide both immersive lesson mode and quick reference mode. Do not let an ad obscure code, explanations, feedback or keyboard.

## Accessibility and performance
Small Android screens, large font scaling, TalkBack, reduced motion, high contrast, landscape editor usability, low-memory devices, no compulsory login for offline content, graceful airplane mode. No fake loading, inflated progress or animated celebration on failure.

## Acceptance tests
- Student can explain at least one token and modify a real example after each beginner lesson.
- A learner can leave midway, reopen offline and resume exactly.
- Error feedback identifies the concept and gives a useful next action.
- C and C++ explanations remain technically distinct.
- Progress corresponds to completed checkpoints, not taps alone.
- Light/dark themes and font scaling remain usable on 360dp screen.
- Static project preview and real compiler output are unmistakably distinct.
- The final APK screenshots reflect actual implemented screens rather than concept art.
