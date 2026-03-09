package com.example.slay_vault.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

// Entidad Room para la tabla "queens". Representa una rival en la BD.
@Entity(tableName = "queens")
public class QueenEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "photo_uri")
    private String photoUri;

    @ColumnInfo(name = "envy_level")
    private float envyLevel; // 0-5 estrellas

    @ColumnInfo(name = "shades_count")
    private int shadesCount = 0; // Se recalcula automáticamente

    @ColumnInfo(name = "last_shade_date")
    private String lastShadeDate = null; // Se actualiza al añadir/borrar shades

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    // Constructor vacío requerido por Room
    public QueenEntity() { }

    // Constructor completo para insertar datos desde código
    @Ignore
    public QueenEntity(@NonNull String id, @NonNull String name, String description,
                       String photoUri, float envyLevel, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photoUri = photoUri;
        this.envyLevel = envyLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y setters requeridos por Room

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public float getEnvyLevel() {
        return envyLevel;
    }

    public void setEnvyLevel(float envyLevel) {
        this.envyLevel = envyLevel;
    }

    public int getShadesCount() {
        return shadesCount;
    }

    public void setShadesCount(int shadesCount) {
        this.shadesCount = shadesCount;
    }

    public String getLastShadeDate() {
        return lastShadeDate;
    }

    public void setLastShadeDate(String lastShadeDate) {
        this.lastShadeDate = lastShadeDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "QueenEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", envyLevel=" + envyLevel +
                ", shadesCount=" + shadesCount +
                '}';
    }
}
