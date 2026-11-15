package com.zamcan.codelab;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Optional native Clang adapter. It NEVER treats a desktop NDK compiler as an
 * Android executable. Packaging a real Android-hosted clang + resource directory
 * is a separate, mandatory release requirement.
 *
 * Only compile-to-object is supported here. Running student binaries directly
 * would not provide the promised sandbox, so run() fails closed.
 */
public final class NativeClangEngine implements CompilerEngine {
    private final File compiler;
    private final File workspace;
    private final File resourceDirectory;
    private final boolean verifiedToolchain;

    public NativeClangEngine(Context context, File compiler, File resourceDirectory,
                             boolean verifiedToolchain) {
        this.compiler=compiler;
        this.resourceDirectory=resourceDirectory;
        this.workspace=new File(context.getCacheDir(),"compiler-workspace");
        this.verifiedToolchain=verifiedToolchain;
    }

    @Override public boolean available() {
        return verifiedToolchain && compiler != null && compiler.isFile()
            && compiler.canExecute() && resourceDirectory != null
            && resourceDirectory.isDirectory();
    }

    @Override public CompileResult compile(Request request) throws Exception {
        if (!available()) throw new IllegalStateException(
            "Android-hosted Clang and its resource directory are not installed or verified");
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper())
            throw new IllegalStateException("Compilation must run off the UI thread");
        if (!workspace.isDirectory() && !workspace.mkdirs())
            throw new IOException("Cannot create private compiler workspace");
        File session = new File(workspace,UUID.randomUUID().toString());
        if (!session.mkdir()) throw new IOException("Cannot create compile session");
        File source = new File(session,request.filename);
        File object = new File(session,"output.o");
        Files.write(source.toPath(),request.source.getBytes(StandardCharsets.UTF_8));
        List<String> args=new ArrayList<>();
        args.add(compiler.getAbsolutePath());
        args.add("-x");
        args.add(request.language==Language.C ? "c" : "c++");
        args.add("-std="+request.standard);
        args.add("-fsyntax-only");
        args.add("-fno-color-diagnostics");
        args.add("-ferror-limit=20");
        args.add("-resource-dir");
        args.add(resourceDirectory.getAbsolutePath());
        args.add(source.getAbsolutePath());
        Process process=new ProcessBuilder(args).directory(session).start();
        StringBuilder output=new StringBuilder(),errors=new StringBuilder();
        Thread stdout=drain(process.getInputStream(),output);
        Thread stderr=drain(process.getErrorStream(),errors);
        boolean finished=process.waitFor(15,TimeUnit.SECONDS);
        if (!finished) process.destroyForcibly();
        process.waitFor();
        stdout.join(1000);stderr.join(1000);
        int exit=finished ? process.exitValue() : -1;
        String err=errors.toString();
        if (!finished) err += "\nCompilation timed out after 15 seconds.";
        // Syntax-only checks produce no executable artifact: never report a
        // successful CompileResult, even if Clang exited zero.
        return new CompileResult(false,exit,output.toString(),err,
            ClangDiagnostics.parse(err),null);
    }

    private static Thread drain(java.io.InputStream input,StringBuilder output) {
        Thread thread=new Thread(()->{
            byte[] buffer=new byte[4096];
            try (java.io.InputStream stream=input) {
                int n;
                while ((n=stream.read(buffer))!=-1) {
                    synchronized(output) {
                        if (output.length()<131072)
                            output.append(new String(buffer,0,Math.min(n,131072-output.length()),
                                StandardCharsets.UTF_8));
                    }
                }
            } catch(IOException ignored) {}
        },"clang-output");
        thread.setDaemon(true);thread.start();
        return thread;
    }

    @Override public RunResult run(String artifactId,String stdin,long timeoutMillis) {
        throw new UnsupportedOperationException(
            "Native student execution is disabled until an isolated Android runner is verified");
    }
}
