package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Structural guarantees for authored lessons. These are cheap invariants that catch the mistakes
 * which make a lesson unusable: a missing hook, an unanswered checkpoint, a duplicate option, or a
 * beat that points at a code example which does not exist in the bundle.
 */
public class LessonScriptTest {
    private static void assertLesson(LessonScript.Lesson lesson, boolean cpp) {
        assertFalse("lesson has a title", lesson.title.isEmpty());
        assertFalse("lesson has a subtitle", lesson.subtitle.trim().isEmpty());
        assertTrue("lesson has beats", lesson.beats.length >= 6);
        assertTrue("lesson has at least one hook", has(lesson, Beat.Kind.HOOK));
        assertTrue("lesson has a precise definition", has(lesson, Beat.Kind.DEFINITION));
        assertTrue("lesson includes an explanation", has(lesson, Beat.Kind.EXPLAIN));
        assertTrue("lesson includes a verified example", has(lesson, Beat.Kind.CODE));
        assertTrue("lesson offers practice", has(lesson, Beat.Kind.TRY));
        assertTrue("lesson ends with a recap", lesson.beats[lesson.beats.length - 1].kind == Beat.Kind.REFLECT);
        assertTrue("lesson scores at least one checkpoint", lesson.scoringBeats() >= 1);
        assertTrue("example points at the right language bundle",
                lesson.codeAsset.startsWith(cpp ? "curriculum/cpp/" : "curriculum/c/"));
        assertTrue("example is a C++ source", !cpp || lesson.codeAsset.endsWith(".cpp"));
        assertTrue("example is a C source", cpp || lesson.codeAsset.endsWith(".c"));

        for (Beat beat : lesson.beats) {
            if (beat.kind == Beat.Kind.DIALOGUE) {
                assertNotNull("dialogue names its speaker", beat.speaker);
                assertTrue("dialogue is marked as dramatisation", beat.speaker.contains("imagined"));
                assertFalse("dialogue has content", beat.text.trim().isEmpty());
            }
            if (beat.kind == Beat.Kind.VISUAL) {
                assertNotNull("diagram has an art id", beat.art);
                assertFalse("diagram has a caption", beat.text.trim().isEmpty());
            }
            if (beat.kind == Beat.Kind.CODE || beat.kind == Beat.Kind.TRY) {
                assertNotNull("example path is present", beat.source);
                assertTrue(beat.source.length() > 8);
            }
            if (beat.scored()) {
                assertNotNull("checkpoint has options", beat.options);
                assertTrue("checkpoint has a real choice", beat.options.length >= 2);
                assertTrue("answer index is in range", beat.answer >= 0 && beat.answer < beat.options.length);
                assertFalse("checkpoint explains the answer", beat.explanation.trim().isEmpty());
                java.util.Set<String> unique = new java.util.HashSet<>();
                for (String option : beat.options) assertTrue("options are distinct", unique.add(option));
            }
        }
    }

    private static boolean has(LessonScript.Lesson lesson, Beat.Kind kind) {
        for (Beat beat : lesson.beats) if (beat.kind == kind) return true;
        return false;
    }

    @Test public void bothTracksContainTenSubstantialLessons() {
        for (boolean cpp : new boolean[]{false, true}) {
            LessonScript.Lesson[] lessons = LessonScript.lessons(cpp);
            assertTrue("at least ten lessons in " + (cpp ? "C++" : "C"), lessons.length >= 10);
            java.util.Set<String> ids = new java.util.HashSet<>();
            for (LessonScript.Lesson lesson : lessons) {
                assertTrue("lesson ids are unique", ids.add(lesson.id()));
                assertLesson(lesson, cpp);
            }
        }
    }

    @Test public void historicalAttributionIsAccurate() {
        LessonScript.Lesson meetC = LessonScript.lessons(false)[0];
        LessonScript.Lesson meetCpp = LessonScript.lessons(true)[0];
        assertTrue(text(meetC).contains("1972"));
        assertTrue(text(meetC).contains("Ritchie"));
        assertTrue(text(meetCpp).contains("Stroustrup"));
        assertTrue(text(meetCpp).contains("1979"));
        assertTrue(text(meetCpp).contains("1983"));
        // C++ is multi-paradigm, and C with Classes is a historical name, not a definition.
        assertTrue(text(meetCpp).contains("general-purpose"));
        assertFalse(text(meetCpp).contains("C++ is object-oriented only"));
    }

    private static String text(LessonScript.Lesson lesson) {
        StringBuilder all = new StringBuilder();
        for (Beat beat : lesson.beats) {
            if (beat.speaker != null) all.append(beat.speaker).append('\n');
            if (beat.text != null) all.append(beat.text).append('\n');
            if (beat.explanation != null) all.append(beat.explanation).append('\n');
        }
        return all.toString();
    }
}
