package com.example.slay_vault.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.ShadeEntryEntity;
import com.example.slay_vault.ui.DivaStrings;

import java.util.Date;
import java.util.List;
import java.util.Random;

// Widget de escritorio que muestra un shade aleatorio del usuario actual.
public class LatestShadeWidgetProvider extends AppWidgetProvider {

    private static final Random RANDOM = new Random();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (context == null || appWidgetManager == null || appWidgetIds == null || appWidgetIds.length == 0) {
            return;
        }
        updateWidgetsAsync(context.getApplicationContext(), appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        requestUpdate(context);
    }

    public static void requestUpdate(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
        ComponentName provider = new ComponentName(appContext, LatestShadeWidgetProvider.class);
        int[] widgetIds = manager.getAppWidgetIds(provider);
        if (widgetIds == null || widgetIds.length == 0) {
            return;
        }
        updateWidgetsAsync(appContext, manager, widgetIds);
    }

    private static void updateWidgetsAsync(@NonNull Context context,
                                           @NonNull AppWidgetManager manager,
                                           @NonNull int[] widgetIds) {
        SlayVaultDatabase.databaseExecutor.execute(() -> {
            String title = DivaStrings.widgetTitle(context);
            String body = DivaStrings.widgetNoShades(context);
            String footer = context.getString(
                    R.string.widget_last_update,
                    DateFormat.getTimeFormat(context).format(new Date()));

            String userId = SessionManager.getUserId(context);
            if (userId == null || userId.trim().isEmpty()) {
                body = DivaStrings.widgetLoginRequired(context);
            } else {
                List<ShadeEntryEntity> shades = SlayVaultDatabase
                        .getInstance(context)
                        .shadeEntryDao()
                        .getAllShadesListByUser(userId);
                if (shades != null && !shades.isEmpty()) {
                    ShadeEntryEntity shade = shades.get(RANDOM.nextInt(shades.size()));
                    body = formatShade(shade);
                }
            }

            for (int widgetId : widgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_latest_shade);
                views.setTextViewText(R.id.widget_title, title);
                views.setTextViewText(R.id.widget_shade_text, body);
                views.setTextViewText(R.id.widget_footer, footer);
                views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context));
                manager.updateAppWidget(widgetId, views);
            }
        });
    }

    @NonNull
    private static String formatShade(@NonNull ShadeEntryEntity shade) {
        String description = shade.getDescription();
        if (description == null || description.trim().isEmpty()) {
            return shade.getTitle();
        }
        return shade.getTitle() + "\n" + description;
    }

    @NonNull
    private static PendingIntent openAppIntent(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                9001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
