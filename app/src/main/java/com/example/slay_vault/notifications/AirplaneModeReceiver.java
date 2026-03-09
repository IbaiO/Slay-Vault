package com.example.slay_vault.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;

// BroadcastReceiver que detecta la activación del modo avión y lanza una notificación + log.
public class AirplaneModeReceiver extends BroadcastReceiver {

    private static final String TAG = "SlayVault✈️";
    private static final String CHANNEL_ID = "slayvault_airplane_mode";
    private static final int NOTIFICATION_ID = 1337;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
            return;
        }

        boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
        if (!isAirplaneModeOn) {
            return;
        }

        String[] messages = context.getResources().getStringArray(R.array.airplane_mode_messages);
        int index = (int) ((System.currentTimeMillis() / 60_000) % messages.length);
        String message = messages[index];

        Log.d(TAG, "══════════════════════════════════════════");
        Log.d(TAG, "  ✈️  MODO AVIÓN DETECTADO  ✈️");
        Log.d(TAG, "  " + message);
        Log.d(TAG, "══════════════════════════════════════════");

        showAirplaneNotification(context, message);
    }

    private void showAirplaneNotification(Context context, String message) {
        ensureChannelCreated(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_makeup)
                .setContentTitle(context.getString(R.string.notification_airplane_title))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }

    private void ensureChannelCreated(Context context) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_airplane_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.notification_airplane_channel_desc));
        nm.createNotificationChannel(channel);
    }
}

