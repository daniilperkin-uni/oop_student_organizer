package com.example.einf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests fuer die Validierung des Notensimulators.
 *
 * <p>
 * Vor der Korrektur lief {@code EingabeValidierung.parseNote} ungeschuetzt im
 * OK-Filter des Dialogs und warf bei "abc" oder leerem Feld eine
 * {@link NumberFormatException}. Der Test prueft den ausgelagerten,
 * JavaFX-freien Validator, der genau diese Eingaben jetzt abfaengt.
 * </p>
 */
class GradesTabControllerTest {

    @Test
    @DisplayName("Nicht-numerische Eingabe wird gemeldet statt zu fliegen")
    void nichtNumerischWirdGemeldet() {
        Optional<String> fehler = assertDoesNotThrow(
                () -> GradesTabController.validiereWunschNote("abc"));
        assertTrue(fehler.isPresent());
    }

    @Test
    @DisplayName("Leere Eingabe wird gemeldet statt zu fliegen")
    void leerWirdGemeldet() {
        assertTrue(GradesTabController.validiereWunschNote("").isPresent());
        assertTrue(GradesTabController.validiereWunschNote("   ").isPresent());
        assertTrue(GradesTabController.validiereWunschNote(null).isPresent());
    }

    @Test
    @DisplayName("Note ausserhalb 1,0-4,0 wird gemeldet")
    void ausserhalbDesBereichs() {
        assertTrue(GradesTabController.validiereWunschNote("0,7").isPresent());
        assertTrue(GradesTabController.validiereWunschNote("4,5").isPresent());
    }

    @Test
    @DisplayName("Gueltige Wunschnote wird akzeptiert")
    void gueltigWirdAkzeptiert() {
        assertTrue(GradesTabController.validiereWunschNote("2,0").isEmpty());
        assertTrue(GradesTabController.validiereWunschNote("3.5").isEmpty());
        assertTrue(GradesTabController.validiereWunschNote("1,0").isEmpty());
        assertTrue(GradesTabController.validiereWunschNote("4,0").isEmpty());
    }

    @Test
    @DisplayName("NaN als Wunschnote wird gemeldet")
    void nanWirdGemeldet() {
        // Gleiche Falle wie in EingabeValidierung.validiereNote: NaN besteht
        // jeden Groessenvergleich und landete sonst im Notensimulator.
        assertTrue(GradesTabController.validiereWunschNote("NaN").isPresent());
    }

    @Test
    @DisplayName("Ein leeres Ergebnis wird nicht pauschal als \"nicht erreichbar\" formuliert")
    void zielZeileUnterscheidetDieGruende() {
        // Frueher erschienen alle drei Faelle als "nicht mehr erreichbar".
        String sicher = GradesTabController.zielZeile(
                3.0, Optional.empty(), LeistungsRechner.ZielAusgang.BEREITS_SICHER);
        assertTrue(sicher.contains("bereits sicher"));

        String keine = GradesTabController.zielZeile(
                3.0, Optional.empty(), LeistungsRechner.ZielAusgang.KEINE_OFFENEN_MODULE);
        assertTrue(keine.contains("keine offenen benoteten Module"));

        String nicht = GradesTabController.zielZeile(
                1.0, Optional.empty(), LeistungsRechner.ZielAusgang.NICHT_ERREICHBAR);
        assertTrue(nicht.contains("nicht mehr erreichbar"));
    }

    @Test
    @DisplayName("Eine nicht bestandene Durchschnittsnote wird als solche benannt")
    void zielZeileBenenntNichtBestandeneNote() {
        String zeile = GradesTabController.zielZeile(
                2.75, Optional.of(4.5), LeistungsRechner.ZielAusgang.NUR_MIT_NICHT_BESTANDEN);
        assertTrue(zeile.contains("4,50") || zeile.contains("4.50"));
        assertTrue(zeile.contains("nicht bestandene"));
    }
}
