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
    void validiereNote() {
        assertTrue(EingabeValidierung.validiereNote("1,3").isEmpty());
        assertTrue(EingabeValidierung.validiereNote("5,7").isPresent());
    }
}
