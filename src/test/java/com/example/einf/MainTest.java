package com.example.einf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer den Beenden-Pfad von {@link Main}.
 *
 * <p>
 * Ohne JavaFX-Toolkit: {@code start(Stage)} wird nicht aufgerufen, geprueft
 * wird nur, ob beim Beenden gespeichert werden darf. Der Speicherort liegt
 * immer in {@link TempDir}; {@code ~/.studenthelfer} bleibt unberuehrt.
 * </p>
 */
@DisplayName("Main")
class MainTest {

    @Test
    @DisplayName("stop() ueberschreibt nach fehlgeschlagenem Laden keine Dateien")
    void stopNachFehlgeschlagenemLadenSchreibtNicht(@TempDir Path verzeichnis) throws IOException {
        // Gueltiger Bestand auf der Platte ...
        SpeicherManager vorher = new SpeicherManager(verzeichnis);
        Modul modul = new Modul("Lineare Algebra", 9, true, Semester.WS24_25);
        modul.setLeistung(new Pruefungsleistung(modul, 2.3));
        vorher.addModul(modul);
        vorher.addDeadline(new Deadline("Abgabe", LocalDate.of(2026, 9, 1),
                DeadlineTyp.ABGABE, "Lineare Algebra"));
        vorher.speichereDaten();

        Path modulDatei = verzeichnis.resolve("module.csv");
        Path deadlineDatei = verzeichnis.resolve("deadlines.csv");
        String moduleVorher = Files.readString(modulDatei, StandardCharsets.UTF_8);
        String deadlinesVorher = Files.readString(deadlineDatei, StandardCharsets.UTF_8);

        // ... aber das Laden scheitert, bevor ein Datensatz im Speicher liegt.
        SpeicherManager kaputt = new SpeicherManager(verzeichnis) {
            @Override
            public void ladeDaten() throws IOException {
                throw new IOException("Testfehler beim Laden");
            }
        };
        Main main = new Main(kaputt);

        assertFalse(main.ladeDatenBeimStart(), "der Ladefehler muss vermerkt werden");
        assertTrue(kaputt.getModule().isEmpty(), "nach dem Fehler ist der Zustand leer");

        // Ohne die Sperre wuerde genau dieser leere Zustand jetzt geschrieben -
        // also die echten Dateien mit einer leeren Tabelle ueberschreiben.
        main.setBeendenAktion(() -> {
            try {
                kaputt.speichereDaten();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        main.stop();

        assertEquals(moduleVorher, Files.readString(modulDatei, StandardCharsets.UTF_8));
        assertEquals(deadlinesVorher, Files.readString(deadlineDatei, StandardCharsets.UTF_8));
        assertFalse(Files.exists(verzeichnis.resolve("module.csv.bak")),
                "nach fehlgeschlagenem Laden darf nicht einmal ein Backup rotieren");
        assertFalse(Files.exists(verzeichnis.resolve("module.csv.tmp")));
    }

    @Test
    @DisplayName("stop() speichert nach erfolgreichem Laden")
    void stopNachErfolgreichemLadenSpeichert(@TempDir Path verzeichnis) {
        Main main = new Main(new SpeicherManager(verzeichnis));

        assertTrue(main.ladeDatenBeimStart());
        AtomicInteger aufrufe = new AtomicInteger();
        main.setBeendenAktion(aufrufe::incrementAndGet);

        main.stop();

        assertEquals(1, aufrufe.get());
    }
}
