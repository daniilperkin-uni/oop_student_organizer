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
        // One open module has to be present, otherwise there is no projection to
        // compare against. SL contributes nothing: (2.0*5 + 1.0*5) / 10 = 1.5.
        LeistungsRechner r = new LeistungsRechner(List.of(modul("A", 5, 2.0), s, modul("Offen", 5, null)));
        assertEquals(1.5, r.simuliereNotendurchschnitt(1.0).orElseThrow(), 1e-9);
    }

    @Test
    @DisplayName("Out-of-range wish grades are rejected")
    void invalidWishRejected() {
        LeistungsRechner r = new LeistungsRechner(List.of(modul("A", 5, null)));
        assertTrue(r.simuliereNotendurchschnitt(0.7).isEmpty());
        assertTrue(r.simuliereNotendurchschnitt(4.5).isEmpty());
    }

    @Test
    @DisplayName("Nothing open -> no projection at all")
    void nothingOpenReturnsEmpty() {
        LeistungsRechner r = new LeistungsRechner(List.of(modul("A", 5, 2.0),
                modul("B", 5, 3.0)));
        // Without an open module there is nothing to project: the value would just
        // be the current average, and the UI presented it as a simulated result.
        assertTrue(r.simuliereNotendurchschnitt(3.9).isEmpty());
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

    @Test
    @DisplayName("Required average may exceed 4.0 - it is an average, not a per-exam grade")
    void requiredAverageAbovePassingGradeIsReported() {
        // 10 ECTS fixed at 1.0, 10 ECTS open, target 2.75 -> (2.75 * 20 - 10) / 10 = 4.5.
        // 4.5 is a legitimate answer to "which average do I need", but it is not a
        // passing outcome, so the simulator has to say so.
        LeistungsRechner r = new LeistungsRechner(List.of(
                modul("Fix", 10, 1.0), modul("Offen", 10, null)));
        assertEquals(4.5, r.benoetigteNoteFuerZiel(2.75).orElseThrow(), 1e-9);
        assertEquals(LeistungsRechner.ZielAusgang.NUR_MIT_NICHT_BESTANDEN, r.zielAusgang(2.75));
    }

    @Test
    @DisplayName("Ziel-Ausgang unterscheidet erreichbar, sicher und ohne offene Module")
    void zielAusgangUnterscheidet() {
        // 10 ECTS at 1.0 fixed, 10 ECTS open: target 1.0 needs 1.0 -> reachable.
        LeistungsRechner r = new LeistungsRechner(List.of(
                modul("Fix", 10, 1.0), modul("Offen", 10, null)));
        assertEquals(LeistungsRechner.ZielAusgang.ERREICHBAR, r.zielAusgang(1.0));

        // Everything already graded: nothing left to simulate.
        LeistungsRechner fertig = new LeistungsRechner(List.of(modul("A", 5, 2.0)));
        assertEquals(LeistungsRechner.ZielAusgang.KEINE_OFFENEN_MODULE, fertig.zielAusgang(2.0));

        // Very good fixed grades: even 5.0 in the open module keeps the target.
        LeistungsRechner sicher = new LeistungsRechner(List.of(
                modul("Fix", 30, 1.0), modul("Offen", 5, null)));
        assertEquals(LeistungsRechner.ZielAusgang.BEREITS_SICHER, sicher.zielAusgang(3.0));
    }
}
