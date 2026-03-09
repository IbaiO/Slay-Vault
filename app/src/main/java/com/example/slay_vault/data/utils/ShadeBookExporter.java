package com.example.slay_vault.data.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.example.slay_vault.data.database.SlayVaultDatabase;
import com.example.slay_vault.data.mappers.ShadeMapper;
import com.example.slay_vault.data.models.Queen;
import com.example.slay_vault.data.models.Shade;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Utilidad para exportar el Libro de las Sombras a un fichero .txt
public class ShadeBookExporter {

    private ShadeBookExporter() { }

    // Escribe el Libro de las Sombras en el URI proporcionado por el SAF
    public static void writeToUri(Context context,
                                   ContentResolver resolver,
                                   Uri uri,
                                   List<Queen> queens,
                                   String header) throws IOException {
        List<Shade> allShades = collectShadesFromRoom(context, queens);
        String content = buildContent(queens, allShades, header);

        try (OutputStream os = resolver.openOutputStream(uri);
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            writer.print(content);
            writer.flush();
        }
    }

    // Comprueba si hay shades para las queens dadas
    public static boolean hasShades(Context context, List<Queen> queens) {
        return !collectShadesFromRoom(context, queens).isEmpty();
    }

    private static List<Shade> collectShadesFromRoom(Context context, List<Queen> queens) {
        List<Shade> result = new ArrayList<>();
        SlayVaultDatabase db = SlayVaultDatabase.getInstance(context.getApplicationContext());
        for (Queen queen : queens) {
            result.addAll(ShadeMapper.fromEntityList(
                    db.shadeEntryDao().getShadesByQueenIdSync(queen.getId())));
        }
        return result;
    }

    private static String buildContent(List<Queen> queens,
                                       List<Shade> allShades,
                                       String header) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String now = sdf.format(new Date());

        sb.append(header).append("\n");
        sb.append("Exportado el: ").append(now).append("\n");
        sb.append("Total de shades: ").append(allShades.size()).append("\n\n");

        for (Queen queen : queens) {
            boolean hasBlock = false;
            StringBuilder block = new StringBuilder();
            block.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            block.append("👑 ").append(queen.getName()).append("\n");
            block.append(String.format(Locale.getDefault(),
                    "   Nivel de envidia: %.1f/5\n", queen.getEnvyLevel()));
            block.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

            for (Shade shade : allShades) {
                if (queen.getId().equals(shade.getQueenId())) {
                    hasBlock = true;
                    block.append("  📌 ").append(shade.getTitle()).append("\n");
                    if (shade.getCategory() != null && !shade.getCategory().isEmpty()) {
                        block.append("     Categoría: ").append(shade.getCategory()).append("\n");
                    }
                    block.append(String.format(Locale.getDefault(),
                            "     Intensidad: %.1f/5\n", shade.getIntensity()));
                    if (shade.getDate() != null) {
                        block.append("     Fecha: ").append(sdf.format(shade.getDate())).append("\n");
                    }
                    if (shade.getDescription() != null && !shade.getDescription().isEmpty()) {
                        block.append("     ").append(shade.getDescription()).append("\n");
                    }
                    if (shade.getTags() != null && !shade.getTags().isEmpty()) {
                        block.append("     Tags: #")
                                .append(String.join(" #", shade.getTags())).append("\n");
                    }
                    block.append("\n");
                }
            }

            if (hasBlock) sb.append(block);
        }

        sb.append("\n═══════════════════════════════════════\n");
        sb.append("    Generado con SlayVault 💅\n");
        sb.append("═══════════════════════════════════════\n");

        return sb.toString();
    }
}
