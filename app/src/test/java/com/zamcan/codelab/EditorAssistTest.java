package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public final class EditorAssistTest {
    @Test public void commonLanguageAndLocalCompletions(){
        assertTrue(EditorAssist.suggestions("pri",3,false).contains("printf"));
        assertTrue(EditorAssist.suggestions("std::co",7,true).contains("std::cout"));
        assertTrue(EditorAssist.suggestions("std::co",7,false).isEmpty());
        String buffer="int balance=0; bal";
        assertTrue(EditorAssist.suggestions(buffer,buffer.length(),false).contains("balance"));
    }

    @Test public void checksNestedDelimiters(){
        assertEquals("Expected ] before )",EditorAssist.delimiterHint("f(a[0)").message);
        assertEquals(1,EditorAssist.delimiterHint("f(").offset);
        assertNull(EditorAssist.delimiterHint("int f(){return (2+3);}"));
    }

    @Test public void ignoresQuotedAndCommentedDelimiters(){
        assertNull(EditorAssist.delimiterHint("puts(\"[\"); /* ( */ // }"));
        assertNull(EditorAssist.delimiterHint("auto s=R\"tag(( { )tag\";"));
        assertNull(EditorAssist.delimiterHint("#define OPEN {\nint f(){return 1'000;}"));
        assertNull(EditorAssist.delimiterHint("#if X\nint a(){\n#else\nint a(){\n#endif\n}"));
    }

    @Test public void undoOneIndentOnlyOnAnOtherwiseEmptyLine(){
        assertEquals(5,EditorAssist.dedentStart("\n        }",9));
        assertEquals(1,EditorAssist.dedentStart("\n\t}",2));
        assertEquals(4,EditorAssist.dedentStart("if(x}",4));
    }
}
