package com.example.slay_vault.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.notifications.ShowChronometerService;
import com.example.slay_vault.ui.DivaStrings;
import com.google.android.material.button.MaterialButton;

// Pantalla de cronometro del show.
public class ShowTimerFragment extends Fragment {

    private static final String PREF_TIMER_RUNNING = "show_timer_running";
    private static final String PREF_TIMER_ACCUMULATED_MS = "show_timer_accumulated_ms";
    private static final String PREF_TIMER_START_REALTIME = "show_timer_start_realtime";

    private Chronometer chronometer;
    private TextView statusText;
    private MaterialButton primaryButton;
    private MaterialButton resetButton;

    private boolean isRunning = false;
    private long accumulatedMs = 0L;
    private long startRealtime = 0L;

    public ShowTimerFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chronometer = view.findViewById(R.id.chronometer_show);
        statusText = view.findViewById(R.id.tv_show_timer_status);
        primaryButton = view.findViewById(R.id.btn_timer_primary);
        resetButton = view.findViewById(R.id.btn_timer_reset);
        TextView hintText = view.findViewById(R.id.tv_show_timer_hint);

        hintText.setText(DivaStrings.showTimerHint(requireContext()));

        if (getActivity() instanceof MainActivity
                && ((MainActivity) getActivity()).getSupportActionBar() != null) {
            ((MainActivity) getActivity()).getSupportActionBar()
                    .setTitle(DivaStrings.screenTitleShowTimer(requireContext()));
        }

        loadState();
        bindActions();
        refreshUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        persistState();
    }

    private void bindActions() {
        primaryButton.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else if (accumulatedMs > 0L) {
                resumeTimer();
            } else {
                startTimer();
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        accumulatedMs = 0L;
        startRealtime = SystemClock.elapsedRealtime();
        isRunning = true;
        chronometer.setBase(startRealtime);
        chronometer.start();
        startForegroundTimer();
        refreshUi();
    }

    private void resumeTimer() {
        startRealtime = SystemClock.elapsedRealtime() - accumulatedMs;
        isRunning = true;
        chronometer.setBase(startRealtime);
        chronometer.start();
        startForegroundTimer();
        refreshUi();
    }

    private void pauseTimer() {
        accumulatedMs = SystemClock.elapsedRealtime() - startRealtime;
        isRunning = false;
        chronometer.stop();
        stopForegroundTimer();
        refreshUi();
    }

    private void resetTimer() {
        isRunning = false;
        accumulatedMs = 0L;
        startRealtime = 0L;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        stopForegroundTimer();
        refreshUi();
    }

    private void refreshUi() {
        long elapsed = isRunning
                ? SystemClock.elapsedRealtime() - startRealtime
                : accumulatedMs;

        chronometer.setBase(SystemClock.elapsedRealtime() - elapsed);
        if (isRunning) {
            chronometer.start();
            primaryButton.setText(DivaStrings.timerPause(requireContext()));
            statusText.setText(DivaStrings.showTimerStatusRunning(requireContext()));
            resetButton.setVisibility(View.VISIBLE);
            return;
        }

        chronometer.stop();
        if (elapsed == 0L) {
            primaryButton.setText(DivaStrings.timerStart(requireContext()));
            statusText.setText(DivaStrings.showTimerStatusReady(requireContext()));
            resetButton.setVisibility(View.GONE);
        } else {
            primaryButton.setText(DivaStrings.timerResume(requireContext()));
            statusText.setText(DivaStrings.showTimerStatusPaused(requireContext()));
            resetButton.setVisibility(View.VISIBLE);
        }
        persistState();
    }

    private void loadState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        isRunning = prefs.getBoolean(PREF_TIMER_RUNNING, false);
        accumulatedMs = prefs.getLong(PREF_TIMER_ACCUMULATED_MS, 0L);
        startRealtime = prefs.getLong(PREF_TIMER_START_REALTIME, 0L);

        if (isRunning && startRealtime <= 0L) {
            isRunning = false;
            accumulatedMs = 0L;
        }
    }

    private void persistState() {
        long elapsed = isRunning
                ? SystemClock.elapsedRealtime() - startRealtime
                : accumulatedMs;

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean(PREF_TIMER_RUNNING, isRunning)
                .putLong(PREF_TIMER_ACCUMULATED_MS, elapsed)
                .putLong(PREF_TIMER_START_REALTIME, isRunning
                        ? SystemClock.elapsedRealtime() - elapsed
                        : 0L)
                .apply();
    }

    private void startForegroundTimer() {
        Intent serviceIntent = new Intent(requireContext(), ShowChronometerService.class);
        serviceIntent.setAction(ShowChronometerService.ACTION_START);
        requireContext().startForegroundService(serviceIntent);
    }

    private void stopForegroundTimer() {
        Intent serviceIntent = new Intent(requireContext(), ShowChronometerService.class);
        serviceIntent.setAction(ShowChronometerService.ACTION_STOP);
        requireContext().startService(serviceIntent);
    }
}

