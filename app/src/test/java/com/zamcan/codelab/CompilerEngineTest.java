package com.zamcan.codelab;

import org.junit.Test;
import java.util.Collections;
import static org.junit.Assert.*;

public class CompilerEngineTest {
    @Test public void languageSelection() {
        assertEquals(CompilerEngine.Language.C,CompilerEngine.languageFor("main.c"));
        assertEquals(CompilerEngine.Language.CPP,CompilerEngine.languageFor("main.cpp"));
        assertEquals(CompilerEngine.Language.CPP,CompilerEngine.languageFor("hello.CXX"));
    }
    @Test(expected=IllegalArgumentException.class)
    public void refusesHeaderAsExecutableSource() { CompilerEngine.languageFor("header.h"); }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsPathTraversal() {
        new CompilerEngine.Request("../private.c","int main(void){return 0;}",CompilerEngine.Language.C);
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsFakeSuccessWithoutArtifact() {
        new CompilerEngine.CompileResult(true,0,"","",Collections.emptyList(),null);
    }
    @Test public void languageStandardIsExplicit() {
        CompilerEngine.Request c=new CompilerEngine.Request("main.c","int main(void){return 0;}",CompilerEngine.Language.C);
        CompilerEngine.Request cpp=new CompilerEngine.Request("main.cpp","int main(){return 0;}",CompilerEngine.Language.CPP);
        assertEquals("c17",c.standard);
        assertEquals("c++17",cpp.standard);
    }
}
