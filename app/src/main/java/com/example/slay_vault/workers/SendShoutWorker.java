package com.example.slay_vault.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.slay_vault.BuildConfig;
import com.example.slay_vault.ui.DivaStrings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SendShoutWorker extends Worker {

    private static final String TAG = "SendShoutWorker";

    public static final String INPUT_MESSAGE = "mensaje";
    public static final String INPUT_USERNAME = "usuario";
    public static final String OUTPUT_ERROR_MESSAGE = "error_message";

    public SendShoutWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        HttpURLConnection conn = null;
        try {
            String mensaje = getInputData().getString(INPUT_MESSAGE);
            String usuario = getInputData().getString(INPUT_USERNAME);

            if (mensaje == null || mensaje.trim().isEmpty() || usuario == null || usuario.trim().isEmpty()) {
                return Result.failure(new Data.Builder()
                        .putString(OUTPUT_ERROR_MESSAGE, DivaStrings.globalShoutSendFailed(getApplicationContext()))
                        .build());
            }

            String baseUrl = BuildConfig.API_BASE_URL;
            String endpoint = baseUrl.endsWith("/") ? "enviar_grito.php" : "/enviar_grito.php";
            URL url = new URL(baseUrl + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            String postData = "mensaje=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8.name())
                    + "&usuario=" + URLEncoder.encode(usuario, StandardCharsets.UTF_8.name());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String responseBody = readStream(responseCode >= 200 && responseCode < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                if (responseBody.trim().isEmpty()) {
                    return Result.success();
                }
                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (!json.has("success") || json.optBoolean("success", true)) {
                        return Result.success();
                    }
                    String message = json.optString("message", DivaStrings.globalShoutSendFailed(getApplicationContext()));
                    return Result.failure(new Data.Builder().putString(OUTPUT_ERROR_MESSAGE, message).build());
                } catch (Exception ignored) {
                    return Result.success();
                }
            }

            if (responseCode == 408 || responseCode == 429 || responseCode >= 500) {
                return Result.retry();
            }

            String errorMessage = parseErrorMessage(
                    responseBody,
                    DivaStrings.globalShoutSendFailed(getApplicationContext()));
            return Result.failure(new Data.Builder().putString(OUTPUT_ERROR_MESSAGE, errorMessage).build());
        } catch (IOException e) {
            Log.e(TAG, "Network error sending shout", e);
            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Error sending shout", e);
            return Result.failure(new Data.Builder()
                    .putString(OUTPUT_ERROR_MESSAGE, DivaStrings.globalShoutSendFailed(getApplicationContext()))
                    .build());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @NonNull
    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    @NonNull
    private String parseErrorMessage(@NonNull String responseBody, @NonNull String fallback) {
        try {
            JSONObject json = new JSONObject(responseBody);
            String message = json.optString("message", "");
            if (message.trim().isEmpty()) {
                message = json.optString("error", "");
            }
            if (!message.trim().isEmpty()) {
                return message;
            }
        } catch (Exception ignored) {
            // Ignore parse failures and use fallback.
        }
        return fallback;
    }
}

