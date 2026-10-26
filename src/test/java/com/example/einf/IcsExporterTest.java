package com.example.einf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer den iCal-Export der Fristen.
 */
class IcsExporterTest {

    @TempDir
    Path tempDir;

    private Deadline frist(String titel, LocalDate datum) {
        return new Deadline(titel, datum, DeadlineTyp.ABGABE, "Mathe");
    }

    @Test
    @DisplayName("Export writes a valid VCALENDAR with one VEVENT per deadline")
    void exportCreatesValidCalendar() throws Exception {
        Path ziel = tempDir.resolve("fristen.ics");
        IcsExporter.exportiere(List.of(
                frist("Abgabe Blatt 3", LocalDate.of(2026, 9, 1)),
                frist("Klausur", LocalDate.of(2026, 9, 15))), ziel);

        String content = Files.readString(ziel);
        assertTrue(content.startsWith("BEGIN:VCALENDAR\r\n"));
        assertTrue(content.endsWith("END:VCALENDAR\r\n"));
        assertEquals(2, content.split("BEGIN:VEVENT", -1).length - 1);
        assertEquals(2, content.split("END:VEVENT", -1).length - 1);
        assertTrue(content.contains("DTSTART;VALUE=DATE:20260901\r\n"));
        // DTEND is exclusive per RFC 5545 -> next day.
        assertTrue(content.contains("DTEND;VALUE=DATE:20260902\r\n"));
    }

    @Test
    @DisplayName("Special characters in titles are RFC 5545 escaped")
    void escapingApplied() {
        assertEquals("Abgabe\\, Blatt 3\\; Teil A", IcsExporter.escape("Abgabe, Blatt 3; Teil A"));
        assertEquals("Zeile\\nUmbruch", IcsExporter.escape("Zeile\nUmbruch"));
        assertEquals("Back\\\\slash", IcsExporter.escape("Back\\slash"));
    }

    @Test
    @DisplayName("A bare CR is escaped like a line break")
    void escapingBareCarriageReturn() {
        // Regression: ein alleinstehendes \r ueberlebte das Escaping und galt
        // im ICS-Textwert dann als Zeilentrenner.
        assertEquals("Zeile\\nUmbruch", IcsExporter.escape("Zeile\rUmbruch"));
        assertEquals("Zeile\\nUmbruch", IcsExporter.escape("Zeile\r\nUmbruch"));
    }

    @Test
    @DisplayName("The deadline text appears once, in SUMMARY; no duplicate DESCRIPTION")
    void noDuplicateDescription() throws Exception {
        // Regression: getTitel() liefert die Beschreibung, daher trug
        // DESCRIPTION exakt denselben Text wie SUMMARY.
        Path ziel = tempDir.resolve("ohne-duplikat.ics");
        IcsExporter.exportiere(List.of(frist("Abgabe Blatt 3", LocalDate.of(2026, 9, 1))), ziel);
        String content = Files.readString(ziel);
        assertTrue(content.contains("SUMMARY:Abgabe Blatt 3\r\n"));
        assertFalse(content.contains("DESCRIPTION:"));
    }

    @Test
    @DisplayName("Module name lands in CATEGORIES")
    void moduleAsCategory() throws Exception {
        Path ziel = tempDir.resolve("eine.ics");
        IcsExporter.exportiere(List.of(frist("Anmeldung", LocalDate.of(2026, 8, 30))), ziel);
        String content = Files.readString(ziel);
        assertTrue(content.contains("CATEGORIES:Mathe\r\n"));
    }

    @Test
    @DisplayName("Empty deadline list still produces a parseable calendar shell")
    void emptyListOk() throws Exception {
        Path ziel = tempDir.resolve("leer.ics");
        IcsExporter.exportiere(List.of(), ziel);
        String content = Files.readString(ziel);
        assertTrue(content.contains("VERSION:2.0"));
        assertTrue(!content.contains("BEGIN:VEVENT"));
    }

    @Test
    @DisplayName("Two exports of the same deadline produce the same UID")
    void uidIsStableAcrossExports() throws Exception {
        // Regression: die UID kam aus d.hashCode(); Deadline definiert weder
        // equals noch hashCode, also war das ein Identitaets-Hash. Jeder Export
        // erzeugte damit eine neue UID, und ein Re-Import der aktualisierten
        // Datei legte den Termin doppelt an, statt ihn zu aktualisieren.
        Path erste = tempDir.resolve("erst.ics");
        Path zweite = tempDir.resolve("zweit.ics");

        IcsExporter.exportiere(List.of(frist("Abgabe Blatt 3", LocalDate.of(2026, 9, 1))), erste);
        // Bewusst eine neue Instanz mit gleichem Inhalt: ein Identitaets-Hash
        // wuerde hier zwangslaeufig abweichen.
        IcsExporter.exportiere(List.of(
                new Deadline("Abgabe Blatt 3", LocalDate.of(2026, 9, 1), DeadlineTyp.ABGABE, "Mathe")), zweite);

        String uid = ersteUid(erste);
        assertEquals(uid, ersteUid(zweite));
        assertTrue(uid.endsWith("@studentischer-organisationshelfer"));
        assertTrue(uid.startsWith("frist-20260901-"));
    }

    @Test
    @DisplayName("Different deadlines still get different UIDs")
    void uidUnterscheidetVerschiedeneFristen() throws Exception {
        Path ziel = tempDir.resolve("zwei.ics");
        IcsExporter.exportiere(List.of(
                frist("Abgabe Blatt 3", LocalDate.of(2026, 9, 1)),
                frist("Abgabe Blatt 3", LocalDate.of(2026, 9, 2))), ziel);

        List<String> uids = Files.readAllLines(ziel).stream()
                .filter(zeile -> zeile.startsWith("UID:"))
                .toList();
        assertEquals(2, uids.size());
        assertEquals(2, uids.stream().distinct().count());
    }

    private static String ersteUid(Path datei) throws java.io.IOException {
        return Files.readAllLines(datei).stream()
                .filter(zeile -> zeile.startsWith("UID:"))
                .findFirst()
                .map(zeile -> zeile.substring("UID:".length()))
                .orElseThrow();
    }
}
