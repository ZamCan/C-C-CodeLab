package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

/** Account switching must make every in-flight callback for the old learner stale. */
public final class AccountSyncGuardTest {
    @Test public void onlyOneRunIsActivePerSync() {
        AccountSyncGuard guard = new AccountSyncGuard();
        long token = guard.begin("accountA");
        assertTrue(token > 0);
        assertTrue(guard.isCurrent(token, "accountA"));
        assertEquals(0, guard.begin("accountB"));
        assertTrue(guard.isCurrent(token, "accountA"));
    }

    @Test public void switchingAccountsInvalidatesTheEarlierCallback() {
        AccountSyncGuard guard = new AccountSyncGuard();
        long first = guard.begin("accountA");
        guard.cancel();
        long second = guard.begin("accountB");
        assertFalse(guard.isCurrent(first, "accountA"));
        assertTrue(guard.isCurrent(second, "accountB"));
        // A stale completion must not stop the newer run.
        guard.finish(first, "accountA");
        assertTrue(guard.isCurrent(second, "accountB"));
    }

    @Test public void ownerMismatchIsNeverCurrent() {
        AccountSyncGuard guard = new AccountSyncGuard();
        long token = guard.begin("accountA");
        assertFalse(guard.isCurrent(token, "accountB"));
        assertFalse(guard.isCurrent(token, null));
        guard.finish(token, "accountB");
        assertTrue(guard.isRunning());
    }

    @Test public void finishingReleasesTheGuardForTheNextRun() {
        AccountSyncGuard guard = new AccountSyncGuard();
        long first = guard.begin("accountA");
        guard.finish(first, "accountA");
        assertFalse(guard.isRunning());
        long second = guard.begin("accountA");
        assertTrue(second > first);
        assertTrue(guard.isCurrent(second, "accountA"));
    }

    @Test public void rejectsMissingOwner() {
        AccountSyncGuard guard = new AccountSyncGuard();
        assertEquals(0, guard.begin(null));
        assertEquals(0, guard.begin(""));
        assertFalse(guard.isRunning());
    }

    @Test public void guestScopeIsNotInterchangeableWithAnAccount() {
        // LearningSync refuses guest stores before reaching the guard; the guard still
        // guarantees that a token minted for an account can never be applied to guest scope.
        AccountSyncGuard guard = new AccountSyncGuard();
        long token = guard.begin("accountA");
        assertFalse(guard.isCurrent(token, "guest"));
        guard.finish(token, "guest");
        assertTrue(guard.isRunning());
    }
}
