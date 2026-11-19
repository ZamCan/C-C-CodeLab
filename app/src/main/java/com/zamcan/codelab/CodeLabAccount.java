package com.zamcan.codelab;

import android.content.Context;
import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.User;
import io.appwrite.services.Account;

/**
 * Appwrite session boundary.
 *
 * <p>The only accepted identity evidence is a successful {@link Account#get} response from the
 * configured project. Preferences, OAuth redirect parameters and cached user IDs are never
 * trusted to authorize cloud access.</p>
 */
public final class CodeLabAccount {
    static final String ENDPOINT = "https://cloud.appwrite.io/v1";
    static final String PROJECT = "codelab-zamcan";
    private static volatile CodeLabAccount instance;
    private final Account account;
    private final Client client;

    public CodeLabAccount(Context context) {
        // Appwrite 23.1.0 exposes both the Kotlin endpoint property setter (void) and a
        // fluent setEndpoint(String): Client. Java cannot disambiguate those overloads, so
        // the endpoint is configured through the documented constructor instead.
        client = new Client(context.getApplicationContext(), ENDPOINT, null, false);
        client.setProject(PROJECT);
        account = new Account(client);
    }

    public static CodeLabAccount shared(Context context) {
        if (instance == null) synchronized (CodeLabAccount.class) {
            if (instance == null) instance = new CodeLabAccount(context);
        }
        return instance;
    }

    public Account service() { return account; }

    public Client client() { return client; }

    public interface Result {
        void authenticated(String verifiedUserId);
        void unauthenticated(String reason);
        default void unavailable(String reason) { unauthenticated(reason); }
    }

    /**
     * Revalidates the real server session before any account-specific database or cloud
     * operation. 401/403 means the session is gone; transportation failures are reported as
     * {@code unavailable} so already verified offline data is preserved.
     */
    public void verify(Result callback) {
        try {
            account.get(new CoroutineCallback<>((user, error) -> {
                if (error != null) {
                    if (error instanceof AppwriteException) {
                        Integer code = ((AppwriteException) error).getCode();
                        if (code != null && (code == 401 || code == 403)) {
                            callback.unauthenticated("Session expired or revoked");
                            return;
                        }
                    }
                    callback.unavailable("Unable to verify session while offline");
                    return;
                }
                if (user == null) {
                    callback.unavailable("Empty session verification response");
                    return;
                }
                String id = ((User<?>) user).getId();
                if (id == null || !id.matches("[A-Za-z0-9_-]{1,36}")) {
                    callback.unauthenticated("Invalid authenticated user identifier");
                    return;
                }
                callback.authenticated(id);
            }));
        } catch (AppwriteException e) {
            callback.unavailable(e.getMessage());
        }
    }
}
