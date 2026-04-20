package com.example.slay_vault.data.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.slay_vault.data.converters.DateConverter;
import com.example.slay_vault.data.converters.StringListConverter;
import com.example.slay_vault.data.dao.QueenDao;
import com.example.slay_vault.data.dao.ShadeEntryDao;
import com.example.slay_vault.data.entities.QueenEntity;
import com.example.slay_vault.data.entities.ShadeEntryEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// BD Room de SlayVault. Tablas: queens y shade_entries. Singleton con ExecutorService para background.
@Database(
    entities = {QueenEntity.class, ShadeEntryEntity.class},
    version = 6,
    exportSchema = false
)
@TypeConverters({DateConverter.class, StringListConverter.class})
public abstract class SlayVaultDatabase extends RoomDatabase {

    // Accesos DAO.
    public abstract QueenDao queenDao();
    public abstract ShadeEntryDao shadeEntryDao();

    // Instancia singleton
    private static volatile SlayVaultDatabase INSTANCE;

    // Pool para operaciones en segundo plano.
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static final String TAG = "SlayVaultDB";

    // Devuelve la instancia unica de la base de datos.
    public static SlayVaultDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (SlayVaultDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            SlayVaultDatabase.class,
                            "slayvault_database"
                    )
                    .addCallback(roomCallback)
                            .fallbackToDestructiveMigration(true)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback de apertura para trazas de diagnóstico.
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.d(TAG, "Base de datos abierta");
        }
    };


    // Cierra y libera la instancia actual.
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}