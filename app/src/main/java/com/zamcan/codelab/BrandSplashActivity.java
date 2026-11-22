package com.zamcan.codelab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Minimal branded transition; no network access, fake loading progress or blocking work. */
public final class BrandSplashActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean launched;
    private final Runnable launch=()->{
        if(launched||isFinishing())return;
        launched=true;
        startActivity(new Intent(this,AccountChoiceActivity.class));
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    };
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        boolean light=getSharedPreferences("academy",MODE_PRIVATE).getBoolean("light_theme",false);
        int background=light?0xfff8fafc:0xff10151d;
        int foreground=light?0xff182331:Color.WHITE;
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        LinearLayout root=new LinearLayout(this);
        root.setGravity(Gravity.CENTER);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);
        TextView brand=new TextView(this);
        brand.setText("Compile\u2022Learn");
        brand.setTextColor(foreground);
        brand.setTextSize(32);
        brand.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);
        TextView language=new TextView(this);
        language.setText("C / C++");
        language.setTextSize(18);
        language.setTextColor(light?0xff167a68:0xff35c6a2);
        language.setLetterSpacing(.13f);
        language.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
        lp.topMargin=(int)(10*getResources().getDisplayMetrics().density);
        root.addView(language,lp);
        setContentView(root);
        brand.setAlpha(0f);language.setAlpha(0f);
        brand.animate().alpha(1f).setDuration(280).start();
        language.animate().alpha(1f).setStartDelay(100).setDuration(300).start();
        handler.postDelayed(launch,650);
    }
    @Override protected void onDestroy(){handler.removeCallbacks(launch);super.onDestroy();}
}
