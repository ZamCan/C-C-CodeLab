package com.zamcan.codelab;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.Calendar;

/**
 * Offline daily study reminder. Disabled until the learner explicitly opts in.
 *
 * <p>Reminders are local Android alarms: they work without an account or network, are
 * re-registered after reboot or an app update, respect quiet hours (22:00–06:59) and use one
 * stable notification ID so repeated deliveries replace rather than stack.</p>
 */
public final class StudyReminders extends android.content.BroadcastReceiver {
    public static final String PREF = "study_reminders";
    private static final String CHANNEL = "codelab_study";
    private static final int REQUEST_CODE = 81;
    private static final int NOTIFICATION_ID = 81;

    public static void configure(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences("academy", Context.MODE_PRIVATE);
        settings.edit().putBoolean(PREF, enabled).apply();
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;
        PendingIntent intent = pending(context);
        alarms.cancel(intent);
        if (!enabled) return;
        int hour = settings.getInt("study_reminder_hour", 18);
        int minute = settings.getInt("study_reminder_minute", 0);
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            hour = 18;
            minute = 0;
        }
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 1);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, intent);
    }

    private static PendingIntent pending(Context context) {
        Intent intent = new Intent(context, StudyReminders.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences("academy", Context.MODE_PRIVATE);
        if (!settings.getBoolean(PREF, false)) return;
        String action = intent == null ? null : intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            configure(context, true);
            return;
        }
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour >= 22 || currentHour < 7) return; // Quiet hours: 22:00–06:59.

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        // minSdk 26: notification channels always exist.
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Learning reminders",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Daily offline study reminder from CodeLab.");
        manager.createNotificationChannel(channel);
        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) return;

        PendingIntent open = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class).putExtra("open_inbox",true),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_codelab_brand)
                .setContentTitle("CodeLab • keep learning")
                .setContentText(resumeMessage(settings))
                .setStyle(new Notification.BigTextStyle().bigText(resumeMessage(settings)))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(open)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
        NotificationInbox.add(context,"Study reminder",resumeMessage(settings));
    }

    /** Resume hint written by the lesson screen; never names another learner's content. */
    private static String resumeMessage(SharedPreferences settings) {
        String hint = settings.getString("resume_hint", "");
        if (hint != null && !hint.isEmpty() && hint.length() <= 80)
            return "Resume “" + hint + "” — a few minutes is enough.";
        return "Continue your C or C++ learning journey.";
    }
}
