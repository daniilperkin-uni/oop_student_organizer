package com.example.einf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests der Persistenz.
 *
 * <p>Alle Tests arbeiten ausschließlich in einem von JUnit bereitgestellten
 * temporären Verzeichnis ({@link TempDir}). Das produktive Verzeichnis
 * {@code ~/.studenthelfer} wird dadurch nie berührt, und die Tests beeinflussen
 * sich gegenseitig nicht.
 */
@DisplayName("SpeicherManager")
class SpeicherManagerTest {

    @TempDir
    Path verzeichnis;

    private SpeicherManager speicher;

    @BeforeEach
    void setUp() {
        speicher = new SpeicherManager(verzeichnis);
    }

    /** Speichert den aktuellen Bestand und liest ihn in einen frischen Manager zurück. */
    private SpeicherManager neuLaden() throws IOException {
        speicher.speichereDaten();
        SpeicherManager geladen = new SpeicherManager(verzeichnis);
        geladen.ladeDaten();
        return geladen;
    }

    @Test
    @DisplayName("liest ein Modul mit Prüfungsleistung unverändert zurück")
    void moduleRoundtrip() throws IOException {
        Modul modul = new Modul("Lineare Algebra", 9, true, Semester.WS24_25);
        modul.setLeistung(new Pruefungsleistung(modul, 2.3));
        speicher.addModul(modul);

        List<Modul> geladen = neuLaden().getModule();

        assertEquals(1, geladen.size());
        Modul m = geladen.get(0);
        assertAll(
                () -> assertEquals(modul.getId(), m.getId()),
                () -> assertEquals("Lineare Algebra", m.getName()),
                () -> assertEquals(9, m.getEcts()),
                () -> assertTrue(m.istBenotet()),
                () -> assertEquals(Semester.WS24_25, m.getSemester()),
                () -> assertInstanceOf(Pruefungsleistung.class, m.getLeistung()),
                () -> assertEquals(2.3, m.getLeistung().getErreichteNote(), 1e-9),
                () -> assertSame(m, m.getLeistung().getModul()));
    }

    @Test
    @DisplayName("liest ein Modul mit Studienleistung und ohne Semester zurück")
    void studienleistungRoundtrip() throws IOException {
        Modul modul = new Modul("Praktikum", 6, false, null);
        modul.setLeistung(new Studienleistung(modul, true));
        speicher.addModul(modul);

        Modul geladen = neuLaden().getModule().get(0);

        assertAll(
                () -> assertNull(geladen.getSemester()),
                () -> assertInstanceOf(Studienleistung.class, geladen.getLeistung()),
                () -> assertTrue(geladen.getLeistung().isBestanden()));
    }

    @Test
    @DisplayName("behält ein Modul ohne erfasste Leistung bei")
    void ohneLeistungRoundtrip() throws IOException {
        speicher.addModul(new Modul("Noch offen", 4, true, Semester.SS26));

        assertNull(neuLaden().getModule().get(0).getLeistung());
    }

    @Test
    @DisplayName("verkraftet ein Semikolon im Modulnamen")
    void semikolonImNamen() throws IOException {
        // Das Semikolon ist das CSV-Trennzeichen. Ohne escape-bewusstes Einlesen
        // würde die Zeile hier auseinanderfallen und alle Folgefelder verschieben.
        Modul modul = new Modul("Mathe; Teil 2", 5, true, Semester.SS25);
        modul.setLeistung(new Pruefungsleistung(modul, 1.7));
        speicher.addModul(modul);

        Modul geladen = neuLaden().getModule().get(0);

        assertAll(
                () -> assertEquals("Mathe; Teil 2", geladen.getName()),
                () -> assertEquals(5, geladen.getEcts()),
                () -> assertEquals(Semester.SS25, geladen.getSemester()),
                () -> assertEquals(1.7, geladen.getLeistung().getErreichteNote(), 1e-9));
    }

    @Test
    @DisplayName("verkraftet Backslashes im Modulnamen")
    void backslashImNamen() throws IOException {
        speicher.addModul(new Modul("Pfad\\Test", 5, true, Semester.SS25));

        assertEquals("Pfad\\Test", neuLaden().getModule().get(0).getName());
    }

    @Test
    @DisplayName("liest eine Frist samt Erledigt-Kennzeichen zurück")
    void deadlineRoundtrip() throws IOException {
        Deadline deadline = new Deadline("Hausarbeit; Kapitel 1", LocalDate.of(2026, 9, 1),
                DeadlineTyp.ABGABE, "Mathe; Teil 2");
        deadline.setErledigt(true);
        speicher.addDeadline(deadline);

        List<Deadline> geladen = neuLaden().getDeadlines();

        assertEquals(1, geladen.size());
        Deadline d = geladen.get(0);
        assertAll(
                () -> assertEquals("Hausarbeit; Kapitel 1", d.getBeschreibung()),
                () -> assertEquals(LocalDate.of(2026, 9, 1), d.getDatum()),
                () -> assertEquals(DeadlineTyp.ABGABE, d.getTyp()),
                () -> assertEquals("Mathe; Teil 2", d.getModulName()),
                () -> assertTrue(d.istErledigt()));
    }

    @Test
    @DisplayName("speichert eine Frist ohne Typ, ohne abzustürzen")
    void deadlineOhneTyp() throws IOException {
        Modul modul = new Modul("Mathe", 5, true, Semester.SS25);
        // Dieser Konstruktor setzt keinen Typ – früher gab es hier eine NullPointerException.
        speicher.addDeadline(new Deadline("Anmeldung", LocalDate.of(2026, 10, 1), modul));

        Deadline geladen = assertDoesNotThrow(this::neuLaden).getDeadlines().get(0);

        assertAll(
                () -> assertNull(geladen.getTyp()),
                () -> assertEquals("Anmeldung", geladen.getBeschreibung()),
                () -> assertEquals("Mathe", geladen.getModulName()));
    }

    @Test
    @DisplayName("behält ein Schaltjahresdatum bei")
    void schaltjahrRoundtrip() throws IOException {
        speicher.addDeadline(new Deadline("Schalttag", LocalDate.of(2024, 2, 29),
                DeadlineTyp.KLAUSUR, ""));

        assertEquals(LocalDate.of(2024, 2, 29), neuLaden().getDeadlines().get(0).getDatum());
    }

    @Test
    @DisplayName("meldet keinen Fehler, wenn noch keine Dateien existieren")
    void ersterStartOhneDateien() {
        SpeicherManager frisch = new SpeicherManager(verzeichnis.resolve("gibt-es-noch-nicht"));

        assertDoesNotThrow(frisch::ladeDaten);
        assertAll(
                () -> assertTrue(frisch.getModule().isEmpty()),
                () -> assertTrue(frisch.getDeadlines().isEmpty()));
    }

    @Test
    @DisplayName("legt das Zielverzeichnis beim Speichern selbst an")
    void legtVerzeichnisAn() throws IOException {
        Path tief = verzeichnis.resolve("a").resolve("b");
        SpeicherManager frisch = new SpeicherManager(tief);
        frisch.addModul(new Modul("Mathe", 5, true, Semester.SS25));

        frisch.speichereDaten();

        assertTrue(Files.exists(tief.resolve("module.csv")));
    }

    @Test
    @DisplayName("leert den bisherigen Bestand vor dem Laden")
    void ladenLeertVorher() throws IOException {
        speicher.addModul(new Modul("Mathe", 5, true, Semester.SS25));
        speicher.speichereDaten();

        speicher.addModul(new Modul("Doppelt", 5, true, Semester.SS25));
        speicher.ladeDaten();

        assertEquals(1, speicher.getModule().size());
    }

    @Test
    @DisplayName("überspringt defekte Zeilen und liest die übrigen ein")
    void defekteZeilenWerdenUebersprungen() throws IOException {
        Files.writeString(verzeichnis.resolve("module.csv"), String.join("\n",
                "id;name;ects;istBenotet;semester;leistungTyp;bestanden;note",
                "a1;Mathe;5;true;WS24_25;PRUEFUNG;false;2.0",
                "a2;Kaputt;keine-zahl;true;WS24_25;KEINE;false;0.0",
                "a4;Physik;6;false;SS25;STUDIEN;true;0.0",
                ""), StandardCharsets.UTF_8);

        Files.writeString(verzeichnis.resolve("deadlines.csv"), String.join("\n",
                "beschreibung;datum;typ;modulName;erledigt",
                "Gute Frist;2026-09-01;ABGABE;Mathe;false",
                "Kaputtes Datum;kein-datum;ABGABE;Mathe;false",
                "Kaputter Typ;2026-09-02;GIBT_ES_NICHT;Mathe;false",
                ""), StandardCharsets.UTF_8);

        speicher.ladeDaten();

        assertAll(
                () -> assertEquals(List.of("Mathe", "Physik"),
                        speicher.getModule().stream().map(Modul::getName).toList()),
                () -> assertEquals(List.of("Gute Frist"),
                        speicher.getDeadlines().stream().map(Deadline::getBeschreibung).toList()));
    }

    @Test
    @DisplayName("schreibt beide Dateien mit Kopfzeile")
    void schreibtKopfzeilen() throws IOException {
        speicher.speichereDaten();

        assertAll(
                () -> assertTrue(Files.readString(verzeichnis.resolve("module.csv"), StandardCharsets.UTF_8)
                        .startsWith("id;name;ects")),
                () -> assertTrue(Files.readString(verzeichnis.resolve("deadlines.csv"), StandardCharsets.UTF_8)
                        .startsWith("beschreibung;datum;typ")));
    }

    @Test
    @DisplayName("entfernt Module und Fristen wieder aus dem Bestand")
    void entfernen() {
        Modul modul = new Modul("Mathe", 5, true, Semester.SS25);
        Deadline deadline = new Deadline("Abgabe", LocalDate.now(), DeadlineTyp.ABGABE, "Mathe");
        speicher.addModul(modul);
        speicher.addDeadline(deadline);

        assertAll(
                () -> assertTrue(speicher.removeModul(modul)),
                () -> assertTrue(speicher.removeDeadline(deadline)),
                () -> assertTrue(speicher.getModule().isEmpty()),
                () -> assertTrue(speicher.getDeadlines().isEmpty()));
    }
}
