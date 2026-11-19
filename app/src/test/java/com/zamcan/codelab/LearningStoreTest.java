package com.zamcan.codelab;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/**
 * Runs the real SQLiteOpenHelper against the Android SQLite implementation.
 * These cover offline writes, the schema-1 to schema-2 migration, queue ownership and the
 * monotonic merge that must never erase a completed lesson.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public final class LearningStoreTest {
    private Context context;

    @Before public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("learning.db");
        context.deleteDatabase(LearnerDatabaseNames.forVerifiedUser("userA"));
        context.deleteDatabase(LearnerDatabaseNames.forVerifiedUser("userB"));
    }

    @Test public void guestCompletionSurvivesReopenAndNeverQueuesUpload() throws Exception {
        LearningStore store = new LearningStore(context);
        store.complete("c-foundation", "0");
        store.close();

        LearningStore reopened = new LearningStore(context);
        try {
            assertTrue(reopened.isGuest());
            assertTrue(reopened.completed("c-foundation", "0"));
            assertEquals(0, reopened.pendingUploads().length());
        } finally {
            reopened.close();
        }
    }

    @Test public void accountQueueIsDrainedOnlyAfterMatchingAcknowledgement() throws Exception {
        LearningStore store = new LearningStore(context, "userA");
        try {
            store.complete("c-foundation", "1");
            JSONArray pending = store.pendingUploads();
            assertEquals(1, pending.length());

            long completedAt = pending.getJSONObject(0).getLong("completed_at");
            try {
                store.acknowledgeUpload("userB", "c-foundation", "1", completedAt);
                fail("another learner must not acknowledge this queue");
            } catch (SecurityException expected) { /* ownership enforced */ }
            assertEquals("queue must survive a foreign acknowledgement", 1, store.pendingUploads().length());

            store.acknowledgeUpload("userA", "c-foundation", "1", completedAt + 1);
            assertEquals("stale acknowledgement must not clear newer work", 1, store.pendingUploads().length());

            store.acknowledgeUpload("userA", "c-foundation", "1", completedAt);
            assertEquals(0, store.pendingUploads().length());
            assertTrue(store.completed("c-foundation", "1"));
        } finally {
            store.close();
        }
    }

    @Test public void mergeKeepsEarliestCompletionAndNeverErasesLocalWork() throws Exception {
        LearningStore store = new LearningStore(context, "userA");
        try {
            store.complete("c-foundation", "2");
            long local = store.pendingUploads().getJSONObject(0).getLong("completed_at");

            JSONObject snapshot = new JSONObject().put("schema", 1).put("progress",
                    new JSONArray().put(new JSONObject()
                            .put("course", "c-foundation").put("lesson", "2")
                            .put("completed_at", local + 60000)));
            store.mergeProgress(snapshot);
            assertTrue(store.completed("c-foundation", "2"));

            JSONObject earlier = new JSONObject().put("schema", 1).put("progress",
                    new JSONArray().put(new JSONObject()
                            .put("course", "c-foundation").put("lesson", "2")
                            .put("completed_at", local - 60000))
                            .put(new JSONObject()
                            .put("course", "c-foundation").put("lesson", "3")
                            .put("completed_at", local)));
            store.mergeProgress(earlier);
            assertTrue(store.completed("c-foundation", "2"));
            assertTrue(store.completed("c-foundation", "3"));
            assertEquals("remote-only lessons are not re-uploaded", 1, store.pendingUploads().length());
        } finally {
            store.close();
        }
    }

    @Test public void mergeRejectsMalformedSnapshots() throws Exception {
        LearningStore store = new LearningStore(context);
        try {
            for (JSONObject bad : new JSONObject[]{
                    new JSONObject().put("schema", 2).put("progress", new JSONArray()),
                    new JSONObject().put("schema", 1).put("progress", new JSONArray()
                            .put(new JSONObject().put("course", "../escape").put("lesson", "0")
                                    .put("completed_at", 1))),
                    new JSONObject().put("schema", 1).put("progress", new JSONArray()
                            .put(new JSONObject().put("course", "c-foundation").put("lesson", "0")
                                    .put("completed_at", -5)))}) {
                try {
                    store.mergeProgress(bad);
                    fail("accepted invalid snapshot: " + bad);
                } catch (org.json.JSONException expected) { /* rejected */ }
            }
            assertFalse(store.completed("c-foundation", "0"));
        } finally {
            store.close();
        }
    }

    @Test     public void schemaOneDatabaseMigratesAndQueuesExistingProgress() throws Exception {
        String database = LearnerDatabaseNames.forVerifiedUser("userA");
        File file = context.getDatabasePath(database);
        File parent = file.getParentFile();
        if (parent != null) assertTrue(parent.isDirectory() || parent.mkdirs());
        SQLiteDatabase legacy = SQLiteDatabase.openOrCreateDatabase(file, null);
        legacy.execSQL("CREATE TABLE progress(course TEXT NOT NULL, lesson TEXT NOT NULL, "
                + "completed_at INTEGER NOT NULL, PRIMARY KEY(course,lesson))");
        legacy.execSQL("CREATE TABLE metadata(name TEXT PRIMARY KEY, value TEXT NOT NULL)");
        legacy.execSQL("INSERT INTO progress VALUES('cpp-foundation','4',1700000000000)");
        legacy.setVersion(1);
        legacy.close();

        LearningStore migrated = new LearningStore(context, "userA");
        try {
            assertTrue("existing completion preserved", migrated.completed("cpp-foundation", "4"));
            JSONArray pending = migrated.pendingUploads();
            assertEquals("migration queues pre-existing account progress", 1, pending.length());
            assertEquals("cpp-foundation", pending.getJSONObject(0).getString("course"));
        } finally {
            migrated.close();
        }
    }

    @Test     public void legacyGuestImportIsRefusedForAccountStorageAndRunsOnceForGuest() throws Exception {
        SharedPreferences preferences = context.getSharedPreferences("academy", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("c-0", true).putBoolean("cpp-1", true).apply();

        try {
            new LearningStore(context, "userB").importLegacy(preferences);
            fail("guest flags must never be imported into an account automatically");
        } catch (SecurityException expected) { /* fail closed */ }

        LearningStore guest = new LearningStore(context);
        try {
            guest.importLegacy(preferences);
            assertTrue(guest.completed("c-foundation", "0"));
            assertTrue(guest.completed("cpp-foundation", "1"));
            // Second run is idempotent and does not duplicate or extend progress.
            guest.importLegacy(preferences);
            assertEquals(2, guest.exportProgress().getJSONArray("progress").length());
        } finally {
            guest.close();
        }
    }

    @Test public void explicitGuestCopyQueuesOnlyForTheVerifiedOwner() throws Exception {
        LearningStore guest = new LearningStore(context);
        guest.complete("c-foundation", "5");
        JSONObject snapshot = guest.exportProgress();
        guest.close();

        LearningStore account = new LearningStore(context, "userA");
        try {
            assertTrue(account.pendingUploads().length() == 0);
            account.mergeProgress(snapshot);
            account.enqueueAllForUpload("userA");
            assertEquals(1, account.pendingUploads().length());
            try {
                account.enqueueAllForUpload("userB");
                fail("another account must not queue this store");
            } catch (SecurityException expected) { /* ownership enforced */ }
        } finally {
            account.close();
        }

        LearningStore otherAccount = new LearningStore(context, "userB");
        try {
            assertFalse("accounts never share a database", otherAccount.completed("c-foundation", "5"));
        } finally {
            otherAccount.close();
        }
    }
}
