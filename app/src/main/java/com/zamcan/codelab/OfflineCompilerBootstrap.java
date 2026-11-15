package com.zamcan.codelab;

import android.content.Context;
import java.io.File;

/**
 * Fails closed until an audited Android-hosted toolchain and runner are
 * provisioned. Never silently substitutes host binaries or simulated output.
 */
public final class OfflineCompilerBootstrap {
    private OfflineCompilerBootstrap() {}
    public static CompilerEngine create(Context context) {
        File root=new File(context.getApplicationInfo().nativeLibraryDir);
        File sysroot=new File(context.getFilesDir(),"wasi-sysroot");
        // No verified Android Clang/WASI/WAMR distribution is bundled yet.
        // These flags must only be enabled after binary integrity and isolation audits.
        return new WasiCompilerEngine(context,
                new File(root,"libclang_host.so"),
                new File(root,"libclangxx_host.so"),
                sysroot,
                new File(root,"libiwasm_host.so"),
                false,false);
    }
}
