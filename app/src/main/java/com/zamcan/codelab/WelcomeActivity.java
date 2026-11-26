package com.zamcan.codelab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/**
 * Quiet landing surface: brand, one sentence of purpose, then the two real ways in.
 *
 * <p>Google sign-in is optional and only used to keep an account's progress separate; the guest
 * path is never blocked, never asks for confirmation and never waits for the network. The layout is
 * inset-aware, scrollable for large fonts and small screens, and uses 48 dp minimum targets.
 */
public final class WelcomeActivity extends Activity {
    @Override protected void attachBaseContext(Context base) {
        android.content.res.Configuration config =
                new android.content.res.Configuration(base.getResources().getConfiguration());
        config.setLocale(Locale.ENGLISH);
        config.setLocales(new android.os.LocaleList(Locale.ENGLISH));
        super.attachBaseContext(base.createConfigurationContext(config));
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        boolean light = AppAppearance.isLight(this);
        if (light) setTheme(R.style.AppThemeLight);
        getWindow().setStatusBarColor(Design.bg(light));
        getWindow().setNavigationBarColor(Design.bg(light));
        if (light) getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        // A learner whose account was already verified enters the workspace immediately.
        String verified = getSharedPreferences("academy", MODE_PRIVATE)
                .getString("last_verified_account", null);
        if (verified != null && state == null) {
            enterWorkspace();
            return;
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Design.bg(light));
        root.addView(new MeshBackdrop(this, light), new FrameLayout.LayoutParams(-1, -1));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(Design.dp(this, 24), Design.dp(this, 20),
                Design.dp(this, 24), Design.dp(this, 26));
        scroll.addView(column, new ScrollView.LayoutParams(-1, -2));

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            column.setPadding(Design.dp(this, 24), Design.dp(this, 20) + insets.getSystemWindowInsetTop(),
                    Design.dp(this, 24), Design.dp(this, 26) + insets.getSystemWindowInsetBottom());
            return insets;
        });

        // Overflow: settings that do not compete with the two primary decisions.
        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        topRow.setMinimumHeight(Design.dp(this, 48));
        ImageButton more = new ImageButton(this);
        more.setImageResource(R.drawable.ic_more_vert);
        more.setColorFilter(Design.muted(light));
        more.setBackground(Design.pressable(this, Design.fill(this, 0x00000000, 24), 0x3396b8ff));
        more.setContentDescription("About CodeLab, appearance and help");
        more.setPadding(Design.dp(this, 12), Design.dp(this, 12), Design.dp(this, 12), Design.dp(this, 12));
        more.setOnClickListener(v -> showMore(light));
        topRow.addView(more, new LinearLayout.LayoutParams(Design.dp(this, 48), Design.dp(this, 48)));
        column.addView(topRow);

        // Brand lockup.
        LinearLayout lockup = new LinearLayout(this);
        lockup.setOrientation(LinearLayout.VERTICAL);
        lockup.setPadding(0, Design.dp(this, 34), 0, 0);
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.ic_codelab_brand);
        mark.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        lockup.addView(mark, new LinearLayout.LayoutParams(Design.dp(this, 76), Design.dp(this, 76)));

        TextView wordmark = Design.text(this, "CodeLab", 34, Design.ink(light), true);
        wordmark.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        wordmark.setPadding(0, Design.dp(this, 18), 0, 0);
        lockup.addView(wordmark);

        TextView track = Design.eyebrow(this, "C & C++ ACADEMY", Design.accent(light));
        track.setPadding(0, Design.dp(this, 8), 0, Design.dp(this, 12));
        lockup.addView(track);

        View rule = new View(this);
        GradientDrawable ruleShape = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Design.accent(light), 0x0062e2c2});
        ruleShape.setCornerRadius(Design.dp(this, 2));
        rule.setBackground(ruleShape);
        lockup.addView(rule, new LinearLayout.LayoutParams(Design.dp(this, 64), Design.dp(this, 3)));
        column.addView(lockup);

        TextView promise = Design.text(this,
                "Learn C and C++ from first principles — then run your own code, offline.",
                22, Design.ink(light), false);
        promise.setPadding(0, Design.dp(this, 26), 0, 0);
        promise.setLineSpacing(Design.dp(this, 5), 1f);
        column.addView(promise);

        TextView detail = Design.text(this,
                "Professor-led lessons with real compiler feedback. No account needed to start.",
                15, Design.muted(light), false);
        detail.setPadding(0, Design.dp(this, 12), 0, 0);
        column.addView(detail);

        View space = new View(this);
        column.addView(space, new LinearLayout.LayoutParams(1, Design.dp(this, 88)));

        View google = googleButton(light);
        column.addView(google, new LinearLayout.LayoutParams(-1, Design.dp(this, 56)));

        android.widget.Button guest = Design.ghost(this, light, "Continue as guest");
        guest.setContentDescription("Continue as guest. Learning progress is stored on this device.");
        guest.setOnClickListener(v -> enterWorkspace());
        LinearLayout.LayoutParams guestParams = new LinearLayout.LayoutParams(-1, Design.dp(this, 56));
        guestParams.topMargin = Design.dp(this, 12);
        column.addView(guest, guestParams);

        TextView disclosure = Design.text(this,
                "Guest progress stays on this device. Signing in later lets you copy it to your account.",
                12, Design.faint(light), false);
        disclosure.setGravity(Gravity.CENTER);
        disclosure.setPadding(0, Design.dp(this, 16), 0, 0);
        column.addView(disclosure);

        setContentView(root);
    }

    /** Official Google mark plus a plain-language label; the provider flow is unchanged. */
    private View googleButton(boolean light) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setMinimumHeight(Design.dp(this, 56));
        button.setClickable(true);
        button.setFocusable(true);
        button.setContentDescription("Continue with Google");
        button.setBackground(Design.pressable(this,
                Design.outlined(this, light ? 0xffffffff : 0xfff4f7fd, 14,
                        light ? Design.line(true) : 0x00000000), 0x662f5fe8));

        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.appwrite_google_g);
        mark.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        button.addView(mark, new LinearLayout.LayoutParams(Design.dp(this, 20), Design.dp(this, 20)));

        TextView label = Design.text(this, "Continue with Google", 16, 0xff1c2230, true);
        label.setPadding(Design.dp(this, 12), 0, 0, 0);
        button.addView(label);
        button.setOnClickListener(v -> {
            Intent workspace = new Intent(this, MainActivity.class);
            startActivity(workspace);
            startActivity(new Intent(this, GoogleSignInActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
        return button;
    }

    private void enterWorkspace() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void showMore(boolean light) {
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException missing) {
            version = "";
        }
        new AlertDialog.Builder(this, light ? android.R.style.Theme_Material_Light_Dialog_Alert
                : android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("CodeLab C & C++")
                .setMessage("A C and C++ academy by ZamCan Academy.\n\n"
                        + "Lessons, practice and the compiler work without a connection. "
                        + "Official certificate examinations, when enabled, require a verified "
                        + "online session.\n\nVersion " + version)
                .setPositiveButton("Close", null)
                .show();
    }
}
