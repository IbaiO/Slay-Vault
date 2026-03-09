package com.example.slay_vault.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.slay_vault.data.entities.QueenEntity;

import java.util.List;

// DAO para Queens
@Dao
public interface QueenDao {

    // Insertar una nueva Drag Queen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(QueenEntity queen);

    // Insertar múltiples Drag Queens
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QueenEntity> queens);

    // Insertar una Drag Queen y devolver su ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAndReturnId(QueenEntity queen);

    // Actualizar una Drag Queen
    @Update
    void update(QueenEntity queen);

    // Actualizar el nivel de envidia de una Drag Queen
    @Query("UPDATE queens SET envy_level = :envyLevel, updated_at = :updatedAt WHERE id = :queenId")
    void updateEnvyLevel(String queenId, float envyLevel, long updatedAt);

    // Actualizar el contador de shades de una Drag Queen
    @Query("UPDATE queens SET shades_count = :shadesCount, updated_at = :updatedAt WHERE id = :queenId")
    void updateShadesCount(String queenId, int shadesCount, long updatedAt);

    // Incrementar el contador de shades de una Drag Queen
    @Query("UPDATE queens SET shades_count = shades_count + 1, updated_at = :updatedAt WHERE id = :queenId")
    void incrementShadesCount(String queenId, long updatedAt);

    // -1 a los shades de una Drag Queen
    @Query("UPDATE queens SET shades_count = MAX(shades_count - 1, 0), updated_at = :updatedAt WHERE id = :queenId")
    void decrementShadesCount(String queenId, long updatedAt);

    // Actualizar la fecha del último shade
    @Query("UPDATE queens SET last_shade_date = :lastShadeDate, updated_at = :updatedAt WHERE id = :queenId")
    void updateLastShadeDate(String queenId, String lastShadeDate, long updatedAt);

    // Eliminar una Drag Queen
    @Delete
    void delete(QueenEntity queen);

    // Eliminar una Drag Queen por ID
    @Query("DELETE FROM queens WHERE id = :queenId")
    void deleteById(String queenId);

    // Eliminar todas las Drag Queens
    @Query("DELETE FROM queens")
    void deleteAll();

    // Obtener todas las Drag Queens (LiveData)
    @Query("SELECT * FROM queens ORDER BY updated_at DESC")
    LiveData<List<QueenEntity>> getAllQueens();

    // Obtener todas las Drag Queens (lista simple)
    @Query("SELECT * FROM queens ORDER BY updated_at DESC")
    List<QueenEntity> getAllQueensList();

    // Obtener una Drag Queen por ID (LiveData)
    @Query("SELECT * FROM queens WHERE id = :queenId")
    LiveData<QueenEntity> getQueenById(String queenId);

    // Obtener una Drag Queen por ID (sin LiveData, para operaciones síncronas)
    @Query("SELECT * FROM queens WHERE id = :queenId")
    QueenEntity getQueenByIdSync(String queenId);

    // Buscar Drag Queens por nombre (búsqueda con LIKE)
    @Query("SELECT * FROM queens WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    LiveData<List<QueenEntity>> searchQueensByName(String searchQuery);

    // Obtener Drag Queens ordenadas por nombre
    @Query("SELECT * FROM queens ORDER BY name ASC")
    LiveData<List<QueenEntity>> getQueensOrderedByName();

    // Obtener Drag Queens ordenadas por nivel de envidia
    @Query("SELECT * FROM queens ORDER BY envy_level DESC")
    LiveData<List<QueenEntity>> getQueensOrderedByEnvy();

    // Obtener Drag Queens ordenadas por cantidad de shades
    @Query("SELECT * FROM queens ORDER BY shades_count DESC")
    LiveData<List<QueenEntity>> getQueensOrderedByShades();

    // Obtener Drag Queens ordenadas por fecha de última actualización
    @Query("SELECT * FROM queens ORDER BY updated_at DESC")
    LiveData<List<QueenEntity>> getQueensOrderedByDate();

    // Obtener Drag Queens con nivel de envidia mayor a un valor
    @Query("SELECT * FROM queens WHERE envy_level >= :minEnvyLevel ORDER BY envy_level DESC")
    LiveData<List<QueenEntity>> getQueensWithHighEnvy(float minEnvyLevel);

    // Obtener Drag Queens que tienen shades
    @Query("SELECT * FROM queens WHERE shades_count > 0 ORDER BY shades_count DESC")
    LiveData<List<QueenEntity>> getQueensWithShades();

    // Contar total de Drag Queens
    @Query("SELECT COUNT(*) FROM queens")
    LiveData<Integer> getQueensCount();

    // Verificar si existe una Drag Queen con un ID específico
    @Query("SELECT EXISTS(SELECT 1 FROM queens WHERE id = :queenId)")
    boolean queenExists(String queenId);

    // Obtener el promedio de nivel de envidia de todas las Drag Queens
    @Query("SELECT AVG(envy_level) FROM queens")
    LiveData<Float> getAverageEnvyLevel();

    // Obtener el total de shades de todas las Drag Queens
    @Query("SELECT SUM(shades_count) FROM queens")
    LiveData<Integer> getTotalShadesCount();

}

