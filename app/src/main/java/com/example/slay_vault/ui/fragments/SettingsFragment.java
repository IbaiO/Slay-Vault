package com.example.slay_vault.ui.fragments;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.notifications.TeaReminderScheduler;
import com.example.slay_vault.ui.ChangePasswordActivity;
import com.example.slay_vault.ui.DivaStrings;
import com.example.slay_vault.ui.LoginActivity;

import java.util.Calendar;

// Fragment que muestra los ajustes de la aplicación (Diva Settings).
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String KEY_ENABLE_NOTIFICATIONS = LocalReminderNotifier.PREF_ENABLE_NOTIFICATIONS;
    public static final String KEY_DIVA_MODE = "diva_mode";
    private static final String KEY_LANGUAGE = "app_language";
    private static final String KEY_LOGOUT = "logout";
    private static final String KEY_CHANGE_PASSWORD = "change_password";
    private static final String KEY_TEA_REMINDER_ENABLED = TeaReminderScheduler.PREF_TEA_REMINDER_ENABLED;
    private static final String KEY_TEA_REMINDER_TIME = "tea_reminder_time";

    private SwitchPreferenceCompat notificationsSwitch;
    private SwitchPreferenceCompat teaReminderSwitch;
    private Preference teaReminderTimePreference;

    private final SharedPreferences.OnSharedPreferenceChangeListener reminderPrefListener =
            (sharedPreferences, key) -> {
                if (!isAdded()) {
                    return;
                }
                if (KEY_ENABLE_NOTIFICATIONS.equals(key)
                        || KEY_TEA_REMINDER_ENABLED.equals(key)
                        || TeaReminderScheduler.PREF_TEA_REMINDER_HOUR.equals(key)
                        || TeaReminderScheduler.PREF_TEA_REMINDER_MINUTE.equals(key)) {
                    syncNotificationSwitchState();
                    syncTeaReminderUiState();
                }
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
                    TeaReminderScheduler.syncReminder(requireContext());
                    Toast.makeText(requireContext(), R.string.notification_enabled_toast, Toast.LENGTH_SHORT).show();
                } else {
                    TeaReminderScheduler.syncReminder(requireContext());
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

        teaReminderSwitch = findPreference(KEY_TEA_REMINDER_ENABLED);
        teaReminderTimePreference = findPreference(KEY_TEA_REMINDER_TIME);
        if (teaReminderSwitch != null) {
            teaReminderSwitch.setTitle(DivaStrings.teaReminderTitle(requireContext()));
            teaReminderSwitch.setSummary(DivaStrings.teaReminderSummary(requireContext()));
            teaReminderSwitch.setOnPreferenceChangeListener(this::onTeaReminderPreferenceChanged);
        }
        if (teaReminderTimePreference != null) {
            teaReminderTimePreference.setTitle(DivaStrings.teaReminderTimeTitle(requireContext()));
            teaReminderTimePreference.setSummary(getTeaReminderTimeSummary());
            teaReminderTimePreference.setOnPreferenceClickListener(preference -> {
                showTeaReminderTimePicker();
                return true;
            });
        }

        SwitchPreferenceCompat divaSwitch = findPreference(KEY_DIVA_MODE);
        if (divaSwitch != null) {
            divaSwitch.setOnPreferenceChangeListener(this::onDivaModePreferenceChanged);
        }

        ListPreference languagePref = findPreference(KEY_LANGUAGE);
        if (languagePref != null) {
            LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
            if (!current.isEmpty()) {
                java.util.Locale locale = current.get(0);
                String lang = locale != null ? locale.getLanguage() : "es";
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

        Preference logoutPreference = findPreference(KEY_LOGOUT);
        if (logoutPreference != null) {
            logoutPreference.setTitle(DivaStrings.logoutTitle(requireContext()));
            logoutPreference.setSummary(DivaStrings.logoutSummary(requireContext()));
            logoutPreference.setOnPreferenceClickListener(preference -> {
                showLogoutConfirmation();
                return true;
            });
        }

        Preference changePasswordPreference = findPreference(KEY_CHANGE_PASSWORD);
        if (changePasswordPreference != null) {
            changePasswordPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        syncNotificationSwitchState();
        syncTeaReminderUiState();
        TeaReminderScheduler.syncReminder(requireContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(reminderPrefListener);
        syncNotificationSwitchState();
        syncTeaReminderUiState();
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .unregisterOnSharedPreferenceChangeListener(reminderPrefListener);
        super.onStop();
    }

    private boolean onNotificationsPreferenceChanged(Preference preference, Object newValue) {
        boolean enableRequested = Boolean.TRUE.equals(newValue);
        setNotificationsPreferenceEnabled(enableRequested);

        if (!enableRequested) {
            Toast.makeText(requireContext(), R.string.notification_disabled_toast, Toast.LENGTH_SHORT).show();
            TeaReminderScheduler.syncReminder(requireContext());
            return true;
        }

        if (!LocalReminderNotifier.hasPermission(requireContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            return false;
        }

        LocalReminderNotifier.showMakeupReminder(requireContext());
        TeaReminderScheduler.syncReminder(requireContext());
        Toast.makeText(requireContext(), R.string.notification_test_sent, Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean onTeaReminderPreferenceChanged(Preference preference, Object newValue) {
        boolean enabled = Boolean.TRUE.equals(newValue);
        TeaReminderScheduler.setEnabled(requireContext(), enabled);
        syncTeaReminderUiState();
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

    private void showTeaReminderTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, TeaReminderScheduler.getReminderHour(requireContext()));
        calendar.set(Calendar.MINUTE, TeaReminderScheduler.getReminderMinute(requireContext()));

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    TeaReminderScheduler.setReminderTime(requireContext(), hourOfDay, minute);
                    syncTeaReminderUiState();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(requireContext()));
        dialog.show();
    }

    private void syncTeaReminderUiState() {
        if (!isAdded()) {
            return;
        }

        boolean notificationsEffective = LocalReminderNotifier.areNotificationsEffectivelyEnabled(requireContext());
        boolean teaReminderEnabled = TeaReminderScheduler.isEnabled(requireContext());
        if (teaReminderSwitch != null) {
            teaReminderSwitch.setChecked(teaReminderEnabled);
            teaReminderSwitch.setEnabled(notificationsEffective);
        }
        if (teaReminderTimePreference != null) {
            teaReminderTimePreference.setEnabled(notificationsEffective && teaReminderEnabled);
            teaReminderTimePreference.setSummary(getTeaReminderTimeSummary());
        }
    }

    private String getTeaReminderTimeSummary() {
        return DivaStrings.teaReminderTimeSummary(requireContext(), TeaReminderScheduler.getFormattedReminderTime(requireContext()));
    }

    // Mantiene el estado visual del switch alineado con permisos + preferencia real.
    private void syncNotificationSwitchState() {
        if (!isAdded() || notificationsSwitch == null) {
            return;
        }
        notificationsSwitch.setChecked(LocalReminderNotifier.areNotificationsEffectivelyEnabled(requireContext()));
    }

    private void showLogoutConfirmation() {
        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(DivaStrings.logoutConfirmTitle(requireContext()))
                .setMessage(DivaStrings.logoutConfirmMessage(requireContext()))
                .setPositiveButton(DivaStrings.logoutConfirmPositive(requireContext()), (dialog, which) -> {
                    // Esperar a que se terminen las operaciones en background de Room
                    SlayVaultDatabase.databaseExecutor.execute(() -> {
                        SessionManager.clearSession(requireContext());
                        SlayVaultDatabase db = SlayVaultDatabase.getInstance(requireContext());
                        // Limpiar datos locales al logout
                        db.shadeEntryDao().deleteAll();
                        db.queenDao().deleteAll();
                        SlayVaultDatabase.closeDatabase();

                        requireActivity().runOnUiThread(() -> {
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        });
                    });
                })
                .setNegativeButton(DivaStrings.logoutConfirmNegative(requireContext()), null)
                .show();
    }
}
