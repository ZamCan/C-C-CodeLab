package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class LearningPathTest {
    @Test public void introductionRequiresEveryAnswer() {
        assertTrue(LearningPath.introductionPassed(1,0," Compiler ","1, 2, 3"));
        assertFalse(LearningPath.introductionPassed(-1,0,"compiler","123"));
        assertFalse(LearningPath.introductionPassed(1,1,"compiler","123"));
        assertFalse(LearningPath.introductionPassed(1,0,"linker","123"));
        assertFalse(LearningPath.introductionPassed(1,0,"compiler","132"));
        assertFalse(LearningPath.introductionPassed(1,0,"compiler","1123"));
    }
    @Test public void navigationEnforcesPrerequisitesAndBounds() {
        assertFalse(LearningPath.canOpen(false,0,10,true));
        assertTrue(LearningPath.canOpen(true,0,10,false));
        assertFalse(LearningPath.canOpen(true,1,10,false));
        assertTrue(LearningPath.canOpen(true,1,10,true));
        assertFalse(LearningPath.canOpen(true,10,10,true));
        assertFalse(LearningPath.canOpen(true,-1,10,true));
    }
    @Test public void corruptResumeStageIsBounded() {
        assertEquals(0,LearningPath.stage(-10));
        assertEquals(2,LearningPath.stage(2));
        assertEquals(3,LearningPath.stage(100));
    }
}
