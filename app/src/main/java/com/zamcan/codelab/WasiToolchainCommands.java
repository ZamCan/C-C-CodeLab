package com.zamcan.codelab;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builds explicit Clang/WASI commands without invoking a shell. */
public final class WasiToolchainCommands {
    private WasiToolchainCommands() {}
    public static List<String> compile(CompilerEngine.Request request, File clang,
            File clangCpp, File sysroot, File input, File output) {
        if (request == null || clang == null || clangCpp == null || sysroot == null
                || input == null || output == null)
            throw new IllegalArgumentException("Missing WASI compilation input");
        List<String> argv=new ArrayList<>();
        argv.add((request.language==CompilerEngine.Language.C?clang:clangCpp).getAbsolutePath());
        argv.add("--target=wasm32-wasip1");
        argv.add("--sysroot="+sysroot.getAbsolutePath());
        argv.add("-std="+request.standard);
        argv.add("-O0");
        argv.add("-fno-color-diagnostics");
        argv.add("-ferror-limit=20");
        argv.add("-Wl,-z,stack-size=1048576");
        argv.add(input.getAbsolutePath());
        argv.add("-o");
        argv.add(output.getAbsolutePath());
        return Collections.unmodifiableList(argv);
    }
}
