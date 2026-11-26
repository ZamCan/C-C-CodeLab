package com.zamcan.codelab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Brief brand transition. The animation is short and ends in the first real screen, never in an
 * artificial wait or a second welcome page.
 */
public final class BrandSplashActivity extends Activity {
    private boolean routed;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        boolean light = AppAppearance.isLight(this);
        if (light) setTheme(R.style.AppThemeLight);
        getWindow().setStatusBarColor(Design.bg(light));
        getWindow().setNavigationBarColor(Design.bg(light));
        if (light) getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        FrameLayout stage = new FrameLayout(this);
        stage.setBackgroundColor(Design.bg(light));
        stage.addView(new MeshBackdrop(this, light), new FrameLayout.LayoutParams(-1, -1));

        LinearLayout lockup = new LinearLayout(this);
        lockup.setOrientation(LinearLayout.VERTICAL);
        lockup.setGravity(Gravity.CENTER);
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.ic_codelab_brand);
        mark.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        lockup.addView(mark, new LinearLayout.LayoutParams(Design.dp(this, 96), Design.dp(this, 96)));

        TextView wordmark = Design.text(this, "CodeLab", 28, Design.ink(light), true);
        wordmark.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        wordmark.setGravity(Gravity.CENTER);
        wordmark.setPadding(0, Design.dp(this, 18), 0, 0);
        lockup.addView(wordmark);

        TextView track = Design.eyebrow(this, "C & C++ ACADEMY", Design.accent(light));
        track.setGravity(Gravity.CENTER);
        track.setPadding(0, Design.dp(this, 8), 0, 0);
        lockup.addView(track);

        stage.addView(lockup, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
        setContentView(stage);

        boolean motion = android.animation.ValueAnimator.areAnimatorsEnabled();
        if (motion) {
            lockup.setAlpha(0f);
            lockup.setScaleX(.92f);
            lockup.setScaleY(.92f);
            lockup.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(360)
                    .withEndAction(this::route).start();
        } else {
            stage.postDelayed(this::route, 120);
        }
    }

    private void route() {
        if (routed || isFinishing() || isDestroyed()) return;
        routed = true;
        startActivity(new Intent(this, WelcomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        routed = true;
    }
}
