package com.zamcan.codelab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/** Brief local brand transition before opening the learner workspace. */
public final class BrandSplashActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable launch = () -> {
        if (isFinishing() || isDestroyed()) return;
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        boolean light = AppAppearance.isLight(this);
        int backdrop = light ? 0xfff6f1ff : 0xff24104f;
        getWindow().setStatusBarColor(backdrop);
        getWindow().setNavigationBarColor(backdrop);
        if (light) getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        FrameLayout stage = new FrameLayout(this);
        stage.setFitsSystemWindows(true);
        stage.setBackgroundColor(backdrop);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_codelab_brand);
        logo.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        stage.addView(logo, new FrameLayout.LayoutParams(dp(112), dp(112), Gravity.CENTER));
        TextView wordmark = new TextView(this);
        wordmark.setText("CodeLab C & C++");
        wordmark.setTextColor(light ? 0xff361e69 : 0xffeadfff);
        wordmark.setTextSize(24);
        wordmark.setGravity(Gravity.CENTER);
        wordmark.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        FrameLayout.LayoutParams bottom = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        bottom.bottomMargin = dp(32);
        stage.addView(wordmark, bottom);
        setContentView(stage);
        logo.setScaleX(.92f);
        logo.setScaleY(.92f);
        logo.animate().scaleX(1f).scaleY(1f).setDuration(240).start();
        handler.postDelayed(launch, 500);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(launch);
        super.onDestroy();
    }
}
