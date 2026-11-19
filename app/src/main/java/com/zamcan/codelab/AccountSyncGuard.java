package com.zamcan.codelab;

/**
 * Serializes progress synchronization and invalidates callbacks from an earlier learner.
 *
 * <p>A sync run is identified by a monotonically increasing token plus the verified owner it
 * started for. Every asynchronous continuation must call {@link #isCurrent(long, String)}
 * before touching local storage, so a late Appwrite response for account A can never write to
 * account B's database after a switch. The guard is deliberately free of Android types so the
 * account-switch race can be unit tested on the JVM.</p>
 */
public final class AccountSyncGuard {
    private long generation;
    private boolean running;
    private String owner;

    /**
     * Starts a run for an owner, or returns {@code 0} when a run is already in flight.
     * A zero token is never current, so callers must stop immediately.
     */
    public synchronized long begin(String owner) {
        if (running || owner == null || owner.isEmpty()) return 0;
        running = true;
        this.owner = owner;
        return ++generation;
    }

    /** True only while {@code token} is the newest run and {@code owner} still matches. */
    public synchronized boolean isCurrent(long token, String owner) {
        return running && token == generation && owner != null && owner.equals(this.owner);
    }

    /** Ends a run only if it is still the current one; stale completions are ignored. */
    public synchronized void finish(long token, String owner) {
        if (isCurrent(token, owner)) {
            running = false;
            this.owner = null;
        }
    }

    /** Abandons the active run and makes every outstanding token stale. */
    public synchronized void cancel() {
        generation++;
        running = false;
        owner = null;
    }

    public synchronized boolean isRunning() {
        return running;
    }
}
