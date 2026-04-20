package com.example.slay_vault.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

// Programa y cancela el recordatorio diario de te.
public final class TeaReminderScheduler {

    public static final String PREF_TEA_REMINDER_ENABLED = "tea_reminder_enabled";
    public static final String PREF_TEA_REMINDER_HOUR = "tea_reminder_hour";
    public static final String PREF_TEA_REMINDER_MINUTE = "tea_reminder_minute";

    static final String ACTION_TEA_REMINDER = "com.example.slay_vault.notifications.ACTION_TEA_REMINDER";

    private static final int REQUEST_CODE_TEA_REMINDER = 2407;
    private static final int DEFAULT_HOUR = 20;
    private static final int DEFAULT_MINUTE = 0;

    private TeaReminderScheduler() {
        // Utilidad estática.
    }

    public static boolean isEnabled(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_TEA_REMINDER_ENABLED, true);
    }

    public static int getReminderHour(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_TEA_REMINDER_HOUR, DEFAULT_HOUR);
    }

    public static int getReminderMinute(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_TEA_REMINDER_MINUTE, DEFAULT_MINUTE);
    }

    public static void setEnabled(@NonNull Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_TEA_REMINDER_ENABLED, enabled)
                .apply();
        syncReminder(context);
    }

    public static void setReminderTime(@NonNull Context context, int hour, int minute) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_TEA_REMINDER_HOUR, hour)
                .putInt(PREF_TEA_REMINDER_MINUTE, minute)
                .apply();
        syncReminder(context);
    }

    public static void syncReminder(@NonNull Context context) {
        if (!isReminderAllowed(context)) {
            cancel(context);
            return;
        }
        scheduleNext(context);
    }

    public static void cancel(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(createPendingIntent(context));
    }

    public static String getFormattedReminderTime(@NonNull Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, getReminderHour(context));
        calendar.set(Calendar.MINUTE, getReminderMinute(context));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return DateFormat.getTimeFormat(context).format(calendar.getTime());
    }

    private static void scheduleNext(@NonNull Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = createPendingIntent(context);
        alarmManager.cancel(pendingIntent);

        long triggerAtMillis = computeNextTriggerMillis(context);
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    private static long computeNextTriggerMillis(@NonNull Context context) {
        Calendar now = Calendar.getInstance();
        Calendar trigger = Calendar.getInstance();
        trigger.set(Calendar.HOUR_OF_DAY, getReminderHour(context));
        trigger.set(Calendar.MINUTE, getReminderMinute(context));
        trigger.set(Calendar.SECOND, 0);
        trigger.set(Calendar.MILLISECOND, 0);

        if (!trigger.after(now)) {
            trigger.add(Calendar.DAY_OF_YEAR, 1);
        }
        return trigger.getTimeInMillis();
    }

    private static boolean isReminderAllowed(@NonNull Context context) {
        return isEnabled(context) && LocalReminderNotifier.areNotificationsEffectivelyEnabled(context);
    }

    private static PendingIntent createPendingIntent(@NonNull Context context) {
        Intent intent = new Intent(context, TeaReminderReceiver.class);
        intent.setAction(ACTION_TEA_REMINDER);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_TEA_REMINDER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}

