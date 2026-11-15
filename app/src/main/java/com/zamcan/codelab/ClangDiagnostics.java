package com.zamcan.codelab;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses actual Clang diagnostic lines without inventing messages or compiler results. */
public final class ClangDiagnostics {
    private static final Pattern LINE = Pattern.compile("^(.+?):([0-9]+):([0-9]+):\\s*(fatal error|error|warning|note):\\s*(.*)$");
    private ClangDiagnostics() {}

    public static List<CompilerEngine.Diagnostic> parse(String stderr) {
        List<CompilerEngine.Diagnostic> result = new ArrayList<>();
        if (stderr == null || stderr.isEmpty()) return result;
        for (String line : stderr.split("\\r?\\n")) {
            Matcher match = LINE.matcher(line);
            if (!match.matches()) continue;
            try {
                int row = Integer.parseInt(match.group(2));
                int column = Integer.parseInt(match.group(3));
                if (row < 1 || column < 1) continue;
                result.add(new CompilerEngine.Diagnostic(match.group(1), row, column,
                    match.group(4), match.group(5)));
            } catch (NumberFormatException ignored) {
                // Keep original stderr visible; skip diagnostics with out-of-range positions.
            }
        }
        return result;
    }
}
