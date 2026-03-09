package com.example.slay_vault.ui.fragments;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.slay_vault.R;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.QueenEntity;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

// Diálogo para añadir o editar una Queen. Gestiona foto via SAF, validación y persistencia en Room.
public class AddEditQueenDialogFragment extends DialogFragment {

    public static final String RESULT_KEY  = "add_edit_queen_result";
    public static final String RESULT_DONE = "result_done";

    private static final String ARG_QUEEN_ID = "arg_queen_id";

    // URI seleccionada por el usuario (válida solo mientras el diálogo está abierto)
    private Uri selectedImageUri = null;
    // Ruta interna guardada (precargada en modo edición)
    private String existingPhotoPath = null;

    private ShapeableImageView photoPreview;
    private ActivityResultLauncher<String> pickImageLauncher;

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

        btnPickPhoto.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );

        if (isEdit) {
            SlayVaultDatabase.databaseExecutor.execute(() -> {
                QueenEntity entity = SlayVaultDatabase
                        .getInstance(requireContext().getApplicationContext())
                        .queenDao()
                        .getQueenByIdSync(queenId);
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

    // Carga la foto existente: fichero interno o drawable por nombre
    @SuppressWarnings("DiscouragedApi")
    private void loadExistingPhoto(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) return;
        File file = new File(photoPath);
        if (file.exists()) {
            photoPreview.setImageURI(Uri.fromFile(file));
        } else {
            int resId = requireContext().getResources().getIdentifier(
                    photoPath, "drawable", requireContext().getPackageName());
            if (resId != 0) photoPreview.setImageResource(resId);
        }
    }

    private void saveQueen(boolean isEdit, @Nullable String queenId,
                           String name, String desc, float envy) {
        final String newPhotoPath = copyImageToInternalStorage(selectedImageUri, queenId);

        SlayVaultDatabase db = SlayVaultDatabase
                .getInstance(requireContext().getApplicationContext());

        SlayVaultDatabase.databaseExecutor.execute(() -> {
            String finalPhotoPath;
            if (newPhotoPath != null) {
                finalPhotoPath = newPhotoPath;
            } else if (isEdit) {
                finalPhotoPath = existingPhotoPath;
            } else {
                finalPhotoPath = null;
            }

            if (isEdit) {
                QueenEntity entity = db.queenDao().getQueenByIdSync(queenId);
                if (entity != null) {
                    entity.setName(name);
                    entity.setDescription(desc);
                    entity.setEnvyLevel(envy);
                    entity.setPhotoUri(finalPhotoPath);
                    entity.setUpdatedAt(new Date());
                    db.queenDao().update(entity);
                    LocalReminderNotifier.notifyQueenUpdated(
                            requireContext().getApplicationContext(), name);
                }
            } else {
                QueenEntity entity = new QueenEntity(
                        UUID.randomUUID().toString(),
                        name, desc, finalPhotoPath, envy,
                        new Date(), new Date()
                );
                db.queenDao().insert(entity);
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

    // Copia la imagen seleccionada al almacenamiento interno de la app
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
}
