package com.zamcan.codelab;

/** Deterministic ad unlock policy. C and C++ are independent learning tracks. */
public final class LessonAccessPolicy {
    private LessonAccessPolicy() {}
    public static boolean needsReward(int lessonIndex) {
        if (lessonIndex < 0 || lessonIndex >= 10) throw new IllegalArgumentException("Lesson index");
        return lessonIndex >= 2;
    }
    public static boolean isAdvanced(int lessonIndex) {
        if (lessonIndex < 0 || lessonIndex >= 10) throw new IllegalArgumentException("Lesson index");
        return lessonIndex >= 4;
    }
    public static String key(boolean cpp, int lessonIndex) {
        if (lessonIndex < 0 || lessonIndex >= 10) throw new IllegalArgumentException("Lesson index");
        return (cpp ? "cpp" : "c") + "-lesson-" + (lessonIndex + 1);
    }
    public static boolean canOpen(boolean previousCompleted, boolean rewarded, int lessonIndex) {
        return previousCompleted && (!needsReward(lessonIndex) || rewarded);
    }
}
