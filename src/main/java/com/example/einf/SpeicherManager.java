package com.example.einf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
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


    /**
     * Returns an unmodifiable view of the loaded modules. The SpeicherManager
     * is the persistence backing store; the live, mutable state is owned by
     * the ModulVerwaltung. Callers must not mutate the returned list directly
     * -- use setModule(...) to replace the whole set before saving.
     */
    public List<Modul> getModule() { return Collections.unmodifiableList(module); }

    /**
     * Returns an unmodifiable view of the loaded deadlines. See getModule().
     */
    public List<Deadline> getDeadlines() { return Collections.unmodifiableList(deadlines); }

    /**
     * Replaces the entire module set (defensive copy). The controller calls
     * this to sync the ModulVerwaltung state back into the SpeicherManager
     * right before persisting, since the managers now own private copies.
     */
    public void setModule(List<Modul> module) {
        this.module.clear();
        if (module != null) this.module.addAll(module);
    }

    /** Replaces the entire deadline set (defensive copy). See setModule(). */
    public void setDeadlines(List<Deadline> deadlines) {
        this.deadlines.clear();
        if (deadlines != null) this.deadlines.addAll(deadlines);
    }

    public void addModul(Modul modul) { module.add(modul); }
    public void addDeadline(Deadline deadline) { deadlines.add(deadline); }

    public boolean removeModul(Modul modul) { return module.remove(modul); }
    public boolean removeDeadline(Deadline deadline) { return deadlines.remove(deadline); }

    /**
     * Schreibt den aktuellen Systemzustand (Module und Deadlines) konsistent in CSV-Dateien.
     *
     * <p>Jede Datei wird zuerst vollständig in eine Nachbardatei
     * {@code <name>.tmp} geschrieben und erst danach atomar über das Ziel
     * gezogen. Ein abgebrochener Schreibvorgang kann die bisherige Datei
     * dadurch nicht mehr zerstören, und die {@code .bak}-Kopie wird nur von
     * einer vollständig geschriebenen Datei rotiert.</p>
     *
     * @throws IOException wenn ein Schreibfehler auftritt
     */
    public void speichereDaten() throws IOException {
        sicherstelleVerzeichnis();
        schreibeAtomar(modulPfad, this::schreibeModule);
        schreibeAtomar(deadlinePfad, this::schreibeDeadlines);
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

    /**
     * Kopiert eine vorhandene Datei nach {@code <name>.bak}, bevor sie
     * überschrieben wird, damit ein fehlgeschlagener Schreibvorgang den
     * letzten gültigen Stand nicht zerstört.
     *
     * <p>Auch die Kopie entsteht über eine temporäre Datei und wird erst
     * danach an ihren Platz gezogen: ein Abbruch darf das bisherige Backup
     * nicht durch eine halbe Kopie ersetzen.</p>
     */
    static void sichereBackup(Path datei) throws IOException {
        if (!Files.exists(datei)) return;
        Path bak = datei.resolveSibling(datei.getFileName() + ".bak");
        Path tmp = datei.resolveSibling(datei.getFileName() + ".bak.tmp");
        // Bewusst kein Files.copy: unter Windows erbt die Kopie das
        // Schreibschutz-Attribut der Quelle, und eine schreibgeschützte
        // .bak.tmp ließe sich nicht mehr an ihren Platz ziehen.
        try (InputStream quelle = Files.newInputStream(datei);
             OutputStream kopie = Files.newOutputStream(tmp)) {
            quelle.transferTo(kopie);
        }
        try {
            ersetze(tmp, bak);
        } catch (IOException e) {
            raeumeAuf(tmp);
            throw e;
        }
    }

    /**
     * Schreibt den Inhalt über eine temporäre Nachbardatei und ersetzt das Ziel
     * erst, wenn der Inhalt vollständig auf der Platte liegt.
     *
     * <p>Die Reihenfolge ist bewusst: erst die komplette Datei schreiben, dann
     * die bisherige Zieldatei nach {@code .bak} sichern, dann atomar ersetzen.
     * Scheitert das Schreiben, bleiben Ziel und Backup unverändert - anders als
     * bei einem {@code newBufferedWriter(ziel)}, das die Zieldatei sofort
     * abschneidet und den letzten gültigen Stand vernichtet.</p>
     */
    private void schreibeAtomar(Path ziel, InhaltSchreiber inhalt) throws IOException {
        Path tmp = ziel.resolveSibling(ziel.getFileName() + ".tmp");
        try {
            inhalt.schreibe(tmp);
            sichereBackup(ziel);
            ersetze(tmp, ziel);
        } catch (IOException | RuntimeException e) {
            raeumeAuf(tmp);
            throw e;
        }
    }

    /**
     * Ersetzt {@code ziel} durch {@code quelle} - atomar, soweit das
     * Dateisystem das unterstützt.
     */
    private static void ersetze(Path quelle, Path ziel) throws IOException {
        try {
            Files.move(quelle, ziel, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Manche Dateisysteme (z. B. einzelne Netzlaufwerke) können nicht
            // atomar ersetzen; dann bleibt nur der normale Move.
            Files.move(quelle, ziel, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Löscht eine temporäre Datei, ohne den ursprünglichen Fehler zu verdecken. */
    private static void raeumeAuf(Path tmp) {
        try {
            Files.deleteIfExists(tmp);
        } catch (IOException ignored) {
            // Aufräumen ist Best Effort; maßgeblich ist der aufrufende Fehler.
        }
    }

    /** Schreibt den vollständigen Dateiinhalt nach {@code ziel}. */
    @FunctionalInterface
    private interface InhaltSchreiber {
        void schreibe(Path ziel) throws IOException;
    }

    private void sicherstelleVerzeichnis() throws IOException {
        Path verzeichnis = modulPfad.getParent();
        if (verzeichnis != null && !Files.exists(verzeichnis)) {
            Files.createDirectories(verzeichnis);
        }
    }

    private void schreibeModule(Path ziel) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(ziel, StandardCharsets.UTF_8)) {
            writer.write("id;name;ects;istBenotet;semester;leistungTyp;bestanden;note");
            writer.newLine();
            for (Modul m : module) {
                String lTyp = "KEINE";
                String bestanden = "false";
                String note = "0.0";
                
                if (m.getLeistung() != null) {
                    if (m.getLeistung() instanceof Pruefungsleistung) {
                        lTyp = "PRUEFUNG";
                        note = String.valueOf(m.getLeistung().getErreichteNote());
                    } else if (m.getLeistung() instanceof Studienleistung) {
                        lTyp = "STUDIEN";
                        bestanden = String.valueOf(m.getLeistung().isBestanden());
                    }
                }

                writer.write(escapeCsv(m.getId())
                        + TRENNZEICHEN + escapeCsv(m.getName())
                        + TRENNZEICHEN + m.getEcts()
                        + TRENNZEICHEN + m.istBenotet()
                        + TRENNZEICHEN + (m.getSemester() != null ? m.getSemester().name() : "NULL")
                        + TRENNZEICHEN + lTyp
                        + TRENNZEICHEN + bestanden
                        + TRENNZEICHEN + note);
                writer.newLine();
            }
        }
    }

    private void schreibeDeadlines(Path ziel) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(ziel, StandardCharsets.UTF_8)) {
            writer.write("beschreibung;datum;typ;modulName;erledigt");
            writer.newLine();
            for (Deadline d : deadlines) {
                // Der Konstruktor Deadline(titel, datum, modul) lässt den Typ auf null;
                // dieser Fall muss beim Schreiben abgefangen werden.
                writer.write(escapeCsv(d.getBeschreibung())
                        + TRENNZEICHEN + d.getDatum().toString()
                        + TRENNZEICHEN + (d.getTyp() != null ? d.getTyp().name() : "NULL")
                        + TRENNZEICHEN + escapeCsv(d.getModulName())
                        + TRENNZEICHEN + d.istErledigt());
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
                try {
                    Modul m = leseModulZeile(splitCsv(zeile));
                    if (m != null) module.add(m);
                } catch (IllegalArgumentException e) {
                    // Fängt auch ValidationException und NumberFormatException.
                    // Defekte Zeile überspringen, statt den gesamten Ladevorgang abzubrechen.
                    System.err.println("Überspringe fehlerhafte Modulzeile: " + zeile);
                }
            }
        } catch (NoSuchFileException e) {
            // Datei existiert noch nicht – kein Fehler beim ersten Start
        }
    }

    /**
     * Baut ein Modul aus den bereits aufgetrennten CSV-Feldern.
     *
     * @return das gelesene Modul, oder {@code null} wenn die Feldanzahl zu keinem
     *         bekannten Format passt
     */
    private Modul leseModulZeile(List<String> teile) {
        // Kompatibilität: falls alte Datei
        if (teile.size() == 4) {
            String name = unescapeCsv(teile.get(0));
            int ects = Integer.parseInt(teile.get(1).trim());
            ModulStatus status = ModulStatus.valueOf(teile.get(2).trim());
            double note = Double.parseDouble(teile.get(3).trim());
            Modul m = new Modul(name, ects, note > 0.0, null);
            if (note > 0.0) {
                m.setLeistung(new Pruefungsleistung(m, note));
            } else if (status == ModulStatus.BESTANDEN) {
                m.setLeistung(new Studienleistung(m, true));
            }
            return m;
        }
        if (teile.size() >= 8) {
            String id = unescapeCsv(teile.get(0));
            String name = unescapeCsv(teile.get(1));
            int ects = Integer.parseInt(teile.get(2).trim());
            boolean istBenotet = Boolean.parseBoolean(teile.get(3).trim());
            String semesterStr = teile.get(4).trim();
            Semester semester = semesterStr.equals("NULL") ? null : Semester.valueOf(semesterStr);

            Modul m = new Modul(id, name, ects, istBenotet, semester);

            String lTyp = teile.get(5).trim();
            if (lTyp.equals("PRUEFUNG")) {
                m.setLeistung(new Pruefungsleistung(m, Double.parseDouble(teile.get(7).trim())));
            } else if (lTyp.equals("STUDIEN")) {
                m.setLeistung(new Studienleistung(m, Boolean.parseBoolean(teile.get(6).trim())));
            }
            return m;
        }
        // Nicht stumm verwerfen: der Aufrufer loggt die Zeile, damit ein
        // abgeschnittener Datensatz nicht beim naechsten Speichern verschwindet.
        throw new IllegalArgumentException("Unerwartete Spaltenzahl: " + teile.size());
    }

    private void ladeDeadlines() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(deadlinePfad, StandardCharsets.UTF_8)) {
            reader.readLine(); // Kopfzeile überspringen
            String zeile;
            while ((zeile = reader.readLine()) != null) {
                if (zeile.isBlank()) continue;
                try {
                    List<String> teile = splitCsv(zeile);
                    if (teile.size() < 4) continue;
                    String beschreibung = unescapeCsv(teile.get(0));
                    LocalDate datum = LocalDate.parse(teile.get(1).trim());
                    String typStr = teile.get(2).trim();
                    DeadlineTyp typ = typStr.equals("NULL") || typStr.isEmpty()
                            ? null
                            : DeadlineTyp.valueOf(typStr);
                    String modulName = unescapeCsv(teile.get(3));
                    boolean erledigt = teile.size() >= 5 && Boolean.parseBoolean(teile.get(4).trim());
                    Deadline deadline = new Deadline(beschreibung, datum, typ, modulName);
                    deadline.setErledigt(erledigt);
                    deadlines.add(deadline);
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    // Fängt auch ValidationException und NumberFormatException.
                    // Defekte Zeile überspringen, statt den gesamten Ladevorgang abzubrechen.
                    System.err.println("Überspringe fehlerhafte Fristzeile: " + zeile);
                }
            }
        } catch (NoSuchFileException e) {
            // Datei existiert noch nicht – kein Fehler beim ersten Start
        }
    }

    /**
     * Maskiert Semikolons und Zeilenumbrüche in einem CSV-Feld.
     *
     * <p>{@code \r\n} wird zuerst zu {@code \\n} zusammengezogen, danach ein
     * einzelnes {@code \n} und schliesslich ein einzelnes {@code \r}: ein
     * alleinstehendes CR wuerde beim naechsten {@code readLine()} (das auch an
     * {@code \r} trennt) die Zeile vorzeitig beenden und alle Folgefelder der
     * Zeile verschieben.</p>
     */
    private String escapeCsv(String wert) {
        if (wert == null) return "";
        return wert.replace("\\", "\\\\")
                .replace(TRENNZEICHEN, "\\;")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }

    /**
     * Trennt eine CSV-Zeile an den Semikolons auf und respektiert dabei die von
     * {@link #escapeCsv(String)} gesetzten Maskierungen.
     *
     * <p>Ein einfaches {@code String.split(";")} würde auch an einem maskierten
     * {@code \;} trennen und dadurch alle nachfolgenden Felder verschieben. Die
     * Escape-Sequenzen selbst bleiben hier erhalten und werden anschließend von
     * {@link #unescapeCsv(String)} aufgelöst.
     */
    private List<String> splitCsv(String zeile) {
        List<String> felder = new ArrayList<>();
        StringBuilder feld = new StringBuilder();
        boolean maskiert = false;

        for (int i = 0; i < zeile.length(); i++) {
            char zeichen = zeile.charAt(i);
            if (maskiert) {
                feld.append(zeichen);
                maskiert = false;
            } else if (zeichen == '\\') {
                feld.append(zeichen);
                maskiert = true;
            } else if (zeichen == ';') {
                felder.add(feld.toString());
                feld.setLength(0);
            } else {
                feld.append(zeichen);
            }
        }
        felder.add(feld.toString());
        return felder;
    }

    /** Stellt ein maskiertes CSV-Feld wieder her. */
    private String unescapeCsv(String wert) {
        if (wert == null) return "";
        StringBuilder ergebnis = new StringBuilder();

        for (int i = 0; i < wert.length(); i++) {
            char zeichen = wert.charAt(i);
            if (zeichen == '\\' && i + 1 < wert.length()) {
                char naechstes = wert.charAt(++i);
                switch (naechstes) {
                    case 'n' -> ergebnis.append('\n');
                    case ';' -> ergebnis.append(';');
                    case '\\' -> ergebnis.append('\\');
                    default -> ergebnis.append(naechstes);
                }
            } else {
                ergebnis.append(zeichen);
            }
        }
        return ergebnis.toString();
    }
}
