package com.example.slay_vault;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
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

    private static final String STATE_ACTIVE_PAGE = "state_active_page";
    private static final String STATE_ACTIVE_SHADE_ID = "state_active_shade_id";
    private static final int PAGE_LIST = 0;
    private static final int PAGE_SETTINGS = 1;
    private static final int PAGE_SHADE_DETAILS = 2;

    private NavController navController;
    private NavController detailsNavController;
    private boolean isDualPane = false;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private boolean airplaneReceiverRegistered = false;

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

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                setNotificationsPreferenceEnabled(isGranted);
                if (isGranted) {
                    LocalReminderNotifier.showMakeupReminder(this);
                    Toast.makeText(this, R.string.notification_enabled_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
                }
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

        View mainContainer = findViewById(R.id.main);
        if (mainContainer == null) {
            mainContainer = findViewById(R.id.main_land);
        }
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        isDualPane = getResources().getBoolean(R.bool.is_dual_pane);

        if (isDualPane) {
            setupDualPaneNavigation();
        } else {
            setupSinglePaneNavigation();
        }

        setupNavigationDrawer();
        restorePaneState(savedInstanceState);

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

        NavController drawerNavController = (navController != null) ? navController : detailsNavController;
        if (drawerNavController != null) {
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.queensListFragment,
                    R.id.settingsFragment,
                    R.id.shadyDetailsFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, drawerNavController, appBarConfiguration);
            }
            // Solo auto-vinculamos el drawer en single-pane.
            if (!isDualPane && navController != null) {
                NavigationUI.setupWithNavController(navigationView, navController);
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_queens_list) {
                if (isDualPane && detailsNavController != null) {
                    safeNavigate(detailsNavController, R.id.placeholderFragment, null);
                } else if (navController != null) {
                    safeNavigate(navController, R.id.action_global_to_queensList, null);
                }
            } else if (id == R.id.nav_settings) {
                if (isDualPane && detailsNavController != null) {
                    safeNavigate(detailsNavController, R.id.settingsFragment, null);
                } else if (navController != null) {
                    safeNavigate(navController, R.id.action_global_to_settings, null);
                }
            } else if (id == R.id.nav_export) {
                exportLauncher.launch(DivaStrings.exportDefaultFilename(this));
            } else if (id == R.id.nav_notification_test) {
                handleNotificationTestAction();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!airplaneReceiverRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            registerReceiver(airplaneModeReceiver, filter);
            airplaneReceiverRegistered = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (airplaneReceiverRegistered) {
            try {
                unregisterReceiver(airplaneModeReceiver);
            } catch (IllegalArgumentException ignored) {
                // Evita crash si el receptor ya no estaba registrado al recrear Activity.
            }
            airplaneReceiverRegistered = false;
        }
    }

    // Configura el NavController en modo de un solo panel
    private void setupSinglePaneNavigation() {
        detailsNavController = null;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_main);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    // Configura el NavController del panel derecho (modo dual)
    private void setupDualPaneNavigation() {
        NavHostFragment mainNavHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_main);
        if (mainNavHostFragment != null) {
            navController = mainNavHostFragment.getNavController();
        }
        NavHostFragment detailsNavHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_detail);
        if (detailsNavHostFragment != null) {
            detailsNavController = detailsNavHostFragment.getNavController();
        }
    }

    // Detalles de una drag
    public void navigateToShadyDetails(String shadyId) {
        if (isDualPane && detailsNavController != null) {
            Bundle args = new Bundle();
            args.putString("shady_id", shadyId);
            safeNavigate(detailsNavController, R.id.shadyDetailsFragment, args);
        } else if (navController != null) {
            Bundle args = new Bundle();
            args.putString("shady_id", shadyId);
            safeNavigate(navController, R.id.action_queensList_to_shadyDetails, args);
        }
    }

    private void restorePaneState(@Nullable Bundle savedInstanceState) {
        int page = PAGE_LIST;
        String shadyId = null;

        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(STATE_ACTIVE_PAGE, PAGE_LIST);
            shadyId = savedInstanceState.getString(STATE_ACTIVE_SHADE_ID);
        }

        if (isDualPane) {
            if (navController != null) {
                safeNavigate(navController, R.id.action_global_to_queensList, null);
            }

            if (detailsNavController == null) {
                return;
            }

            if (page == PAGE_SETTINGS) {
                safeNavigate(detailsNavController, R.id.settingsFragment, null);
            } else if (page == PAGE_SHADE_DETAILS && shadyId != null) {
                Bundle args = new Bundle();
                args.putString("shady_id", shadyId);
                safeNavigate(detailsNavController, R.id.shadyDetailsFragment, args);
            } else {
                safeNavigate(detailsNavController, R.id.placeholderFragment, null);
            }
            return;
        }

        if (navController == null) {
            return;
        }

        if (page == PAGE_SETTINGS) {
            safeNavigate(navController, R.id.action_global_to_settings, null);
        } else if (page == PAGE_SHADE_DETAILS && shadyId != null) {
            Bundle args = new Bundle();
            args.putString("shady_id", shadyId);
            safeNavigate(navController, R.id.action_queensList_to_shadyDetails, args);
        }
    }

    private int getActivePage() {
        NavController controller = isDualPane ? detailsNavController : navController;
        if (controller == null || controller.getCurrentDestination() == null) {
            return PAGE_LIST;
        }

        int destinationId = controller.getCurrentDestination().getId();
        if (destinationId == R.id.settingsFragment) {
            return PAGE_SETTINGS;
        }
        if (destinationId == R.id.shadyDetailsFragment) {
            return PAGE_SHADE_DETAILS;
        }
        return PAGE_LIST;
    }

    @Nullable
    private String getActiveShadeId() {
        NavController controller = isDualPane ? detailsNavController : navController;
        if (controller == null || controller.getCurrentDestination() == null) {
            return null;
        }
        if (controller.getCurrentDestination().getId() != R.id.shadyDetailsFragment) {
            return null;
        }
        if (controller.getCurrentBackStackEntry() == null || controller.getCurrentBackStackEntry().getArguments() == null) {
            return null;
        }
        return controller.getCurrentBackStackEntry().getArguments().getString("shady_id");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_ACTIVE_PAGE, getActivePage());
        outState.putString(STATE_ACTIVE_SHADE_ID, getActiveShadeId());
    }

    private void safeNavigate(@NonNull NavController controller,
                              int destinationOrActionId,
                              @Nullable Bundle args) {
        try {
            if (args == null) {
                controller.navigate(destinationOrActionId);
            } else {
                controller.navigate(destinationOrActionId, args);
            }
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // Evita crash por navegacion invalida durante rotacion/recreacion.
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
                    R.id.queensListFragment,
                    R.id.settingsFragment,
                    R.id.shadyDetailsFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    private void handleNotificationTestAction() {
        if (LocalReminderNotifier.areNotificationsEffectivelyEnabled(this)) {
            LocalReminderNotifier.showMakeupReminder(this);
            Toast.makeText(this, R.string.notification_test_sent, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(DivaStrings.notificationsDisabledDialogTitle(this))
                .setMessage(DivaStrings.notificationsDisabledDialogMessage(this))
                .setPositiveButton(DivaStrings.notificationsDisabledDialogPositive(this), (dialog, which) -> {
                    setNotificationsPreferenceEnabled(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            && !LocalReminderNotifier.hasPermission(this)) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    } else {
                        LocalReminderNotifier.showMakeupReminder(this);
                        Toast.makeText(this, R.string.notification_test_sent, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(DivaStrings.notificationsDisabledDialogNegative(this), null)
                .show();
    }

    private void setNotificationsPreferenceEnabled(boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(LocalReminderNotifier.PREF_ENABLE_NOTIFICATIONS, enabled)
                .apply();
    }
}