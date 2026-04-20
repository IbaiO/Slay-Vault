package com.example.slay_vault.data.mappers;

import com.example.slay_vault.data.entities.ShadeEntryEntity;
import com.example.slay_vault.data.models.Shade;

import java.util.ArrayList;
import java.util.List;

// Mapper para convertir entre ShadeEntryEntity (Room) y Shade (modelo de dominio)
public class ShadeMapper {

    // Convierte ShadeEntryEntity a Shade
    public static Shade fromEntity(ShadeEntryEntity entity) {
        if (entity == null) {
            return null;
        }

        Shade shade = new Shade();
        shade.setId(entity.getId());
        shade.setQueenId(entity.getQueenId());
        shade.setUserId(entity.getUserId());
        shade.setTitle(entity.getTitle());
        shade.setDescription(entity.getDescription());
        shade.setCategory(entity.getCategory());
        shade.setIntensity(entity.getIntensity());
        shade.setDate(entity.getDate());
        shade.setTags(entity.getTags());
        shade.setCreatedAt(entity.getCreatedAt());
        shade.setUpdatedAt(entity.getUpdatedAt());
        shade.setLatitude(entity.getLatitude());
        shade.setLongitude(entity.getLongitude());
        shade.setLocationAddress(entity.getLocationAddress());

        return shade;
    }


    // Convierte lista de ShadeEntryEntity a lista de Shade
    public static List<Shade> fromEntityList(List<ShadeEntryEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<Shade> shades = new ArrayList<>();
        for (ShadeEntryEntity entity : entities) {
            shades.add(fromEntity(entity));
        }
        return shades;
    }
}