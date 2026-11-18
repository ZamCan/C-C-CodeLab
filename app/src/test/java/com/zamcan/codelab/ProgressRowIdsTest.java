package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ProgressRowIdsTest {
    @Test public void stableDistinctLessonIds() {
        String a=ProgressRowIds.forLesson("c-foundation","1");
        assertEquals(a,ProgressRowIds.forLesson("c-foundation","1"));
        assertNotEquals(a,ProgressRowIds.forLesson("cpp-foundation","1"));
        assertNotEquals(a,ProgressRowIds.forLesson("c-foundation","2"));
        assertTrue(a.matches("p_[0-9a-f]{32}"));
    }
    @Test public void rejectsUnsafeKeys() {
        for(String value:new String[]{"","../", "a/b", "has space"}) {
            try {ProgressRowIds.forLesson(value,"1");fail(value);}
            catch(IllegalArgumentException expected) {}
        }
    }
}
