package com.example.slay_vault;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.mappers.QueenMapper;
import com.example.slay_vault.data.utils.ShadeBookExporter;
import com.example.slay_vault.notifications.AirplaneModeReceiver;
import com.example.slay_vault.notifications.LocalReminderNotifier;
import com.example.slay_vault.ui.DivaStrings;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private NavController detailsNavController;
    private boolean isDualPane = false;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private final AirplaneModeReceiver airplaneModeReceiver = new AirplaneModeReceiver();

    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/plain"),
                    uri -> {
                        if (uri == null) return;
                        SlayVaultDatabase.databaseExecutor.execute(() -> {
                            try {
                                        java.util.List<com.example.slay_vault.data.models.Queen> queens =
                                        QueenMapper.fromEntityList(
                                                SlayVaultDatabase.getInstance(getApplicationContext())
                                                        .queenDao()
                                                        .getAllQueensList());
                                ShadeBookExporter.writeToUri(
                                        getApplicationContext(),
                                        getContentResolver(),
                                        uri,
                                        queens,
                                        DivaStrings.exportFileHeader(this));
                                runOnUiThread(() -> Toast.makeText(this,
                                        DivaStrings.exportSuccess(this), Toast.LENGTH_LONG).show());
                            } catch (IOException e) {
                                runOnUiThread(() -> Toast.makeText(this,
                                        DivaStrings.exportError(this), Toast.LENGTH_SHORT).show());
                            }
                        });
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        LocalReminderNotifier.createChannel(this);

        drawerLayout = findViewById(R.id.drawer_layout);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        isDualPane = getResources().getBoolean(R.bool.is_dual_pane);

        if (isDualPane) {
            setupDualPaneNavigation();
        } else {
            setupSinglePaneNavigation();
        }

        setupNavigationDrawer();

        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigation_view);

        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (navController != null) {
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.queensListFragment, R.id.settingsFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            }
            NavigationUI.setupWithNavController(navigationView, navController);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_queens_list) {
                if (navController != null) {
                    navController.navigate(R.id.action_global_to_queensList);
                }
            } else if (id == R.id.nav_settings) {
                if (navController != null) {
                    navController.navigate(R.id.action_global_to_settings);
                }
            } else if (id == R.id.nav_export) {
                exportLauncher.launch(DivaStrings.exportDefaultFilename(this));
            } else if (id == R.id.nav_notification_test) {
                LocalReminderNotifier.showMakeupReminder(this);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(airplaneModeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(airplaneModeReceiver);
    }

    // Configura el NavController en modo de un solo panel
    private void setupSinglePaneNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    // Configura el NavController del panel derecho (modo dual)
    private void setupDualPaneNavigation() {
        NavHostFragment detailsNavHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.shady_details_container);
        if (detailsNavHostFragment != null) {
            detailsNavController = detailsNavHostFragment.getNavController();
        }
    }

    // Detalles de una drag
    public void navigateToShadyDetails(String shadyId) {
        if (isDualPane && detailsNavController != null) {
            Bundle args = new Bundle();
            args.putString("shady_id", shadyId);
            detailsNavController.navigate(R.id.shadyDetailsFragment, args);
        } else if (navController != null) {
            Bundle args = new Bundle();
            args.putString("shady_id", shadyId);
            navController.navigate(R.id.action_queensList_to_shadyDetails, args);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (isDualPane && detailsNavController != null
                && detailsNavController.navigateUp()) {
            return true;
        }
        if (navController != null) {
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.queensListFragment, R.id.settingsFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}