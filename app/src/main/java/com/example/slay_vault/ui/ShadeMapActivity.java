package com.example.slay_vault.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.slay_vault.MainActivity;
import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.entities.ShadeEntryEntity;
import com.example.slay_vault.data.utils.LocationManager;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Actividad que muestra el mapa con los "shades" (cotilleos) geolocalizados
 * Implementa Google Maps y gestión de permisos de ubicación en tiempo de ejecución
 */
public class ShadeMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ShadeMapActivity";
    private static final float DEFAULT_ZOOM = 12f;

    private GoogleMap googleMap;
    private LocationManager locationManager;
    private SupportMapFragment mapFragment;
    private MaterialToolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shade_map);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
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

        // Inicializar LocationManager
        locationManager = new LocationManager(this);

        // Registrar launcher para permisos de ubicación
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> locationManager.handlePermissionResult(granted)
        );
        locationManager.setPermissionLauncher(locationPermissionLauncher);

        // Obtener el fragmento de mapa
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Observar cambios de permiso
        locationManager.getPermissionStatus().observe(this, status -> {
            switch (status) {
                case GRANTED:
                    Toast.makeText(this, getString(R.string.shade_map_location_enabled), Toast.LENGTH_SHORT).show();
                    if (googleMap != null) {
                        enableMyLocation();
                    }
                    break;
                case DENIED:
                    Toast.makeText(this, getString(R.string.shade_map_permission_required),
                            Toast.LENGTH_LONG).show();
                    break;
                case NOT_REQUESTED:
                    break;
            }
        });

        // Observar cambios de ubicación
        locationManager.getCurrentLocation().observe(this, location -> {
            if (location != null && googleMap != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
                Log.i(TAG, "Cámara animada a la ubicación actual");
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        Log.i(TAG, "Mapa listo");

        // Configurar el mapa
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Solicitar permiso de ubicación si es necesario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationPermission();
        } else {
            locationManager.handlePermissionResult(true);
        }

        // Cargar y mostrar los shades en el mapa
        loadShadesOnMap();
    }

    /**
     * Habilita la visualización de ubicación en tiempo real en el mapa
     */
    private void enableMyLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                locationManager.getLastLocation();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error habilitando Mi Ubicación", e);
        }
    }

    private void loadShadesOnMap() {
        SlayVaultDatabase.databaseExecutor.execute(() -> {
            SlayVaultDatabase db = SlayVaultDatabase.getInstance(getApplicationContext());
            String userId = SessionManager.getUserId(this);
            List<ShadeEntryEntity> shades = (userId == null || userId.trim().isEmpty())
                    ? db.shadeEntryDao().getAllShadesList()
                    : db.shadeEntryDao().getAllShadesListByUser(userId);

            runOnUiThread(() -> renderShadesOnMap(shades));
        });
    }

    private void setupNavigationDrawer() {
        if (drawerLayout == null || navigationView == null) {
            return;
        }

        bindDrawerHeader();

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
        );
        drawerLayout.addDrawerListener(drawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_shade_map) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindDrawerHeader();
    }

    private void bindDrawerHeader() {
        if (navigationView == null || navigationView.getHeaderCount() == 0) {
            return;
        }
        View header = navigationView.getHeaderView(0);
        TextView usernameView = header.findViewById(R.id.tv_drawer_user_name);
        TextView hintView = header.findViewById(R.id.tv_drawer_user_hint);

        String username = SessionManager.getUsername(this);
        usernameView.setText((username == null || username.trim().isEmpty())
                ? getString(R.string.auth_username)
                : username);
        hintView.setText(R.string.profile_tap_to_edit);

        header.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileEditActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void renderShadesOnMap(@NonNull List<ShadeEntryEntity> shades) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int validMarkers = 0;

        for (ShadeEntryEntity shade : shades) {
            Double latitude = shade.getLatitude();
            Double longitude = shade.getLongitude();
            if (!hasValidCoordinates(latitude, longitude)) {
                continue;
            }

            String title = shade.getTitle() != null && !shade.getTitle().trim().isEmpty()
                    ? shade.getTitle()
                    : getString(R.string.shade_map_title);
            String snippet = shade.getDescription() != null ? shade.getDescription() : "";
            float markerColor = colorForIntensity(shade.getIntensity());

            LatLng latLng = addMarkerToMap(title, snippet, latitude, longitude, markerColor);
            boundsBuilder.include(latLng);
            validMarkers++;
        }

        if (validMarkers == 0) {
            Toast.makeText(this, getString(R.string.shade_map_no_location_data), Toast.LENGTH_SHORT).show();
            return;
        }

        if (validMarkers == 1) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(boundsBuilder.build().getCenter(), DEFAULT_ZOOM));
            return;
        }

        int paddingPx = Math.round(getResources().getDisplayMetrics().density * 48);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), paddingPx));
    }

    private boolean hasValidCoordinates(Double latitude, Double longitude) {
        return latitude != null
                && longitude != null
                && latitude >= -90d
                && latitude <= 90d
                && longitude >= -180d
                && longitude <= 180d;
    }

    private float colorForIntensity(float intensity) {
        if (intensity >= 4f) {
            return BitmapDescriptorFactory.HUE_RED;
        }
        if (intensity >= 2.5f) {
            return BitmapDescriptorFactory.HUE_ORANGE;
        }
        return BitmapDescriptorFactory.HUE_YELLOW;
    }

    /**
     * Añade un marcador al mapa
     *
     * @param title    Título del shade
     * @param snippet  Descripción del shade
     * @param latitude Latitud
     * @param longitude Longitud
     * @param markerColor Color del marcador (HUE_RED, HUE_ORANGE, etc.)
     */
    private LatLng addMarkerToMap(String title, String snippet, double latitude, double longitude, float markerColor) {
        LatLng location = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

        googleMap.addMarker(markerOptions);
        Log.d(TAG, "Marcador agregado: " + title + " en " + latitude + ", " + longitude);
        return location;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }
}


