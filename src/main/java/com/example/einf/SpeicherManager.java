package com.example.einf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet das persistente Speichern und Laden aller Anwendungsdaten.
 */

public class SpeicherManager {

    private static final String MODULE_DATEI = "module.csv";
    private static final String DEADLINES_DATEI = "deadlines.csv";
    private static final String TRENNZEICHEN = ";";

    private final Path modulPfad;
    private final Path deadlinePfad;

    private final List<Modul> module = new ArrayList<>();
    private final List<Deadline> deadlines = new ArrayList<>();

    /** Erstellt einen SpeicherManager, der Dateien im aktuellen Arbeitsverzeichnis ablegt. */
    public SpeicherManager() {
        this(Paths.get(System.getProperty("user.home"), ".studenthelfer"));
    }

    /**
     * Erstellt einen SpeicherManager mit benutzerdefiniertem Speicherort.
     * Das Verzeichnis wird bei Bedarf automatisch angelegt.
     */
    public SpeicherManager(Path verzeichnis) {
        this.modulPfad = verzeichnis.resolve(MODULE_DATEI);
        this.deadlinePfad = verzeichnis.resolve(DEADLINES_DATEI);
    }


    public List<Modul> getModule() { return module; }
    public List<Deadline> getDeadlines() { return deadlines; }

    public void addModul(Modul modul) { module.add(modul); }
    public void addDeadline(Deadline deadline) { deadlines.add(deadline); }

    public boolean removeModul(Modul modul) { return module.remove(modul); }
    public boolean removeDeadline(Deadline deadline) { return deadlines.remove(deadline); }

    /**
     * Schreibt den aktuellen Systemzustand (Module und Deadlines) konsistent in CSV-Dateien.
     *
     * @throws IOException wenn ein Schreibfehler auftritt
     */
    public void speichereDaten() throws IOException {
        sicherstelleVerzeichnis();
        schreibeModule();
        schreibeDeadlines();
    }

    /**
     * Lädt Module und Deadlines aus den CSV-Dateien und rekonstruiert den Systemzustand.
     * Existiert eine Datei noch nicht, wird dies ohne Fehler übergangen.
     *
     * @throws IOException bei unerwarteten Lesefehlern (z. B. Zugriffsverweigerung)
     */
    public void ladeDaten() throws IOException {
        module.clear();
        deadlines.clear();
        ladeModule();
        ladeDeadlines();
    }

    private void sicherstelleVerzeichnis() throws IOException {
        Path verzeichnis = modulPfad.getParent();
        if (verzeichnis != null && !Files.exists(verzeichnis)) {
            Files.createDirectories(verzeichnis);
        }
    }

    private void schreibeModule() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(modulPfad, StandardCharsets.UTF_8)) {
            writer.write("name;ects;status;note");
            writer.newLine();
            for (Modul m : module) {
                writer.write(escapeCsv(m.getName())
                        + TRENNZEICHEN + m.getEcts()
                        + TRENNZEICHEN + m.getStatus().name()
                        + TRENNZEICHEN + m.getNote());
                writer.newLine();
            }
        }
    }

    private void schreibeDeadlines() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(deadlinePfad, StandardCharsets.UTF_8)) {
            writer.write("beschreibung;datum;typ;modulName");
            writer.newLine();
            for (Deadline d : deadlines) {
                writer.write(escapeCsv(d.getBeschreibung())
                        + TRENNZEICHEN + d.getDatum().toString()
                        + TRENNZEICHEN + d.getTyp().name()
                        + TRENNZEICHEN + escapeCsv(d.getModulName()));
                writer.newLine();
            }
        }
    }

    private void ladeModule() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(modulPfad, StandardCharsets.UTF_8)) {
            reader.readLine(); // Kopfzeile überspringen
            String zeile;
            while ((zeile = reader.readLine()) != null) {
                if (zeile.isBlank()) continue;
                String[] teile = zeile.split(TRENNZEICHEN, -1);
                if (teile.length < 4) continue;
                String name = unescapeCsv(teile[0]);
                int ects = Integer.parseInt(teile[1].trim());
                ModulStatus status = ModulStatus.valueOf(teile[2].trim());
                double note = Double.parseDouble(teile[3].trim());
                module.add(new Modul(name, ects, status, note));
            }
        } catch (NoSuchFileException e) {
            // Datei existiert noch nicht – kein Fehler beim ersten Start
        }
    }

    private void ladeDeadlines() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(deadlinePfad, StandardCharsets.UTF_8)) {
            reader.readLine(); // Kopfzeile überspringen
            String zeile;
            while ((zeile = reader.readLine()) != null) {
                if (zeile.isBlank()) continue;
                String[] teile = zeile.split(TRENNZEICHEN, -1);
                if (teile.length < 4) continue;
                String beschreibung = unescapeCsv(teile[0]);
                LocalDate datum = LocalDate.parse(teile[1].trim());
                DeadlineTyp typ = DeadlineTyp.valueOf(teile[2].trim());
                String modulName = unescapeCsv(teile[3]);
                deadlines.add(new Deadline(beschreibung, datum, typ, modulName));
            }
        } catch (NoSuchFileException e) {
            // Datei existiert noch nicht – kein Fehler beim ersten Start
        }
    }

    /** Maskiert Semikolons und Zeilenumbrüche in einem CSV-Feld. */
    private String escapeCsv(String wert) {
        if (wert == null) return "";
        return wert.replace("\\", "\\\\").replace(TRENNZEICHEN, "\\;").replace("\n", "\\n");
    }

    /** Stellt ein maskiertes CSV-Feld wieder her. */
    private String unescapeCsv(String wert) {
        if (wert == null) return "";
        return wert.replace("\\n", "\n").replace("\\;", TRENNZEICHEN).replace("\\\\", "\\");
    }
}
