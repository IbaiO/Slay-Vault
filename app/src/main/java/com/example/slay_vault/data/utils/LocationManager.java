package com.example.slay_vault.data.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Gestor centralizado de ubicación usando FusedLocationProviderClient
 * Maneja permisos en tiempo de ejecución y obtención de la última ubicación conocida
 */
public class LocationManager {
    private static final String TAG = "LocationManager";
    
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<Location>();
    private final MutableLiveData<LocationPermissionStatus> permissionStatus =
            new MutableLiveData<LocationPermissionStatus>(LocationPermissionStatus.NOT_REQUESTED);

    private ActivityResultLauncher<String> locationPermissionLauncher;
    
    public LocationManager(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    /**
     * Registra el launcher para solicitudes de permisos
     */
    public void setPermissionLauncher(ActivityResultLauncher<String> launcher) {
        this.locationPermissionLauncher = launcher;
    }
    
    /**
     * Solicita permiso de ubicación
     */
    public void requestLocationPermission() {
        if (locationPermissionLauncher == null) {
            Log.w(TAG, "LocationPermissionLauncher no está registrado");
            return;
        }
        
        if (hasLocationPermission()) {
            permissionStatus.setValue(LocationPermissionStatus.GRANTED);
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    
    /**
     * Obtiene la última ubicación conocida
     */
    public void getLastLocation() {
        if (!hasLocationPermission()) {
            permissionStatus.setValue(LocationPermissionStatus.DENIED);
            Log.w(TAG, "Permiso de ubicación no concedido");
            return;
        }
        
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation.setValue(location);
                            Log.i(TAG, "Ubicación obtenida: " + location.getLatitude() + ", " + location.getLongitude());
                        } else {
                            Log.w(TAG, "No se pudo obtener la ubicación actual");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener ubicación", e);
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Error de seguridad al obtener ubicación", e);
        }
    }
    
    /**
     * Verifica si la app tiene permiso de ubicación
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * LiveData con la ubicación actual
     */
    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }
    
    /**
     * LiveData con el estado del permiso de ubicación
     */
    public LiveData<LocationPermissionStatus> getPermissionStatus() {
        return permissionStatus;
    }
    
    /**
     * Manejo del resultado de la solicitud de permiso
     */
    public void handlePermissionResult(boolean granted) {
        if (granted) {
            permissionStatus.setValue(LocationPermissionStatus.GRANTED);
            getLastLocation();
        } else {
            permissionStatus.setValue(LocationPermissionStatus.DENIED);
            Log.w(TAG, "Permiso de ubicación rechazado por el usuario");
        }
    }
    
    /**
     * Estados posibles del permiso de ubicación
     */
    public enum LocationPermissionStatus {
        GRANTED,      // Permiso concedido
        DENIED,       // Permiso denegado
        NOT_REQUESTED // Permiso no solicitado aún
    }
}
