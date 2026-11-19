package com.zamcan.codelab;

import io.appwrite.exceptions.AppwriteException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.junit.Test;
import static org.junit.Assert.*;

/** Retry only failures that can succeed later; never loop on authorization failures. */
public final class LearningSyncPolicyTest {
    @Test public void offlineAndServerFailuresAreTransient() {
        assertTrue(LearningSync.isTransient(new IOException("offline")));
        assertTrue(LearningSync.isTransient(new SocketTimeoutException("timeout")));
        assertTrue(LearningSync.isTransient(new AppwriteException("no response", null, null, null)));
        assertTrue(LearningSync.isTransient(new AppwriteException("server", 500, "general_error", null)));
        assertTrue(LearningSync.isTransient(new AppwriteException("unavailable", 503, "general_error", null)));
        assertTrue(LearningSync.isTransient(new AppwriteException("throttled", 429, "general_rate_limit_exceeded", null)));
        assertTrue(LearningSync.isTransient(new AppwriteException("timeout", 408, "general_request_timeout", null)));
    }

    @Test public void authorizationAndValidationFailuresStopRetrying() {
        assertFalse(LearningSync.isTransient(new AppwriteException("expired", 401, "general_unauthorized_scope", null)));
        assertFalse(LearningSync.isTransient(new AppwriteException("forbidden", 403, "general_access_forbidden", null)));
        assertFalse(LearningSync.isTransient(new AppwriteException("missing", 404, "general_not_found", null)));
        assertFalse(LearningSync.isTransient(new AppwriteException("conflict", 409, "document_already_exists", null)));
        assertFalse(LearningSync.isTransient(new AppwriteException("invalid", 400, "general_argument_invalid", null)));
    }
}
