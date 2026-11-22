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
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/** Explicit account-versus-guest decision before opening the academy. */
public final class AccountChoiceActivity extends Activity {
    static final String PREF="academy";
    static final String CHOICE="entry_mode_selected";
    private GradientDrawable tint(int color,int radius){GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(radius*getResources().getDisplayMetrics().density);return bg;}
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
        title.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));
        root.addView(title);
        TextView brand=new TextView(this);brand.setText("CODELAB   /   C & C++");brand.setTextSize(13);brand.setLetterSpacing(0.16f);brand.setTextColor(0xff7ee7f5);brand.setPadding(0,12,0,0);root.addView(brand);
        TextView info=new TextView(this);
        info.setText("Google: Sync progress across devices.\nGuest: Save progress on this device only.");
        info.setTextSize(16);info.setTextColor(0xffd4e3f6);info.setPadding(0,24,0,24);
        root.addView(info);
        TextView details=new TextView(this);
        details.setText("More about progress and recovery  ▾");details.setTextColor(0xff7ee7f5);details.setTextSize(15);
        root.addView(details);
        details.setPadding(0,0,0,24);
        details.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Where your progress is saved").setMessage("Google: completed learning progress syncs when online, and can be restored after signing in on another device. Unsynced progress may not yet be recoverable.\n\nGuest: progress remains only on this device. It cannot be restored on another device, and may be lost after uninstalling, clearing data, or losing the device. You can choose to copy guest progress to your account later.").setPositiveButton("Got it",null).show());
        Button google=new Button(this);google.setText("Continue with Google");
        google.setAllCaps(false);google.setTextColor(0xff10151d);google.setBackground(tint(0xffd8efff,18));
        LinearLayout.LayoutParams googleParams=new LinearLayout.LayoutParams(-1,60);googleParams.bottomMargin=14;root.addView(google,googleParams);
        google.setOnClickListener(v->startActivity(new Intent(this,GoogleSignInActivity.class)));
        Button guest=new Button(this);guest.setText("Continue as Guest");
        guest.setAllCaps(false);guest.setTextColor(Color.WHITE);guest.setBackground(tint(0xff28394b,18));root.addView(guest,new LinearLayout.LayoutParams(-1,60));
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
