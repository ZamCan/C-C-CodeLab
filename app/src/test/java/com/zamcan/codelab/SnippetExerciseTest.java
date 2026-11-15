package com.zamcan.codelab;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class SnippetExerciseTest {
    @Test public void starterProgramsHaveCorrectRearrangement() {
        for(SnippetExercise exercise:SnippetExercise.starterExercises()) {
            assertEquals(exercise.answer.size(),exercise.score(exercise.answer));
            assertTrue(exercise.correctedSource().contains("main("));
        }
    }
    @Test public void incorrectSnippetGetsExactExpectedNumber() {
        SnippetExercise exercise=SnippetExercise.starterExercises().get(0);
        List<SnippetExercise.Feedback> feedback=exercise.check(Arrays.asList(2,1,3,4,5));
        assertFalse(feedback.get(0).correct);
        assertEquals(2,feedback.get(0).selectedId);
        assertEquals(1,feedback.get(0).correctId);
        assertTrue(feedback.get(2).correct);
        assertEquals(3,exercise.score(Arrays.asList(2,1,3,4,5)));
    }
    @Test(expected=IllegalArgumentException.class)
    public void duplicateSnippetRejected() {
        SnippetExercise.starterExercises().get(0).check(Arrays.asList(1,1,3,4,5));
    }
}
