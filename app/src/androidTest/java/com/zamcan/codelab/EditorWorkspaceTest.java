package com.zamcan.codelab;

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/** Real Android home navigation, keyboard typing and accepting a completion in the scratch editor. */
@RunWith(AndroidJUnit4.class)
public final class EditorWorkspaceTest {
    @Test public void homeOpensCompilerAndCompletesPrintf() throws IOException {
        Instrumentation device = InstrumentationRegistry.getInstrumentation();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            Predicate<View> compilerTile = view -> view.isClickable()
                    && String.valueOf(view.getContentDescription()).startsWith("Compiler.");
            awaitVisible(scenario, compilerTile);
            capture(device, "home");
            scenario.onActivity(activity -> {
                View root = activity.findViewById(android.R.id.content);
                assertTrue(find(root, compilerTile).performClick());
                CodeEditor editor = (CodeEditor)find(root, view -> view instanceof CodeEditor);
                assertNotNull(editor);
                editor.setText("");
                assertTrue(editor.requestFocusFromTouch());
            });

            device.waitForIdleSync();
            device.sendStringSync("pri");
            Predicate<View> printf = view -> view instanceof Button
                    && "Insert printf".contentEquals(String.valueOf(view.getContentDescription()));
            awaitVisible(scenario, printf);
            capture(device, "editor");
            scenario.onActivity(activity -> {
                View root = activity.findViewById(android.R.id.content);
                assertTrue(find(root, printf).performClick());
                CodeEditor editor = (CodeEditor)find(root, view -> view instanceof CodeEditor);
                assertEquals("printf", editor.getText().toString());
                assertEquals(6, editor.getSelectionStart());
            });
        }
    }

    private static void awaitVisible(ActivityScenario<MainActivity> scenario, Predicate<View> match) {
        long deadline = SystemClock.uptimeMillis() + 10000;
        AtomicBoolean visible = new AtomicBoolean();
        do {
            scenario.onActivity(activity -> visible.set(activity.hasWindowFocus()
                    && find(activity.findViewById(android.R.id.content), match) != null));
            if (visible.get()) return;
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() < deadline);
        fail("Expected editor workspace control to be visible");
    }

    private static View find(View view, Predicate<View> match) {
        if (!view.isShown()) return null;
        if (match.test(view)) return view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View candidate = find(group.getChildAt(i), match);
                if (candidate != null) return candidate;
            }
        }
        return null;
    }

    private static void capture(Instrumentation device, String name) throws IOException {
        device.waitForIdleSync();
        Bitmap bitmap = device.getUiAutomation().takeScreenshot();
        assertNotNull("Emulator screenshot unavailable", bitmap);
        Context app = device.getTargetContext();
        File external = app.getExternalFilesDir(null);
        assertNotNull(external);
        File directory = new File(external, "ui-smoke");
        assertTrue(directory.isDirectory() || directory.mkdirs());
        try {
            try (OutputStream file = new FileOutputStream(new File(directory, name + ".png"))) {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, file));
            }
            // connectedDebugAndroidTest can uninstall the app before Actions pulls the PNGs.
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CodeLabTest");
                Uri output = app.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                assertNotNull(output);
                try (OutputStream image = app.getContentResolver().openOutputStream(output)) {
                    assertNotNull(image);
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, image));
                }
            }
        } finally {
            bitmap.recycle();
        }
    }
}
