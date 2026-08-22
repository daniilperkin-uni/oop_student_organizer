package com.example.einf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests fuer die Semesterstatistik-Aggregation (SemesterStatistik.Datensatz).
 */
class SemesterStatistikTest {

    private Modul modul(String name, int ects, Semester sem, Double note) {
        Modul m = new Modul(name, ects, note != null, sem);
        if (note != null) {
            m.setLeistung(new Pruefungsleistung(m, note));
        }
        return m;
    }

    @Test
    @DisplayName("Modules without semester land in 'Kein Semester' bucket")
    void ohneSemesterGruppiert() {
        SemesterStatistik s = new SemesterStatistik(List.of(
                modul("A", 5, null, 2.0),
                modul("B", 5, Semester.SS25, 1.0)));

        assertEquals(2, s.datensaetze().size());
        SemesterStatistik.Datensatz ss25 = s.finde("Sommersemester 2025");
        assertEquals(5, ss25.gesamtEcts());
        SemesterStatistik.Datensatz kein = s.finde("Kein Semester");
        // Note 2.0 ist im deutschen System eine Bestehensnote (<= 4.0).
        assertEquals(5, kein.gesamtEcts());
        assertEquals(5, kein.bestandeneEcts());
    }

    @Test
    @DisplayName("Passed ECTS, GPA and progress are computed per semester")
    void kennzahlenProSemester() {
        // SS25: A (5 ECTS, Note 1.0), B (5 ECTS, offen) -> 5/10 bestanden, GPA 1.0
        // WS24_25: C (10 ECTS, Note 3.0) -> 10/10, GPA 3.0
        SemesterStatistik s = new SemesterStatistik(List.of(
                modul("A", 5, Semester.SS25, 1.0),
                modul("B", 5, Semester.SS25, null),
                modul("C", 10, Semester.WS24_25, 3.0)));

        SemesterStatistik.Datensatz ss25 = s.finde("Sommersemester 2025");
        assertEquals(5, ss25.bestandeneEcts());
        assertEquals(10, ss25.gesamtEcts());
        assertEquals(1.0, ss25.durchschnitt(), 1e-9);
        assertEquals(50.0, ss25.fortschrittProzent(), 1e-9);

        SemesterStatistik.Datensatz ws24 = s.finde("Wintersemester 2024/25");
        assertEquals(10, ws24.bestandeneEcts());
        assertEquals(100.0, ws24.fortschrittProzent(), 1e-9);
        assertEquals(3.0, ws24.durchschnitt(), 1e-9);
    }

    @Test
    @DisplayName("Semesters appear in enum declaration order")
    void reihenfolge() {
        SemesterStatistik s = new SemesterStatistik(List.of(
                modul("C", 5, Semester.WS23_24, 1.0),
                modul("A", 5, Semester.SS26, 2.0)));
        var liste = s.datensaetze();
        assertEquals("Wintersemester 2023/24", liste.get(0).bezeichnung());
        assertEquals("Sommersemester 2026", liste.get(liste.size() - 1).bezeichnung());
    }

    @Test
    @DisplayName("Empty module list produces empty statistics")
    void leerOk() {
        SemesterStatistik s = new SemesterStatistik(List.of());
        assertEquals(0, s.datensaetze().size());
    }
}
