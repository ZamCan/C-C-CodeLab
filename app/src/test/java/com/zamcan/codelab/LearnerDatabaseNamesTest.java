package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public final class LearnerDatabaseNamesTest {
    @Test public void differentAccountsUseDifferentDatabases() {
        String a=LearnerDatabaseNames.forVerifiedUser("user-A");
        String b=LearnerDatabaseNames.forVerifiedUser("user-B");
        assertNotEquals(a,b);
        assertEquals(a,LearnerDatabaseNames.forVerifiedUser("user-A"));
        assertNotEquals("learning.db",a);
        assertTrue(a.matches("learning-user-[0-9a-f]{64}\\.db"));
    }
    @Test public void cannotUsePathTraversalOrEmptyIdentity() {
        for(String id:new String[]{"","../guest","a/b","space id","a\\\\b"}) {
            try {LearnerDatabaseNames.forVerifiedUser(id);fail("Accepted "+id);}
            catch(IllegalArgumentException expected) {}
        }
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsMissingIdentity() {LearnerDatabaseNames.forVerifiedUser(null);}
    @Test(expected=IllegalArgumentException.class)
    public void rejectsExcessivelyLongIdentity() {LearnerDatabaseNames.forVerifiedUser("x".repeat(129));}
}
