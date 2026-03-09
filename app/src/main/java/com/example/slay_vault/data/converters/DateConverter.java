package com.example.slay_vault.data.converters;

import androidx.room.TypeConverter;

import java.util.Date;

// TypeConverter Date <--> Long
public class DateConverter {

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date fromTimestamp(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}

