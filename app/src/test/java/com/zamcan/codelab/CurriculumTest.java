package com.zamcan.codelab;
import org.junit.Test;
import static org.junit.Assert.*;
public class CurriculumTest {
 @Test public void eachTrackContainsTenCompletePracticeLessons() {
  for(boolean cpp:new boolean[]{false,true}) {
   Lesson[] lessons=Curriculum.lessons(cpp);assertEquals(10,lessons.length);
   java.util.Set<String> titles=new java.util.HashSet<>();
   for(Lesson lesson:lessons){assertTrue(titles.add(lesson.title));assertTrue(lesson.explanation.length()>150);assertTrue(lesson.code.contains("main("));assertFalse(lesson.challenge.isEmpty());assertTrue(lesson.answer>=0&&lesson.answer<lesson.options.length);assertFalse(lesson.feedback.isEmpty());}
  }
 }
}
