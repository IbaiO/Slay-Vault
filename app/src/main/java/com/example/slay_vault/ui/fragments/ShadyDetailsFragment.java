package com.example.slay_vault.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slay_vault.R;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.ShadeEntryEntity;
import com.example.slay_vault.data.mappers.QueenMapper;
import com.example.slay_vault.data.mappers.ShadeMapper;
import com.example.slay_vault.data.models.Queen;
import com.example.slay_vault.data.models.Shade;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.example.slay_vault.ui.adapters.ShadesAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

// Fragment que muestra los detalles de un shade (rival).
public class ShadyDetailsFragment extends Fragment {

    private ImageView queenHeaderImage;
    private TextView queenNameDetail;
    private TextView queenDescriptionDetail;
    private RatingBar queenEnvyRatingDetail;
    private TextView shadesTotalCount;
    private TextView avgShadeIntensity;
    private RecyclerView shadesRecyclerView;
    private LinearLayout emptyStateShades;

    private ShadesAdapter shadesAdapter;
    private Queen currentQueen;

    private static final String REQUEST_DELETE_SHADE = "request_delete_shade";
    private static final String TAG_DELETE_DIALOG = "tag_delete_shade_dialog";
    private int pendingDeleteShadePosition = RecyclerView.NO_POSITION;

    public ShadyDetailsFragment() {
        // Constructor vacío requerido
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shady_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queenHeaderImage = view.findViewById(R.id.queen_header_image);
        queenNameDetail = view.findViewById(R.id.queen_name_detail);
        queenDescriptionDetail = view.findViewById(R.id.queen_description_detail);
        queenEnvyRatingDetail = view.findViewById(R.id.queen_envy_rating_detail);
        shadesTotalCount = view.findViewById(R.id.shades_total_count);
        avgShadeIntensity = view.findViewById(R.id.avg_shade_intensity);
        shadesRecyclerView = view.findViewById(R.id.shades_recycler_view);
        emptyStateShades = view.findViewById(R.id.empty_state_shades);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView emptyStateShadesTitle = view.findViewById(R.id.empty_state_shades_title);
        FloatingActionButton fabAddShade = view.findViewById(R.id.fab_add_shade);
        MaterialButton btnBuyWig = view.findViewById(R.id.btn_buy_wig);

        if (emptyStateShadesTitle != null) {
            emptyStateShadesTitle.setText(DivaStrings.emptyStateShades(requireContext()));
        }

        toolbar.setNavigationOnClickListener(v ->
            Navigation.findNavController(v).navigateUp()
        );

        setupShadesRecyclerView();

        fabAddShade.setOnClickListener(v -> {
            if (currentQueen != null) {
                AddEditShadeDialogFragment.newInstance(currentQueen.getId())
                    .show(getParentFragmentManager(), "tag_add_shade");
            }
        });

        getParentFragmentManager().setFragmentResultListener(
                AddEditShadeDialogFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (key, result) -> { }
        );

        btnBuyWig.setText(DivaStrings.buttonSearchQueen(requireContext()));
        btnBuyWig.setOnClickListener(v -> {
            String query = (currentQueen != null && currentQueen.getName() != null && !currentQueen.getName().isEmpty())
                    ? currentQueen.getName()
                    : DivaStrings.searchQueenFallback(requireContext());
            Uri searchUri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, searchUri);
            startActivity(browserIntent);
        });

        setupDeleteDialogResultListener();

        if (getArguments() != null) {
            String queenId = getArguments().getString("shady_id");
            loadQueenDetails(queenId);
        } else {
            updateEmptyState();
        }
    }

    private void setupShadesRecyclerView() {
        shadesAdapter = new ShadesAdapter();

        shadesAdapter.setOnShadeClickListener(new ShadesAdapter.OnShadeClickListener() {
            @Override
            public void onShadeClick(Shade shade, int position) {
                showShadeDetailsDialog(shade);
            }

            @Override
            public void onShadeLongClick(Shade shade, int position) {
                showShadeOptions(shade, position);
            }
        });

        shadesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        shadesRecyclerView.setAdapter(shadesAdapter);
        shadesRecyclerView.setNestedScrollingEnabled(false);
    }

    // Carga queen y shades desde Room como LiveData
    private void loadQueenDetails(String rawQueenId) {
        if (rawQueenId == null || rawQueenId.trim().isEmpty()) {
            updateEmptyState();
            return;
        }

        SlayVaultDatabase db = SlayVaultDatabase.getInstance(requireContext().getApplicationContext());

        db.queenDao().getQueenById(rawQueenId).observe(getViewLifecycleOwner(), entity -> {
            if (entity != null) {
                currentQueen = QueenMapper.fromEntity(entity);
                displayQueenInfo(currentQueen);
            }
        });

        db.shadeEntryDao().getShadesByQueenId(rawQueenId).observe(getViewLifecycleOwner(), entities -> {
            List<Shade> shades = ShadeMapper.fromEntityList(entities);
            shadesAdapter.setShades(shades);
            updateStatistics(shades);
            updateEmptyState();
        });
    }

    @SuppressWarnings("DiscouragedApi") // getIdentifier es necesario: el nombre del drawable viene de la BD
    private void displayQueenInfo(Queen queen) {
        queenNameDetail.setText(queen.getName());
        queenDescriptionDetail.setText(queen.getDescription());
        queenEnvyRatingDetail.setRating(queen.getEnvyLevel());

        String photoPath = queen.getPhotoUri();
        if (photoPath != null && !photoPath.isEmpty()) {
            java.io.File file = new java.io.File(photoPath);
            if (file.exists()) {
                queenHeaderImage.setImageURI(null);
                queenHeaderImage.setImageURI(Uri.fromFile(file));
            } else {
                int resId = getResources().getIdentifier(
                        photoPath, "drawable", requireContext().getPackageName());
                queenHeaderImage.setImageResource(resId != 0 ? resId : R.mipmap.ic_launcher);
            }
        } else {
            queenHeaderImage.setImageResource(R.mipmap.ic_launcher);
        }
    }

    // Actualiza las estadísticas de shades (total e intensidad media)
    private void updateStatistics(List<Shade> shades) {
        int totalShades = shades.size();
        shadesTotalCount.setText(String.valueOf(totalShades));

        if (totalShades > 0) {
            float totalIntensity = 0;
            for (Shade shade : shades) {
                totalIntensity += shade.getIntensity();
            }
            float avgIntensity = totalIntensity / totalShades;
            avgShadeIntensity.setText(String.format(Locale.getDefault(), "%.1f", avgIntensity));
        } else {
            avgShadeIntensity.setText("0.0");
        }
    }

    // Actualiza la visibilidad del empty state según si hay shades
    private void updateEmptyState() {
        if (shadesAdapter.getItemCount() == 0) {
            emptyStateShades.setVisibility(View.VISIBLE);
            shadesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateShades.setVisibility(View.GONE);
            shadesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Muestra un diálogo con los detalles completos del shade
    private void showShadeDetailsDialog(Shade shade) {
        new AlertDialog.Builder(requireContext())
                .setTitle(shade.getTitle())
                .setMessage(shade.getDescription())
                .setPositiveButton(DivaStrings.actionClose(requireContext()), null)
                .show();
    }

    // Muestra el menú de opciones para un shade (editar, eliminar, compartir)
    private void showShadeOptions(Shade shade, int position) {
        String[] options = {
            DivaStrings.actionEdit(requireContext()),
            DivaStrings.actionDelete(requireContext()),
            DivaStrings.actionShare(requireContext())
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(shade.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (currentQueen != null) {
                                AddEditShadeDialogFragment
                                    .newInstanceForEdit(currentQueen.getId(), shade.getId())
                                    .show(getParentFragmentManager(), "tag_edit_shade");
                            }
                            break;
                        case 1:
                            confirmDeleteShade(shade, position);
                            break;
                        case 2:
                            shareShade(shade);
                            break;
                    }
                })
                .show();
    }

    // Escucha la respuesta del diálogo de borrado y borra el shade en Room
    private void setupDeleteDialogResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                REQUEST_DELETE_SHADE,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    boolean confirmed = result.getBoolean(DeleteConfirmDialogFragment.RESULT_CONFIRMED, false);
                    if (!confirmed) {
                        pendingDeleteShadePosition = RecyclerView.NO_POSITION;
                        return;
                    }

                    Shade shade = shadesAdapter.getShade(pendingDeleteShadePosition);
                    if (shade != null) {
                        String shadeTitle = shade.getTitle();
                        String queenId = shade.getQueenId();
                        SlayVaultDatabase.databaseExecutor.execute(() -> {
                            SlayVaultDatabase db = SlayVaultDatabase
                                    .getInstance(requireContext().getApplicationContext());
                            db.shadeEntryDao().deleteById(shade.getId());
                            long now = System.currentTimeMillis();
                            db.queenDao().decrementShadesCount(queenId, now);
                            ShadeEntryEntity latest = db.shadeEntryDao()
                                    .getMostRecentShadeByQueenSync(queenId);
                            String dateStr = null;
                            if (latest != null && latest.getDate() != null) {
                                dateStr = new java.text.SimpleDateFormat(
                                        "dd/MM/yyyy", java.util.Locale.getDefault())
                                        .format(latest.getDate());
                            }
                            db.queenDao().updateLastShadeDate(queenId, dateStr, now);
                        });
                        LocalReminderNotifier.notifyShadeDeleted(requireContext(), shadeTitle);
                        Toast.makeText(getContext(), getString(R.string.shade_deleted), Toast.LENGTH_SHORT).show();
                    }

                    pendingDeleteShadePosition = RecyclerView.NO_POSITION;
                }
        );
    }

    // Comparte el shade via Intent implícito (cualquier app de mensajería/RRSS)
    private void shareShade(Shade shade) {
        String queenName = (currentQueen != null) ? currentQueen.getName() : getString(R.string.search_queen_fallback);
        String body = shade.getTitle();
        if (shade.getDescription() != null && !shade.getDescription().isEmpty()) {
            body += "\n\n" + shade.getDescription();
        }
        body += "\n\n— " + DivaStrings.shareFooter(requireContext(), queenName);

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, shade.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);

        startActivity(Intent.createChooser(sendIntent, DivaStrings.actionShare(requireContext())));
    }

    // Abre el diálogo de confirmación de borrado de un shade
    private void confirmDeleteShade(Shade shade, int position) {
        pendingDeleteShadePosition = position;
        DeleteConfirmDialogFragment.newInstance(
                        REQUEST_DELETE_SHADE,
                        shade != null ? shade.getId() : null,
                        shade != null ? shade.getTitle() : null
                )
                .show(getParentFragmentManager(), TAG_DELETE_DIALOG);
    }
}
