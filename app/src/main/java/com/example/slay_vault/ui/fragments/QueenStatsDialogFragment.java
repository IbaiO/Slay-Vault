package com.example.slay_vault.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.slay_vault.R;
import com.example.slay_vault.data.dao.ShadeEntryDao;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.QueenEntity;
import com.example.slay_vault.ui.DivaStrings;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// DialogFragment que muestra las estadísticas de una Queen desde Room
public class QueenStatsDialogFragment extends DialogFragment {

    private static final String ARG_QUEEN_ID = "queen_id";

    public static QueenStatsDialogFragment newInstance(String queenId) {
        QueenStatsDialogFragment f = new QueenStatsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUEEN_ID, queenId);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_queen_stats, null, false);

        ShapeableImageView photo       = view.findViewById(R.id.stats_queen_photo);
        TextView           tvName      = view.findViewById(R.id.stats_queen_name);
        RatingBar          rbEnvy      = view.findViewById(R.id.stats_envy_rating);
        TextView           tvShades    = view.findViewById(R.id.stats_shades_value);
        RatingBar          rbAvg       = view.findViewById(R.id.stats_avg_intensity);
        TextView           tvFavCat    = view.findViewById(R.id.stats_fav_category_value);
        TextView           tvLastShade = view.findViewById(R.id.stats_last_shade_value);
        TextView           tvSince     = view.findViewById(R.id.stats_since_value);

        ((TextView) view.findViewById(R.id.stats_label_envy))
                .setText(DivaStrings.envyLevel(requireContext()));
        ((TextView) view.findViewById(R.id.stats_label_avg))
                .setText(DivaStrings.shadeIntensity(requireContext()));
        ((TextView) view.findViewById(R.id.stats_label_shades))
                .setText(DivaStrings.statsTotalShades(requireContext()));
        ((TextView) view.findViewById(R.id.stats_label_fav_cat))
                .setText(DivaStrings.statsFavCategory(requireContext()));
        ((TextView) view.findViewById(R.id.stats_label_last))
                .setText(DivaStrings.statsLastShade(requireContext()));
        ((TextView) view.findViewById(R.id.stats_label_since))
                .setText(DivaStrings.statsRegisteredSince(requireContext()));
        ((com.google.android.material.button.MaterialButton) view.findViewById(R.id.stats_btn_close))
                .setText(DivaStrings.actionClose(requireContext()));

        String queenId = getArguments() != null ? getArguments().getString(ARG_QUEEN_ID) : null;

        if (queenId != null) {
            SlayVaultDatabase.databaseExecutor.execute(() -> {
                SlayVaultDatabase db = SlayVaultDatabase.getInstance(
                        requireContext().getApplicationContext());

                QueenEntity entity = db.queenDao().getQueenByIdSync(queenId);
                if (entity == null || !isAdded()) return;

                ShadeEntryDao shadeDao = db.shadeEntryDao();
                int count       = shadeDao.getShadesCountByQueenSync(queenId);
                Float avgRaw    = shadeDao.getAverageIntensityByQueenSync(queenId);
                float avg       = avgRaw != null ? avgRaw : 0f;
                String favCat   = shadeDao.getMostUsedCategoryByQueenSync(queenId);
                com.example.slay_vault.data.entities.ShadeEntryEntity lastShade =
                        shadeDao.getMostRecentShadeByQueenSync(queenId);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                String lastShadeStr;
                if (lastShade != null && lastShade.getDate() != null) {
                    lastShadeStr = sdf.format(lastShade.getDate());
                } else {
                    lastShadeStr = getString(R.string.stats_no_data);
                }

                String sinceStr;
                if (entity.getCreatedAt() != null) {
                    sinceStr = sdf.format(entity.getCreatedAt());
                } else {
                    sinceStr = sdf.format(new Date());
                }

                String favCatStr = (favCat != null && !favCat.isEmpty())
                        ? favCat
                        : getString(R.string.stats_no_data);

                final String finalLastShade = lastShadeStr;
                final String finalSince     = sinceStr;
                final String finalFavCat    = favCatStr;
                final float  finalAvg       = avg;
                final int    finalCount     = count;

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    tvName.setText(entity.getName());
                    rbEnvy.setRating(entity.getEnvyLevel());

                    if (finalCount == 0) {
                        tvShades.setText(getString(R.string.no_shades_yet));
                    } else if (finalCount == 1) {
                        tvShades.setText(getString(R.string.shade_count_single));
                    } else {
                        tvShades.setText(getString(R.string.shades_count, finalCount));
                    }

                    rbAvg.setRating(finalAvg);
                    tvFavCat.setText(finalFavCat);
                    tvLastShade.setText(finalLastShade);
                    tvSince.setText(finalSince);

                    if (getDialog() != null) {
                        getDialog().setTitle(DivaStrings.dialogStatsTitle(requireContext(), entity.getName()));
                    }

                    if (entity.getPhotoUri() != null && !entity.getPhotoUri().isEmpty()) {
                        java.io.File file = new java.io.File(entity.getPhotoUri());
                        if (file.exists()) {
                            photo.setImageURI(android.net.Uri.fromFile(file));
                        } else {
                            int resId = requireContext().getResources().getIdentifier(
                                    entity.getPhotoUri(), "drawable",
                                    requireContext().getPackageName());
                            photo.setImageResource(resId != 0 ? resId : R.mipmap.ic_launcher);
                        }
                    }
                });
            });
        }

        view.findViewById(R.id.stats_btn_close).setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}