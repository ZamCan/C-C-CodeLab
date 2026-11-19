package com.zamcan.codelab;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.Calendar;

/** Offline daily study reminder. Disabled until learner explicitly opts in. */
public final class StudyReminders extends android.content.BroadcastReceiver {
    public static final String PREF="study_reminders";
    private static final String CHANNEL="codelab_study";
    public static void configure(Context context,boolean enabled){
        context.getSharedPreferences("academy",Context.MODE_PRIVATE).edit().putBoolean(PREF,enabled).apply();
        AlarmManager alarms=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent=PendingIntent.getBroadcast(context,81,new Intent(context,StudyReminders.class),
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarms.cancel(intent);
        if(!enabled)return;
        Calendar next=Calendar.getInstance();
        android.content.SharedPreferences settings=context.getSharedPreferences("academy",Context.MODE_PRIVATE);
        int hour=settings.getInt("study_reminder_hour",18);
        int minute=settings.getInt("study_reminder_minute",0);
        if(hour<0||hour>23||minute<0||minute>59){hour=18;minute=0;}
        next.set(Calendar.HOUR_OF_DAY,hour);next.set(Calendar.MINUTE,minute);next.set(Calendar.SECOND,0);
        if(next.getTimeInMillis()<=System.currentTimeMillis())next.add(Calendar.DAY_OF_YEAR,1);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,next.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,intent);
    }
    @Override public void onReceive(Context context,Intent intent){
        if(!context.getSharedPreferences("academy",Context.MODE_PRIVATE).getBoolean(PREF,false))return;
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){configure(context,true);return;}
        int currentHour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(currentHour>=22 || currentHour<7)return; // Respect default 22:00–07:00 quiet hours.
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(
            new NotificationChannel(CHANNEL,"Learning reminders",NotificationManager.IMPORTANCE_DEFAULT));
        if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)return;
        PendingIntent open=PendingIntent.getActivity(context,0,new Intent(context,MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(context,CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("CodeLab • keep learning")
            .setContentText("Continue your C or C++ learning journey.")
            .setAutoCancel(true).setContentIntent(open).build();
        manager.notify(81,notification);
    }
}
