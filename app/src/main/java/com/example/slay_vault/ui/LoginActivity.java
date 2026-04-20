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
import androidx.appcompat.app.AppCompatActivity;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.remote.AuthService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private final ExecutorService authExecutor = Executors.newSingleThreadExecutor();
    private final AuthService authService = new AuthService();

    private EditText etUsuario;
    private EditText etPassword;
    private ProgressBar progress;
    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SessionManager.isLoggedIn(this)) {
            openMain();
            return;
        }

        etUsuario = findViewById(R.id.et_usuario);
        etPassword = findViewById(R.id.et_password);
        progress = findViewById(R.id.progress_auth);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(v -> performAuth(true));
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        authExecutor.shutdownNow();
        super.onDestroy();
    }

    private void performAuth(boolean login) {
        if (!login) {
            return;
        }
        String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.auth_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        authExecutor.execute(() -> {
            try {
                AuthService.AuthResult result = login
                        ? authService.login(usuario, password)
                        : authService.register(usuario, password);

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
                    FirebaseMessaging.getInstance().subscribeToTopic("divas_global");
                    syncAndContinue(result.userId);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void syncAndContinue(@NonNull String userId) {
        authExecutor.execute(() -> {
            try {
                AuthService.SyncPayload payload = authService.fetchUserData(userId);
                SlayVaultDatabase db = SlayVaultDatabase.getInstance(getApplicationContext());
                // Sincroniza la copia local con los datos remotos.
                db.shadeEntryDao().deleteAll();
                db.queenDao().deleteAll();
                db.queenDao().insertAll(payload.queens);
                db.shadeEntryDao().insertAll(payload.shades);
            } catch (Exception ignored) {
                // Si falla la sync, se permite continuar.
            }
            runOnUiThread(() -> {
                setLoading(false);
                openMain();
            });
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        etUsuario.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

