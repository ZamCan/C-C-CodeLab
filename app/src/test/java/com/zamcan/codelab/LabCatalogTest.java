package com.zamcan.codelab;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LabCatalogTest {
    @Test public void mathLabsAreCompleteAndRunnable() {
        List<LabCatalog.Lab> labs = LabCatalog.math();
        assertTrue(labs.size() >= 9);
        Set<String> titles = new HashSet<>();
        for (LabCatalog.Lab lab : labs) {
            assertTrue(titles.add(lab.title));
            assertFalse(lab.summary.isEmpty());
            assertTrue(lab.source.contains("main("));
            assertFalse(lab.source.contains("\r"));
        }
    }

    @Test public void codeLabsCoverBothLanguages() {
        List<LabCatalog.Lab> labs = LabCatalog.code();
        assertTrue(labs.size() >= 10);
        boolean c = false, cpp = false;
        for (LabCatalog.Lab lab : labs) {
            assertTrue(lab.source.contains("main("));
            if (lab.cpp) cpp = true;
            else c = true;
        }
        assertTrue(c);
        assertTrue(cpp);
    }
}
