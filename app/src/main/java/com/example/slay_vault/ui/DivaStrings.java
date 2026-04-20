package com.example.slay_vault.ui;

import android.content.Context;

import androidx.preference.PreferenceManager;

import com.example.slay_vault.R;

import java.util.Locale;

// Textos de UI con soporte para Diva Mode.
public class DivaStrings {

    public static final String KEY_DIVA_MODE = "diva_mode";

    public static boolean isDivaModeEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIVA_MODE, false);
    }

    // Títulos de pantalla.

    public static String screenTitleQueensList(Context ctx) {
        return isDivaModeEnabled(ctx) ? "👑 Mis Enemigas, Boobs" : ctx.getString(R.string.queens_list);
    }

    public static String screenTitleSettings(Context ctx) {
        return isDivaModeEnabled(ctx) ? "💅 El Camerino" : ctx.getString(R.string.settings);
    }

    public static String screenTitleShowTimer(Context ctx) {
        return isDivaModeEnabled(ctx) ? "⏱️ Reloj del Show" : ctx.getString(R.string.show_timer_title);
    }

    // Diálogos add/edit

    public static String dialogAddQueenTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Nueva enemiga, honey 💋" : ctx.getString(R.string.dialog_add_queen_title);
    }

    public static String dialogEditQueenTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Editar lagarta 📝" : ctx.getString(R.string.dialog_edit_queen_title);
    }

    public static String dialogAddShadeTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Spill the tea ☕" : ctx.getString(R.string.dialog_add_shade_title);
    }

    public static String dialogEditShadeTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Editar el té ✏️" : ctx.getString(R.string.dialog_edit_shade_title);
    }

    public static String shadeSaveLocationLabel(Context ctx) {
        if (isDivaModeEnabled(ctx)) {
            return "Guardar ubicación del drama";
        }
        return isSpanish(ctx) ? "Guardar ubicación" : "Save location";
    }

    public static String shadeLocationNotSaved(Context ctx) {
        if (isDivaModeEnabled(ctx)) {
            return "Sin ubicación guardada.";
        }
        return isSpanish(ctx)
                ? "No se guardará ubicación para este shade."
                : "Location will not be saved for this shade.";
    }

    public static String shadeLocationLocating(Context ctx) {
        if (isDivaModeEnabled(ctx)) {
            return "Buscando coordenadas, reina...";
        }
        return isSpanish(ctx) ? "Buscando ubicación..." : "Fetching location...";
    }

    public static String shadeLocationCaptured(Context ctx, String coords) {
        if (isDivaModeEnabled(ctx)) {
            return "Ubicación fichada: " + coords;
        }
        return isSpanish(ctx)
                ? "Ubicación guardada: " + coords
                : "Saved location: " + coords;
    }

    public static String shadeLocationPermissionDenied(Context ctx) {
        if (isDivaModeEnabled(ctx)) {
            return "Sin permiso no puedo ubicar el tea.";
        }
        return isSpanish(ctx) ? "Permiso de ubicación denegado." : "Location permission denied.";
    }

    public static String shadeLocationUnavailable(Context ctx) {
        if (isDivaModeEnabled(ctx)) {
            return "No he encontrado ubicación ahora mismo.";
        }
        return isSpanish(ctx)
                ? "No se pudo obtener la ubicación actual."
                : "Could not get current location.";
    }

    public static String shadeLocationNeededToSave(Context ctx) {
        if (isDivaModeEnabled(ctx)) {
            return "Activa ubicación o desmarca la opción para guardar.";
        }
        return isSpanish(ctx)
                ? "Activa ubicación o desmarca la opción para guardar."
                : "Enable location or disable the save-location option.";
    }

    // Listas y estados vacíos

    public static String emptyStateQueens(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Aún no tienes enemigas... o sí las tienes y aún no lo sabes, honey. 😏"
                : ctx.getString(R.string.empty_state_queens);
    }

    public static String emptyStateShades(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "No hay té que derramar... todavía. ☕"
                : ctx.getString(R.string.empty_state_shades);
    }

    // Diálogo de borrado

    public static String dialogDeleteTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "¿Perdonar a esta lagarta? 🐍" : ctx.getString(R.string.dialog_delete_title);
    }

    public static String dialogDeleteMessage(Context ctx, String name) {
        return isDivaModeEnabled(ctx)
                ? "¿Seguro que quieres perdonar a " + name + "? Porque yo NO la perdonaría, pero tú mandas, reinona."
                : ctx.getString(R.string.dialog_delete_message, name);
    }

    public static String dialogDeleteConfirm(Context ctx) {
        return isDivaModeEnabled(ctx) ? "¡La perdono, pero no la olvido! 💅" : ctx.getString(R.string.dialog_slay);
    }

    public static String dialogDeleteCancel(Context ctx) {
        return isDivaModeEnabled(ctx) ? "¡No, que sigue en mi lista negra! 📋" : ctx.getString(R.string.dialog_sashay_away);
    }

    // Textos de notificaciones

    public static String notificationMakeupTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "¡EMERGENCIA DIVA! 🚨" : ctx.getString(R.string.notification_makeup_title);
    }

    public static String notificationMakeupBody(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Cariño, ponte el rímel YA. Tienes un show en 2 horas y no puedes llegar al escenario con esa cara."
                : ctx.getString(R.string.notification_makeup_body);
    }

    public static String notificationsDisabledDialogTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Aviso al camerino"
                : ctx.getString(R.string.notification_disabled_dialog_title);
    }

    public static String notificationsDisabledDialogMessage(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Sin notificaciones no hay rama, reinona. ¿Quieres activarlas ahora?"
                : ctx.getString(R.string.notification_disabled_dialog_message);
    }

    public static String notificationsDisabledDialogPositive(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Activalas ya 💅"
                : ctx.getString(R.string.notification_disabled_dialog_positive);
    }

    public static String notificationsDisabledDialogNegative(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Paso por ahora"
                : ctx.getString(R.string.notification_disabled_dialog_negative);
    }

    public static String actionShowTimerStart(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Cronometrar show"
                : ctx.getString(R.string.show_timer);
    }

    public static String actionShowTimerStop(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Cerrar show en vivo"
                : ctx.getString(R.string.action_show_timer_stop);
    }

    public static String showTimerHint(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Controla el numero para salir del escenario en el momento exacto, reina."
                : ctx.getString(R.string.show_timer_hint);
    }

    public static String showTimerStatusReady(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Lista para empezar el show."
                : ctx.getString(R.string.show_timer_status_ready);
    }

    public static String showTimerStatusRunning(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Show en vivo. Mantiene el ritmo."
                : ctx.getString(R.string.show_timer_status_running);
    }

    public static String showTimerStatusPaused(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Pausa tecnica. Puedes retomar o reiniciar."
                : ctx.getString(R.string.show_timer_status_paused);
    }

    public static String timerStart(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Iniciar" : ctx.getString(R.string.timer_start);
    }

    public static String timerPause(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Pausar" : ctx.getString(R.string.timer_pause);
    }

    public static String timerResume(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Reanudar" : ctx.getString(R.string.timer_resume);
    }

    public static String showTimerStarted(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Cronometro en marcha, reinona."
                : ctx.getString(R.string.show_timer_started);
    }

    public static String showTimerStopped(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Show cerrado. Respira y retoca."
                : ctx.getString(R.string.show_timer_stopped);
    }

    public static String notificationShowTimerTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Show en vivo"
                : ctx.getString(R.string.notification_show_timer_title);
    }

    public static String notificationShowTimerBody(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Cronometro activo. Que no se alargue el numerito."
                : ctx.getString(R.string.notification_show_timer_text);
    }

    public static String notificationShowTimerStop(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Cerrar"
                : ctx.getString(R.string.notification_show_timer_stop);
    }

    public static String widgetTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "La Hora del Té"
                : ctx.getString(R.string.widget_title);
    }

    public static String widgetNoShades(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Sin té por ahora. Hoy toca portarse bien."
                : ctx.getString(R.string.widget_no_shades);
    }

    public static String widgetLoginRequired(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Inicia sesión para ver el té del dia."
                : ctx.getString(R.string.widget_login_required);
    }

    public static String teaReminderTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "La Hora del Té diario"
                : ctx.getString(R.string.tea_reminder_title);
    }

    public static String teaReminderSummary(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Activa un aviso diario para repasar el tea del dia."
                : ctx.getString(R.string.tea_reminder_summary);
    }

    public static String teaReminderTimeTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Hora del Té"
                : ctx.getString(R.string.tea_reminder_time_title);
    }

    public static String teaReminderTimeSummary(Context ctx, String formattedTime) {
        if (isDivaModeEnabled(ctx)) {
            return "Se sirve cada dia a las " + formattedTime;
        }
        return ctx.getString(R.string.tea_reminder_time_summary, formattedTime);
    }

    public static String teaReminderNotificationTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "La Hora del Té"
                : ctx.getString(R.string.tea_reminder_notification_title);
    }

    public static String teaReminderNotificationBody(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Momento de repasar tus shades del dia, reinona."
                : ctx.getString(R.string.tea_reminder_notification_body);
    }

    public static String logoutTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Cerrar camerino" : ctx.getString(R.string.logout);
    }

    public static String logoutSummary(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Salir del club y volver al acceso, reinona"
                : ctx.getString(R.string.logout_summary);
    }

    public static String logoutConfirmTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "¿Bajas del escenario?"
                : ctx.getString(R.string.logout_confirm_title);
    }

    public static String logoutConfirmMessage(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Si cierras sesión, te mandamos directa al backstage. ¿Seguimos?"
                : ctx.getString(R.string.logout_confirm_message);
    }

    public static String logoutConfirmPositive(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Si, cierro el show"
                : ctx.getString(R.string.logout_confirm_positive);
    }

    public static String logoutConfirmNegative(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "No, me quedo"
                : ctx.getString(R.string.logout_confirm_negative);
    }

    // Notificaciones de eventos CRUD

    public static String notifQueenCreatedTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "💋 ¡Nueva lagarta fichada!" : ctx.getString(R.string.notification_crud_queen_created_title);
    }
    public static String notifQueenCreatedBody(Context ctx, String name) {
        return isDivaModeEnabled(ctx)
                ? name + " ya está en tu lista negra, reinona. Ojo con ella. 👁️"
                : ctx.getString(R.string.notification_crud_queen_created_body, name);
    }

    public static String notifQueenUpdatedTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "✏️ Dosier actualizado" : ctx.getString(R.string.notification_crud_queen_updated_title);
    }
    public static String notifQueenUpdatedBody(Context ctx, String name) {
        return isDivaModeEnabled(ctx)
                ? "El expediente de " + name + " ha sido retocado. Muy bien documentada, cariño."
                : ctx.getString(R.string.notification_crud_queen_updated_body, name);
    }

    public static String notifQueenDeletedTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "🕊️ Lagarta perdonada (por ahora)" : ctx.getString(R.string.notification_crud_queen_deleted_title);
    }
    public static String notifQueenDeletedBody(Context ctx, String name) {
        return isDivaModeEnabled(ctx)
                ? name + " ha sido borrada de la lista. Qué magnánima eres, honey. 😇"
                : ctx.getString(R.string.notification_crud_queen_deleted_body, name);
    }

    public static String notifShadeCreatedTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "☕ ¡El té está servido!" : ctx.getString(R.string.notification_crud_shade_created_title);
    }
    public static String notifShadeCreatedBody(Context ctx, String title) {
        return isDivaModeEnabled(ctx)
                ? "\"" + title + "\" ha sido añadido al Libro del Té. Delicioso. 🍵"
                : ctx.getString(R.string.notification_crud_shade_created_body, title);
    }

    public static String notifShadeUpdatedTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "✏️ El té ha sido recalentado" : ctx.getString(R.string.notification_crud_shade_updated_title);
    }
    public static String notifShadeUpdatedBody(Context ctx, String title) {
        return isDivaModeEnabled(ctx)
                ? "\"" + title + "\" ha sido perfeccionado. Cada detalle, cada drama. 💅"
                : ctx.getString(R.string.notification_crud_shade_updated_body, title);
    }

    public static String notifShadeDeletedTitle(Context ctx) {
        return isDivaModeEnabled(ctx) ? "🫗 El té ha sido derramado (y limpiado)" : ctx.getString(R.string.notification_crud_shade_deleted_title);
    }
    public static String notifShadeDeletedBody(Context ctx, String title) {
        return isDivaModeEnabled(ctx)
                ? "\"" + title + "\" borrado del Libro del Té. Qué lástima, era tan bueno ese cotilleo. 😢"
                : ctx.getString(R.string.notification_crud_shade_deleted_body, title);
    }

    // Mensajes de feedback.

    public static String toastDivaModeEnabled() {
        return "¡MODO DIVA TOTAL ACTIVADO, REINONA! El mundo tiembla. 👑🔥";
    }

    public static String toastDivaModeDisabled() {
        return "Modo normal. Qué aburrida, pero tú sabrás.";
    }

    // Exportación SAF

    public static String exportDefaultFilename(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "el_te_mas_caliente.txt"
                : ctx.getString(R.string.export_default_filename);
    }

    public static String exportSuccess(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "¡El Libro de las Sombras ha sido sellado, reinona! 📖✨"
                : ctx.getString(R.string.export_success);
    }

    public static String exportError(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Error al guardar. Ni el drama sale bien hoy, honey. 😤"
                : ctx.getString(R.string.export_error);
    }


    public static String exportFileHeader(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "═══════════════════════════════════════\n"
                  + "  ☕ EL LIBRO DEL TÉ MÁS CALIENTE ☕\n"
                  + "      (alias: Material para la abogada)\n"
                  + "═══════════════════════════════════════\n"
                : ctx.getString(R.string.export_file_header);
    }

    // Intents implícitos

    public static String buttonSearchQueen(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "🔍 ¡Espíame a esta lagarta!"
                : ctx.getString(R.string.btn_search_queen);
    }

    public static String searchQueenFallback(Context ctx) {
        return isDivaModeEnabled(ctx) ? "lagarta envidiosa" : ctx.getString(R.string.search_queen_fallback);
    }

    public static String buttonAssignAnthem(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Asignar himno del drama"
                : ctx.getString(R.string.assign_anthem);
    }

    public static String buttonPlayAnthem(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Reproducir himno"
                : ctx.getString(R.string.play_anthem);
    }

    public static String anthemSelectTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Elige el himno de esta rival"
                : ctx.getString(R.string.anthem_select_title);
    }

    public static String anthemPermissionDenied(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Sin permiso de audio no hay playlist, reinona."
                : ctx.getString(R.string.anthem_permission_denied);
    }

    public static String anthemNoSongsFound(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "No encontré canciones en este móvil, drama total."
                : ctx.getString(R.string.anthem_no_songs_found);
    }

    public static String anthemAssigned(Context ctx, String songTitle) {
        return isDivaModeEnabled(ctx)
                ? "Himno fichado: " + songTitle
                : ctx.getString(R.string.anthem_assigned, songTitle);
    }

    public static String anthemMissing(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Esta rival sigue sin himno asignado."
                : ctx.getString(R.string.anthem_missing);
    }

    public static String anthemPlaybackError(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "No pude reproducir el himno."
                : ctx.getString(R.string.anthem_playback_error);
    }

    public static String anthemUnknownTitle(Context ctx) {
        return isDivaModeEnabled(ctx)
                ? "Canción sin titulo"
                : ctx.getString(R.string.anthem_unknown_title);
    }

    public static String globalShoutTitle(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_title);
        }
        return isSpanish(ctx) ? "Grito de Guerra" : "Battle Cry";
    }

    public static String globalShoutSubtitle(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_subtitle);
        }
        return isSpanish(ctx)
                ? "Lanza un mensaje global y deja claro quien manda, reinona."
                : "Drop a global message and remind everyone who runs this stage.";
    }

    public static String globalShoutHint(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_hint);
        }
        return isSpanish(ctx) ? "Suelta el tea global..." : "Spill your global tea...";
    }

    public static String globalShoutSend(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_send);
        }
        return isSpanish(ctx) ? "Enviar grito" : "Send battle cry";
    }

    public static String globalShoutMessageRequired(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_message_required);
        }
        return isSpanish(ctx)
                ? "No puedes mandar silencio, reina."
                : "Silence is not a message, queen.";
    }

    public static String globalShoutSent(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_sent);
        }
        return isSpanish(ctx) ? "Grito enviado. Que empiece el drama." : "Battle cry sent. Let the drama begin.";
    }

    public static String globalShoutSendFailed(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_send_failed);
        }
        return isSpanish(ctx)
                ? "No se pudo mandar el grito. El backstage esta caido."
                : "Could not send your battle cry. Backstage is down.";
    }

    public static String globalShoutDefaultTitle(Context ctx) {
        if (!isDivaModeEnabled(ctx)) {
            return ctx.getString(R.string.global_shout_default_title);
        }
        return isSpanish(ctx) ? "Grito de Guerra" : "Battle Cry";
    }

    // Menús contextuales

    public static String actionEdit(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Retocar ✏️" : ctx.getString(R.string.action_edit);
    }

    public static String actionDelete(Context ctx) {
        return isDivaModeEnabled(ctx) ? "¡Tachar de la lista! 🗑️" : ctx.getString(R.string.action_delete);
    }

    public static String actionShare(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Difundir el cotilleo 📢" : ctx.getString(R.string.action_share);
    }

    public static String shareFooter(Context ctx, String queenName) {
        return isDivaModeEnabled(ctx)
                ? "☕ Cotilleo sobre " + queenName + " — SlayVault 👑💅"
                : ctx.getString(R.string.shade_share_footer, queenName);
    }

    public static String actionClose(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Cerrar el telón 🎭" : ctx.getString(R.string.action_close);
    }

    public static String actionViewStats(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Ver el dosier 🕵️" : ctx.getString(R.string.action_view_stats);
    }

    // Estadísticas

    public static String envyLevel(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Nivel de odio 😤" : ctx.getString(R.string.envy_level);
    }

    public static String shadeIntensity(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Intensidad del veneno 🐍" : ctx.getString(R.string.shade_intensity);
    }

    public static String statsTotalShades(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Shades lanzados 🎯" : ctx.getString(R.string.stats_total_shades);
    }

    public static String statsFavCategory(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Especialidad del veneno 🧪" : ctx.getString(R.string.stats_fav_category);
    }

    public static String statsLastShade(Context ctx) {
        return isDivaModeEnabled(ctx) ? "Último té servido ☕" : ctx.getString(R.string.stats_last_shade);
    }

    public static String statsRegisteredSince(Context ctx) {
        return isDivaModeEnabled(ctx) ? "En la lista negra desde 📋" : ctx.getString(R.string.stats_registered_since);
    }

    public static String dialogStatsTitle(Context ctx, String name) {
        return isDivaModeEnabled(ctx) ? "📊 Dosier de " + name : ctx.getString(R.string.queen_stats_toast, name);
    }

    private static boolean isSpanish(Context ctx) {
        Locale locale = ctx.getResources().getConfiguration().getLocales().get(0);
        return locale != null && "es".equalsIgnoreCase(locale.getLanguage());
    }
}
