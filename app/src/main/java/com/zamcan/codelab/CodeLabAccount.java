package com.zamcan.codelab;

import android.content.Context;
import java.util.Map;
import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.User;
import io.appwrite.services.Account;

/** Appwrite session boundary. Never infer account identity from preferences or OAuth redirects. */
public final class CodeLabAccount {
    private static volatile CodeLabAccount instance;
    private final Account account;
    private final Client client;
    public CodeLabAccount(Context context) {
        client=new Client(context.getApplicationContext())
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("codelab-zamcan");
        account=new Account(client);
    }
    public static CodeLabAccount shared(Context context) {
        if(instance==null) synchronized(CodeLabAccount.class) {
            if(instance==null)instance=new CodeLabAccount(context);
        }
        return instance;
    }
    public Account service(){return account;}
    public Client client(){return client;}
    public interface Result {
        void authenticated(String verifiedUserId);
        void unauthenticated(String reason);
    }
    /** Revalidate the actual server session before opening any account-specific database. */
    public void verify(Result callback) {
        try {
            account.get(new CoroutineCallback<>((user,error)->{
                if(error!=null || user==null) {
                    callback.unauthenticated(error==null?"No active session":error.getMessage());
                    return;
                }
                String id=((User<?>)user).getId();
                if(id==null || !id.matches("[A-Za-z0-9_-]{1,36}")) {
                    callback.unauthenticated("Invalid authenticated user identifier");
                    return;
                }
                callback.authenticated(id);
            }));
        } catch(AppwriteException e) { callback.unauthenticated(e.getMessage()); }
    }
}
