package com.zamcan.codelab;

import java.util.Locale;

/** Identifies explicit reasoning prompts, including prompts following an authored subtitle. */
final class ReadingPause {
    private ReadingPause() {}

    static boolean required(String text,boolean code,boolean challenge) {
        if (challenge) return true;
        if (code) return false;
        for (String paragraph : text.split("\\n\\s*\\n")) {
            String lead=paragraph.trim().toLowerCase(Locale.ROOT);
            if (lead.endsWith("?") || lead.contains("? ") || lead.contains("?\n")
                    || lead.startsWith("think first") || lead.startsWith("brainstorm")
                    || lead.startsWith("checkpoint") || lead.startsWith("question")
                    || lead.startsWith("reflection:") || lead.startsWith("think:")
                    || lead.startsWith("exercise:") || lead.startsWith("predict ")
                    || lead.contains(". predict ") || lead.contains(", predict ")) return true;
        }
        return false;
    }
}
