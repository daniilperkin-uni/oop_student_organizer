package com.example.einf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EingabeValidierungTest {

    @Test
    void validiereModulName() {
        assertTrue(EingabeValidierung.validiereModulName("Algorithmen").isEmpty());
        assertTrue(EingabeValidierung.validiereModulName("  ").isPresent());
    }

    @Test
    void validiereEcts() {
        assertTrue(EingabeValidierung.validiereEcts("6").isEmpty());
        assertTrue(EingabeValidierung.validiereEcts("abc").isPresent());
        assertTrue(EingabeValidierung.validiereEcts("0").isPresent());
        assertTrue(EingabeValidierung.validiereEcts("-5").isPresent());
    }

    @Test
    void validiereEctsHatEineObergrenze() {
        // Regression: eine vertippte Ziffer (60 statt 6) machte ein Modul zum
        // Schwergewicht und verzerrte jeden ECTS-gewichteten Schnitt.
        assertTrue(EingabeValidierung.validiereEcts("30").isEmpty(), "30 ECTS sind zulaessig");
        assertTrue(EingabeValidierung.validiereEcts("31").isPresent());
        assertTrue(EingabeValidierung.validiereEcts("60").isPresent());
        assertTrue(EingabeValidierung.validiereEcts("999").isPresent());
        assertEquals("ECTS müssen zwischen 1 und 30 liegen.",
                EingabeValidierung.validiereEcts("60").orElseThrow());
    }

    @Test
    void validiereNote() {
        assertTrue(EingabeValidierung.validiereNote("1,3").isEmpty());
        assertTrue(EingabeValidierung.validiereNote("5,7").isPresent());
    }

    @Test
    void validiereNoteLehntNichtEndlicheWerteAb() {
        // Double.parseDouble("NaN") gelingt, und jeder Größenvergleich mit NaN
        // ist false - ohne isFinite rutschte NaN bis in die CSV-Datei.
        assertTrue(EingabeValidierung.validiereNote("NaN").isPresent());
        assertTrue(EingabeValidierung.validiereNote("nan").isPresent());
        assertTrue(EingabeValidierung.validiereNote("Infinity").isPresent());
        assertTrue(EingabeValidierung.validiereNote("-Infinity").isPresent());
    }
}
