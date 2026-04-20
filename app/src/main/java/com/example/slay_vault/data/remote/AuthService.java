package com.example.slay_vault.data.remote;

import androidx.annotation.NonNull;

import com.example.slay_vault.BuildConfig;
import com.example.slay_vault.data.entities.QueenEntity;
import com.example.slay_vault.data.entities.ShadeEntryEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthService {

    // URL base de API configurada en local.properties.
    private static final String BASE_URL = BuildConfig.API_BASE_URL;

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final String userId;
        public final String username;


        public AuthResult(boolean success, String message, String userId, String username) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.username = username;
        }
    }

    public static class SyncPayload {
        public final List<QueenEntity> queens;
        public final List<ShadeEntryEntity> shades;

        public SyncPayload(List<QueenEntity> queens, List<ShadeEntryEntity> shades) {
            this.queens = queens;
            this.shades = shades;
        }
    }

    public AuthResult login(@NonNull String usuario, @NonNull String password) throws Exception {
        JSONObject response = postForm("auth_login.php", usuario, password);
        return parseAuthResponse(response);
    }

    public AuthResult register(@NonNull String usuario, @NonNull String password) throws Exception {
        JSONObject response = postForm("auth_register.php", usuario, password);
        return parseAuthResponse(response);
    }

    public AuthResult changePassword(@NonNull String userId,
                                     @NonNull String currentPassword,
                                     @NonNull String newPassword) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("current_password", currentPassword);
        params.put("new_password", newPassword);
        JSONObject response = postForm("update_password.php", params);
        return parseAuthResponse(response);
    }

    public AuthResult updateProfile(@NonNull String userId,
                                    @NonNull String username) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("usuario", username);
        JSONObject response = postForm("update_profile.php", params);
        return parseAuthResponse(response);
    }

    public SyncPayload fetchUserData(@NonNull String userId) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "sync_user_data.php?usuario=" + URLEncoder.encode(userId, "UTF-8"));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);

            int code = connection.getResponseCode();
            String body = readStream(code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());
            JSONObject json = new JSONObject(body);
            if (!json.optBoolean("success", false)) {
                throw new IOException(json.optString("message", "Sync error"));
            }

            List<QueenEntity> queens = new ArrayList<>();
            JSONArray queensArray = json.optJSONArray("queens");
            if (queensArray != null) {
                for (int i = 0; i < queensArray.length(); i++) {
                    JSONObject q = queensArray.getJSONObject(i);
                    QueenEntity queen = new QueenEntity();
                    queen.setId(q.optString("id"));
                    queen.setUserId(q.optString("user_id", userId));
                    queen.setName(q.optString("name"));
                    queen.setDescription(q.optString("description", ""));
                    queen.setPhotoUri(q.optString("photo_uri", ""));
                    queen.setEnvyLevel((float) q.optDouble("envy_level", 0f));
                    queen.setShadesCount(q.optInt("shades_count", 0));
                    queen.setLastShadeDate(q.optString("last_shade_date", null));
                    if (!q.isNull("song_id")) {
                        queen.setSongId(q.optLong("song_id"));
                    }
                    queen.setCreatedAt(new Date());
                    queen.setUpdatedAt(new Date());
                    queens.add(queen);
                }
            }

            List<ShadeEntryEntity> shades = new ArrayList<>();
            JSONArray shadesArray = json.optJSONArray("shades");
            if (shadesArray != null) {
                for (int i = 0; i < shadesArray.length(); i++) {
                    JSONObject s = shadesArray.getJSONObject(i);
                    ShadeEntryEntity shade = new ShadeEntryEntity();
                    shade.setId(s.optString("id"));
                    shade.setUserId(s.optString("user_id", userId));
                    shade.setQueenId(s.optString("queen_id"));
                    shade.setTitle(s.optString("title"));
                    shade.setDescription(s.optString("description", ""));
                    shade.setCategory(s.optString("category", "General"));
                    shade.setIntensity((float) s.optDouble("intensity", 0f));
                    // Usar la fecha del servidor (formato ISO8601: YYYY-MM-DD HH:MM:SS)
                    if (!s.isNull("date")) {
                        try {
                            String dateStr = s.optString("date");
                            if (dateStr != null && !dateStr.isEmpty()) {
                                // Parsear fecha ISO8601 del servidor MySQL
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
                                shade.setDate(sdf.parse(dateStr));
                            } else {
                                shade.setDate(new Date());
                            }
                        } catch (java.text.ParseException e) {
                            shade.setDate(new Date());
                        }
                    } else {
                        shade.setDate(new Date());
                    }
                    if (!s.isNull("latitude")) {
                        shade.setLatitude(s.optDouble("latitude"));
                    }
                    if (!s.isNull("longitude")) {
                        shade.setLongitude(s.optDouble("longitude"));
                    }
                    shade.setLocationAddress(s.optString("location_address", ""));
                    shade.setTags(null);
                    shade.setCreatedAt(new Date());
                    shade.setUpdatedAt(new Date());
                    shades.add(shade);
                }
            }

            return new SyncPayload(queens, shades);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void upsertQueen(@NonNull QueenEntity queen) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("id", queen.getId());
        params.put("user_id", queen.getUserId());
        params.put("name", queen.getName());
        params.put("description", safe(queen.getDescription()));
        params.put("photo_uri", safe(queen.getPhotoUri()));
        params.put("envy_level", String.valueOf(queen.getEnvyLevel()));
        params.put("shades_count", String.valueOf(queen.getShadesCount()));
        params.put("last_shade_date", safe(queen.getLastShadeDate()));
        params.put("song_id", queen.getSongId() == null ? "" : String.valueOf(queen.getSongId()));
        JSONObject json = postForm("upsert_queen.php", params);
        ensureSuccess(json, "Queen upsert failed");
    }

    public void deleteQueen(@NonNull String userId, @NonNull String queenId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("queen_id", queenId);
        JSONObject json = postForm("delete_queen.php", params);
        ensureSuccess(json, "Queen delete failed");
    }

    public void upsertShade(@NonNull ShadeEntryEntity shade) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("id", shade.getId());
        params.put("user_id", shade.getUserId());
        params.put("queen_id", shade.getQueenId());
        params.put("title", shade.getTitle());
        params.put("description", safe(shade.getDescription()));
        params.put("category", safe(shade.getCategory()));
        params.put("intensity", String.valueOf(shade.getIntensity()));
        long dateMs = shade.getDate() != null ? shade.getDate().getTime() : 0L;
        params.put("date_ms", String.valueOf(dateMs));
        params.put("latitude", shade.getLatitude() == null ? "" : String.valueOf(shade.getLatitude()));
        params.put("longitude", shade.getLongitude() == null ? "" : String.valueOf(shade.getLongitude()));
        params.put("location_address", safe(shade.getLocationAddress()));
        JSONObject json = postForm("upsert_shade.php", params);
        ensureSuccess(json, "Shade upsert failed");
    }

    public void deleteShade(@NonNull String userId, @NonNull String shadeId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("shade_id", shadeId);
        JSONObject json = postForm("delete_shade.php", params);
        ensureSuccess(json, "Shade delete failed");
    }

    public String uploadQueenPhoto(@NonNull String userId,
                                   @NonNull String queenId,
                                   @NonNull String photoBase64) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "upload_queen_photo.php");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String payload = "user_id=" + URLEncoder.encode(userId, "UTF-8")
                    + "&queen_id=" + URLEncoder.encode(queenId, "UTF-8")
                    + "&image_base64=" + URLEncoder.encode(photoBase64, "UTF-8");

            try (OutputStream os = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(payload);
                writer.flush();
            }

            int code = connection.getResponseCode();
            String body = readStream(code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());
            JSONObject json = new JSONObject(body);
            if (!json.optBoolean("success", false)) {
                throw new IOException(json.optString("message", "Photo upload failed"));
            }
            return json.optString("photo_url", "");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONObject postForm(String endpoint, String usuario, String password) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("usuario", usuario);
        params.put("password", password);
        return postForm(endpoint, params);
    }

    private JSONObject postForm(String endpoint, Map<String, String> params) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder payloadBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (payloadBuilder.length() > 0) {
                    payloadBuilder.append('&');
                }
                payloadBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                payloadBuilder.append('=');
                payloadBuilder.append(URLEncoder.encode(safe(entry.getValue()), "UTF-8"));
            }

            try (OutputStream os = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(payloadBuilder.toString());
                writer.flush();
            }

            int code = connection.getResponseCode();
            String body = readStream(code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());
            try {
                return new JSONObject(body);
            } catch (JSONException jsonException) {
                throw new IOException("Invalid server response from " + endpoint + ": " + abbreviate(body), jsonException);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private AuthResult parseAuthResponse(JSONObject json) {
        boolean success = json.optBoolean("success", false);
        String message = json.optString("message", "");
        JSONObject user = json.optJSONObject("user");
        String userId = user != null ? user.optString("id", "") : "";
        String username = user != null ? user.optString("usuario", "") : "";
        return new AuthResult(success, message, userId, username);
    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "{}";
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

    private void ensureSuccess(JSONObject json, String fallbackMessage) throws IOException {
        if (json == null || !json.optBoolean("success", false)) {
            String message = json != null ? json.optString("message", fallbackMessage) : fallbackMessage;
            throw new IOException(message);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String abbreviate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "(empty body)";
        }
        String singleLine = value.replace('\n', ' ').replace('\r', ' ').trim();
        return singleLine.length() <= 200 ? singleLine : singleLine.substring(0, 200) + "...";
    }
}

