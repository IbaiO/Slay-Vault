package com.example.slay_vault.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Cargador de foto de Queen: soporta URL remota, ruta local y drawable por nombre.
public final class QueenPhotoLoader {

    private static final ExecutorService IMAGE_EXECUTOR = Executors.newFixedThreadPool(2);

    private QueenPhotoLoader() {
    }

    public static void load(@NonNull ImageView target,
                            @Nullable String photoRef,
                            @DrawableRes int fallbackResId) {
        String normalized = photoRef != null ? photoRef.trim() : "";
        target.setTag(normalized);

        if (normalized.isEmpty()) {
            target.setImageResource(fallbackResId);
            return;
        }

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            target.setImageResource(fallbackResId);
            loadRemote(target, normalized, fallbackResId);
            return;
        }

        File file = new File(normalized);
        if (file.exists()) {
            target.setImageURI(null);
            target.setImageURI(Uri.fromFile(file));
            return;
        }

        int resId = target.getContext().getResources().getIdentifier(
                normalized,
                "drawable",
                target.getContext().getPackageName()
        );
        target.setImageResource(resId != 0 ? resId : fallbackResId);
    }

    private static void loadRemote(@NonNull ImageView target,
                                   @NonNull String url,
                                   @DrawableRes int fallbackResId) {
        IMAGE_EXECUTOR.execute(() -> {
            Bitmap bitmap;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(12000);
                connection.setDoInput(true);
                inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception ignored) {
                bitmap = null;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception ignored) {
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            final Bitmap finalBitmap = bitmap;
            target.post(() -> {
                Object currentTag = target.getTag();
                if (!url.equals(currentTag)) {
                    return;
                }
                if (finalBitmap != null) {
                    target.setImageBitmap(finalBitmap);
                } else {
                    target.setImageResource(fallbackResId);
                }
            });
        });
    }
}

