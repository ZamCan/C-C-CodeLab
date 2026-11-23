package com.zamcan.codelab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;

/** Minimal branded transition; no network access, fake loading progress or blocking work. */
public final class BrandSplashActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean launched;
    private final Runnable launch=()->{
        if(launched||isFinishing())return;
        launched=true;
        startActivity(new Intent(this,MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    };
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        boolean light=getSharedPreferences("academy",MODE_PRIVATE).getBoolean("light_theme",false);
        int background=light?0xfff8fafc:0xff10151d;
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        LinearLayout root=new LinearLayout(this);
        root.setGravity(Gravity.CENTER);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22),dp(22),dp(22),dp(22));
        root.setBackgroundColor(background);
        BrandHero hero=new BrandHero(this,light);
        root.addView(hero,new LinearLayout.LayoutParams(-1,dp(178)));
        setContentView(root);
        hero.setAlpha(0f);hero.setTranslationY(dp(10));
        hero.animate().alpha(1f).translationY(0).setDuration(360).start();
        handler.postDelayed(launch,650);
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    @Override protected void onDestroy(){handler.removeCallbacks(launch);super.onDestroy();}
}
