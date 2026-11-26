package com.zamcan.codelab;

/**
 * One authored step of a lesson. A lesson is a chain of beats, never a wall of text: the learner
 * can move forward one idea at a time, go back, and must answer reasoning checkpoints.
 */
final class Beat {
    enum Kind { HOOK, DIALOGUE, DEFINITION, EXPLAIN, VISUAL, CODE, PREDICT, CHECK, TRY, REFLECT }

    final Kind kind;
    /** Dialogue speaker, including the explicit fictionalisation note where relevant. */
    final String speaker;
    /** Prose, caption, prompt or instruction depending on {@link #kind}. */
    final String text;
    /** Illustration id for {@link Kind#VISUAL}. */
    final String art;
    /** Bundled example path for {@link Kind#CODE} and {@link Kind#TRY}. */
    final String source;
    /** Expected output shown under a code example, or empty when the example reads input. */
    final String output;
    /** Question shown for a prediction or checkpoint. */
    final String prompt;
    final String[] options;
    final int answer;
    final String explanation;

    private Beat(Kind kind, String speaker, String text, String art, String source, String output,
            String prompt, String[] options, int answer, String explanation) {
        this.kind = kind;
        this.speaker = speaker;
        this.text = text;
        this.art = art;
        this.source = source;
        this.output = output;
        this.prompt = prompt;
        this.options = options;
        this.answer = answer;
        this.explanation = explanation;
    }

    static Beat hook(String text) {
        return new Beat(Kind.HOOK, null, text, null, null, null, null, null, -1, null);
    }

    static Beat say(String speaker, String text) {
        return new Beat(Kind.DIALOGUE, speaker, text, null, null, null, null, null, -1, null);
    }

    static Beat define(String text) {
        return new Beat(Kind.DEFINITION, null, text, null, null, null, null, null, -1, null);
    }

    static Beat explain(String text) {
        return new Beat(Kind.EXPLAIN, null, text, null, null, null, null, null, -1, null);
    }

    static Beat visual(String art, String caption) {
        return new Beat(Kind.VISUAL, null, caption, art, null, null, null, null, -1, null);
    }

    static Beat code(String source, String output) {
        return new Beat(Kind.CODE, null, null, null, source, output, null, null, -1, null);
    }

    static Beat predict(String prompt, String[] options, int answer, String explanation) {
        return new Beat(Kind.PREDICT, null, null, null, null, null, prompt, options, answer, explanation);
    }

    static Beat check(String prompt, String[] options, int answer, String explanation) {
        return new Beat(Kind.CHECK, null, null, null, null, null, prompt, options, answer, explanation);
    }

    static Beat tryIt(String instruction, String source) {
        return new Beat(Kind.TRY, null, instruction, null, source, null, null, null, -1, null);
    }

    static Beat reflect(String text) {
        return new Beat(Kind.REFLECT, null, text, null, null, null, null, null, -1, null);
    }

    /** Predictions and checks are scored; everything else is reading. */
    boolean scored() {
        return kind == Kind.PREDICT || kind == Kind.CHECK;
    }

    String label() {
        switch (kind) {
            case HOOK: return "Question";
            case DIALOGUE: return "Dialogue";
            case DEFINITION: return "Definition";
            case EXPLAIN: return "Explanation";
            case VISUAL: return "Diagram";
            case CODE: return "Example";
            case PREDICT: return "Predict";
            case CHECK: return "Check";
            case TRY: return "Try it";
            default: return "Recap";
        }
    }
}
