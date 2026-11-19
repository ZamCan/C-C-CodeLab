package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

/** Deterministic cloud identity contract: guest and account records never alias. */
public final class AccountIsolationTest {
    @Test public void independentGoogleAccountsNeverShareProgressRow() {
        for(String course:new String[]{"c-foundation","cpp-foundation"}) {
            for(int lesson=0;lesson<10;lesson++) {
                String key=Integer.toString(lesson);
                String first=ProgressRowIds.forLesson("accountA",course,key);
                String second=ProgressRowIds.forLesson("accountB",course,key);
                assertNotEquals(first,second);
                assertEquals(first,ProgressRowIds.forLesson("accountA",course,key));
                assertTrue(first.length()<=36);
            }
        }
    }
    @Test public void privateDatabaseNamesAreDeterministicAndDistinct() {
        String first=LearnerDatabaseNames.forVerifiedUser("accountA");
        String second=LearnerDatabaseNames.forVerifiedUser("accountB");
        assertNotEquals(first,second);
        assertEquals(first,LearnerDatabaseNames.forVerifiedUser("accountA"));
        assertFalse(first.equals("learning.db"));
    }
}
