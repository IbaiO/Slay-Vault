package com.example.slay_vault.ui.fragments;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.ShadeEntryEntity;
import com.example.slay_vault.data.mappers.QueenMapper;
import com.example.slay_vault.data.mappers.ShadeMapper;
import com.example.slay_vault.data.models.Queen;
import com.example.slay_vault.data.models.Shade;
import com.example.slay_vault.data.remote.AuthService;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.example.slay_vault.ui.adapters.ShadesAdapter;
import com.example.slay_vault.ui.utils.QueenPhotoLoader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;

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
    private MaterialButton btnPlayAnthem;

    private ShadesAdapter shadesAdapter;
    private Queen currentQueen;
    private MediaPlayer mediaPlayer;

    private static final String REQUEST_DELETE_SHADE = "request_delete_shade";
    private static final String TAG_DELETE_DIALOG = "tag_delete_shade_dialog";
    private int pendingDeleteShadePosition = RecyclerView.NO_POSITION;

    private final ActivityResultLauncher<String> audioPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isAdded()) {
                    return;
                }
                if (isGranted) {
                    showAnthemPickerDialog();
                } else {
                    Toast.makeText(requireContext(), DivaStrings.anthemPermissionDenied(requireContext()), Toast.LENGTH_SHORT).show();
                }
            });

    private static class DeviceSong {
        final long id;
        final String title;

        DeviceSong(long id, @NonNull String title) {
            this.id = id;
            this.title = title;
        }
    }

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
        MaterialButton btnAssignAnthem = view.findViewById(R.id.btn_assign_anthem);
        btnPlayAnthem = view.findViewById(R.id.btn_play_anthem);

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
        btnAssignAnthem.setText(DivaStrings.buttonAssignAnthem(requireContext()));
        btnPlayAnthem.setText(DivaStrings.buttonPlayAnthem(requireContext()));
        btnBuyWig.setOnClickListener(v -> {
            String query = (currentQueen != null && currentQueen.getName() != null && !currentQueen.getName().isEmpty())
                    ? currentQueen.getName()
                    : DivaStrings.searchQueenFallback(requireContext());
            Uri searchUri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, searchUri);
            startActivity(browserIntent);
        });

        btnAssignAnthem.setOnClickListener(v -> requestAudioPermissionAndPickSong());
        btnPlayAnthem.setOnClickListener(v -> playAssignedAnthem());
        syncAnthemButtons();

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

        String userId = SessionManager.getUserId(requireContext());
        if (userId == null || userId.trim().isEmpty()) {
            updateEmptyState();
            return;
        }

        SlayVaultDatabase db = SlayVaultDatabase.getInstance(requireContext().getApplicationContext());

        db.queenDao().getQueenByIdForUser(rawQueenId, userId).observe(getViewLifecycleOwner(), entity -> {
            if (entity != null) {
                currentQueen = QueenMapper.fromEntity(entity);
                displayQueenInfo(currentQueen);
            }
        });

        db.shadeEntryDao().getShadesByQueenIdForUser(rawQueenId, userId).observe(getViewLifecycleOwner(), entities -> {
            List<Shade> shades = ShadeMapper.fromEntityList(entities);
            shadesAdapter.setShades(shades);
            updateStatistics(shades);
            updateEmptyState();
        });
    }

    private void displayQueenInfo(Queen queen) {
        queenNameDetail.setText(queen.getName());
        queenDescriptionDetail.setText(queen.getDescription());
        queenEnvyRatingDetail.setRating(queen.getEnvyLevel());
        QueenPhotoLoader.load(queenHeaderImage, queen.getPhotoUri(), R.mipmap.ic_launcher);
        syncAnthemButtons();
    }

    private void requestAudioPermissionAndPickSong() {
        if (!isAdded()) {
            return;
        }

        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            showAnthemPickerDialog();
            return;
        }
        audioPermissionLauncher.launch(permission);
    }

    private void showAnthemPickerDialog() {
        List<DeviceSong> songs = queryDeviceSongs();
        if (songs.isEmpty()) {
            Toast.makeText(requireContext(), DivaStrings.anthemNoSongsFound(requireContext()), Toast.LENGTH_SHORT).show();
            return;
        }

        String[] songTitles = new String[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            songTitles[i] = songs.get(i).title;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(DivaStrings.anthemSelectTitle(requireContext()))
                .setItems(songTitles, (dialog, which) -> saveSelectedAnthem(songs.get(which)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private List<DeviceSong> queryDeviceSongs() {
        List<DeviceSong> songs = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE
        };

        try (Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                null,
                MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC"
        )) {
            if (cursor == null) {
                return songs;
            }
            int idIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);

            while (cursor.moveToNext()) {
                long songId = cursor.getLong(idIdx);
                String title = cursor.getString(titleIdx);
                if (title == null || title.trim().isEmpty()) {
                    title = DivaStrings.anthemUnknownTitle(requireContext());
                }
                songs.add(new DeviceSong(songId, title));
            }
        }

        return songs;
    }

    private void saveSelectedAnthem(@NonNull DeviceSong song) {
        if (currentQueen == null) {
            return;
        }
        String userId = SessionManager.getUserId(requireContext());
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        SlayVaultDatabase.databaseExecutor.execute(() -> {
            SlayVaultDatabase db = SlayVaultDatabase.getInstance(requireContext().getApplicationContext());
            com.example.slay_vault.data.entities.QueenEntity queenEntity =
                    db.queenDao().getQueenByIdSyncForUser(currentQueen.getId(), userId);
            if (queenEntity == null) {
                return;
            }

            queenEntity.setSongId(song.id);
            queenEntity.setUpdatedAt(new Date());
            db.queenDao().update(queenEntity);
            try {
                new AuthService().upsertQueen(queenEntity);
            } catch (Exception ignored) {
                // Si falla remoto, mantenemos el dato local.
            }

            requireActivity().runOnUiThread(() -> {
                if (currentQueen != null) {
                    currentQueen.setSongId(song.id);
                }
                syncAnthemButtons();
                Toast.makeText(requireContext(), DivaStrings.anthemAssigned(requireContext(), song.title), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void playAssignedAnthem() {
        if (currentQueen == null || currentQueen.getSongId() == null) {
            Toast.makeText(requireContext(), DivaStrings.anthemMissing(requireContext()), Toast.LENGTH_SHORT).show();
            return;
        }

        releaseMediaPlayer();
        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentQueen.getSongId());
        try {
            mediaPlayer = MediaPlayer.create(requireContext(), songUri);
            if (mediaPlayer == null) {
                Toast.makeText(requireContext(), DivaStrings.anthemPlaybackError(requireContext()), Toast.LENGTH_SHORT).show();
                return;
            }
            mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
            mediaPlayer.start();
        } catch (Exception e) {
            releaseMediaPlayer();
            Toast.makeText(requireContext(), DivaStrings.anthemPlaybackError(requireContext()), Toast.LENGTH_SHORT).show();
        }
    }

    private void syncAnthemButtons() {
        if (btnPlayAnthem == null) {
            return;
        }
        boolean hasAnthem = currentQueen != null && currentQueen.getSongId() != null;
        btnPlayAnthem.setEnabled(hasAnthem);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Actualiza total e intensidad media.
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

    // Muestra opciones de editar, eliminar y compartir.
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
                        String userId = SessionManager.getUserId(requireContext());
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

                            if (userId != null && !userId.trim().isEmpty()) {
                                try {
                                    new AuthService().deleteShade(userId, shade.getId());
                                } catch (Exception ignored) {
                                    // Si falla remoto, se mantiene el borrado local.
                                }
                            }
                        });
                        LocalReminderNotifier.notifyShadeDeleted(requireContext(), shadeTitle);
                        Toast.makeText(getContext(), getString(R.string.shade_deleted), Toast.LENGTH_SHORT).show();
                    }

                    pendingDeleteShadePosition = RecyclerView.NO_POSITION;
                }
        );
    }

    // Comparte el shade mediante intent implícito.
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

    @Override
    public void onStop() {
        releaseMediaPlayer();
        super.onStop();
    }
}
