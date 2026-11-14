package com.zamcan.codelab;

public final class Lesson {
    public final String title, explanation, code, challenge, question, feedback;
    public final String[] options;
    public final int answer;
    public Lesson(String title, String explanation, String code, String challenge, String question, String[] options, int answer, String feedback) {
        this.title=title; this.explanation=explanation; this.code=code; this.challenge=challenge;
        this.question=question; this.options=options; this.answer=answer; this.feedback=feedback;
    }
}
