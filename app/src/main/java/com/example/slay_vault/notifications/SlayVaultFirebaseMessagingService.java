package com.example.slay_vault.notifications;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.ui.DivaStrings;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

/**
 * Base FCM service for receiving token updates and data/notification messages.
 * Further behavior (global message persistence + local notification routing) will
 * be added in subsequent implementation steps.
 */
public class SlayVaultFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "SlayVaultFCM";
    private static final String TOPIC_GLOBAL = "divas_global";
    private static final String GLOBAL_CHANNEL_ID = "global_shout_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed: " + token);
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        if ((title == null || title.trim().isEmpty()) && remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
        }
        if ((body == null || body.trim().isEmpty()) && remoteMessage.getNotification() != null) {
            body = remoteMessage.getNotification().getBody();
        }

        if (title == null || title.trim().isEmpty()) {
            title = DivaStrings.globalShoutDefaultTitle(this);
        }
        if (body == null || body.trim().isEmpty()) {
            return;
        }

        boolean hasNotificationPayload = remoteMessage.getNotification() != null;
        if (isAppInForeground() || !hasNotificationPayload) {
            showLocalNotification(title, body);
        }
        Log.d(TAG, "FCM message received");
    }

    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (getPackageName().equals(appProcess.processName)) {
                return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
            }
        }
        return false;
    }

    private void showLocalNotification(@NonNull String title, @NonNull String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ensureNotificationChannel();

        Intent openApp = new Intent(this, MainActivity.class);
        openApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, GLOBAL_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this)
                .notify((int) (System.currentTimeMillis() & 0x7fffffff), builder.build());
    }

    private void ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(GLOBAL_CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                GLOBAL_CHANNEL_ID,
                getString(R.string.global_shout_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(getString(R.string.global_shout_channel_description));
        manager.createNotificationChannel(channel);
    }
}

