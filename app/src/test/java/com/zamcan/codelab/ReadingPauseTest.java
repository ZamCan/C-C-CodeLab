package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReadingPauseTest {
    @Test public void gatesActualQuestionsAndAuthoredPrompts(){
        for(String prompt:new String[]{
            "What will this print?", "A new example\n\nWhat happens next?",
            "Which answer? Look at the result.", "Brainstorm map\nVary the inputs",
            "CHECKPOINT\nExplain how the loop ends", "Question 2\nName the type",
            "Before you run this, predict the result.", "Here is a loop. Predict its output."})
            assertTrue(prompt,ReadingPause.required(prompt,false,false));
    }
    @Test public void optionalProseAndSourceAreSkippable(){
        assertFalse(ReadingPause.required("The compiler translates the program.",false,false));
        assertFalse(ReadingPause.required("int n = ok ? 1 : 0;",true,false));
        assertTrue(ReadingPause.required("Change the loop bound and explain the result.",false,true));
    }
}
