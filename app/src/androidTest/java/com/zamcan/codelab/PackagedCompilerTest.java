package com.zamcan.codelab;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/** Exercises the installed APK, WebView asset interceptor, worker and WASI runtime together. */
@RunWith(AndroidJUnit4.class)
public class PackagedCompilerTest {
    @Test public void bundledRuntimePassesOnAndroid() {
        AtomicReference<String> report = new AtomicReference<>("");
        try (ActivityScenario<CompilerDiagnosticsActivity> scenario =
                     ActivityScenario.launch(CompilerDiagnosticsActivity.class)) {
            long deadline = SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(14);
            do {
                scenario.onActivity(activity -> {
                    View content = activity.findViewById(android.R.id.content);
                    report.set(readReport(content));
                });
                if (report.get().contains("Observed passes:")) break;
                SystemClock.sleep(500);
            } while (SystemClock.elapsedRealtime() < deadline);

            String result = report.get();
            Log.i("CodeLabCompilerTest", result);
            assertTrue(result, result.contains("PASS C input / output"));
            assertTrue(result, result.contains("PASS C++ containers"));
            assertTrue(result, result.contains("Observed passes: 6 / 6."));
        }
    }

    private static String readReport(View view) {
        if (view instanceof TextView) {
            String text = ((TextView) view).getText().toString();
            if (text.startsWith("Device: ")) return text;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                String report = readReport(group.getChildAt(index));
                if (!report.isEmpty()) return report;
            }
        }
        return "";
    }
}
