package com.zamcan.codelab;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

/**
 * Semantic design tokens for CodeLab's native surfaces.
 *
 * <p>Dark is the primary appearance: a midnight-navy canvas with a single teal accent and a warm
 * amber highlight reserved for milestones. The light theme keeps the same hierarchy and contrast
 * ratios rather than inverting saturation. All spacing follows an 8 dp rhythm.
 */
final class Design {
    private Design() {}

    // Dark (primary)
    static final int NIGHT_BG = 0xff05090f;
    static final int NIGHT_BG_DEEP = 0xff02050a;
    static final int NIGHT_SURFACE = 0xff0c1524;
    static final int NIGHT_SURFACE_HIGH = 0xff131f33;
    static final int NIGHT_LINE = 0xff223049;
    static final int NIGHT_INK = 0xffeef4ff;
    static final int NIGHT_MUTED = 0xff9db0ca;
    static final int NIGHT_FAINT = 0xff6b7f9c;
    static final int NIGHT_ACCENT = 0xff62e2c2;
    static final int NIGHT_ACCENT_SOFT = 0xff123b36;
    static final int NIGHT_AMBER = 0xffffcf7a;
    static final int NIGHT_CODE = 0xff081120;
    static final int NIGHT_DANGER = 0xffff9a8f;

    // Light
    static final int DAY_BG = 0xfff5f8fd;
    static final int DAY_BG_DEEP = 0xffeaf0fa;
    static final int DAY_SURFACE = 0xffffffff;
    static final int DAY_SURFACE_HIGH = 0xfff2f6fd;
    static final int DAY_LINE = 0xffdbe4f1;
    static final int DAY_INK = 0xff0d1a2c;
    static final int DAY_MUTED = 0xff4c5d75;
    static final int DAY_FAINT = 0xff73839a;
    static final int DAY_ACCENT = 0xff0b7d69;
    static final int DAY_ACCENT_SOFT = 0xffdef5ef;
    static final int DAY_AMBER = 0xff8a5a00;
    static final int DAY_CODE = 0xfff1f6fd;
    static final int DAY_DANGER = 0xffb02a20;

    static final int SUCCESS = 0xff3fcf9a;

    static int bg(boolean light) { return light ? DAY_BG : NIGHT_BG; }
    static int bgDeep(boolean light) { return light ? DAY_BG_DEEP : NIGHT_BG_DEEP; }
    static int surface(boolean light) { return light ? DAY_SURFACE : NIGHT_SURFACE; }
    static int surfaceHigh(boolean light) { return light ? DAY_SURFACE_HIGH : NIGHT_SURFACE_HIGH; }
    static int line(boolean light) { return light ? DAY_LINE : NIGHT_LINE; }
    static int ink(boolean light) { return light ? DAY_INK : NIGHT_INK; }
    static int muted(boolean light) { return light ? DAY_MUTED : NIGHT_MUTED; }
    static int faint(boolean light) { return light ? DAY_FAINT : NIGHT_FAINT; }
    static int accent(boolean light) { return light ? DAY_ACCENT : NIGHT_ACCENT; }
    static int accentSoft(boolean light) { return light ? DAY_ACCENT_SOFT : NIGHT_ACCENT_SOFT; }
    static int amber(boolean light) { return light ? DAY_AMBER : NIGHT_AMBER; }
    static int code(boolean light) { return light ? DAY_CODE : NIGHT_CODE; }
    static int danger(boolean light) { return light ? DAY_DANGER : NIGHT_DANGER; }

    static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static float sp(Context context, float value) {
        return android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP, value,
                context.getResources().getDisplayMetrics());
    }

    static GradientDrawable fill(Context context, int color, float radiusDp) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(context, radiusDp));
        return shape;
    }

    static GradientDrawable outlined(Context context, int color, float radiusDp, int strokeColor) {
        GradientDrawable shape = fill(context, color, radiusDp);
        shape.setStroke(Math.max(1, dp(context, 1)), strokeColor);
        return shape;
    }

    static RippleDrawable pressable(Context context, GradientDrawable shape, int rippleColor) {
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), shape, null);
    }

    static TextView text(Context context, String value, float sizeSp, int color, boolean medium) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setIncludeFontPadding(false);
        view.setLineSpacing(dp(context, 4), 1f);
        if (medium) view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return view;
    }

    static TextView eyebrow(Context context, String value, int color) {
        TextView view = text(context, value.toUpperCase(java.util.Locale.ENGLISH), 11, color, true);
        view.setLetterSpacing(0.14f);
        return view;
    }

    static Button primary(Context context, boolean light, String label) {
        Button button = baseButton(context, label);
        button.setTextColor(light ? 0xff06231d : 0xff04231c);
        button.setBackground(pressable(context,
                fill(context, accent(light), 14), 0x55ffffff));
        button.setMinHeight(dp(context, 54));
        return button;
    }

    static Button ghost(Context context, boolean light, String label) {
        Button button = baseButton(context, label);
        button.setTextColor(ink(light));
        button.setBackground(pressable(context,
                outlined(context, light ? 0xffffffff : 0x00101826, 14, line(light)), 0x3396b8ff));
        button.setMinHeight(dp(context, 54));
        return button;
    }

    static Button quiet(Context context, boolean light, String label) {
        Button button = baseButton(context, label);
        button.setTextColor(accent(light));
        button.setBackground(pressable(context,
                outlined(context, light ? 0x00ffffff : 0x00101826, 12, line(light)), 0x3396b8ff));
        button.setMinHeight(dp(context, 48));
        return button;
    }

    private static Button baseButton(Context context, String label) {
        Button button = new Button(context);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(context, 18), dp(context, 12), dp(context, 18), dp(context, 12));
        return button;
    }
}
