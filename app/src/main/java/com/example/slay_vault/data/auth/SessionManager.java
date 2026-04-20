package com.example.slay_vault.data.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public final class SessionManager {

    private static final String KEY_USER_ID = "auth_user_id";
    private static final String KEY_USERNAME = "auth_username";

    private SessionManager() {
    }

    private static SharedPreferences prefs(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static void saveSession(@NonNull Context context, @NonNull String userId, @NonNull String username) {
        prefs(context).edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public static void clearSession(@NonNull Context context) {
        prefs(context).edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply();
    }

    public static boolean isLoggedIn(@NonNull Context context) {
        String userId = getUserId(context);
        return userId != null && !userId.trim().isEmpty();
    }

    public static String getUserId(@NonNull Context context) {
        return prefs(context).getString(KEY_USER_ID, null);
    }

    public static String getUsername(@NonNull Context context) {
        return prefs(context).getString(KEY_USERNAME, null);
    }

}

