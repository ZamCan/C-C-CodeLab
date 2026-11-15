package com.zamcan.codelab;

import android.content.Context;
import android.os.Looper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Offline WASI compiler and interpreter adapter.
 * The host must supply verified, executable Android-hosted clang/clang++/iwasm
 * and a matching WASI sysroot. It does not download or extract executables.
 * Not enabled in production until the interpreter's isolation is audited.
 */
public final class WasiCompilerEngine implements CompilerEngine {
    private static final int MAX_OUTPUT=128*1024;
    private final File clang,clangCpp,sysroot,iwasm,workspace;
    private final boolean verified,isolatedRunner;
    private final Map<String,File> artifacts=new LinkedHashMap<>();
    private static final int MAX_ARTIFACTS=20;

    public WasiCompilerEngine(Context context,File clang,File clangCpp,File sysroot,
                              File iwasm,boolean verified,boolean isolatedRunner) {
        this.clang=clang;this.clangCpp=clangCpp;this.sysroot=sysroot;this.iwasm=iwasm;
        this.verified=verified;this.isolatedRunner=isolatedRunner;
        workspace=new File(context.getCacheDir(),"wasi-sessions");
    }
    @Override public boolean available() {
        return verified && isolatedRunner && executable(clang) && executable(clangCpp)
                && executable(iwasm) && sysroot!=null && sysroot.isDirectory();
    }
    private static boolean executable(File f) { return f!=null && f.isFile() && f.canExecute(); }
    private static void workerThread() {
        if (Looper.myLooper()==Looper.getMainLooper())
            throw new IllegalStateException("Compile and Run must not block the UI thread");
    }
    @Override public synchronized CompileResult compile(Request request) throws Exception {
        workerThread();
        if (!available()) throw new IllegalStateException("Verified offline WASI toolchain and isolated runtime unavailable");
        if (request==null) throw new IllegalArgumentException("Missing compile request");
        if (!workspace.isDirectory() && !workspace.mkdirs()) throw new IOException("Cannot create workspace");
        File session=new File(workspace,UUID.randomUUID().toString());
        if (!session.mkdir()) throw new IOException("Cannot create compile session");
        File source=new File(session,request.filename);
        File wasm=new File(session,"program.wasm");
        Files.write(source.toPath(),request.source.getBytes(StandardCharsets.UTF_8));
        Execution result=execute(WasiToolchainCommands.compile(request,clang,clangCpp,sysroot,source,wasm),
                session,"",20000);
        boolean ok=!result.timedOut && result.exitCode==0 && wasm.isFile() && wasm.length()>0;
        String id=null;
        if(ok) {
            id=UUID.randomUUID().toString();
            if(artifacts.size()>=MAX_ARTIFACTS) {
                String oldest=artifacts.keySet().iterator().next();
                File stale=artifacts.remove(oldest);
                if(stale!=null) { stale.delete(); File dir=stale.getParentFile(); if(dir!=null)dir.delete(); }
            }
            artifacts.put(id,wasm);
        } else {
            source.delete();wasm.delete();session.delete();
        }
        String stderr=result.stderr+(result.timedOut?"\nCompilation timed out.":"");
        return new CompileResult(ok,result.exitCode,result.stdout,stderr,ClangDiagnostics.parse(stderr),id);
    }
    @Override public synchronized RunResult run(String artifactId,String stdin,long timeoutMillis) throws Exception {
        workerThread();
        if (!available()) throw new IllegalStateException("Verified isolated WASI runtime unavailable");
        File wasm=artifacts.get(artifactId);
        if (wasm==null || !wasm.isFile()) throw new IllegalArgumentException("Unknown compiled artifact");
        if(timeoutMillis<1 || timeoutMillis>30000) throw new IllegalArgumentException("Invalid timeout");
        if(stdin==null)stdin="";
        if(stdin.getBytes(StandardCharsets.UTF_8).length>65536)
            throw new IllegalArgumentException("Standard input exceeds 64 KiB");
        Execution e=execute(Arrays.asList(iwasm.getAbsolutePath(),wasm.getAbsolutePath()),
                wasm.getParentFile(),stdin,timeoutMillis);
        return new RunResult(e.stdout,e.stderr,e.exitCode,e.timedOut);
    }
    private static final class Execution {
        String stdout="",stderr="";
        int exitCode=-1;
        boolean timedOut;
    }
    private static Thread drain(InputStream stream,ByteArrayOutputStream out) {
        Thread t=new Thread(()->{
            try(InputStream input=stream) {
                byte[] b=new byte[4096];int n;
                while((n=input.read(b))!=-1) {
                    synchronized(out) { int room=MAX_OUTPUT-out.size();if(room>0)out.write(b,0,Math.min(n,room)); }
                }
            }catch(IOException ignored){}
        },"wasi-stream-drain");
        t.setDaemon(true);t.start();return t;
    }
    private static Execution execute(List<String> args,File directory,String stdin,long timeout) throws Exception {
        Process process=new ProcessBuilder(args).directory(directory).start();
        ByteArrayOutputStream out=new ByteArrayOutputStream(),err=new ByteArrayOutputStream();
        Thread stdout=drain(process.getInputStream(),out),stderr=drain(process.getErrorStream(),err);
        byte[] input=stdin.getBytes(StandardCharsets.UTF_8);
        Thread writer=new Thread(()->{
            try(OutputStream pipe=process.getOutputStream()){pipe.write(input);pipe.flush();}
            catch(IOException ignored){}
        },"wasi-stdin");
        writer.setDaemon(true);writer.start();
        Execution result=new Execution();
        try {
            if(!process.waitFor(timeout,TimeUnit.MILLISECONDS)) {
                result.timedOut=true;process.destroyForcibly();process.waitFor();
            }
            result.exitCode=result.timedOut?-1:process.exitValue();
            stdout.join(2000);stderr.join(2000);writer.join(1000);
            result.stdout=out.toString("UTF-8");result.stderr=err.toString("UTF-8");
            return result;
        } catch(InterruptedException e) {
            process.destroyForcibly();Thread.currentThread().interrupt();throw e;
        } finally { if(process.isAlive())process.destroyForcibly(); }
    }
}
