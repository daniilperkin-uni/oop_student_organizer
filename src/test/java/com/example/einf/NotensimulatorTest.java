package com.example.einf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer die Notensimulator-Erweiterungen des LeistungsRechner
 * (simuliereNotendurchschnitt / benoetigteNoteFuerZiel).
 */
class NotensimulatorTest {

    private Modul modul(String name, int ects, Double note) {
        Modul m = new Modul(name, ects, true, null);
        if (note != null) {
            m.setLeistung(new Pruefungsleistung(m, note));
        }
        return m;
    }

    @Test
    @DisplayName("Projection with all-open modules equals the wish grade")
    void projectionAllOpen() {
        LeistungsRechner r = new LeistungsRechner(List.of(
                modul("A", 5, null), modul("B", 5, null)));
        assertEquals(2.5, r.simuliereNotendurchschnitt(2.5).orElseThrow(), 1e-9);
    }

    @Test
    @DisplayName("Projection weights open and finished modules by ECTS")
    void projectionWeighted() {
        // 10 ECTS at 1.0 fixed + 10 ECTS open assumed 3.0 -> 2.0 overall.
        LeistungsRechner r = new LeistungsRechner(List.of(
                modul("Fix", 10, 1.0), modul("Offen", 10, null)));
        assertEquals(2.0, r.simuliereNotendurchschnitt(3.0).orElseThrow(), 1e-9);
    }

    @Test
    @DisplayName("Ungraded (Studienleistung) modules are ignored by the simulator")
    void unbenotetIgnored() {
        Modul s = new Modul("SL", 5, false, null);
        s.setLeistung(new Studienleistung(s, true));
        LeistungsRechner r = new LeistungsRechner(List.of(modul("A", 5, 2.0), s));
        assertEquals(2.0, r.simuliereNotendurchschnitt(1.0).orElseThrow(), 1e-9);
    }

    @Test
    @DisplayName("Out-of-range wish grades are rejected")
    void invalidWishRejected() {
        LeistungsRechner r = new LeistungsRechner(List.of(modul("A", 5, null)));
        assertTrue(r.simuliereNotendurchschnitt(0.7).isEmpty());
        assertTrue(r.simuliereNotendurchschnitt(4.5).isEmpty());
    }

    @Test
    @DisplayName("Nothing open -> no simulation possible")
    void nothingOpenNoProjection() {
        LeistungsRechner r = new LeistungsRechner(List.of(modul("A", 5, 2.0),
                modul("B", 5, 3.0)));
        // With nothing open the projection equals the actual average, so it
        // stays computable; only the wish grade itself comes back.
        assertEquals(2.5, r.simuliereNotendurchschnitt(3.9).orElseThrow(), 1e-9);
    }

    @Test
    @DisplayName("Required grade solves the weighted equation exactly")
    void requiredGradeExact() {
        // Fix: 6 ECTS at 2.5. Open: 4 ECTS. Target 2.0 ->
        // x = (2.0*10 - 15) / 4 = 1.25
        LeistungsRechner r = new LeistungsRechner(List.of(
                modul("Fix", 6, 2.5), modul("Offen", 4, null)));
        assertEquals(1.25, r.benoetigteNoteFuerZiel(2.0).orElseThrow(), 1e-9);
    }

    @Test
    @DisplayName("Impossible target yields empty instead of a nonsense grade")
    void impossibleTargetEmpty() {
        // Fix: 30 ECTS at 4.0. Open: 2 ECTS. Target 1.0 would need -47 -> empty.
        LeistungsRechner r = new LeistungsRechner(List.of(
                modul("Fix", 30, 4.0), modul("Offen", 2, null)));
        assertTrue(r.benoetigteNoteFuerZiel(1.0).isEmpty());
    }

    @Test
    @DisplayName("Trivially safe target below any achievable minimum also reports empty")
    void tooEasyTargetEmpty() {
        // Everything already graded -> nothing open -> empty.
        LeistungsRechner r = new LeistungsRechner(List.of(modul("A", 5, 1.0)));
        Optional<Double> out = r.benoetigteNoteFuerZiel(2.0);
        assertTrue(out.isEmpty());
        assertFalse(out.isPresent());
    }
}
