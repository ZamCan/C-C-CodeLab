package com.zamcan.codelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/**
 * Original computational mesh artwork drawn natively with Canvas.
 *
 * <p>A quiet perspective grid rises from the lower half of the screen behind a slow aurora sweep:
 * the visual metaphor is a network of compiled instructions, not decoration. The drawing is static
 * (no per-frame allocations or timers), so it costs the same as a gradient once laid out and it is
 * accessible as a single decorative element.
 */
final class MeshBackdrop extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final boolean light;

    MeshBackdrop(Context context, boolean light) {
        super(context);
        this.light = light;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 0, width * .35f, height,
                light ? new int[]{0xffffffff, 0xffeef4fd, 0xffe2ebf8}
                      : new int[]{0xff04070d, 0xff070d18, 0xff03070e},
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);

        // Aurora: two very low-contrast radial sweeps, cool teal and deep indigo.
        paint.setShader(new RadialGradient(width * .18f, height * .62f, width * .78f,
                light ? new int[]{0x3320c7a6, 0x00000000}
                      : new int[]{0x3316b39a, 0x00000000},
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(new RadialGradient(width * .95f, height * .95f, width * .7f,
                light ? 0x221d5bb8 : 0x2a2a4bd6, 0x00000000, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);

        // Mesh horizon sits low so the brand lockup keeps clean space above it.
        float horizon = height * .46f;
        float horizonY = height * .58f;
        float vanishX = width * .5f;

        Paint mesh = new Paint(Paint.ANTI_ALIAS_FLAG);
        mesh.setStyle(Paint.Style.STROKE);
        mesh.setStrokeWidth(Math.max(1f, Design.dp(getContext(), .9f)));
        mesh.setStrokeCap(Paint.Cap.ROUND);

        // Horizontal curves: spacing tightens toward the horizon, like a receding plane.
        int rows = 18;
        for (int row = 0; row <= rows; row++) {
            float t = row / (float) rows;
            float eased = t * t;
            float y = horizon + (height - horizon) * eased;
            float sag = Design.dp(getContext(), 26) * (1f - t) + Design.dp(getContext(), 6);
            path.reset();
            path.moveTo(-width * .05f, y);
            path.cubicTo(width * .28f, y - sag, width * .72f, y + sag, width * 1.05f, y);
            int alpha = (int) (52 * (1f - t) + 10);
            mesh.setColor(withAlpha(light ? 0xff1a4f9c : 0xff8fd8ff, alpha));
            canvas.drawPath(path, mesh);
        }

        // Converging verticals from the horizon to the bottom edge.
        int columns = 14;
        for (int column = 0; column <= columns; column++) {
            float spread = (column - columns / 2f) / (columns / 2f); // -1..1
            float bottomX = vanishX + spread * width * 1.25f;
            float startX = vanishX + spread * width * .12f;
            Path line = new Path();
            line.moveTo(startX, horizonY - Design.dp(getContext(), 22));
            line.lineTo(bottomX, height + Design.dp(getContext(), 4));
            mesh.setColor(withAlpha(light ? 0xff2c6bc0 : 0xff7fc6ff, 28));
            canvas.drawPath(line, mesh);
        }

        // A few brighter nodes: places where the network "executes".
        Paint node = new Paint(Paint.ANTI_ALIAS_FLAG);
        node.setStyle(Paint.Style.FILL);
        float[] xs = {.16f, .38f, .63f, .82f};
        float[] ys = {.68f, .79f, .72f, .88f};
        for (int i = 0; i < xs.length; i++) {
            float x = width * xs[i];
            float y = height * ys[i];
            int color = i % 2 == 0 ? (light ? 0xff0b7d69 : 0xff62e2c2) : (light ? 0xff8a5a00 : 0xffffcf7a);
            node.setColor(withAlpha(color, 150));
            canvas.drawCircle(x, y, Design.dp(getContext(), 2.4f), node);
            node.setColor(withAlpha(color, 34));
            canvas.drawCircle(x, y, Design.dp(getContext(), 9), node);
        }

        // Bottom vignette keeps full-width buttons readable over the artwork.
        paint.setShader(new LinearGradient(0, height * .62f, 0, height,
                new int[]{0x00000000, light ? 0x99f7fafe : 0xcc03070d}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, height * .62f, width, height, paint);
        paint.setShader(null);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00ffffff) | (Math.max(0, Math.min(255, alpha)) << 24);
    }
}
