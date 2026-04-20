package com.example.slay_vault.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.remote.AuthService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileEditActivity extends AppCompatActivity {

    private final ExecutorService profileExecutor = Executors.newSingleThreadExecutor();
    private final AuthService authService = new AuthService();

    private EditText etUsername;
    private ProgressBar progress;
    private Button btnSave;
    private Button btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        String userId = SessionManager.getUserId(this);
        if (userId == null || userId.trim().isEmpty()) {
            finish();
            return;
        }

        etUsername = findViewById(R.id.et_profile_username);
        progress = findViewById(R.id.progress_profile);
        btnSave = findViewById(R.id.btn_profile_save);
        btnCancel = findViewById(R.id.btn_profile_cancel);

        String currentUsername = SessionManager.getUsername(this);
        if (currentUsername != null) {
            etUsername.setText(currentUsername);
            etUsername.setSelection(currentUsername.length());
        }

        btnSave.setOnClickListener(v -> updateUsername());
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        profileExecutor.shutdownNow();
        super.onDestroy();
    }

    private void updateUsername() {
        String userId = SessionManager.getUserId(this);
        if (userId == null || userId.trim().isEmpty()) {
            finish();
            return;
        }

        String newUsername = etUsername.getText() != null
                ? etUsername.getText().toString().trim() : "";
        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, R.string.auth_username_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUsername = SessionManager.getUsername(this);
        if (currentUsername != null && currentUsername.equals(newUsername)) {
            finish();
            return;
        }

        setLoading(true);
        profileExecutor.execute(() -> {
            try {
                AuthService.AuthResult result = authService.updateProfile(userId, newUsername);
                runOnUiThread(() -> {
                    setLoading(false);
                    if (!result.success || result.userId == null || result.userId.isEmpty()) {
                        Toast.makeText(this,
                                result.message == null || result.message.isEmpty()
                                        ? getString(R.string.profile_update_failed)
                                        : result.message,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    SessionManager.saveSession(this, result.userId, result.username);
                    Toast.makeText(this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    String message = e.getMessage();
                    Toast.makeText(this,
                            message == null || message.trim().isEmpty() ? getString(R.string.profile_update_failed) : message,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        etUsername.setEnabled(!loading);
        btnSave.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
    }
}

