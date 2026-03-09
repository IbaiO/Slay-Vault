package com.example.slay_vault.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.mappers.QueenMapper;
import com.example.slay_vault.data.models.Queen;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.example.slay_vault.ui.adapters.QueensAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

// Lista de rivales con RecyclerView. Gestiona añadir, editar, borrar y ordenar queens.
public class QueensListFragment extends Fragment {

    private RecyclerView recyclerView;
    private QueensAdapter adapter;
    private LinearLayout emptyState;

    private static final String REQUEST_DELETE_QUEEN = "request_delete_queen";
    private static final String TAG_DELETE_DIALOG = "tag_delete_queen_dialog";
    private int pendingDeleteQueenPosition = RecyclerView.NO_POSITION;

    public QueensListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queens_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.queens_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        TextView emptyStateTitle = view.findViewById(R.id.empty_state_title);
        FloatingActionButton fabAddQueen = view.findViewById(R.id.fab_add_queen);

        if (emptyStateTitle != null) {
            emptyStateTitle.setText(DivaStrings.emptyStateQueens(requireContext()));
        }

        if (getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() != null) {
            ((MainActivity) getActivity()).getSupportActionBar()
                    .setTitle(DivaStrings.screenTitleQueensList(requireContext()));
        }

        setupRecyclerView();

        fabAddQueen.setOnClickListener(v ->
            AddEditQueenDialogFragment.newInstance()
                .show(getParentFragmentManager(), "tag_add_queen")
        );

        getParentFragmentManager().setFragmentResultListener(
                AddEditQueenDialogFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (key, result) -> { }
        );

        setupDeleteDialogResultListener();

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.queens_list_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.sort_by_name) {
                    adapter.sortBy(QueensAdapter.SortOrder.NAME);
                    return true;
                } else if (itemId == R.id.sort_by_envy) {
                    adapter.sortBy(QueensAdapter.SortOrder.ENVY_DESC);
                    return true;
                } else if (itemId == R.id.sort_by_shades) {
                    adapter.sortBy(QueensAdapter.SortOrder.SHADES_DESC);
                    return true;
                } else if (itemId == R.id.sort_by_date) {
                    adapter.sortBy(QueensAdapter.SortOrder.DATE_DESC);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        observeQueens();
    }

    // Inicializa el RecyclerView y los listeners de clic
    private void setupRecyclerView() {
        adapter = new QueensAdapter();

        adapter.setOnQueenClickListener(new QueensAdapter.OnQueenClickListener() {
            @Override
            public void onQueenClick(Queen queen, int position) {
                navigateToQueenDetails(queen);
            }

            @Override
            public void onQueenLongClick(Queen queen, int position) {
                showQueenOptions(queen, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    // Navega al detalle de la queen (adaptativo dual/simple)
    private void navigateToQueenDetails(Queen queen) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToShadyDetails(queen.getId());
        } else {
            Bundle args = new Bundle();
            args.putString("shady_id", queen.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_queensList_to_shadyDetails, args);
        }
    }

    // Muestra el menú contextual de long click (editar, eliminar, estadísticas)
    private void showQueenOptions(Queen queen, int position) {
        String[] options = {
            DivaStrings.actionEdit(requireContext()),
            DivaStrings.actionDelete(requireContext()),
            DivaStrings.actionViewStats(requireContext())
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(queen.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            AddEditQueenDialogFragment.newInstance(queen.getId())
                                .show(getParentFragmentManager(), "tag_edit_queen");
                            break;
                        case 1:
                            confirmDeleteQueen(queen, position);
                            break;
                        case 2:
                            QueenStatsDialogFragment.newInstance(queen.getId())
                                    .show(getParentFragmentManager(), "tag_queen_stats");
                            break;
                    }
                })
                .show();
    }

    // Escucha la confirmación del diálogo de borrado y borra en Room
    private void setupDeleteDialogResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                REQUEST_DELETE_QUEEN,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    boolean confirmed = result.getBoolean(DeleteConfirmDialogFragment.RESULT_CONFIRMED, false);
                    if (!confirmed) {
                        pendingDeleteQueenPosition = RecyclerView.NO_POSITION;
                        return;
                    }

                    Queen queen = adapter.getQueen(pendingDeleteQueenPosition);
                    if (queen != null) {
                        String queenName = queen.getName();
                        SlayVaultDatabase.databaseExecutor.execute(() ->
                                SlayVaultDatabase.getInstance(requireContext().getApplicationContext())
                                        .queenDao()
                                        .deleteById(queen.getId())
                        );
                        LocalReminderNotifier.notifyQueenDeleted(requireContext(), queenName);
                        Toast.makeText(getContext(), getString(R.string.queen_deleted, queenName), Toast.LENGTH_SHORT).show();
                    }

                    pendingDeleteQueenPosition = RecyclerView.NO_POSITION;
                }
        );
    }

    // Abre el diálogo de confirmación de borrado de una queen
    private void confirmDeleteQueen(Queen queen, int position) {
        pendingDeleteQueenPosition = position;
        DeleteConfirmDialogFragment.newInstance(
                        REQUEST_DELETE_QUEEN,
                        queen != null ? queen.getId() : null,
                        queen != null ? queen.getName() : null
                )
                .show(getParentFragmentManager(), TAG_DELETE_DIALOG);
    }

    // Muestra u oculta el empty state según si el adapter está vacío
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Observa las queens de Room con LiveData y actualiza el adapter
    private void observeQueens() {
        SlayVaultDatabase.getInstance(requireContext().getApplicationContext())
                .queenDao()
                .getAllQueens()
                .observe(getViewLifecycleOwner(), entities -> {
                    List<Queen> queens = QueenMapper.fromEntityList(entities);
                    adapter.setQueens(queens);
                    updateEmptyState();
                });
    }
}
