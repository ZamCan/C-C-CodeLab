package com.zamcan.codelab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Honest compilation boundary. An Android-compatible toolchain and isolated runner must
 * implement this contract; neither host clang nor canned console text is accepted.
 */
public interface CompilerEngine {
    enum Language { C, CPP }

    final class Request {
        public final String filename;
        public final String source;
        public final Language language;
        public final String standard;
        public Request(String filename, String source, Language language) {
            if (filename == null || !filename.matches("[A-Za-z0-9_.-]{1,128}") ||
                    filename.startsWith(".") || filename.contains(".."))
                throw new IllegalArgumentException("Invalid source filename");
            if (source == null) throw new IllegalArgumentException("Source is required");
            if (source.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 256 * 1024)
                throw new IllegalArgumentException("Source exceeds 256 KiB");
            if (language == null) throw new IllegalArgumentException("Language is required");
            this.filename = filename;
            this.source = source;
            this.language = language;
            this.standard = language == Language.C ? "c17" : "c++17";
        }
    }

    final class Diagnostic {
        public final String filename;
        public final int line;
        public final int column;
        public final String severity;
        public final String message;
        public Diagnostic(String filename, int line, int column, String severity, String message) {
            this.filename=filename;this.line=line;this.column=column;
            this.severity=severity;this.message=message;
        }
    }

    final class CompileResult {
        public final boolean success;
        public final int exitCode;
        public final String stdout;
        public final String stderr;
        public final List<Diagnostic> diagnostics;
        /** Opaque artifact token. Never expose arbitrary file paths from student input. */
        public final String artifactId;
        public CompileResult(boolean success,int exitCode,String stdout,String stderr,
                             List<Diagnostic> diagnostics,String artifactId) {
            if (success && (exitCode != 0 || artifactId == null || artifactId.isEmpty()))
                throw new IllegalArgumentException("Successful compile requires artifact and zero exit code");
            this.success=success;this.exitCode=exitCode;this.stdout=stdout;this.stderr=stderr;
            this.diagnostics=Collections.unmodifiableList(new ArrayList<>(diagnostics));
            this.artifactId=artifactId;
        }
    }

    final class RunResult {
        public final String stdout,stderr;
        public final int exitCode;
        public final boolean timedOut;
        public RunResult(String stdout,String stderr,int exitCode,boolean timedOut) {
            this.stdout=stdout;this.stderr=stderr;this.exitCode=exitCode;this.timedOut=timedOut;
        }
    }

    /** Must be true only after toolchain integrity, platform support and runner isolation checks. */
    boolean available();
    /** Implementations must execute off the UI thread and report original toolchain diagnostics. */
    CompileResult compile(Request request) throws Exception;
    /** Execute untrusted code only in a separately verified isolation boundary. */
    RunResult run(String artifactId,String stdin,long timeoutMillis) throws Exception;

    static Language languageFor(String filename) {
        if (filename == null) throw new IllegalArgumentException("Missing filename");
        String lower=filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".c")) return Language.C;
        if (lower.endsWith(".cpp") || lower.endsWith(".cc") || lower.endsWith(".cxx"))
            return Language.CPP;
        throw new IllegalArgumentException("Expected a C or C++ source file");
    }
}
