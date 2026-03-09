package com.example.slay_vault.data.models;

import java.util.Date;

// Modelo de datos para una Queen (rival)
public class Queen {

    private String id;
    private String name;
    private String description;
    private String photoUri;
    private float envyLevel; // Nivel de envidia (0-5 estrellas)
    private int shadesCount = 0;
    private String lastShadeDate = null;
    private Date createdAt;
    private Date updatedAt;

    // Constructor vacío requerido por Room
    public Queen() {
    }

    // Constructor principal: createdAt y updatedAt se asignan con la fecha actual
    public Queen(String id, String name, String description, String photoUri, float envyLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photoUri = photoUri;
        this.envyLevel = envyLevel;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }

    // Constructor con fechas explícitas (para datos de muestra hardcodeados)
    public Queen(String id, String name, String description, String photoUri, float envyLevel, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photoUri = photoUri;
        this.envyLevel = envyLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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
        return "Queen{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", envyLevel=" + envyLevel +
                ", shadesCount=" + shadesCount +
                '}';
    }
}

