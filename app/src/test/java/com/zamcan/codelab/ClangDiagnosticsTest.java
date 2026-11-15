package com.zamcan.codelab;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class ClangDiagnosticsTest {
    @Test public void parsesRealClangFormat() {
        String stderr="main.c:3:5: error: use of undeclared identifier 'x'\\n"
            +"    x = 2;\\n"
            +"    ^\\n"
            +"main.c:1:1: warning: unused variable 'y'\\n";
        List<CompilerEngine.Diagnostic> d=ClangDiagnostics.parse(stderr);
        assertEquals(2,d.size());
        assertEquals("main.c",d.get(0).filename);
        assertEquals(3,d.get(0).line);
        assertEquals(5,d.get(0).column);
        assertEquals("error",d.get(0).severity);
        assertEquals("warning",d.get(1).severity);
    }
    @Test public void ignoresUnstructuredOutputWithoutInventingErrors() {
        assertTrue(ClangDiagnostics.parse("linker failed\\n").isEmpty());
    }
}
