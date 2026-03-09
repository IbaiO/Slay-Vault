package com.example.slay_vault.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.slay_vault.data.converters.DateConverter;
import com.example.slay_vault.data.converters.StringListConverter;

import java.util.Date;
import java.util.List;

// Entidad Room para la tabla "shade_entries". Representa un shade sobre una Queen. CASCADE elimina sus shades al borrar la Queen.
@Entity(
    tableName = "shade_entries",
    foreignKeys = @ForeignKey(
        entity = QueenEntity.class,
        parentColumns = "id",
        childColumns = "queen_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = "queen_id")}
)
@TypeConverters({DateConverter.class, StringListConverter.class})
public class ShadeEntryEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "queen_id")
    @NonNull
    private String queenId;

    @ColumnInfo(name = "title")
    @NonNull
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "category")
    @NonNull
    private String category;

    @ColumnInfo(name = "intensity")
    private float intensity; // 1-5 estrellas

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "tags")
    private List<String> tags;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    // Constructor vacío requerido por Room
    public ShadeEntryEntity() { }

    // Constructor completo para insertar datos desde código
    @Ignore
    public ShadeEntryEntity(@NonNull String id, @NonNull String queenId, @NonNull String title,
                            String description, @NonNull String category, float intensity,
                            Date date, List<String> tags, Date createdAt, Date updatedAt) {
        this.id = id;
        this.queenId = queenId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.intensity = intensity;
        this.date = date;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y setters

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getQueenId() {
        return queenId;
    }

    public void setQueenId(@NonNull String queenId) {
        this.queenId = queenId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
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

    @Override
    public String toString() {
        return "ShadeEntryEntity{" +
                "id='" + id + '\'' +
                ", queenId='" + queenId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", intensity=" + intensity +
                ", date=" + date +
                '}';
    }
}

