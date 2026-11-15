package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class LessonAccessPolicyTest {
    @Test public void firstTwoLessonsDoNotRequireAds() {
        assertFalse(LessonAccessPolicy.needsReward(0));
        assertFalse(LessonAccessPolicy.needsReward(1));
    }
    @Test public void lessonThreeThroughTenRequireUnlock() {
        for (int i = 2; i < 10; i++) assertTrue(LessonAccessPolicy.needsReward(i));
    }
    @Test public void fifthThroughTenthAreAdvanced() {
        for (int i = 0; i < 10; i++) assertEquals(i >= 4, LessonAccessPolicy.isAdvanced(i));
    }
    @Test public void cAndCppUnlocksAreIndependent() {
        assertNotEquals(LessonAccessPolicy.key(false, 4), LessonAccessPolicy.key(true, 4));
    }
    @Test public void previousLessonAndRewardAreRequired() {
        assertFalse(LessonAccessPolicy.canOpen(false, true, 4));
        assertFalse(LessonAccessPolicy.canOpen(true, false, 4));
        assertTrue(LessonAccessPolicy.canOpen(true, true, 4));
        assertTrue(LessonAccessPolicy.canOpen(true, false, 1));
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsOutOfRangeLessons() { LessonAccessPolicy.needsReward(10); }
}
