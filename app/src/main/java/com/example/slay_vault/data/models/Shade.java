package com.example.slay_vault.data.models;

import java.util.Date;
import java.util.List;

// Modelo de datos para un Shade (trapito sucio)
public class Shade {

    private String id;
    private String queenId; // ID de la Queen a la que pertenece este shade
    private String title;
    private String description;
    private String category; // Categoría (Outfit, Comentario, Actitud, etc.)
    private float intensity; // Nivel de shade/intensidad (1-5 estrellas)
    private Date date; // Fecha del shade
    private List<String> tags; // Etiquetas/tags asociados
    private Date createdAt;
    private Date updatedAt;

    // Constructor vacío requerido por Room
    public Shade() {
    }

    // Constructor completo: createdAt y updatedAt se asignan con la fecha actual
    public Shade(String id, String queenId, String title, String description,
                 String category, float intensity, Date date, List<String> tags) {
        this.id = id;
        this.queenId = queenId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.intensity = intensity;
        this.date = date;
        this.tags = tags;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueenId() {
        return queenId;
    }

    public void setQueenId(String queenId) {
        this.queenId = queenId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    // Añade un tag al shade si no existe ya
    public void addTag(String tag) {
        if (tags != null && !tags.contains(tag)) {
            tags.add(tag);
            this.updatedAt = new Date();
        }
    }

    // Elimina un tag del shade
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
            this.updatedAt = new Date();
        }
    }

    @Override
    public String toString() {
        return "Shade{" +
                "id='" + id + '\'' +
                ", queenId='" + queenId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", intensity=" + intensity +
                ", date=" + date +
                '}';
    }
}

