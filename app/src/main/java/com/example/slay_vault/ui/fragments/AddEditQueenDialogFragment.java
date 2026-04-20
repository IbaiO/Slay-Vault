package com.example.slay_vault.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.text.TextUtils;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.QueenEntity;
import com.example.slay_vault.data.remote.AuthService;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.example.slay_vault.ui.utils.QueenPhotoLoader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

// Diálogo para añadir o editar una Queen. Gestiona foto por galería/cámara, validación y persistencia en Room.
public class AddEditQueenDialogFragment extends DialogFragment {

    public static final String RESULT_KEY  = "add_edit_queen_result";
    public static final String RESULT_DONE = "result_done";

    private static final String ARG_QUEEN_ID = "arg_queen_id";

    // URI temporal seleccionada por el usuario.
    private Uri selectedImageUri = null;
    // Ruta interna de foto ya guardada.
    private String existingPhotoPath = null;

    private ShapeableImageView photoPreview;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
    private Uri cameraTempUri;

    // Crea el diálogo en modo añadir
    public static AddEditQueenDialogFragment newInstance() {
        return new AddEditQueenDialogFragment();
    }

    // Crea el diálogo en modo editar para la queen con ese ID
    public static AddEditQueenDialogFragment newInstance(String queenId) {
        AddEditQueenDialogFragment f = new AddEditQueenDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUEEN_ID, queenId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        photoPreview.setImageURI(uri);
                        Toast.makeText(requireContext(),
                                getString(R.string.photo_selected), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && cameraTempUri != null) {
                        selectedImageUri = cameraTempUri;
                        photoPreview.setImageURI(selectedImageUri);
                        Toast.makeText(requireContext(),
                                getString(R.string.photo_selected), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_queen, null, false);

        photoPreview                    = view.findViewById(R.id.iv_queen_photo_preview);
        MaterialButton btnPickPhoto     = view.findViewById(R.id.btn_pick_photo);
        TextInputLayout  tilName        = view.findViewById(R.id.til_queen_name);
        TextInputEditText etName        = view.findViewById(R.id.et_queen_name);
        TextInputEditText etDescription = view.findViewById(R.id.et_queen_description);
        RatingBar         rbEnvy        = view.findViewById(R.id.rb_envy_level);

        String queenId = getArguments() != null ? getArguments().getString(ARG_QUEEN_ID) : null;
        boolean isEdit = queenId != null;

        btnPickPhoto.setOnClickListener(v -> showPhotoSourceDialog());

        if (isEdit) {
            SlayVaultDatabase.databaseExecutor.execute(() -> {
                final String userId = SessionManager.getUserId(requireContext());
                if (userId == null || userId.trim().isEmpty()) {
                    Toast.makeText(requireContext(), R.string.auth_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                QueenEntity entity = SlayVaultDatabase
                        .getInstance(requireContext().getApplicationContext())
                        .queenDao()
                        .getQueenByIdSyncForUser(queenId, userId);
                if (entity != null) {
                    existingPhotoPath = entity.getPhotoUri();
                    requireActivity().runOnUiThread(() -> {
                        etName.setText(entity.getName());
                        etDescription.setText(entity.getDescription());
                        rbEnvy.setRating(entity.getEnvyLevel());
                            loadExistingPhoto(existingPhotoPath);
                    });
                }
            });
        }

        String title = isEdit
                ? DivaStrings.dialogEditQueenTitle(requireContext())
                : DivaStrings.dialogAddQueenTitle(requireContext());

        view.findViewById(R.id.btn_cancel_queen).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btn_save_queen).setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                tilName.setError(getString(R.string.error_name_required));
                return;
            }
            tilName.setError(null);
            String desc = etDescription.getText() != null
                    ? etDescription.getText().toString().trim() : "";
            float envy = rbEnvy.getRating();
            saveQueen(isEdit, queenId, name, desc, envy);
        });

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(view)
                .create();
        dialog.setCancelable(true);
        return dialog;
    }

    // Carga la foto existente en la vista previa.
    @SuppressWarnings("DiscouragedApi")
    private void loadExistingPhoto(String photoPath) {
        QueenPhotoLoader.load(photoPreview, photoPath, R.mipmap.ic_launcher_round);
    }

    private void showPhotoSourceDialog() {
        String[] options = {
                getString(R.string.photo_source_gallery),
                getString(R.string.photo_source_camera)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.photo_source_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageLauncher.launch("image/*");
                    } else {
                        launchCameraCapture();
                    }
                })
                .show();
    }

    private void launchCameraCapture() {
        try {
            File cameraDir = new File(requireContext().getCacheDir(), "camera_photos");
            if (!cameraDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cameraDir.mkdirs();
            }
            File photoFile = File.createTempFile("queen_", ".jpg", cameraDir);
            cameraTempUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            takePhotoLauncher.launch(cameraIntent);
        } catch (IOException e) {
            Toast.makeText(requireContext(), R.string.photo_camera_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQueen(boolean isEdit, @Nullable String queenId,
                           String name, String desc, float envy) {
        final String userId = SessionManager.getUserId(requireContext());
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.auth_required, Toast.LENGTH_SHORT).show();
            return;
        }

        final String resolvedQueenId = (isEdit && queenId != null)
                ? queenId
                : UUID.randomUUID().toString();
        final String newPhotoPath = copyImageToInternalStorage(selectedImageUri, resolvedQueenId);

        SlayVaultDatabase db = SlayVaultDatabase
                .getInstance(requireContext().getApplicationContext());
        AuthService authService = new AuthService();

        SlayVaultDatabase.databaseExecutor.execute(() -> {
            String finalPhotoPath = isEdit ? existingPhotoPath : null;
            if (newPhotoPath != null) {
                finalPhotoPath = newPhotoPath;
                String base64Photo = encodeImageAsBase64(selectedImageUri);
                if (base64Photo != null && !base64Photo.isEmpty()) {
                    try {
                        String remoteUrl = authService.uploadQueenPhoto(userId, resolvedQueenId, base64Photo);
                        if (remoteUrl != null && !remoteUrl.trim().isEmpty()) {
                            finalPhotoPath = remoteUrl.trim();
                        }
                    } catch (Exception ignored) {
                        // Si falla la subida remota, se conserva la copia local.
                    }
                }
            }

            if (isEdit) {
                QueenEntity entity = db.queenDao().getQueenByIdSyncForUser(queenId, userId);
                if (entity != null) {
                    entity.setName(name);
                    entity.setDescription(desc);
                    entity.setEnvyLevel(envy);
                    entity.setPhotoUri(finalPhotoPath);
                    entity.setUpdatedAt(new Date());
                    db.queenDao().update(entity);
                    try {
                        authService.upsertQueen(entity);
                    } catch (Exception ignored) {
                        // Si falla remoto, el guardado local se mantiene.
                    }
                    LocalReminderNotifier.notifyQueenUpdated(
                            requireContext().getApplicationContext(), name);
                }
            } else {
                QueenEntity entity = new QueenEntity(
                        resolvedQueenId,
                        userId,
                        name, desc, finalPhotoPath, envy,
                        new Date(), new Date()
                );
                db.queenDao().insert(entity);
                try {
                    authService.upsertQueen(entity);
                } catch (Exception ignored) {
                    // Si falla remoto, el guardado local se mantiene.
                }
                LocalReminderNotifier.notifyQueenCreated(
                        requireContext().getApplicationContext(), name);
            }

            requireActivity().runOnUiThread(() -> {
                Bundle result = new Bundle();
                result.putBoolean(RESULT_DONE, true);
                getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
                dismiss();
            });
        });
    }

    // Copia la imagen seleccionada al almacenamiento interno.
    @Nullable
    private String copyImageToInternalStorage(@Nullable Uri sourceUri, @Nullable String existingQueenId) {
        if (sourceUri == null) return null;
        try {
            File photoDir = new File(requireContext().getFilesDir(), "queen_photos");
            if (!photoDir.exists()) //noinspection ResultOfMethodCallIgnored
                photoDir.mkdirs();

            String filename = (existingQueenId != null ? existingQueenId : UUID.randomUUID().toString()) + ".jpg";
            File destFile = new File(photoDir, filename);

            try (InputStream in = requireContext().getContentResolver().openInputStream(sourceUri);
                 FileOutputStream out = new FileOutputStream(destFile)) {
                if (in == null) return null;
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String encodeImageAsBase64(@Nullable Uri sourceUri) {
        if (sourceUri == null) {
            return null;
        }
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri)) {
            if (inputStream == null) {
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, buffer);
            byte[] photoBytes = buffer.toByteArray();
            return Base64.encodeToString(photoBytes, Base64.DEFAULT);
        } catch (IOException e) {
            return null;
        }
    }
}
