package com.zamcan.codelab;
import org.junit.Test;
import static org.junit.Assert.*;
public class EditorRulesTest {
    @Test public void openingBraceAddsFourSpaces(){assertEquals("        ",EditorRules.indentAfter("    if (ready) {\n",16));}
    @Test public void normalLineKeepsIndent(){String source="    return 0;\n";assertEquals("    ",EditorRules.indentAfter(source,source.length()-1));}
    @Test public void invalidOffsetDoesNotChangeText(){assertEquals("",EditorRules.indentAfter("abc",3));}
}
