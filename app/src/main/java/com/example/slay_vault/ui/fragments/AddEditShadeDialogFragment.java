package com.example.slay_vault.ui.fragments;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.ShadeEntryEntity;
import com.example.slay_vault.data.remote.AuthService;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
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

    private static final String LOCATION_COORDS_FORMAT = "%.5f, %.5f";

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private SwitchMaterial switchSaveLocation;
    private TextView tvLocationStatus;
    private Double selectedLatitude;
    private Double selectedLongitude;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        fetchLastKnownLocation();
                    } else {
                        clearSelectedLocation();
                        if (switchSaveLocation != null) {
                            switchSaveLocation.setChecked(false);
                        }
                        updateLocationStatus();
                        Toast.makeText(requireContext(),
                                DivaStrings.shadeLocationPermissionDenied(requireContext()),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
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
        switchSaveLocation               = view.findViewById(R.id.switch_save_location);
        tvLocationStatus                 = view.findViewById(R.id.tv_location_status);

        switchSaveLocation.setText(DivaStrings.shadeSaveLocationLabel(requireContext()));
        updateLocationStatus();
        switchSaveLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            if (isChecked) {
                requestLocationForShade();
            } else {
                clearSelectedLocation();
                updateLocationStatus();
            }
        });

        String queenId = getArguments() != null ? getArguments().getString(ARG_QUEEN_ID) : null;
        String shadeId = getArguments() != null ? getArguments().getString(ARG_SHADE_ID) : null;
        boolean isEdit = shadeId != null;

        final String userId = SessionManager.getUserId(requireContext());
        if (userId == null || userId.trim().isEmpty()) {
            return new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.auth_required)
                    .setMessage(R.string.auth_required)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        if (isEdit) {
            SlayVaultDatabase.databaseExecutor.execute(() -> {
                ShadeEntryEntity entity = SlayVaultDatabase
                        .getInstance(requireContext().getApplicationContext())
                        .shadeEntryDao()
                        .getShadeByIdSyncForUser(shadeId, userId);
                if (entity != null) {
                    requireActivity().runOnUiThread(() -> {
                        etTitle.setText(entity.getTitle());
                        etDescription.setText(entity.getDescription());
                        etCategory.setText(entity.getCategory());
                        rbIntensity.setRating(entity.getIntensity());
                        selectedLatitude = entity.getLatitude();
                        selectedLongitude = entity.getLongitude();
                        switchSaveLocation.setChecked(selectedLatitude != null && selectedLongitude != null);
                        updateLocationStatus();
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

            if (switchSaveLocation.isChecked() && (selectedLatitude == null || selectedLongitude == null)) {
                Toast.makeText(requireContext(),
                        DivaStrings.shadeLocationNeededToSave(requireContext()),
                        Toast.LENGTH_SHORT).show();
                requestLocationForShade();
                return;
            }

            Double latitudeToSave = switchSaveLocation.isChecked() ? selectedLatitude : null;
            Double longitudeToSave = switchSaveLocation.isChecked() ? selectedLongitude : null;
            saveShade(isEdit, queenId, shadeId, userId, titleText, desc, category, intensity,
                    latitudeToSave, longitudeToSave);
        });

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(view)
                .create();
        dialog.setCancelable(true);
        return dialog;
    }

    private void saveShade(boolean isEdit, @Nullable String queenId, @Nullable String shadeId,
                           @NonNull String userId, String title, String desc, String category,
                           float intensity, @Nullable Double latitude, @Nullable Double longitude) {
        SlayVaultDatabase db = SlayVaultDatabase
                .getInstance(requireContext().getApplicationContext());
        AuthService authService = new AuthService();

        SlayVaultDatabase.databaseExecutor.execute(() -> {
            if (isEdit) {
                ShadeEntryEntity entity = db.shadeEntryDao().getShadeByIdSyncForUser(shadeId, userId);
                if (entity != null) {
                    entity.setTitle(title);
                    entity.setDescription(desc);
                    entity.setCategory(category);
                    entity.setIntensity(intensity);
                    if (latitude == null || longitude == null) {
                        entity.setLatitude(null);
                        entity.setLongitude(null);
                        entity.setLocationAddress(null);
                    } else {
                        entity.setLatitude(latitude);
                        entity.setLongitude(longitude);
                    }
                    entity.setUpdatedAt(new Date());
                    db.shadeEntryDao().update(entity);
                    refreshQueenShadeStats(db, entity.getQueenId());
                    try {
                        Log.d("AddEditShadeDialog", "Updating shade: " + entity.getId());
                        Log.d("AddEditShadeDialog", "Latitude: " + entity.getLatitude() + ", Longitude: " + entity.getLongitude());
                        authService.upsertShade(entity);
                        Log.d("AddEditShadeDialog", "Shade updated successfully on server");
                    } catch (Exception e) {
                        Log.e("AddEditShadeDialog", "Failed to update shade on server", e);
                    }
                    LocalReminderNotifier.notifyShadeUpdated(
                            requireContext().getApplicationContext(), title);
                }
            } else {
                if (queenId == null) return;
                ShadeEntryEntity entity = new ShadeEntryEntity(
                        UUID.randomUUID().toString(),
                        userId,
                        queenId, title, desc, category,
                        intensity, new Date(), null, new Date(), new Date()
                );
                entity.setLatitude(latitude);
                entity.setLongitude(longitude);
                db.shadeEntryDao().insert(entity);
                long now = System.currentTimeMillis();
                db.queenDao().incrementShadesCount(queenId, now);
                refreshQueenLastShadeDate(db, queenId, now);
                try {
                    Log.d("AddEditShadeDialog", "Creating new shade: " + entity.getId());
                    Log.d("AddEditShadeDialog", "User: " + userId + ", Queen: " + queenId);
                    Log.d("AddEditShadeDialog", "Latitude: " + entity.getLatitude() + ", Longitude: " + entity.getLongitude());
                    authService.upsertShade(entity);
                    Log.d("AddEditShadeDialog", "Shade created successfully on server");
                } catch (Exception e) {
                    Log.e("AddEditShadeDialog", "Failed to create shade on server", e);
                }
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

    private void requestLocationForShade() {
        if (hasFineLocationPermission()) {
            fetchLastKnownLocation();
            return;
        }
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void fetchLastKnownLocation() {
        if (!hasFineLocationPermission()) {
            clearSelectedLocation();
            if (switchSaveLocation != null) {
                switchSaveLocation.setChecked(false);
            }
            updateLocationStatus();
            return;
        }

        if (tvLocationStatus != null) {
            tvLocationStatus.setText(DivaStrings.shadeLocationLocating(requireContext()));
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this::handleLocationResult)
                    .addOnFailureListener(e -> handleLocationResult(null));
        } catch (SecurityException ignored) {
            handleLocationResult(null);
        }
    }

    private void handleLocationResult(@Nullable Location location) {
        if (location == null) {
            clearSelectedLocation();
            if (switchSaveLocation != null) {
                switchSaveLocation.setChecked(false);
            }
            updateLocationStatus();
            Toast.makeText(requireContext(),
                    DivaStrings.shadeLocationUnavailable(requireContext()),
                    Toast.LENGTH_LONG).show();
            return;
        }

        selectedLatitude = location.getLatitude();
        selectedLongitude = location.getLongitude();
        updateLocationStatus();
    }

    private void clearSelectedLocation() {
        selectedLatitude = null;
        selectedLongitude = null;
    }

    private void updateLocationStatus() {
        if (tvLocationStatus == null) {
            return;
        }
        if (selectedLatitude == null || selectedLongitude == null) {
            tvLocationStatus.setText(DivaStrings.shadeLocationNotSaved(requireContext()));
            return;
        }
        String coords = String.format(java.util.Locale.getDefault(), LOCATION_COORDS_FORMAT,
                selectedLatitude, selectedLongitude);
        tvLocationStatus.setText(DivaStrings.shadeLocationCaptured(requireContext(), coords));
    }

    // Recalcula contador y fecha del ultimo shade.
    private static void refreshQueenShadeStats(SlayVaultDatabase db, String queenId) {
        long now = System.currentTimeMillis();
        int count = db.shadeEntryDao().getShadesCountByQueenSync(queenId);
        db.queenDao().updateShadesCount(queenId, count, now);
        refreshQueenLastShadeDate(db, queenId, now);
    }

    // Actualiza la fecha del ultimo shade de la queen.
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

