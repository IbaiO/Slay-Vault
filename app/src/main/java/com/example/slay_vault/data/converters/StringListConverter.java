package com.example.slay_vault.data.converters;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

// TypeConverter  List<String> <--> String JSON
public class StringListConverter {

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        for (String item : list) {
            jsonArray.put(item);
        }
        return jsonArray.toString();
    }

    @TypeConverter
    public static List<String> toStringList(String json) {
        List<String> result = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            return result;
        }

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.optString(i));
            }
        } catch (JSONException ignored) {
            // Devolver lista vacía
        }

        return result;
    }
}
