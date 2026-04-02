package com.example.slay_vault.ui;

import android.content.Context;

import androidx.preference.PreferenceManager;

import com.example.slay_vault.R;

// Textos de la app con soporte Diva Mode. Cuando está activo devuelve frases drag (siempre ES); si no, delega en strings.xml respetando el idioma elegido.
public class DivaStrings {

    public static final String KEY_DIVA_MODE = "diva_mode";

    public static boolean isDivaModeEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIVA_MODE, false);
    }

    // Navegación

    public static String screenTitleQueensList(Context ctx) {
        return isDivaModeEnabled(ctx) ? "👑 Mis Enemigas, Boobs" : ctx.getString(R.string.queens_list);
    }

    public static String screenTitleSettings(Context ctx) {
        return isDivaModeEnabled(ctx) ? "💅 El Camerino" : ctx.getString(R.string.settings);
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

    // Notificaciones de recordatorio

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

    // Toasts de feedback

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
}
