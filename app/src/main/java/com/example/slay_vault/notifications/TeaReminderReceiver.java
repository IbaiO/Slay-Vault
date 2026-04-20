package com.example.slay_vault.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Recibe el alarm manager y relanza el recordatorio diario.
public class TeaReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        String action = intent.getAction();
        if (TeaReminderScheduler.ACTION_TEA_REMINDER.equals(action)) {
            LocalReminderNotifier.showTeaReminder(context);
            TeaReminderScheduler.syncReminder(context);
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            TeaReminderScheduler.syncReminder(context);
        }
    }
}

