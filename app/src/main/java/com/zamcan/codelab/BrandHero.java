package com.zamcan.codelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/** Original native wordmark and code emblem for the home and boot surfaces. */
public final class BrandHero extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean light;

    public BrandHero(Context context, boolean light) {
        super(context);
        this.light = light;
        setContentDescription("Compile and Learn. C and C++ practice.");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(dp(178));
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float radius = dp(24);
        paint.setShader(new LinearGradient(0, 0, w, h,
                light ? new int[]{0xffffffff, 0xffeaf7f6} : new int[]{0xff213e63, 0xff18324f},
                null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(0, 0, w, h), radius, radius, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(light ? 0x33326f91 : 0x557ee7f5);
        canvas.drawRoundRect(new RectF(dp(1), dp(1), w - dp(1), h - dp(1)), radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);

        float pad = dp(18);
        float mark = Math.min(dp(92), h - pad * 2);
        if (w < dp(350)) mark = Math.min(dp(74), h - pad * 2);
        float mx = pad, my = (h - mark) / 2;
        paint.setShader(new LinearGradient(mx, my, mx + mark, my + mark,
                new int[]{0xff0e8f7d, 0xff35c6a2, 0xff74dcc2}, null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(mx, my, mx + mark, my + mark), mark * .25f, mark * .25f, paint);
        paint.setShader(null);

        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        line.setStrokeWidth(Math.max(dp(3), mark * .055f));
        line.setColor(0xfff8fffd);
        Path left = new Path();
        left.moveTo(mx + mark * .34f, my + mark * .31f);
        left.lineTo(mx + mark * .19f, my + mark * .50f);
        left.lineTo(mx + mark * .34f, my + mark * .69f);
        canvas.drawPath(left, line);
        Path right = new Path();
        right.moveTo(mx + mark * .66f, my + mark * .31f);
        right.lineTo(mx + mark * .81f, my + mark * .50f);
        right.lineTo(mx + mark * .66f, my + mark * .69f);
        canvas.drawPath(right, line);
        line.setColor(0xffffd27a);
        canvas.drawLine(mx + mark * .43f, my + mark * .72f,
                mx + mark * .57f, my + mark * .28f, line);

        float textX = mx + mark + dp(18);
        float available = w - textX - dp(18);
        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        title.setTextSize(Math.min(dp(33), Math.max(dp(23), available * .24f)));
        float wordWidth = title.measureText("CompileLearn");
        if (wordWidth > available) title.setTextSize(title.getTextSize() * available / wordWidth);
        float baseline = my + mark * .46f;
        title.setColor(light ? 0xff17324f : 0xfff7fffd);
        canvas.drawText("Compile", textX, baseline, title);
        float codeWidth = title.measureText("Compile");
        title.setColor(light ? 0xff0e8f7d : 0xff7ee7f5);
        canvas.drawText("Learn", textX + codeWidth, baseline, title);

        Paint small = new Paint(Paint.ANTI_ALIAS_FLAG);
        small.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        small.setTextSize(Math.min(dp(11), Math.max(dp(9), available * .036f)));
        small.setLetterSpacing(.11f);
        float taglineWidth = small.measureText("C / C++ COMPILER  •  GUIDED PRACTICE");
        if (taglineWidth > available) small.setTextSize(small.getTextSize() * available / taglineWidth);
        small.setColor(light ? 0xff52617a : 0xffc7d8ee);
        canvas.drawText("C / C++ COMPILER  •  GUIDED PRACTICE", textX, my + mark * .68f, small);

        Paint badge = new Paint(Paint.ANTI_ALIAS_FLAG);
        badge.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        badge.setTextSize(Math.min(dp(10), Math.max(dp(8), available * .033f)));
        badge.setLetterSpacing(.08f);
        float badgeWidth = badge.measureText("LEARN AT YOUR PACE  •  NO ACCOUNT REQUIRED");
        if (badgeWidth > available) badge.setTextSize(badge.getTextSize() * available / badgeWidth);
        badge.setColor(light ? 0xff1d6f8c : 0xff8de8ff);
        canvas.drawText("LEARN AT YOUR PACE  •  NO ACCOUNT REQUIRED", textX, my + mark * .85f, badge);

        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setColor(0xffffd27a);
        canvas.drawCircle(mx + mark * .78f, my + mark * .19f, dp(4), dot);
    }
}
