package com.zamcan.codelab;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fast, local editing aids. Clang remains responsible for full language diagnostics. */
final class EditorAssist {
    private static final String[] COMMON = ("printf scanf puts fgets fprintf sizeof return while switch "
            + "break continue case default else unsigned signed static const void int double float char "
            + "long short struct enum typedef main include define stdio.h stdlib.h string.h math.h").split(" ");
    private static final String[] CPP = ("std::cout std::cin std::cerr std::endl std::string std::vector "
            + "std::array std::sort std::getline std::size_t std::unique_ptr std::make_unique "
            + "iostream string vector array algorithm memory namespace class public private template "
            + "constexpr nullptr true false auto bool").split(" ");
    private static final Pattern TOKEN = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
    private static final Pattern CONDITIONAL = Pattern.compile("(?m)^[ \\t]*#[ \\t]*(?:if|ifdef|ifndef)\\b");
    private static final int MAX_SCAN = 24000;

    static final class Hint {
        final int offset;
        final String message;
        Hint(int offset, String message) {
            this.offset = offset;
            this.message = message;
        }
    }

    static int prefixStart(CharSequence source, int cursor) {
        int start = cursor;
        while (start > 0 && Character.isJavaIdentifierPart(source.charAt(start - 1))) start--;
        if (start >= 5 && source.subSequence(start - 5, start).toString().equals("std::")) start -= 5;
        return start;
    }

    static List<String> suggestions(CharSequence source, int cursor, boolean cpp) {
        LinkedHashSet<String> found = new LinkedHashSet<>();
        if (cursor < 0 || cursor > source.length()) return new ArrayList<>();
        int start = prefixStart(source, cursor);
        String prefix = source.subSequence(start, cursor).toString();
        if (prefix.length() < 2) return new ArrayList<>();
        if (cpp) for (String word : CPP) add(found, word, prefix);
        for (String word : COMMON) add(found, word, prefix);
        Matcher local = TOKEN.matcher(source.subSequence(Math.max(0, start - 4000), start));
        while (found.size() < 4 && local.find()) add(found, local.group(), prefix);
        return new ArrayList<>(found);
    }

    private static void add(LinkedHashSet<String> matches, String word, String prefix) {
        if (matches.size() < 4 && word.length() > prefix.length() && word.startsWith(prefix)) matches.add(word);
    }

    /** Delimiter check deliberately defers conditional compilation and long buffers to Clang. */
    static Hint delimiterHint(CharSequence source) {
        int length = source.length();
        if (length == 0 || length > MAX_SCAN) return null;
        String value = source.toString();
        if (CONDITIONAL.matcher(value).find()) return null;
        int[] stack = new int[length];
        int depth = 0;
        for (int offset = 0; offset < length; offset++) {
            char c = value.charAt(offset);
            if (value.startsWith("//", offset) || c == '#') {
                int end = value.indexOf('\n', offset);
                while (end > offset && value.charAt(end - 1) == '\\') end = value.indexOf('\n', end + 1);
                offset = end < 0 ? length : end;
                continue;
            }
            if (value.startsWith("/*", offset)) {
                int end = value.indexOf("*/", offset + 2);
                offset = end < 0 ? length : end + 1;
                continue;
            }
            if (value.startsWith("R\"", offset)) {
                int open = value.indexOf('(', offset + 2);
                if (open >= 0 && open - offset <= 18) {
                    String marker = ")" + value.substring(offset + 2, open) + "\"";
                    int end = value.indexOf(marker, open + 1);
                    offset = end < 0 ? length : end + marker.length() - 1;
                    continue;
                }
            }
            if (c == '"' || c == '\'') {
                // C++ digit separators sit between digits or hexadecimal letters.
                if (c == '\'' && offset > 0 && offset + 1 < length
                        && Character.digit(value.charAt(offset - 1), 16) >= 0
                        && Character.digit(value.charAt(offset + 1), 16) >= 0) continue;
                for (offset++; offset < length; offset++) {
                    char literal = value.charAt(offset);
                    if (literal == '\\') offset++;
                    else if (literal == c || literal == '\n') break;
                }
                continue;
            }
            if ("([{".indexOf(c) >= 0) {
                stack[depth++] = offset;
                continue;
            }
            int closing = ")]}".indexOf(c);
            if (closing < 0) continue;
            if (depth == 0) return new Hint(offset, "Unexpected " + c);
            char opener = source.charAt(stack[depth - 1]);
            char expected = ")]}".charAt("([{".indexOf(opener));
            if (expected != c) return new Hint(offset, "Expected " + expected + " before " + c);
            depth--;
        }
        if (depth == 0) return null;
        int offset = stack[depth - 1];
        return new Hint(offset, "Unclosed " + source.charAt(offset));
    }

    static int dedentStart(CharSequence text, int brace) {
        int start = brace;
        while (start > 0 && (text.charAt(start - 1) == ' ' || text.charAt(start - 1) == '\t')) start--;
        if (start == brace || (start > 0 && text.charAt(start - 1) != '\n')) return brace;
        if (text.charAt(brace - 1) == '\t') return brace - 1;
        int remove = brace;
        while (remove > start && brace - remove < 4 && text.charAt(remove - 1) == ' ') remove--;
        return remove;
    }

    private EditorAssist() {}
}
