package com.zamcan.codelab;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Academic-note typography without changing the authored characters. */
public final class RichText {
    private RichText() {}

    private static final Pattern INLINE_CODE = Pattern.compile(
            "(?<![A-Za-z0-9_])(?:#include|#define|printf|scanf|fgets|fopen|fclose|"
            + "std::[A-Za-z_]+|main|return|int|double|float|char|void|bool|if|else|for|while|"
            + "switch|case|break|continue|class|struct|public|private|const|constexpr|auto|"
            + "namespace|using|template|vector|string|size_t|NULL|nullptr)(?![A-Za-z0-9_])");

    public static CharSequence format(String source, boolean light, boolean code) {
        SpannableString text = new SpannableString(source == null ? "" : source);
        if (code) { SyntaxHighlighter.apply(text, light); return text; }
        int accent = light ? 0xff0e8f7d : 0xff7ee7f5;
        int codeColor = light ? 0xff086b86 : 0xff8de8ff;
        int headingColor = light ? 0xff182a4b : 0xfff3f7ff;

        int start = 0;
        String value = text.toString();
        while (start < text.length()) {
            int end = value.indexOf('\n', start);
            if (end < 0) end = value.length();
            String raw = value.substring(start, end);
            String line = raw.trim();
            if (isHeading(line)) {
                text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new ForegroundColorSpan(headingColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new RelativeSizeSpan(1.05f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                int accentStart = start + raw.indexOf(line);
                if (accentStart >= start && accentStart < end) {
                    text.setSpan(new ForegroundColorSpan(accent), accentStart,
                            Math.min(end, accentStart + Math.min(line.length(), 42)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (line.startsWith("- ") || line.startsWith("• ")) {
                int bullet = start + raw.indexOf(line);
                text.setSpan(new ForegroundColorSpan(accent), bullet, Math.min(end, bullet + 2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            start = end + 1;
        }

        Matcher matcher = INLINE_CODE.matcher(text);
        while (matcher.find()) {
            text.setSpan(new TypefaceSpan("monospace"), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(codeColor), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    private static boolean isHeading(String line) {
        if (line.isEmpty() || line.length() > 96) return false;
        if (line.startsWith("#") || line.startsWith("//") || line.endsWith(";")
                || line.endsWith("{") || line.endsWith("}")) return false;
        if (line.matches("^\\d+[.)]\\s+.*")) return true;
        if (line.endsWith(":") && line.split("\\s+").length <= 10) return true;
        return line.equals(line.toUpperCase(Locale.ROOT)) && line.matches(".*[A-Z].*");
    }
}
