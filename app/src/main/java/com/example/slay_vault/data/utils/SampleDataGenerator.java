package com.example.slay_vault.data.utils;
import com.example.slay_vault.data.entities.QueenEntity;
import com.example.slay_vault.data.entities.ShadeEntryEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
// Utilidad para generar datos de ejemplo para la base de datos
public class SampleDataGenerator {
    // Crea un Date a partir de año, mes (1-based) y día
    private static Date date(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, 12, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
    // Genera la lista de queens de ejemplo (datos hardcodeados)
    public static List<QueenEntity> generateSampleQueens() {
        List<QueenEntity> queens = new ArrayList<>();
        queens.add(new QueenEntity(
                "queen_1", "Carmen Farala",
                "Diseñadora y ganadora de Drag Race España S1, conocida por su perfeccionismo y looks impecables",
                "carmen_farala", 4.5f, date(2026, 1, 30), date(2026, 2, 5)));
        queens.add(new QueenEntity(
                "queen_2", "Pupi Poisson",
                "Comedy queen veterana de Madrid famosa por su humor absurdo y energía caótica",
                "pupi_poisson", 4.0f, date(2026, 2, 9), date(2026, 2, 9)));
        queens.add(new QueenEntity(
                "queen_3", "Samantha Ballentines",
                "Drag gaditana con más de 20 años de carrera conocida por su humor irreverente y actuaciones caóticas",
                "samantha_ballentines", 3.5f, date(2026, 2, 5), date(2026, 2, 5)));
        queens.add(new QueenEntity(
                "queen_4", "Krystal Forever",
                "Drag colombiana conocida por su polémica participación en Drag Race España S5",
                "krystal_forever", 4.0f, date(2026, 3, 2), date(2026, 3, 2)));
        queens.add(new QueenEntity(
                "queen_5", "Estrella Xtravaganza",
                "Drag andaluza muy teatral, cantante y performer con gran presencia escénica",
                "estrella_xtravaganza", 3.5f, date(2026, 3, 1), date(2026, 3, 3)));
        queens.add(new QueenEntity(
                "queen_6", "Bestiah",
                "Drag española con estética intensa y enfoque artístico en performance y música",
                "bestiah", 4.5f, date(2026, 2, 25), date(2026, 2, 25)));
        queens.add(new QueenEntity(
                "queen_7", "Le Cocó",
                "Ganadora de Drag Race España S4 conocida por su elegancia, confianza y glamour",
                "le_coco", 3.0f, date(2025, 12, 25), date(2025, 12, 25)));
        return queens;
    }

    public static List<ShadeEntryEntity> generateSampleShades() {
        List<ShadeEntryEntity> shades = new ArrayList<>();
        Date now = new Date();
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_1",
                "El outfit demasiado perfecto",
                "Otra vez un look cosido por ella misma y perfecto... empieza a ser sospechoso. ¿Seguro que no tiene un taller clandestino?",
                "Outfit",
                4f,
                date(2026, 3, 5),
                Arrays.asList("costura", "runway", "perfección"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_1",
                "Nivel de confianza",
                "Dice que todo lo hace ella sola. Cariño, con ese ego cabrían tres coronas más.",
                "Actitud",
                3f,
                date(2026, 2, 18),
                Arrays.asList("ego", "ganadora", "confianza"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_1",
                "La mirada en el backstage",
                "Cuando alguien menciona 'máquina de coser', Carmen aparece de la nada como si la hubieran invocado.",
                "Evento",
                2f,
                date(2026, 1, 30),
                Arrays.asList("costura", "backstage"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_2",
                "El chiste eterno",
                "Pupi empezó el chiste en 2007 y todavía no sabemos dónde está el remate.",
                "Comentario Shady",
                3f,
                date(2026, 3, 4),
                Arrays.asList("comedia", "absurdo"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_2",
                "Improvisación peligrosa",
                "Su humor es tan improvisado que ni ella sabe qué va a decir después.",
                "Actitud",
                4f,
                date(2026, 2, 10),
                Arrays.asList("impro", "comedia"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_3",
                "El outfit improvisado",
                "Ese vestido parece hecho con tres pelucas, cinta americana y mucha fe.",
                "Outfit",
                3f,
                date(2026, 3, 3),
                Arrays.asList("look", "improvisado"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_3",
                "Energía caótica",
                "Samantha no entra al escenario, lo invade como si fuera una fiesta que se le fue de las manos.",
                "Actitud",
                2f,
                date(2026, 2, 5),
                Arrays.asList("caos", "escenario"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_4",
                "Sed de protagonismo",
                "Se fuerza demasiado en destacar en el concurso, ubícate reina.",
                "Comentario Shady",
                3f,
                date(2026, 3, 2),
                Arrays.asList("protagonista", "caótica"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_5",
                "Momento musical",
                "Si le das un micrófono, un foco y un ventilador, te monta un musical entero en cinco minutos.",
                "Evento",
                4f,
                date(2026, 3, 1),
                Arrays.asList("musical", "teatro"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_6",
                "El look intenso",
                "Ese maquillaje es tan intenso que podría protagonizar su propia película de terror.",
                "Outfit",
                4f,
                date(2026, 2, 28),
                Arrays.asList("maquillaje", "oscuro"),
                now, now
        ));
        shades.add(new ShadeEntryEntity(
                UUID.randomUUID().toString(),
                "queen_6",
                "Actitud misteriosa",
                "Siempre parece que está a punto de revelar un secreto oscuro… pero nunca lo hace.",
                "Comentario Shady",
                2f,
                date(2026, 2, 14),
                Arrays.asList("misterio", "estética"),
                now, now
        ));
        return shades;
    }
}
