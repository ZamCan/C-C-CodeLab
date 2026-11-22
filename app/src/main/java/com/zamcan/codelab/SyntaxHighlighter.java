package com.zamcan.codelab;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small deterministic C/C++ colourer shared by the editor and reading cards. */
public final class SyntaxHighlighter {
    private SyntaxHighlighter() {}

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "alignas", "alignof", "asm", "auto", "break", "case", "catch", "class", "const",
            "constexpr", "continue", "default", "delete", "do", "else", "enum", "explicit",
            "extern", "false", "for", "friend", "goto", "if", "inline", "mutable", "namespace",
            "new", "noexcept", "nullptr", "operator", "private", "protected", "public", "register",
            "return", "sizeof", "static", "static_assert", "struct", "switch", "template", "this",
            "throw", "true", "try", "typedef", "typename", "union", "using", "virtual", "volatile",
            "while", "define", "include", "ifdef", "ifndef", "endif", "pragma"));

    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
            "bool", "char", "char16_t", "char32_t", "double", "float", "int", "long", "short",
            "signed", "unsigned", "void", "wchar_t", "size_t", "ssize_t", "int8_t", "int16_t",
            "int32_t", "int64_t", "uint8_t", "uint16_t", "uint32_t", "uint64_t", "FILE",
            "std", "string", "vector", "array", "map", "set", "unique_ptr", "shared_ptr"));

    private static final Pattern TOKEN = Pattern.compile(
            "(?m)//[^\\n]*|/\\*[\\s\\S]*?\\*/|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'"
            + "|^\\s*#\\s*\\w+|\\b[A-Za-z_]\\w*\\b|\\b\\d+(?:\\.\\d+)?\\b|\\b[A-Za-z_]\\w*(?=\\s*\\()");

    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    public static void apply(Spannable text, boolean light) {
        if (text == null) return;
        for (ForegroundColorSpan span : text.getSpans(0, text.length(), ForegroundColorSpan.class)) text.removeSpan(span);
        for (StyleSpan span : text.getSpans(0, text.length(), StyleSpan.class)) text.removeSpan(span);
        int comment = light ? 0xff69758a : 0xff91a2ba;
        int string = light ? 0xff087a68 : 0xff8ee7c8;
        int preprocessor = light ? 0xffa82d67 : 0xffffa3d0;
        int keyword = light ? 0xff265ec9 : 0xff9fc3ff;
        int type = light ? 0xff7b45b7 : 0xffd2b4ff;
        int number = light ? 0xff986000 : 0xffffd27a;
        int function = light ? 0xff086b86 : 0xff8de8ff;

        Matcher matcher = TOKEN.matcher(text);
        while (matcher.find()) {
            String token = matcher.group();
            int color;
            int style = Typeface.NORMAL;
            if (token.startsWith("//") || token.startsWith("/*")) {
                color = comment; style = Typeface.ITALIC;
            } else if (token.startsWith("\"") || token.startsWith("'")) {
                color = string;
            } else if (token.trim().startsWith("#")) {
                color = preprocessor;
            } else if (KEYWORDS.contains(token)) {
                color = keyword; style = Typeface.BOLD;
            } else if (TYPES.contains(token)) {
                color = type;
            } else if (NUMBER.matcher(token).matches()) {
                color = number;
            } else if (matcher.end() < text.length() && text.charAt(matcher.end()) == '(') {
                color = function;
            } else {
                continue;
            }
            text.setSpan(new ForegroundColorSpan(color), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (style != Typeface.NORMAL) text.setSpan(new StyleSpan(style), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
