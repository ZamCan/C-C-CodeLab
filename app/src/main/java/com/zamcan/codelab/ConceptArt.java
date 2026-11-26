package com.zamcan.codelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/**
 * Original instructional diagrams drawn with Canvas, one composition per concept family.
 *
 * <p>Each drawing explains a relationship — flow, memory, call order, indexing — instead of acting
 * as decoration. A caption supplied by the lesson states the idea in words for screen readers and
 * for anyone who cannot see the diagram.
 */
final class ConceptArt extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean light;
    private String art = "pipeline";

    ConceptArt(Context context, boolean light) {
        super(context);
        this.light = light;
        text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setArt(String id) {
        if (id != null) art = id;
        invalidate();
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = Design.dp(getContext(), 196);
        setMeasuredDimension(width, resolveSize(height, heightSpec));
    }

    private float dp(float value) { return Design.dp(getContext(), value); }

    @Override protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Design.surface(light));
        RectF frame = new RectF(0, 0, width, height);
        canvas.drawRoundRect(frame, dp(16), dp(16), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1, dp(1)));
        paint.setColor(Design.line(light));
        canvas.drawRoundRect(new RectF(dp(.5f), dp(.5f), width - dp(.5f), height - dp(.5f)), dp(16), dp(16), paint);
        paint.setStyle(Paint.Style.FILL);

        if (art.equals("lineage") || art.equals("cpp-lineage")) drawLineage(canvas, width, height);
        else if (art.equals("pipeline") || art.equals("files") || art.equals("debug")) drawFlow(canvas, width, height);
        else if (art.equals("memory") || art.equals("init") || art.equals("reference") || art.equals("pointer")) drawMemory(canvas, width, height);
        else if (art.equals("io") || art.equals("streams")) drawIo(canvas, width, height);
        else if (art.equals("branch")) drawBranch(canvas, width, height);
        else if (art.equals("loop")) drawLoop(canvas, width, height);
        else if (art.equals("stack")) drawStack(canvas, width, height);
        else if (art.equals("array") || art.equals("vector") || art.equals("struct")) drawCells(canvas, width, height);
        else if (art.equals("class") || art.equals("inherit") || art.equals("template")) drawType(canvas, width, height);
        else if (art.equals("algorithms")) drawBars(canvas, width, height);
        else if (art.equals("raii")) drawTimeline(canvas, width, height);
        else drawFlow(canvas, width, height);
    }

    // --- drawing primitives -------------------------------------------------------------------

    private void box(Canvas canvas, float x, float y, float w, float h, String title, String detail,
            int fillColor, int borderColor, int titleColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1, dp(1)));
        paint.setColor(borderColor);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), dp(10), dp(10), paint);
        paint.setStyle(Paint.Style.FILL);

        text.setColor(titleColor);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(dp(detail.isEmpty() ? 12 : 12));
        float centerY = detail.isEmpty() ? y + h / 2 + dp(4) : y + h / 2 - dp(1);
        canvas.drawText(title, x + w / 2, centerY, text);
        if (!detail.isEmpty()) {
            text.setColor(Design.muted(light));
            text.setTextSize(dp(10));
            canvas.drawText(detail, x + w / 2, y + h / 2 + dp(13), text);
        }
    }

    private void arrow(Canvas canvas, float x1, float y1, float x2, float y2) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.6f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Design.accent(light));
        canvas.drawLine(x1, y1, x2, y2, paint);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        float size = dp(6);
        Path head = new Path();
        head.moveTo(x2, y2);
        head.lineTo(x2 - (float) Math.cos(angle - 0.5) * size, y2 - (float) Math.sin(angle - 0.5) * size);
        head.lineTo(x2 - (float) Math.cos(angle + 0.5) * size, y2 - (float) Math.sin(angle + 0.5) * size);
        head.close();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(head, paint);
    }

    private void caption(Canvas canvas, String value, float x, float y, float maxWidth) {
        text.setColor(Design.muted(light));
        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(dp(10));
        if (text.measureText(value) > maxWidth) {
            float scale = maxWidth / text.measureText(value);
            text.setTextSize(dp(10) * Math.max(.7f, scale));
        }
        canvas.drawText(value, x, y, text);
    }

    // --- compositions -------------------------------------------------------------------------

    private void drawLineage(Canvas canvas, float w, float h) {
        boolean cpp = art.equals("cpp-lineage");
        String[] labels = cpp
                ? new String[]{"C\n1972", "C with\nClasses\n1979", "C++\n1983", "Modern\nISO C++"}
                : new String[]{"BCPL\n1967", "B\n1969", "C\n1972", "C++\n1983"};
        float margin = dp(18);
        float gap = dp(8);
        float cell = (w - margin * 2 - gap * 3) / 4;
        float top = h * .30f;
        float boxHeight = h * .34f;
        for (int i = 0; i < 4; i++) {
            float x = margin + i * (cell + gap);
            boolean accent = i == 2;
            box(canvas, x, top, cell, boxHeight, labels[i].split("\n")[0],
                    labels[i].substring(labels[i].indexOf('\n') + 1).replace("\n", " "),
                    accent ? Design.accentSoft(light) : Design.surfaceHigh(light),
                    accent ? Design.accent(light) : Design.line(light),
                    accent ? Design.accent(light) : Design.ink(light));
            if (i < 3) arrow(canvas, x + cell + dp(1), top + boxHeight / 2, x + cell + gap - dp(2), top + boxHeight / 2);
        }
        caption(canvas, cpp ? "Each step added tools; none discarded the C foundation."
                : "Ideas travelled forward: BCPL influenced B, B became C.", margin, h - dp(16), w - margin * 2);
    }

    private void drawFlow(Canvas canvas, float w, float h) {
        String[] stages;
        String[] details;
        if (art.equals("files")) {
            stages = new String[]{"fopen", "fprintf", "fclose"};
            details = new String[]{"check NULL", "check < 0", "check != 0"};
        } else if (art.equals("debug")) {
            stages = new String[]{"Specify", "Test", "Diagnose"};
            details = new String[]{"inputs & rules", "expected output", "fix the cause"};
        } else {
            stages = new String[]{"Source", "Compiler", "Linker", "Program"};
            details = new String[]{"main.c", "object code", "symbols", "executable"};
        }
        int n = stages.length;
        float margin = dp(14);
        float gap = dp(12);
        float cell = (w - margin * 2 - gap * (n - 1)) / n;
        float top = h * .28f;
        float boxHeight = h * .38f;
        for (int i = 0; i < n; i++) {
            float x = margin + i * (cell + gap);
            box(canvas, x, top, cell, boxHeight, stages[i], details[i],
                    i == 0 ? Design.accentSoft(light) : Design.surfaceHigh(light),
                    i == 0 ? Design.accent(light) : Design.line(light), Design.ink(light));
            if (i < n - 1) arrow(canvas, x + cell + dp(1), top + boxHeight / 2, x + cell + gap - dp(2), top + boxHeight / 2);
        }
        String note = art.equals("files") ? "Every operation can fail; the return value is part of the design."
                : art.equals("debug") ? "A failing test is a question, not a verdict."
                : "Compilation and linking are separate stages with different errors.";
        caption(canvas, note, margin, h - dp(16), w - margin * 2);
    }

    private void drawMemory(Canvas canvas, float w, float h) {
        boolean pointer = art.equals("pointer");
        boolean reference = art.equals("reference");
        float top = h * .30f;
        float boxW = w * (pointer ? .30f : .34f);
        float boxH = h * .30f;
        float left = w * .12f;

        box(canvas, left, top, boxW, boxH, art.equals("init") ? "int days" : "value",
                art.equals("init") ? "7 (const)" : "10", Design.accentSoft(light), Design.accent(light), Design.ink(light));
        text.setTextAlign(Paint.Align.LEFT);
        caption(canvas, art.equals("init") ? "initialised at declaration" : "0x1000  ·  an int object",
                left, top + boxH + dp(16), boxW * 1.6f);

        float right = w * (pointer ? .58f : .56f);
        if (pointer || reference) {
            box(canvas, right, top, boxW, boxH, pointer ? "pointer" : "reference",
                    pointer ? "0x1000" : "alias", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
            arrow(canvas, right, top + boxH / 2, left + boxW + dp(4), top + boxH / 2);
            caption(canvas, pointer ? "storing &value; * follows it" : "the same object, another name",
                    right - dp(2), top + boxH + dp(16), w - right - dp(4));
        } else {
            box(canvas, right, top, boxW, boxH, "double", "2.5", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
            caption(canvas, "8 bytes, approximate", right, top + boxH + dp(16), boxW);
        }
        caption(canvas, "A declaration reserves storage; the type fixes its size and interpretation.",
                dp(14), h - dp(16), w - dp(28));
    }

    private void drawIo(Canvas canvas, float w, float h) {
        float top = h * .28f;
        float boxH = h * .34f;
        float margin = dp(14);
        float gap = dp(10);
        float cell = (w - margin * 2 - gap * 2) / 3;
        box(canvas, margin, top, cell, boxH, art.equals("streams") ? "std::cin" : "Keyboard", "input",
                Design.surfaceHigh(light), Design.line(light), Design.ink(light));
        box(canvas, margin + cell + gap, top, cell, boxH, "Program", "validate", Design.accentSoft(light),
                Design.accent(light), Design.ink(light));
        box(canvas, margin + (cell + gap) * 2, top, cell, boxH, art.equals("streams") ? "std::cout" : "Screen",
                "formatted", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
        arrow(canvas, margin + cell + dp(1), top + boxH / 2, margin + cell + gap - dp(2), top + boxH / 2);
        arrow(canvas, margin + cell * 2 + gap + dp(1), top + boxH / 2, margin + cell * 2 + gap * 2 - dp(2), top + boxH / 2);
        caption(canvas, "Input is untrusted until it has been converted and range-checked.", margin, h - dp(16), w - margin * 2);
    }

    private void drawBranch(Canvas canvas, float w, float h) {
        float cx = w / 2;
        float top = h * .20f;
        float diamondW = dp(120), diamondH = dp(52);
        Path diamond = new Path();
        diamond.moveTo(cx, top);
        diamond.lineTo(cx + diamondW / 2, top + diamondH / 2);
        diamond.lineTo(cx, top + diamondH);
        diamond.lineTo(cx - diamondW / 2, top + diamondH / 2);
        diamond.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Design.accentSoft(light));
        canvas.drawPath(diamond, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Design.accent(light));
        canvas.drawPath(diamond, paint);
        paint.setStyle(Paint.Style.FILL);
        text.setColor(Design.ink(light));
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(dp(11));
        canvas.drawText("condition", cx, top + diamondH / 2 + dp(4), text);

        float boxesY = top + diamondH + dp(30);
        float boxW = (w - dp(28) - dp(16)) / 2;
        float boxH = h * .22f;
        box(canvas, dp(14), boxesY, boxW, boxH, "true", "this block runs", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
        box(canvas, dp(14) + boxW + dp(16), boxesY, boxW, boxH, "false", "the other block runs", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
        arrow(canvas, cx - dp(10), top + diamondH + dp(2), dp(14) + boxW * .55f, boxesY - dp(2));
        arrow(canvas, cx + dp(10), top + diamondH + dp(2), dp(14) + boxW + dp(16) + boxW * .45f, boxesY - dp(2));
        caption(canvas, "Exactly one branch of an if / else chain executes.", dp(14), h - dp(16), w - dp(28));
    }

    private void drawLoop(Canvas canvas, float w, float h) {
        float top = h * .24f;
        float boxW = dp(96), boxH = dp(46);
        float left = dp(16);
        box(canvas, left, top, boxW, boxH, "i = 1", "initialise", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
        box(canvas, left + boxW + dp(14), top, boxW, boxH, "i <= 5 ?", "test first", Design.accentSoft(light), Design.accent(light), Design.ink(light));
        box(canvas, left, top + boxH + dp(46), boxW, boxH, "body", "add i", Design.surfaceHigh(light), Design.line(light), Design.ink(light));

        arrow(canvas, left + boxW + dp(1), top + boxH / 2, left + boxW + dp(13), top + boxH / 2);
        arrow(canvas, left + boxW * 1.5f + dp(14), top + boxH + dp(1), left + boxW * 1.5f + dp(14), top + boxH + dp(20));
        arrow(canvas, left + boxW * .5f, top + boxH + dp(45), left + boxW * .5f, top + boxH + dp(46));
        arrow(canvas, left + boxW + dp(2), top + boxH + dp(68), left + boxW + dp(46), top + boxH + dp(68));
        arrow(canvas, left + boxW + dp(46), top + boxH + dp(68), left + boxW + dp(46), top + boxH / 2);

        float traceX = left + boxW * 2 + dp(40);
        if (traceX + dp(70) < w) {
            caption(canvas, "trace", traceX, top + dp(10), w - traceX);
            String[] rows = {"i=1 total=1", "i=2 total=3", "i=3 total=6"};
            for (int i = 0; i < rows.length; i++) caption(canvas, rows[i], traceX, top + dp(30) + i * dp(16), w - traceX);
        }
        caption(canvas, "Initialise, test, execute, update — and always move toward the exit.", dp(14), h - dp(16), w - dp(28));
    }

    private void drawStack(Canvas canvas, float w, float h) {
        float left = dp(20);
        float boxW = w - dp(40);
        float boxH = h * .19f;
        String[] frames = {"main · local variables", "square(6) · parameter copy", "return 36 to caller"};
        for (int i = 0; i < frames.length; i++) {
            float y = h * .14f + i * (boxH + dp(8));
            boolean accent = i == 1;
            box(canvas, left, y, boxW, boxH, frames[i], i == 2 ? "frame removed" : "active frame",
                    accent ? Design.accentSoft(light) : Design.surfaceHigh(light),
                    accent ? Design.accent(light) : Design.line(light), Design.ink(light));
        }
        arrow(canvas, left + boxW - dp(12), h * .14f + boxH, left + boxW - dp(12), h * .14f + boxH + dp(8));
        caption(canvas, "Calls stack up; returns unwind in reverse order.", left, h - dp(12), boxW);
    }

    private void drawCells(Canvas canvas, float w, float h) {
        int count = 4;
        float margin = dp(18);
        float gap = dp(6);
        float cellW = (w - margin * 2 - gap * (count - 1)) / count;
        float top = h * .28f;
        float cellH = h * .28f;
        String[] values = art.equals("struct")
                ? new String[]{"id", "score", "name", "..."}
                : new String[]{"70", "80", "90", "?"};
        for (int i = 0; i < count; i++) {
            float x = margin + i * (cellW + gap);
            boolean accent = i == count - 1;
            box(canvas, x, top, cellW, cellH, values[i], art.equals("struct") ? "member" : "[" + i + "]",
                    accent ? Design.accentSoft(light) : Design.surfaceHigh(light),
                    accent ? Design.accent(light) : Design.line(light), Design.ink(light));
        }
        text.setColor(Design.muted(light));
        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(dp(10));
        canvas.drawText(art.equals("struct") ? "one object, named members, compiler padding"
                : "indices 0 … 3 · index 4 is outside the array", margin, top + cellH + dp(20), text);
        if (!art.equals("struct")) {
            text.setColor(Design.danger(light));
            canvas.drawText("last valid index = count − 1", margin, top + cellH + dp(38), text);
        }
        caption(canvas, "Contiguous storage: the type fixes each element's size.", margin, h - dp(14), w - margin * 2);
    }

    private void drawType(Canvas canvas, float w, float h) {
        float margin = dp(16);
        float top = h * .18f;
        if (art.equals("inherit")) {
            float baseW = w - margin * 2;
            box(canvas, margin, top, baseW, h * .20f, "Shape", "virtual area() = 0", Design.accentSoft(light), Design.accent(light), Design.ink(light));
            float childW = (baseW - dp(12)) / 2;
            box(canvas, margin, top + h * .32f, childW, h * .20f, "Rectangle", "override area", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
            box(canvas, margin + childW + dp(12), top + h * .32f, childW, h * .20f, "Circle", "override area", Design.surfaceHigh(light), Design.line(light), Design.ink(light));
            arrow(canvas, margin + childW / 2, top + h * .20f + dp(2), margin + childW / 2, top + h * .32f - dp(2));
            arrow(canvas, w - margin - childW / 2, top + h * .20f + dp(2), w - margin - childW / 2, top + h * .32f - dp(2));
            caption(canvas, "The caller sees Shape; the object decides which area() runs.", margin, h - dp(14), baseW);
            return;
        }
        float boxW = w - margin * 2;
        float rowH = h * .13f;
        box(canvas, margin, top, boxW, h * .17f, art.equals("template") ? "template <typename T>" : "class Counter",
                art.equals("template") ? "a pattern, not a type" : "invariant: 0 ≤ value ≤ 100",
                Design.accentSoft(light), Design.accent(light), Design.ink(light));
        String[] rows = art.equals("template")
                ? new String[]{"larger(3, 7) → T = int", "larger(\"Ada\", …) → T = string"}
                : new String[]{"private: int value", "public: increment()", "public: get() const"};
        for (int i = 0; i < rows.length; i++) {
            float y = top + h * .22f + i * (rowH + dp(6));
            box(canvas, margin + dp(14), y, boxW - dp(28), rowH, rows[i], "",
                    Design.surfaceHigh(light), Design.line(light), Design.muted(light));
        }
        caption(canvas, art.equals("template") ? "Instantiated per used type at compile time."
                : "Private state, a small public surface, one preserved rule.", margin, h - dp(14), boxW);
    }

    private void drawBars(Canvas canvas, float w, float h) {
        int[] values = {9, 2, 5};
        float margin = dp(24);
        float baseline = h * .72f;
        float barW = dp(30);
        float gap = (w - margin * 2 - barW * 3) / 2;
        for (int i = 0; i < values.length; i++) {
            float left = margin + i * (barW + gap);
            float barH = (h * .40f) * (values[i] / 9f);
            paint.setColor(i == 1 ? Design.accent(light) : Design.line(light));
            canvas.drawRoundRect(new RectF(left, baseline - barH, left + barW, baseline), dp(6), dp(6), paint);
            text.setColor(Design.ink(light));
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(dp(11));
            canvas.drawText(Integer.toString(values[i]), left + barW / 2, baseline - barH - dp(6), text);
            text.setColor(Design.muted(light));
            text.setTextSize(dp(10));
            canvas.drawText("[" + i + "]", left + barW / 2, baseline + dp(14), text);
        }
        caption(canvas, "Sorting reorders the same range in place: 9, 2, 5 → 2, 5, 9.", margin, h - dp(12), w - margin * 2);
    }

    private void drawTimeline(Canvas canvas, float w, float h) {
        float margin = dp(20);
        float y = h * .45f;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Design.line(light));
        canvas.drawLine(margin, y, w - margin, y, paint);
        String[] nodes = {"acquire", "use", "release"};
        for (int i = 0; i < nodes.length; i++) {
            float x = margin + (w - margin * 2) * i / 2f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(i == 2 ? Design.accent(light) : Design.amber(light));
            canvas.drawCircle(x, y, dp(6), paint);
            text.setColor(Design.ink(light));
            text.setTextAlign(i == 2 ? Paint.Align.RIGHT : (i == 0 ? Paint.Align.LEFT : Paint.Align.CENTER));
            text.setTextSize(dp(11));
            canvas.drawText(nodes[i], x, y - dp(14), text);
            text.setColor(Design.muted(light));
            text.setTextSize(dp(10));
            canvas.drawText(i == 0 ? "constructor" : i == 1 ? "while alive" : "destructor", x, y + dp(22), text);
        }
        caption(canvas, "Every exit path — return or exception — runs the destructor.", margin, h - dp(14), w - margin * 2);
    }
}
