package com.example.slay_vault.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.slay_vault.data.entities.ShadeEntryEntity;

import java.util.Date;
import java.util.List;

// DAO para ShadeEntries
@Dao
public interface ShadeEntryDao {

    // Insertar un nuevo Shade
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShadeEntryEntity shade);

    // Insertar múltiples Shades
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ShadeEntryEntity> shades);

    // Insertar un Shade y devolver su ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAndReturnId(ShadeEntryEntity shade);

    // Actualizar un Shade existente
    @Update
    void update(ShadeEntryEntity shade);

    // Actualizar la intensidad de un Shade
    @Query("UPDATE shade_entries SET intensity = :intensity, updated_at = :updatedAt WHERE id = :shadeId")
    void updateIntensity(String shadeId, float intensity, long updatedAt);

    // Actualizar la categoría de un Shade
    @Query("UPDATE shade_entries SET category = :category, updated_at = :updatedAt WHERE id = :shadeId")
    void updateCategory(String shadeId, String category, long updatedAt);

    // Eliminar un Shade
    @Delete
    void delete(ShadeEntryEntity shade);

    // Eliminar un Shade por ID
    @Query("DELETE FROM shade_entries WHERE id = :shadeId")
    void deleteById(String shadeId);

    // Eliminar todos los Shades de una Queen específica
    @Query("DELETE FROM shade_entries WHERE queen_id = :queenId")
    void deleteAllByQueenId(String queenId);

    // Eliminar todos los Shades de un usuario
    @Query("DELETE FROM shade_entries WHERE user_id = :userId")
    void deleteAllByUser(String userId);

    // Eliminar todos los Shades
    @Query("DELETE FROM shade_entries")
    void deleteAll();

    // Obtener todos los Shades de una Queen específica (LiveData)
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getShadesByQueenId(String queenId);

    // Obtener todos los Shades de una Queen específica (LiveData) para un usuario específico
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId AND user_id = :userId ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getShadesByQueenIdForUser(String queenId, String userId);

    // Obtener todos los Shades de una Queen (lista simple, sin LiveData)
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId ORDER BY date DESC")
    List<ShadeEntryEntity> getShadesByQueenIdSync(String queenId);

    // Obtener todos los Shades de una Queen (lista simple, sin LiveData) para un usuario específico
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId AND user_id = :userId ORDER BY date DESC")
    List<ShadeEntryEntity> getShadesByQueenIdSyncForUser(String queenId, String userId);

    // Obtener un Shade por ID (LiveData)
    @Query("SELECT * FROM shade_entries WHERE id = :shadeId")
    LiveData<ShadeEntryEntity> getShadeById(String shadeId);

    // Obtener un Shade por ID para un usuario específico (LiveData)
    @Query("SELECT * FROM shade_entries WHERE id = :shadeId AND user_id = :userId")
    LiveData<ShadeEntryEntity> getShadeByIdForUser(String shadeId, String userId);

    // Obtener un Shade por ID (sin LiveData)
    @Query("SELECT * FROM shade_entries WHERE id = :shadeId")
    ShadeEntryEntity getShadeByIdSync(String shadeId);

    // Obtener un Shade por ID para un usuario específico (sin LiveData)
    @Query("SELECT * FROM shade_entries WHERE id = :shadeId AND user_id = :userId")
    ShadeEntryEntity getShadeByIdSyncForUser(String shadeId, String userId);

    // Obtener todos los Shades (LiveData)
    @Query("SELECT * FROM shade_entries ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getAllShades();

    // Obtener todos los Shades (lista simple)
    @Query("SELECT * FROM shade_entries ORDER BY date DESC")
    List<ShadeEntryEntity> getAllShadesList();

    // Obtener todos los Shades de un usuario específico (LiveData)
    @Query("SELECT * FROM shade_entries WHERE user_id = :userId ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getAllShadesByUser(String userId);

    // Obtener todos los Shades de un usuario específico (lista simple, sin LiveData)
    @Query("SELECT * FROM shade_entries WHERE user_id = :userId ORDER BY date DESC")
    List<ShadeEntryEntity> getAllShadesListByUser(String userId);

    // Buscar Shades por título o descripción
    @Query("SELECT * FROM shade_entries WHERE title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> searchShades(String searchQuery);

    // Obtener Shades de una Queen filtrados por categoría
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId AND category = :category ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getShadesByQueenAndCategory(String queenId, String category);

    // Obtener Shades por categoría (todas las Queens)
    @Query("SELECT * FROM shade_entries WHERE category = :category ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getShadesByCategory(String category);

    // Obtener Shades con intensidad mayor o igual a un valor
    @Query("SELECT * FROM shade_entries WHERE intensity >= :minIntensity ORDER BY intensity DESC, date DESC")
    LiveData<List<ShadeEntryEntity>> getShadesWithHighIntensity(float minIntensity);

    // Obtener Shades de una Queen con intensidad mayor a un valor
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId AND intensity >= :minIntensity ORDER BY intensity DESC")
    LiveData<List<ShadeEntryEntity>> getQueenShadesWithHighIntensity(String queenId, float minIntensity);

    // Obtener Shades ordenados por intensidad (descendente)
    @Query("SELECT * FROM shade_entries ORDER BY intensity DESC, date DESC")
    LiveData<List<ShadeEntryEntity>> getShadesOrderedByIntensity();

    // Obtener Shades de un rango de fechas
    @Query("SELECT * FROM shade_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getShadesByDateRange(Date startDate, Date endDate);

    // Contar total de Shades de una Queen
    @Query("SELECT COUNT(*) FROM shade_entries WHERE queen_id = :queenId")
    LiveData<Integer> getShadesCountByQueen(String queenId);

    // Contar total de Shades de una Queen (síncrono)
    @Query("SELECT COUNT(*) FROM shade_entries WHERE queen_id = :queenId")
    int getShadesCountByQueenSync(String queenId);

    // Contar total de Shades en la base de datos
    @Query("SELECT COUNT(*) FROM shade_entries")
    LiveData<Integer> getTotalShadesCount();

    // Obtener el promedio de intensidad de los Shades de una Queen
    @Query("SELECT AVG(intensity) FROM shade_entries WHERE queen_id = :queenId")
    LiveData<Float> getAverageIntensityByQueen(String queenId);

    // Obtener el promedio de intensidad de los Shades de una Queen (síncrono)
    @Query("SELECT AVG(intensity) FROM shade_entries WHERE queen_id = :queenId")
    Float getAverageIntensityByQueenSync(String queenId);

    // Obtener todas las categorías únicas
    @Query("SELECT DISTINCT category FROM shade_entries ORDER BY category ASC")
    LiveData<List<String>> getAllCategories();

    // Obtener el Shade más reciente de una Queen
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId ORDER BY date DESC LIMIT 1")
    LiveData<ShadeEntryEntity> getMostRecentShadeByQueen(String queenId);

    // Obtener el Shade más reciente de una Queen (síncrono)
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId ORDER BY date DESC LIMIT 1")
    ShadeEntryEntity getMostRecentShadeByQueenSync(String queenId);

    // Obtener el Shade más intenso de una Queen
    @Query("SELECT * FROM shade_entries WHERE queen_id = :queenId ORDER BY intensity DESC LIMIT 1")
    LiveData<ShadeEntryEntity> getMostIntenseShadeByQueen(String queenId);

    // Verificar si existe un Shade con un ID específico
    @Query("SELECT EXISTS(SELECT 1 FROM shade_entries WHERE id = :shadeId)")
    boolean shadeExists(String shadeId);

    // Obtener Shades recientes (últimos N días)
    @Query("SELECT * FROM shade_entries WHERE date >= :sinceDate ORDER BY date DESC")
    LiveData<List<ShadeEntryEntity>> getRecentShades(Date sinceDate);

    // Obtener la categoría más usada de una Queen (síncrono).
    @Query("SELECT category FROM shade_entries WHERE queen_id = :queenId " +
           "GROUP BY category ORDER BY COUNT(*) DESC LIMIT 1")
    String getMostUsedCategoryByQueenSync(String queenId);
}
