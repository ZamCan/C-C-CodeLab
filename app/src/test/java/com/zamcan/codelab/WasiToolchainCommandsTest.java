package com.zamcan.codelab;

import org.junit.Test;
import java.io.File;
import java.util.List;
import static org.junit.Assert.*;

public class WasiToolchainCommandsTest {
    @Test public void c17TargetsWasi() {
        CompilerEngine.Request request=new CompilerEngine.Request("main.c","int main(void){return 0;}",CompilerEngine.Language.C);
        List<String> args=WasiToolchainCommands.compile(request,new File("/tool/clang"),
                new File("/tool/clang++"),new File("/sysroot"),new File("/src/main.c"),new File("/out/main.wasm"));
        assertEquals("/tool/clang",args.get(0));
        assertTrue(args.contains("--target=wasm32-wasip1"));
        assertTrue(args.contains("-std=c17"));
        assertEquals("/out/main.wasm",args.get(args.size()-1));
    }
    @Test public void cpp17UsesCxxDriverForLinking() {
        CompilerEngine.Request request=new CompilerEngine.Request("main.cpp","#include <iostream>",CompilerEngine.Language.CPP);
        List<String> args=WasiToolchainCommands.compile(request,new File("/tool/clang"),
                new File("/tool/clang++"),new File("/sysroot"),new File("/src/main.cpp"),new File("/out/main.wasm"));
        assertEquals("/tool/clang++",args.get(0));
        assertTrue(args.contains("-std=c++17"));
    }
}
