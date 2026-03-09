package com.example.slay_vault.data.mappers;

import com.example.slay_vault.data.entities.QueenEntity;
import com.example.slay_vault.data.models.Queen;

import java.util.ArrayList;
import java.util.List;

// Mapper para convertir entre QueenEntity (Room) y Queen (modelo de dominio)
public class QueenMapper {

    // Convierte QueenEntity a Queen
    public static Queen fromEntity(QueenEntity entity) {
        if (entity == null) {
            return null;
        }

        Queen queen = new Queen();
        queen.setId(entity.getId());
        queen.setName(entity.getName());
        queen.setDescription(entity.getDescription());
        queen.setPhotoUri(entity.getPhotoUri());
        queen.setEnvyLevel(entity.getEnvyLevel());
        queen.setShadesCount(entity.getShadesCount());
        queen.setLastShadeDate(entity.getLastShadeDate());
        queen.setCreatedAt(entity.getCreatedAt());
        queen.setUpdatedAt(entity.getUpdatedAt());

        return queen;
    }

    // Convierte Queen a QueenEntity
    public static QueenEntity toEntity(Queen queen) {
        if (queen == null) {
            return null;
        }

        QueenEntity entity = new QueenEntity();
        entity.setId(queen.getId());
        entity.setName(queen.getName());
        entity.setDescription(queen.getDescription());
        entity.setPhotoUri(queen.getPhotoUri());
        entity.setEnvyLevel(queen.getEnvyLevel());
        entity.setShadesCount(queen.getShadesCount());
        entity.setLastShadeDate(queen.getLastShadeDate());
        entity.setCreatedAt(queen.getCreatedAt());
        entity.setUpdatedAt(queen.getUpdatedAt());

        return entity;
    }

    // Convierte lista de QueenEntity a lista de Queen
    public static List<Queen> fromEntityList(List<QueenEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<Queen> queens = new ArrayList<>();
        for (QueenEntity entity : entities) {
            queens.add(fromEntity(entity));
        }
        return queens;
    }
}

