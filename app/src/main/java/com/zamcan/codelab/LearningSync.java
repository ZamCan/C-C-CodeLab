package com.zamcan.codelab;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import io.appwrite.Query;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Row;
import io.appwrite.models.RowList;
import io.appwrite.services.TablesDB;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Private, merge-only progress synchronization for one verified Appwrite account.
 *
 * <p>Ordering guarantees:</p>
 * <ol>
 *   <li>The server session is verified before any cloud call.</li>
 *   <li>The verified user ID must equal the local store's owner scope.</li>
 *   <li>Every asynchronous callback re-checks an {@link AccountSyncGuard} token, so a late
 *       response from a previous account can never touch the active learner's database.</li>
 *   <li>Queued work is acknowledged only after a confirmed remote write.</li>
 *   <li>Guest progress is never uploaded and has no queue.</li>
 * </ol>
 */
public final class LearningSync {
    static final String DATABASE = "codelab-learning";
    static final String TABLE = "lesson-progress";
    /** Small, bounded retry budget; retries never duplicate rows because IDs are stable. */
    private static final int MAX_TRANSIENT_ATTEMPTS = 3;
    private static final long RETRY_STEP_MILLIS = 1500L;
    private static final int PAGE_SIZE = 100;

    private final TablesDB tables;
    private final CodeLabAccount identity;
    private final AccountSyncGuard guard = new AccountSyncGuard();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int transientAttempts;

    public LearningSync(Context context) {
        identity = CodeLabAccount.shared(context);
        tables = new TablesDB(identity.client());
    }

    /** Invalidates all callbacks from an earlier account or an abandoned sync. */
    public void cancel() {
        guard.cancel();
        handler.removeCallbacksAndMessages(null);
        transientAttempts = 0;
    }

    /** Verifies the session, then drains the queue and merges this account's cloud progress. */
    public void sync(final LearningStore store) {
        if (store == null || store.isGuest()) return;
        final String owner = store.ownerScope();
        final long token = guard.begin(owner);
        if (token == 0) return; // A run is already in flight.
        transientAttempts = 0;
        identity.verify(new CodeLabAccount.Result() {
            @Override public void authenticated(String verifiedUserId) {
                if (!guard.isCurrent(token, owner)) return;
                if (!owner.equals(verifiedUserId)) {
                    guard.finish(token, owner);
                    return;
                }
                upload(store, owner, token);
            }

            @Override public void unauthenticated(String reason) {
                guard.finish(token, owner);
            }

            @Override public void unavailable(String reason) {
                guard.finish(token, owner);
            }
        });
    }

    private void upload(LearningStore store, String owner, long token) {
        if (!guard.isCurrent(token, owner)) return;
        final JSONArray pending;
        try {
            pending = store.pendingUploads();
        } catch (Exception e) {
            guard.finish(token, owner);
            return;
        }
        if (pending.length() == 0) {
            download(store, owner, token, null);
            return;
        }
        JSONObject item = pending.optJSONObject(0);
        if (item == null) {
            guard.finish(token, owner);
            return;
        }
        String course = item.optString("course");
        String lesson = item.optString("lesson");
        long completedAt = item.optLong("completed_at");
        final String rowId;
        try {
            rowId = ProgressRowIds.forLesson(owner, course, lesson);
        } catch (Exception e) {
            guard.finish(token, owner);
            return;
        }
        Map<String, Object> data = payload(course, lesson, completedAt);
        List<String> permissions = Arrays.asList(
                "read(\"user:" + owner + "\")",
                "update(\"user:" + owner + "\")",
                "delete(\"user:" + owner + "\")");
        try {
            tables.createRow(DATABASE, TABLE, rowId, data, permissions,
                    new CoroutineCallback<>((created, error) -> {
                if (!guard.isCurrent(token, owner)) return;
                if (error == null) {
                    acknowledge(store, owner, token, course, lesson, completedAt);
                    return;
                }
                resolveExistingRow(store, owner, token, rowId, data, course, lesson,
                        completedAt, error);
            }));
        } catch (AppwriteException e) {
            retryOrStop(store, owner, token, e);
        }
    }

    /**
     * A create usually fails with 409 when an earlier upload succeeded but its acknowledgement
     * was lost. The row is read under the current user's permissions and merged by earliest
     * completion time instead of being overwritten blindly.
     */
    private void resolveExistingRow(LearningStore store, String owner, long token, String rowId,
                                    Map<String, Object> data, String course, String lesson,
                                    long completedAt, Throwable createError) {
        try {
            tables.getRow(DATABASE, TABLE, rowId, new CoroutineCallback<>((existing, readError) -> {
                if (!guard.isCurrent(token, owner)) return;
                if (readError != null || existing == null) {
                    retryOrStop(store, owner, token, readError != null ? readError : createError);
                    return;
                }
                try {
                    Row<?> row = (Row<?>) existing;
                    Map<?, ?> remote = (Map<?, ?>) row.getData();
                    if (!course.equals(remote.get("course")) || !lesson.equals(remote.get("lesson")))
                        throw new SecurityException("Cloud row key mismatch");
                    long remoteAt = ((Number) remote.get("completed_at")).longValue();
                    if (remoteAt <= 0) throw new IllegalStateException("Invalid remote timestamp");
                    if (remoteAt <= completedAt) {
                        acknowledge(store, owner, token, course, lesson, completedAt);
                        return;
                    }
                    updateEarlierRow(store, owner, token, rowId, data, course, lesson, completedAt);
                } catch (Exception e) {
                    guard.finish(token, owner);
                }
            }));
        } catch (AppwriteException e) {
            retryOrStop(store, owner, token, e);
        }
    }

    private void updateEarlierRow(LearningStore store, String owner, long token, String rowId,
                                  Map<String, Object> data, String course, String lesson,
                                  long completedAt) {
        try {
            tables.updateRow(DATABASE, TABLE, rowId, data, (List<String>) null,
                    new CoroutineCallback<>((updated, error) -> {
                if (!guard.isCurrent(token, owner)) return;
                if (error != null) {
                    retryOrStop(store, owner, token, error);
                    return;
                }
                acknowledge(store, owner, token, course, lesson, completedAt);
            }));
        } catch (AppwriteException e) {
            retryOrStop(store, owner, token, e);
        }
    }

    private static Map<String, Object> payload(String course, String lesson, long completedAt) {
        Map<String, Object> data = new HashMap<>();
        data.put("course", course);
        data.put("lesson", lesson);
        data.put("completed_at", completedAt);
        data.put("schema_version", 1);
        return data;
    }

    /** Removes one queue entry only after the remote write was confirmed for this owner. */
    private void acknowledge(LearningStore store, String owner, long token,
                             String course, String lesson, long completedAt) {
        if (!guard.isCurrent(token, owner)) return;
        try {
            store.acknowledgeUpload(owner, course, lesson, completedAt);
        } catch (Exception e) {
            guard.finish(token, owner);
            return;
        }
        transientAttempts = 0;
        upload(store, owner, token);
    }

    private void download(LearningStore store, String owner, long token, String after) {
        if (!guard.isCurrent(token, owner)) return;
        List<String> queries = new ArrayList<>();
        // A stable order is required for cursor pagination; $id is unique per row.
        queries.add(Query.Companion.orderAsc("$id"));
        queries.add(Query.Companion.limit(PAGE_SIZE));
        if (after != null) queries.add(Query.Companion.cursorAfter(after));
        try {
            tables.listRows(DATABASE, TABLE, queries,
                    new CoroutineCallback<>((result, error) -> {
                if (!guard.isCurrent(token, owner)) return;
                if (error != null || result == null) {
                    retryOrStop(store, owner, token, error);
                    return;
                }
                mergePage(store, owner, token, result);
            }));
        } catch (AppwriteException e) {
            retryOrStop(store, owner, token, e);
        }
    }

    private void mergePage(LearningStore store, String owner, long token, Object result) {
        try {
            RowList<?> page = (RowList<?>) result;
            JSONArray entries = new JSONArray();
            String last = null;
            int count = 0;
            for (Object value : page.getRows()) {
                Row<?> row = (Row<?>) value;
                Map<?, ?> data = (Map<?, ?>) row.getData();
                String course = (String) data.get("course");
                String lesson = (String) data.get("lesson");
                // Ownership check: a row visible to this session must still hash to this
                // account, so another learner's data can never be merged into this store.
                if (!row.getId().equals(ProgressRowIds.forLesson(owner, course, lesson)))
                    throw new SecurityException("Progress row ownership mismatch");
                JSONObject entry = new JSONObject();
                entry.put("course", course);
                entry.put("lesson", lesson);
                entry.put("completed_at", ((Number) data.get("completed_at")).longValue());
                entries.put(entry);
                last = row.getId();
                count++;
            }
            JSONObject snapshot = new JSONObject();
            snapshot.put("schema", 1);
            snapshot.put("progress", entries);
            if (!guard.isCurrent(token, owner)) return;
            store.mergeProgress(snapshot);
            transientAttempts = 0;
            if (count == PAGE_SIZE && last != null) download(store, owner, token, last);
            else guard.finish(token, owner);
        } catch (Exception e) {
            guard.finish(token, owner);
        }
    }

    /**
     * Retries only failures that can succeed later (offline, timeout, throttling, 5xx).
     * Authorization and validation failures stop immediately so the queue is preserved
     * without hammering the backend.
     */
    private void retryOrStop(LearningStore store, String owner, long token, Throwable error) {
        if (!guard.isCurrent(token, owner)) return;
        if (!isTransient(error) || transientAttempts >= MAX_TRANSIENT_ATTEMPTS) {
            guard.finish(token, owner);
            return;
        }
        transientAttempts++;
        final long delay = RETRY_STEP_MILLIS * transientAttempts;
        handler.postDelayed(() -> {
            if (guard.isCurrent(token, owner)) upload(store, owner, token);
        }, delay);
    }

    static boolean isTransient(Throwable error) {
        if (error instanceof AppwriteException) {
            Integer code = ((AppwriteException) error).getCode();
            if (code == null) return true;
            return code == 0 || code == 408 || code == 429 || code >= 500;
        }
        return error instanceof IOException;
    }
}
