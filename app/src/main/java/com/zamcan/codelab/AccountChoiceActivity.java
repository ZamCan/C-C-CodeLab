package com.zamcan.codelab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Explicit account-versus-guest decision before opening the academy. */
public final class AccountChoiceActivity extends Activity {
    static final String PREF="academy";
    static final String CHOICE="entry_mode_selected";
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(getSharedPreferences(PREF,MODE_PRIVATE).getBoolean(CHOICE,false)){
            startActivity(new Intent(this,MainActivity.class));finish();return;
        }
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);
        root.setPadding(28,32,28,32);root.setBackgroundColor(0xff10151d);
        TextView title=new TextView(this);
        title.setText("How would you like to learn?");title.setTextSize(27);title.setTextColor(Color.WHITE);
        root.addView(title);
        TextView info=new TextView(this);
        info.setText("Google account: your completed learning progress is synchronized securely when online and can be restored on another device after sign-in.\n\nGuest: progress stays on this device only. It cannot be restored on another device and may be lost if app data is cleared, the app is removed, or the device is lost.");
        info.setTextSize(16);info.setTextColor(0xffd4e3f6);info.setPadding(0,24,0,24);
        root.addView(info);
        Button google=new Button(this);google.setText("Continue with Google");
        root.addView(google,new LinearLayout.LayoutParams(-1,-2));
        google.setOnClickListener(v->startActivity(new Intent(this,GoogleSignInActivity.class)));
        Button guest=new Button(this);guest.setText("Continue as Guest");
        root.addView(guest,new LinearLayout.LayoutParams(-1,-2));
        guest.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Offline guest progress")
            .setMessage("Guest progress is stored only on this device. It cannot be recovered after data loss or opened on another device. Continue?")
            .setNegativeButton("Go back",null)
            .setPositiveButton("Continue as Guest",(d,w)->{
                getSharedPreferences(PREF,MODE_PRIVATE).edit().putBoolean(CHOICE,true).apply();
                startActivity(new Intent(this,MainActivity.class));finish();
            }).show());
        setContentView(root);
    }
}
