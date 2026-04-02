package com.example.slay_vault.ui.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.example.slay_vault.R;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;

// Fragment que muestra los ajustes de la aplicación (Diva Settings).
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String KEY_ENABLE_NOTIFICATIONS = LocalReminderNotifier.PREF_ENABLE_NOTIFICATIONS;
    public static final String KEY_DIVA_MODE = "diva_mode";
    private static final String KEY_LANGUAGE = "app_language";

    private SwitchPreferenceCompat notificationsSwitch;
    private final SharedPreferences.OnSharedPreferenceChangeListener notificationPrefListener =
            (sharedPreferences, key) -> {
                if (!KEY_ENABLE_NOTIFICATIONS.equals(key) || !isAdded()) {
                    return;
                }
                syncNotificationSwitchState();
            };

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isAdded() || notificationsSwitch == null) {
                    return;
                }

                setNotificationsPreferenceEnabled(isGranted);
                syncNotificationSwitchState();
                if (isGranted) {
                    LocalReminderNotifier.showMakeupReminder(requireContext());
                    Toast.makeText(requireContext(), R.string.notification_enabled_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    public SettingsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (getActivity() != null) {
            getActivity().setTitle(DivaStrings.screenTitleSettings(requireContext()));
        }

        notificationsSwitch = findPreference(KEY_ENABLE_NOTIFICATIONS);
        if (notificationsSwitch != null) {
            notificationsSwitch.setOnPreferenceChangeListener(this::onNotificationsPreferenceChanged);
        }

        SwitchPreferenceCompat divaSwitch = findPreference(KEY_DIVA_MODE);
        if (divaSwitch != null) {
            divaSwitch.setOnPreferenceChangeListener(this::onDivaModePreferenceChanged);
        }

        ListPreference languagePref = findPreference(KEY_LANGUAGE);
        if (languagePref != null) {
            LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
            if (!current.isEmpty()) {
                String lang = current.get(0).getLanguage();
                languagePref.setValue(lang);
            } else {
                    languagePref.setValue("es");
            }
            languagePref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());

            languagePref.setOnPreferenceChangeListener((pref, newValue) -> {
                String langCode = (String) newValue;
                LocaleListCompat locales = "en".equals(langCode)
                        ? LocaleListCompat.forLanguageTags("en")
                        : LocaleListCompat.forLanguageTags("es");
                AppCompatDelegate.setApplicationLocales(locales);
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        syncNotificationSwitchState();
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(notificationPrefListener);
        syncNotificationSwitchState();
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .unregisterOnSharedPreferenceChangeListener(notificationPrefListener);
        super.onStop();
    }

    private boolean onNotificationsPreferenceChanged(Preference preference, Object newValue) {
        boolean enableRequested = Boolean.TRUE.equals(newValue);
        setNotificationsPreferenceEnabled(enableRequested);

        if (!enableRequested) {
            Toast.makeText(requireContext(), R.string.notification_disabled_toast, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!LocalReminderNotifier.hasPermission(requireContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            return false;
        }

        LocalReminderNotifier.showMakeupReminder(requireContext());
        Toast.makeText(requireContext(), R.string.notification_test_sent, Toast.LENGTH_SHORT).show();
        return true;
    }

    // Guarda el Modo Diva en SharedPreferences y muestra feedback al usuario
    private boolean onDivaModePreferenceChanged(Preference preference, Object newValue) {
        boolean divaEnabled = Boolean.TRUE.equals(newValue);
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean(KEY_DIVA_MODE, divaEnabled)
                .apply();
        if (divaEnabled) {
            Toast.makeText(requireContext(), DivaStrings.toastDivaModeEnabled(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), DivaStrings.toastDivaModeDisabled(), Toast.LENGTH_SHORT).show();
        }
        requireActivity().recreate();
        return true;
    }

    private void setNotificationsPreferenceEnabled(boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean(KEY_ENABLE_NOTIFICATIONS, enabled)
                .apply();
    }

    // Mantiene el estado visual del switch alineado con permisos + preferencia real.
    private void syncNotificationSwitchState() {
        if (!isAdded() || notificationsSwitch == null) {
            return;
        }
        notificationsSwitch.setChecked(LocalReminderNotifier.areNotificationsEffectivelyEnabled(requireContext()));
    }
}
