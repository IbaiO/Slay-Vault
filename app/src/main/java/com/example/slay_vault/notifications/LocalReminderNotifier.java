package com.example.slay_vault.notifications;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.ui.DivaStrings;

// Gestiona canales y notificaciones locales.
public final class LocalReminderNotifier {

    public static final String CHANNEL_ID       = "slayvault_show_reminders";
    public static final String CHANNEL_CRUD_ID  = "slayvault_crud_events";
    public static final String PREF_ENABLE_NOTIFICATIONS = "enable_notifications";

    private static final int SHOW_REMINDER_NOTIFICATION_ID = 1001;
    private static final int CRUD_NOTIFICATION_ID          = 1002;

    private LocalReminderNotifier() { }

    // Crea los canales de notificación. Llamar desde onCreate de MainActivity
    public static void createChannel(@NonNull Context context) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch1 = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            ch1.setDescription(context.getString(R.string.notification_channel_description));
            ch1.enableVibration(true);
            ch1.setVibrationPattern(new long[]{0L, 350L, 180L, 350L});
            nm.createNotificationChannel(ch1);
        }

        if (nm.getNotificationChannel(CHANNEL_CRUD_ID) == null) {
            NotificationChannel ch2 = new NotificationChannel(
                    CHANNEL_CRUD_ID,
                    context.getString(R.string.notification_crud_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            ch2.setDescription(context.getString(R.string.notification_crud_channel_desc));
            nm.createNotificationChannel(ch2);
        }
    }

    @SuppressLint("MissingPermission")
    public static void showMakeupReminder(@NonNull Context context) {
        createChannel(context);
        if (!areNotificationsEffectivelyEnabled(context)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_makeup)
                .setContentTitle(DivaStrings.notificationMakeupTitle(context))
                .setContentText(DivaStrings.notificationMakeupBody(context))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(DivaStrings.notificationMakeupBody(context)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(openAppIntent(context))
                .setVibrate(new long[]{0L, 350L, 180L, 350L});

        NotificationManagerCompat.from(context).notify(SHOW_REMINDER_NOTIFICATION_ID, builder.build());
    }

    // Notifica que se ha creado una queen
    public static void notifyQueenCreated(@NonNull Context context, @NonNull String queenName) {
        postCrud(context,
                DivaStrings.notifQueenCreatedTitle(context),
                DivaStrings.notifQueenCreatedBody(context, queenName));
    }

    // Notifica que se ha editado una queen
    public static void notifyQueenUpdated(@NonNull Context context, @NonNull String queenName) {
        postCrud(context,
                DivaStrings.notifQueenUpdatedTitle(context),
                DivaStrings.notifQueenUpdatedBody(context, queenName));
    }

    // Notifica que se ha eliminado una queen
    public static void notifyQueenDeleted(@NonNull Context context, @NonNull String queenName) {
        postCrud(context,
                DivaStrings.notifQueenDeletedTitle(context),
                DivaStrings.notifQueenDeletedBody(context, queenName));
    }

    // Notifica que se ha añadido un shade
    public static void notifyShadeCreated(@NonNull Context context, @NonNull String shadeTitle) {
        postCrud(context,
                DivaStrings.notifShadeCreatedTitle(context),
                DivaStrings.notifShadeCreatedBody(context, shadeTitle));
    }

    // Notifica que se ha editado un shade
    public static void notifyShadeUpdated(@NonNull Context context, @NonNull String shadeTitle) {
        postCrud(context,
                DivaStrings.notifShadeUpdatedTitle(context),
                DivaStrings.notifShadeUpdatedBody(context, shadeTitle));
    }

    // Notifica que se ha eliminado un shade
    public static void notifyShadeDeleted(@NonNull Context context, @NonNull String shadeTitle) {
        postCrud(context,
                DivaStrings.notifShadeDeletedTitle(context),
                DivaStrings.notifShadeDeletedBody(context, shadeTitle));
    }

    @SuppressLint("MissingPermission")
    private static void postCrud(@NonNull Context context, String title, String body) {
        createChannel(context);
        if (!areNotificationsEffectivelyEnabled(context)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CRUD_ID)
                .setSmallIcon(R.drawable.ic_notification_makeup)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(openAppIntent(context));

        NotificationManagerCompat.from(context).notify(CRUD_NOTIFICATION_ID, builder.build());
    }

    public static boolean hasPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean areNotificationsEffectivelyEnabled(@NonNull Context context) {
        boolean userEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_ENABLE_NOTIFICATIONS, true);
        return userEnabled && NotificationManagerCompat.from(context).areNotificationsEnabled() && hasPermission(context);
    }

    private static PendingIntent openAppIntent(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
