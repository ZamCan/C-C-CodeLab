package com.zamcan.codelab;

/** Offline practice policy, independent of Android and certificate assessment. */
public final class LearningPath {
    public static final String[] STAGES = {"Understand", "Worked example", "Try it", "Checkpoint"};
    private LearningPath() {}

    public static int stage(int saved) {
        return Math.max(0, Math.min(STAGES.length - 1, saved));
    }

    public static boolean canOpen(boolean introductionPassed, int index, int count, boolean previousPassed) {
        return introductionPassed && index >= 0 && index < count && (index == 0 || previousPassed);
    }

    public static boolean introductionPassed(int language, int compatibility, String compiler, String pipeline) {
        return language == 1 && compatibility == 0
            && "compiler".equalsIgnoreCase(compiler.trim())
            && "123".equals(pipeline.replaceAll("[\\s,→>-]", ""));
    }
}
