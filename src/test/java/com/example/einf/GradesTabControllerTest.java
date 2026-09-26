package com.example.einf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
