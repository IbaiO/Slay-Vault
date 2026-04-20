package com.example.slay_vault.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.remote.AuthService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private final ExecutorService authExecutor = Executors.newSingleThreadExecutor();
    private final AuthService authService = new AuthService();

    private EditText etUsuario;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private ProgressBar progress;
    private Button btnRegister;
    private Button btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsuario = findViewById(R.id.et_usuario);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        progress = findViewById(R.id.progress_register);
        btnRegister = findViewById(R.id.btn_register_confirm);
        btnCancel = findViewById(R.id.btn_register_cancel);

        btnRegister.setOnClickListener(v -> performRegister());
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        authExecutor.shutdownNow();
        super.onDestroy();
    }

    private void performRegister() {
        String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, R.string.auth_register_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, R.string.auth_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        authExecutor.execute(() -> {
            try {
                AuthService.AuthResult result = authService.register(usuario, password);
                runOnUiThread(() -> {
                    if (!result.success || result.userId == null || result.userId.isEmpty()) {
                        setLoading(false);
                        Toast.makeText(this,
                                result.message == null || result.message.isEmpty()
                                        ? getString(R.string.auth_failed)
                                        : result.message,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    SessionManager.saveSession(this, result.userId, result.username);
                    syncAndContinue(result.userId);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    String message = e.getMessage();
                    Toast.makeText(this,
                            message == null || message.trim().isEmpty() ? getString(R.string.auth_failed) : message,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void syncAndContinue(@NonNull String userId) {
        authExecutor.execute(() -> {
            try {
                AuthService.SyncPayload payload = authService.fetchUserData(userId);
                SlayVaultDatabase db = SlayVaultDatabase.getInstance(getApplicationContext());
                db.shadeEntryDao().deleteAll();
                db.queenDao().deleteAll();
                db.queenDao().insertAll(payload.queens);
                db.shadeEntryDao().insertAll(payload.shades);
            } catch (Exception ignored) {
                // Si falla la sync inicial, se permite continuar.
            }
            runOnUiThread(() -> {
                setLoading(false);
                openMain();
            });
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        etUsuario.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
