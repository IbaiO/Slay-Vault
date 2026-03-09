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
import com.example.slay_vault.data.utils.SampleDataGenerator;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// BD Room de SlayVault. Tablas: queens y shade_entries. Singleton con ExecutorService para background.
@Database(
    entities = {QueenEntity.class, ShadeEntryEntity.class},
    version = 3,
    exportSchema = false
)
@TypeConverters({DateConverter.class, StringListConverter.class})
public abstract class SlayVaultDatabase extends RoomDatabase {

    // DAOs implementados por Room
    public abstract QueenDao queenDao();
    public abstract ShadeEntryDao shadeEntryDao();

    // Instancia singleton
    private static volatile SlayVaultDatabase INSTANCE;

    // Pool de hilos para operaciones en background (4 hilos)
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static final String TAG = "SlayVaultDB";

    // Devuelve la instancia única, creándola si es necesario
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

    // Callback: inserta datos de ejemplo al crear la BD o tras migración destructiva
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            populateSampleData();
        }

        @Override
        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
            super.onDestructiveMigration(db);
            populateSampleData();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.d(TAG, "Base de datos abierta");
        }
    };

    // Inserta Drag Queens y shades de ejemplo y recalcula contadores
    private static void populateSampleData() {
        databaseExecutor.execute(() -> {
            if (INSTANCE == null) return;

            QueenDao queenDao = INSTANCE.queenDao();
            ShadeEntryDao shadeDao = INSTANCE.shadeEntryDao();

            queenDao.insertAll(SampleDataGenerator.generateSampleQueens());
            shadeDao.insertAll(SampleDataGenerator.generateSampleShades());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            long now = System.currentTimeMillis();
            List<QueenEntity> queens = queenDao.getAllQueensList();
            for (QueenEntity queen : queens) {
                int count = shadeDao.getShadesCountByQueenSync(queen.getId());
                queenDao.updateShadesCount(queen.getId(), count, now);

                ShadeEntryEntity latest = shadeDao.getMostRecentShadeByQueenSync(queen.getId());
                if (latest != null && latest.getDate() != null) {
                    queenDao.updateLastShadeDate(queen.getId(),
                            sdf.format(latest.getDate()), now);
                }
            }

            Log.d(TAG, "Datos de ejemplo insertados correctamente");
        });
    }

    // Cierra y libera la instancia
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}