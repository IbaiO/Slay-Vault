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

public class ChangePasswordActivity extends AppCompatActivity {

    private final ExecutorService profileExecutor = Executors.newSingleThreadExecutor();
    private final AuthService authService = new AuthService();

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private ProgressBar progress;
    private Button btnSave;
    private Button btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        String userId = SessionManager.getUserId(this);
        if (userId == null || userId.trim().isEmpty()) {
            finish();
            return;
        }

        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_new_password);
        progress = findViewById(R.id.progress_profile);
        btnSave = findViewById(R.id.btn_profile_save);
        btnCancel = findViewById(R.id.btn_profile_cancel);

        btnSave.setOnClickListener(v -> updatePassword());
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        profileExecutor.shutdownNow();
        super.onDestroy();
    }

    private void updatePassword() {
        String userId = SessionManager.getUserId(this);
        if (userId == null || userId.trim().isEmpty()) {
            finish();
            return;
        }

        String currentPassword = etCurrentPassword.getText() != null
                ? etCurrentPassword.getText().toString().trim() : "";
        String newPassword = etNewPassword.getText() != null
                ? etNewPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null
                ? etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, R.string.password_change_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, R.string.password_change_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        profileExecutor.execute(() -> {
            try {
                AuthService.AuthResult result = authService.changePassword(userId, currentPassword, newPassword);
                runOnUiThread(() -> {
                    setLoading(false);
                    if (!result.success) {
                        Toast.makeText(this,
                                result.message == null || result.message.isEmpty()
                                        ? getString(R.string.password_update_failed)
                                        : result.message,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(this, R.string.password_update_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    String message = e.getMessage();
                    Toast.makeText(this,
                            message == null || message.trim().isEmpty() ? getString(R.string.password_update_failed) : message,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        etCurrentPassword.setEnabled(!loading);
        etNewPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        btnSave.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
    }
}

