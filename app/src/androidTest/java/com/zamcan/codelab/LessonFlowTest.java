package com.zamcan.codelab;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LessonFlowTest {
    @Test public void retryDoesNotEraseFirstAttemptAndReadingSkipsStopAtQuestions() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("academy", Context.MODE_PRIVATE).edit()
                .putBoolean("intro_checkpoint_v1", true).apply();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View root = activity.findViewById(android.R.id.content);
                control(root, v -> String.valueOf(v.getContentDescription()).endsWith("Resume your saved lesson")).performClick();
                assertFalse(button(root, "Next").isEnabled());
                ((EditText)control(root, v -> v instanceof EditText)).setText("C gives systems programmers control over memory.");
                click(root, "Next");
                click(root, "Skip");
                assertFalse(button(root, "Check").isEnabled());
                answer(root, "The final executable");
                assertFalse(button(root, "Check").isEnabled());
                answer(root, "An object file containing machine code");
                click(root, "Next");
                click(root, "Skip");
                answer(root, "C is compiled and gives direct access to memory");
                click(root, "Next");
                click(root, "Skip");
                click(root, "Finish");
                control(root, v -> v instanceof TextView && ((TextView)v).getText().toString().contains("50%  (1/2 first attempts)"));
            });
        }
    }

    private static void answer(View root, String option) {
        control(root,v -> option.contentEquals(String.valueOf(v.getContentDescription()))).performClick();
        click(root,"Check");
    }

    private static View button(View root, String text) {
        return control(root,v -> v instanceof android.widget.Button && text.contentEquals(((TextView)v).getText()));
    }

    private static void click(View root,String text) {
        View action = button(root,text);
        assertTrue(text, action.isEnabled());
        assertTrue(action.performClick());
    }

    private static View control(View root, Predicate<View> test) {
        View result = find(root,test);
        assertNotNull("Missing lesson control",result);
        return result;
    }

    private static View find(View view,Predicate<View> test) {
        if (view.getVisibility()!=View.VISIBLE) return null;
        if (test.test(view)) return view;
        if (view instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View result=find(group.getChildAt(i),test);
                if(result!=null)return result;
            }
        }
        return null;
    }
}
