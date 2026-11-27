package com.zamcan.codelab;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * The lesson surface: one beat at a time, a stable progress rail, Back/Next, and mandatory
 * prediction and check beats. Next never jumps to a different topic by itself — it continues the
 * current narrative, which is exactly what the authored beat sequence encodes.
 */
public final class BeatPlayer extends FrameLayout {
    interface Session {
        int beat();
        void beat(int index);
        boolean answered(int index);
        void answered(int index, boolean correct);
        int firstAttempt(int index);
        String prediction(int index);
        void prediction(int index, String text);
        long activeMillis();
        void addActiveMillis(long duration);
        boolean bookmarked();
        void bookmarked(boolean value);
        void openCompiler(String asset, boolean cpp);
        void closed();
        void finished(Result result);
    }

    static final class Result {
        final int correct;
        final int total;
        final long millis;
        Result(int correct, int total, long millis) {
            this.correct = correct;
            this.total = total;
            this.millis = millis;
        }
    }

    private final boolean light;
    private final LessonScript.Lesson lesson;
    private final boolean cpp;
    private final Session session;
    private final TextView heading;
    private final TextView railLabel;
    private final LinearLayout rail;
    private final LinearLayout canvas;
    private final ScrollView scroll;
    private final Button back;
    private final Button next;
    private final Button bookmark;
    private final Button skip;
    private android.widget.EditText prediction;
    private long activeSince = -1;
    private int index;
    private int selected = -1;
    private boolean resolved;
    private LinearLayout optionsBox;
    private TextView feedback;
    private View[] optionViews;


    BeatPlayer(Context context, boolean light, LessonScript.Lesson lesson, boolean cpp, Session session) {
        super(context);
        this.light = light;
        this.lesson = lesson;
        this.cpp = cpp;
        this.session = session;
        setBackgroundColor(Design.bg(light));
        index = Math.max(0, Math.min(lesson.beats.length - 1, session.beat()));

        setOnApplyWindowInsetsListener((view, insets) -> {
            setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        addView(column, new LayoutParams(-1, -1));

        LinearLayout bar = new LinearLayout(context);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Design.dp(context, 8), Design.dp(context, 6), Design.dp(context, 8), Design.dp(context, 6));
        Button close = chromeButton("\u2715", "Close lesson");
        close.setOnClickListener(v -> session.closed());
        bar.addView(close, new LinearLayout.LayoutParams(Design.dp(context, 46), Design.dp(context, 46)));

        heading = Design.text(context, lesson.title, 16, Design.ink(light), true);
        heading.setSingleLine(true);
        heading.setEllipsize(android.text.TextUtils.TruncateAt.END);
        heading.setPadding(Design.dp(context, 8), 0, Design.dp(context, 8), 0);
        bar.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));

        bookmark = chromeButton(session.bookmarked() ? "\u2605" : "\u2606", "Bookmark this lesson");
        bookmark.setTextColor(session.bookmarked() ? Design.amber(light) : Design.muted(light));
        bookmark.setOnClickListener(v -> {
            boolean value = !session.bookmarked();
            session.bookmarked(value);
            bookmark.setText(value ? "\u2605" : "\u2606");
            bookmark.setTextColor(value ? Design.amber(light) : Design.muted(light));
            bookmark.setContentDescription(value ? "Remove bookmark" : "Bookmark this lesson");
        });
        bar.addView(bookmark, new LinearLayout.LayoutParams(Design.dp(context, 46), Design.dp(context, 46)));
        column.addView(bar);

        rail = new LinearLayout(context);
        rail.setPadding(Design.dp(context, 20), 0, Design.dp(context, 20), Design.dp(context, 10));
        column.addView(rail);

        railLabel = Design.text(context, "", 11, Design.faint(light), true);
        railLabel.setPadding(Design.dp(context, 22), 0, Design.dp(context, 22), Design.dp(context, 6));
        railLabel.setLetterSpacing(.1f);
        column.addView(railLabel);

        scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        column.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        canvas = new LinearLayout(context);
        canvas.setOrientation(LinearLayout.VERTICAL);
        canvas.setPadding(Design.dp(context, 22), Design.dp(context, 4), Design.dp(context, 22), Design.dp(context, 24));
        scroll.addView(canvas, new ScrollView.LayoutParams(-1, -2));

        LinearLayout footer = new LinearLayout(context);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(Design.dp(context, 18), Design.dp(context, 8), Design.dp(context, 18), Design.dp(context, 14));
        back = Design.quiet(context, light, "Back");
        back.setOnClickListener(v -> move(-1));
        footer.addView(back, new LinearLayout.LayoutParams(0, Design.dp(context, 52), 1));
        skip = Design.quiet(context, light, "Skip");
        skip.setContentDescription("Skip reading to the next required question");
        skip.setOnClickListener(v -> {
            int target = index + 1;
            while (target < lesson.beats.length - 1 && !required(lesson.beats[target])) target++;
            move(target - index);
        });
        footer.addView(skip, new LinearLayout.LayoutParams(0, Design.dp(context, 52), 1));
        next = Design.primary(context, light, "Next");
        next.setOnClickListener(v -> advance());
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, Design.dp(context, 52), 2);
        nextParams.setMarginStart(Design.dp(context, 10));
        footer.addView(next, nextParams);
        column.addView(footer);

        render();
    }

    private Button chromeButton(String label, String description) {
        Button button = new Button(getContext(), null, android.R.attr.borderlessButtonStyle);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(18);
        button.setTextColor(Design.muted(light));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(0, 0, 0, 0);
        button.setContentDescription(description);
        return button;
    }

    private int dp(float value) { return Design.dp(getContext(), value); }

    private void move(int delta) {
        int target = index + delta;
        if (target < 0) {
            session.closed();
            return;
        }
        if (target >= lesson.beats.length) {
            finish();
            return;
        }
        index = target;
        session.beat(index);
        render();
    }

    private void advance() {
        Beat beat = lesson.beats[index];
        if (beat.scored() && !resolved) {
            if (selected >= 0) resolve(selected, beat);
            return;
        }
        if (beat.kind == Beat.Kind.HOOK && prediction.getText().toString().trim().isEmpty()) return;
        move(1);
    }

    private static boolean required(Beat beat) {
        return beat.scored() || beat.kind == Beat.Kind.HOOK;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (hasWindowFocus()) activeSince = android.os.SystemClock.elapsedRealtime();
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused && activeSince < 0) activeSince = android.os.SystemClock.elapsedRealtime();
        if (!focused) saveTime();
    }

    @Override protected void onDetachedFromWindow() {
        saveTime();
        super.onDetachedFromWindow();
    }

    private void saveTime() {
        if (activeSince < 0) return;
        session.addActiveMillis(android.os.SystemClock.elapsedRealtime() - activeSince);
        activeSince = -1;
    }

    private void finish() {
        int correct = 0, measured = 0;
        for (int i = 0; i < lesson.beats.length; i++) {
            if (!lesson.beats[i].scored()) continue;
            int first = session.firstAttempt(i);
            if (first >= 0) {
                measured++;
                correct += first;
            }
        }
        saveTime();
        session.finished(new Result(correct, measured, session.activeMillis()));
    }

    private void render() {
        Beat beat = lesson.beats[index];
        selected = -1;
        resolved = session.answered(index);
        prediction = null;

        heading.setText(lesson.title);
        railLabel.setText("LESSON " + (index + 1) + " OF " + lesson.beats.length + "  ·  " + beat.label().toUpperCase(java.util.Locale.ENGLISH));
        buildRail();

        canvas.removeAllViews();
        optionsBox = null;
        feedback = null;
        optionViews = null;

        switch (beat.kind) {
            case HOOK:
                canvas.addView(eyebrow("QUESTION", Design.amber(light)));
                canvas.addView(prose(beat.text, 21, Design.ink(light)));
                prediction = new android.widget.EditText(getContext());
                prediction.setHint("Your prediction");
                prediction.setTextColor(Design.ink(light));
                prediction.setHintTextColor(Design.muted(light));
                prediction.setMaxLines(3);
                prediction.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(1000)});
                prediction.setText(session.prediction(index));
                prediction.addTextChangedListener(new android.text.TextWatcher() {
                    public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
                    public void onTextChanged(CharSequence s,int start,int before,int count) {
                        session.prediction(index,s.toString());
                        enableAction(!s.toString().trim().isEmpty());
                    }
                    public void afterTextChanged(android.text.Editable s) {}
                });
                canvas.addView(prediction);
                break;
            case DIALOGUE:
                canvas.addView(eyebrow("DIALOGUE", Design.accent(light)));
                canvas.addView(dialogue(beat.speaker, beat.text));
                break;
            case DEFINITION:
                canvas.addView(eyebrow("DEFINITION", Design.accent(light)));
                canvas.addView(prose(beat.text, 17, Design.ink(light)));
                break;
            case EXPLAIN:
                canvas.addView(eyebrow("EXPLANATION", Design.muted(light)));
                canvas.addView(prose(beat.text, 17, Design.ink(light)));
                break;
            case VISUAL: {
                canvas.addView(eyebrow("DIAGRAM", Design.muted(light)));
                ConceptArt art = new ConceptArt(getContext(), light);
                art.setArt(beat.art);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                params.topMargin = dp(6);
                params.bottomMargin = dp(12);
                canvas.addView(art, params);
                canvas.addView(prose(beat.text, 14, Design.muted(light)));
                break;
            }
            case CODE: {
                canvas.addView(eyebrow("EXAMPLE", Design.accent(light)));
                addCode(beat.source);
                if (beat.output != null && !beat.output.isEmpty()) {
                    TextView out = Design.text(getContext(), "Output\n" + beat.output, 13, Design.muted(light), false);
                    out.setTypeface(Typeface.MONOSPACE);
                    out.setPadding(dp(14), dp(10), dp(14), dp(10));
                    out.setBackground(Design.outlined(getContext(), Design.surface(light), 12, Design.line(light)));
                    LinearLayout.LayoutParams outParams = new LinearLayout.LayoutParams(-1, -2);
                    outParams.topMargin = dp(10);
                    outParams.bottomMargin = dp(10);
                    canvas.addView(out, outParams);
                }
                addOpenCompiler(beat.source, "Open this example in the compiler");
                break;
            }
            case PREDICT:
            case CHECK: {
                canvas.addView(eyebrow(beat.kind == Beat.Kind.PREDICT ? "PREDICT THE RESULT" : "CHECK YOUR UNDERSTANDING",
                        Design.amber(light)));
                canvas.addView(prose(beat.prompt == null ? "" : beat.prompt, 17, Design.ink(light)));
                canvas.addView(prose(beat.kind == Beat.Kind.PREDICT
                        ? "Choose the outcome you expect before you run anything."
                        : "Select one answer.", 13, Design.faint(light)));
                addOptions(beat);
                break;
            }
            case TRY:
                canvas.addView(eyebrow("YOUR TURN", Design.accent(light)));
                canvas.addView(prose(beat.text, 17, Design.ink(light)));
                addCode(beat.source);
                addOpenCompiler(beat.source, "Open in the compiler and experiment");
                break;
            default:
                canvas.addView(eyebrow("RECAP", Design.accent(light)));
                canvas.addView(prose(beat.text, 17, Design.ink(light)));
                break;
        }

        back.setText(index == 0 ? "Close" : "Back");
        boolean last = index == lesson.beats.length - 1;
        boolean needsAnswer = beat.scored() && !resolved;
        next.setText(needsAnswer ? "Check" : last ? "Finish" : "Next");
        next.setContentDescription(next.getText());
        skip.setVisibility(required(beat) || last ? GONE : VISIBLE);
        enableAction(!needsAnswer && (prediction == null || !prediction.getText().toString().trim().isEmpty()));
        scroll.post(() -> scroll.scrollTo(0, 0));
    }

    private void buildRail() {
        rail.removeAllViews();
        for (int i = 0; i < lesson.beats.length; i++) {
            View segment = new View(getContext());
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(dp(3));
            shape.setColor(i < index ? Design.accent(light)
                    : i == index ? (light ? 0xff0b7d69 : 0xff9ff2df)
                    : (light ? 0xffdde6f2 : 0xff22304a));
            segment.setBackground(shape);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(i == index ? 5 : 4), 1);
            params.setMarginStart(i == 0 ? 0 : dp(4));
            rail.addView(segment, params);
        }
    }

    private TextView eyebrow(String value, int color) {
        TextView view = Design.eyebrow(getContext(), value, color);
        view.setPadding(0, 0, 0, dp(10));
        return view;
    }

    private TextView prose(String value, float size, int color) {
        TextView view = Design.text(getContext(), "", size, color, false);
        view.setText(RichText.format(value, light, false));
        view.setLineSpacing(dp(6), 1f);
        view.setTextIsSelectable(true);
        view.setPadding(0, 0, 0, dp(16));
        return view;
    }

    private View dialogue(String speaker, String body) {
        LinearLayout block = new LinearLayout(getContext());
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(16), dp(14), dp(16), dp(16));
        block.setBackground(Design.outlined(getContext(), Design.surface(light), 14, Design.line(light)));

        TextView who = Design.text(getContext(), speaker, 12, Design.accent(light), true);
        who.setPadding(0, 0, 0, dp(8));
        block.addView(who);

        TextView said = Design.text(getContext(), "", 17, Design.ink(light), false);
        said.setText("\u201C" + body + "\u201D");
        said.setLineSpacing(dp(6), 1f);
        said.setTextIsSelectable(true);
        block.addView(said);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(16);
        block.setLayoutParams(params);
        return block;
    }

    private void addCode(String asset) {
        String source;
        try {
            source = AssetText.read(getContext(), asset);
        } catch (Exception missing) {
            TextView error = Design.text(getContext(),
                    "This example could not be loaded from the offline bundle.", 14, Design.danger(light), false);
            canvas.addView(error);
            return;
        }
        TextView code = new TextView(getContext());
        SpannableString styled = new SpannableString(source.trim());
        SyntaxHighlighter.apply(styled, light);
        code.setText(styled);
        code.setTypeface(Typeface.MONOSPACE);
        code.setTextSize(13);
        code.setTextColor(Design.ink(light));
        code.setLineSpacing(dp(3), 1f);
        code.setTextIsSelectable(true);
        code.setPadding(dp(14), dp(14), dp(14), dp(14));
        code.setBackground(Design.outlined(getContext(), Design.code(light), 12, Design.line(light)));

        HorizontalScrollView horizontal = new HorizontalScrollView(getContext());
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.addView(code, new HorizontalScrollView.LayoutParams(-2, -2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(4);
        params.bottomMargin = dp(6);
        canvas.addView(horizontal, params);
    }

    private void addOpenCompiler(String asset, String label) {
        Button open = Design.quiet(getContext(), light, label);
        open.setOnClickListener(v -> session.openCompiler(asset, cpp));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(48));
        params.topMargin = dp(6);
        params.bottomMargin = dp(10);
        canvas.addView(open, params);
    }

    private void addOptions(Beat beat) {
        optionsBox = new LinearLayout(getContext());
        optionsBox.setOrientation(LinearLayout.VERTICAL);
        optionViews = new View[beat.options.length];

        for (int i = 0; i < beat.options.length; i++) {
            final int choice = i;
            LinearLayout option = new LinearLayout(getContext());
            option.setGravity(Gravity.CENTER_VERTICAL);
            option.setMinimumHeight(dp(56));
            option.setPadding(dp(14), dp(12), dp(14), dp(12));
            option.setFocusable(true);
            option.setContentDescription(beat.options[i]);
            option.setBackground(Design.pressable(getContext(),
                    Design.outlined(getContext(), Design.surface(light), 12, Design.line(light)), 0x3396b8ff));

            TextView marker = Design.text(getContext(), String.valueOf((char) ('A' + i)), 12,
                    Design.faint(light), true);
            marker.setGravity(Gravity.CENTER);
            marker.setBackground(Design.outlined(getContext(), Design.surfaceHigh(light), 18, Design.line(light)));
            option.addView(marker, new LinearLayout.LayoutParams(dp(32), dp(32)));

            TextView label = Design.text(getContext(), beat.options[i], 15, Design.ink(light), false);
            label.setPadding(dp(12), 0, 0, 0);
            option.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            option.setOnClickListener(v -> select(choice, beat));
            optionViews[i] = option;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(8);
            optionsBox.addView(option, params);
        }
        canvas.addView(optionsBox);

        feedback = Design.text(getContext(), "", 14, Design.muted(light), false);
        feedback.setPadding(dp(2), dp(10), dp(2), dp(14));
        feedback.setVisibility(GONE);
        feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        canvas.addView(feedback);

        if (resolved) {
            // Previously answered correctly: show the state and let the learner continue.
            markResolved(beat.answer, beat, true);
        }
    }

    private void select(int choice, Beat beat) {
        if (resolved) return;
        selected = choice;
        for (int i = 0; i < optionViews.length; i++) {
            boolean active = i == choice;
            optionViews[i].setSelected(active);
            optionViews[i].setBackground(Design.pressable(getContext(),
                    Design.outlined(getContext(), active ? Design.accentSoft(light) : Design.surface(light), 12,
                            active ? Design.accent(light) : Design.line(light)), 0x3396b8ff));
        }
        enableAction(true);
    }

    private void enableAction(boolean enabled) {
        next.setEnabled(enabled);
        next.setAlpha(enabled ? 1f : .45f);
    }

    private void resolve(int choice, Beat beat) {
        boolean correct = choice == beat.answer;
        session.answered(index, correct);
        if (correct) {
            resolved = true;
            markResolved(choice, beat, true);
            next.setText(index == lesson.beats.length - 1 ? "Finish" : "Next");
            next.setEnabled(true);
            next.setAlpha(1f);
            next.setContentDescription(index == lesson.beats.length - 1 ? "Finish lesson" : "Next");
        } else {
            resolved = false;
            for (int i = 0; i < optionViews.length; i++) {
                boolean chosen = i == choice;
                optionViews[i].setBackground(Design.pressable(getContext(),
                        Design.outlined(getContext(), chosen ? (light ? 0xfffdecea : 0xff3a1a19) : Design.surface(light),
                                12, chosen ? Design.danger(light) : Design.line(light)), 0x3396b8ff));
            }
            if (feedback != null) {
                feedback.setVisibility(VISIBLE);
                feedback.setTextColor(Design.danger(light));
                feedback.setText("Not yet. " + beat.explanation + "\n\nChoose again — the explanation above is the clue.");
            }
            selected = -1;
            enableAction(false);
        }
    }

    private void markResolved(int choice, Beat beat, boolean correct) {
        if (feedback != null) {
            feedback.setVisibility(VISIBLE);
            feedback.setTextColor(Design.SUCCESS);
            feedback.setText((correct ? "Correct. " : "") + beat.explanation);
        }
        if (optionViews != null) {
            for (int i = 0; i < optionViews.length; i++) {
                boolean right = i == beat.answer;
                optionViews[i].setBackground(Design.outlined(getContext(),
                        right ? (light ? 0xffe4f8ef : 0xff11302a) : Design.surface(light), 12,
                        right ? Design.SUCCESS : Design.line(light)));
            }
        }
        if (next != null) {
            next.setEnabled(true);
            next.setAlpha(1f);
        }
    }
}
