package com.zamcan.codelab;

import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.enums.OAuthProvider;

/** Appwrite-managed browser OAuth, using the registered Android callback URI. */
public final class GoogleSignInActivity extends ComponentActivity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){finish();return;}
        CodeLabAccount.shared(this).service().createOAuth2Session(
            this, OAuthProvider.GOOGLE, null, null, null,
            new CoroutineCallback<>((result,error)->{
                runOnUiThread(()->{
                    if(isFinishing() || isDestroyed())return;
                    if(error!=null){
                        Toast.makeText(this,"Google sign-in did not finish. You can continue offline.",Toast.LENGTH_LONG).show();
                    }
                    finish();
                });
            })
        );
    }
}
