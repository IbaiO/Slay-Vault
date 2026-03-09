package com.example.slay_vault.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.slay_vault.R;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.ShadeEntryEntity;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.UUID;

// DialogFragment para añadir o editar un Shade.
public class AddEditShadeDialogFragment extends DialogFragment {

    public static final String RESULT_KEY  = "add_edit_shade_result";
    public static final String RESULT_DONE = "result_done";

    private static final String ARG_QUEEN_ID = "arg_queen_id";
    private static final String ARG_SHADE_ID = "arg_shade_id";

    // Crea el diálogo en modo añadir shade para la queen indicada
    public static AddEditShadeDialogFragment newInstance(String queenId) {
        AddEditShadeDialogFragment f = new AddEditShadeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUEEN_ID, queenId);
        f.setArguments(args);
        return f;
    }

    // Crea el diálogo en modo editar para el shade indicado
    public static AddEditShadeDialogFragment newInstanceForEdit(String queenId, String shadeId) {
        AddEditShadeDialogFragment f = new AddEditShadeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUEEN_ID, queenId);
        args.putString(ARG_SHADE_ID, shadeId);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_shade, null, false);

        TextInputLayout   tilTitle       = view.findViewById(R.id.til_shade_title);
        TextInputEditText etTitle        = view.findViewById(R.id.et_shade_title);
        TextInputEditText etDescription  = view.findViewById(R.id.et_shade_description);
        TextInputEditText etCategory     = view.findViewById(R.id.et_shade_category);
        RatingBar         rbIntensity    = view.findViewById(R.id.rb_shade_intensity);

        String queenId = getArguments() != null ? getArguments().getString(ARG_QUEEN_ID) : null;
        String shadeId = getArguments() != null ? getArguments().getString(ARG_SHADE_ID) : null;
        boolean isEdit = shadeId != null;

        if (isEdit) {
            SlayVaultDatabase.databaseExecutor.execute(() -> {
                ShadeEntryEntity entity = SlayVaultDatabase
                        .getInstance(requireContext().getApplicationContext())
                        .shadeEntryDao()
                        .getShadeByIdSync(shadeId);
                if (entity != null) {
                    requireActivity().runOnUiThread(() -> {
                        etTitle.setText(entity.getTitle());
                        etDescription.setText(entity.getDescription());
                        etCategory.setText(entity.getCategory());
                        rbIntensity.setRating(entity.getIntensity());
                    });
                }
            });
        }

        String title = isEdit
                ? DivaStrings.dialogEditShadeTitle(requireContext())
                : DivaStrings.dialogAddShadeTitle(requireContext());

        view.findViewById(R.id.btn_cancel_shade).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btn_save_shade).setOnClickListener(v -> {
            String titleText = etTitle.getText() != null
                    ? etTitle.getText().toString().trim() : "";
            if (TextUtils.isEmpty(titleText)) {
                tilTitle.setError(getString(R.string.error_title_required));
                return;
            }
            tilTitle.setError(null);
            String desc     = etDescription.getText() != null
                    ? etDescription.getText().toString().trim() : "";
            String category = etCategory.getText() != null
                    ? etCategory.getText().toString().trim() : "";
            if (TextUtils.isEmpty(category)) category = getString(R.string.category_default);
            float intensity = rbIntensity.getRating();
            saveShade(isEdit, queenId, shadeId, titleText, desc, category, intensity);
        });

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(view)
                .create();
        dialog.setCancelable(true);
        return dialog;
    }

    private void saveShade(boolean isEdit, @Nullable String queenId, @Nullable String shadeId,
                           String title, String desc, String category, float intensity) {
        SlayVaultDatabase db = SlayVaultDatabase
                .getInstance(requireContext().getApplicationContext());

        SlayVaultDatabase.databaseExecutor.execute(() -> {
            if (isEdit) {
                ShadeEntryEntity entity = db.shadeEntryDao().getShadeByIdSync(shadeId);
                if (entity != null) {
                    entity.setTitle(title);
                    entity.setDescription(desc);
                    entity.setCategory(category);
                    entity.setIntensity(intensity);
                    entity.setUpdatedAt(new Date());
                    db.shadeEntryDao().update(entity);
                    refreshQueenShadeStats(db, entity.getQueenId());
                    LocalReminderNotifier.notifyShadeUpdated(
                            requireContext().getApplicationContext(), title);
                }
            } else {
                if (queenId == null) return;
                ShadeEntryEntity entity = new ShadeEntryEntity(
                        UUID.randomUUID().toString(),
                        queenId, title, desc, category,
                        intensity, new Date(), null, new Date(), new Date()
                );
                db.shadeEntryDao().insert(entity);
                long now = System.currentTimeMillis();
                db.queenDao().incrementShadesCount(queenId, now);
                refreshQueenLastShadeDate(db, queenId, now);
                LocalReminderNotifier.notifyShadeCreated(
                        requireContext().getApplicationContext(), title);
            }

            requireActivity().runOnUiThread(() -> {
                Bundle result = new Bundle();
                result.putBoolean(RESULT_DONE, true);
                getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
                dismiss();
            });
        });
    }

    // Recalcula shadesCount y lastShadeDate para la queen indicada
    private static void refreshQueenShadeStats(SlayVaultDatabase db, String queenId) {
        long now = System.currentTimeMillis();
        int count = db.shadeEntryDao().getShadesCountByQueenSync(queenId);
        db.queenDao().updateShadesCount(queenId, count, now);
        refreshQueenLastShadeDate(db, queenId, now);
    }

    // Actualiza lastShadeDate con la fecha del shade más reciente de la queen
    private static void refreshQueenLastShadeDate(SlayVaultDatabase db, String queenId, long updatedAt) {
        ShadeEntryEntity latest = db.shadeEntryDao().getMostRecentShadeByQueenSync(queenId);
        String dateStr = null;
        if (latest != null && latest.getDate() != null) {
            dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    .format(latest.getDate());
        }
        db.queenDao().updateLastShadeDate(queenId, dateStr, updatedAt);
    }
}



