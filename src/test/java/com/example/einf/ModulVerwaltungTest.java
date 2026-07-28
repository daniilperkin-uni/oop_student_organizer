package com.example.einf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModulVerwaltungTest {

    private ModulVerwaltung verwaltung;

    @BeforeEach
    void setUp() {
        verwaltung = new ModulVerwaltung();
    }

    @Test
    void addModulUndGetModulFindenDasModul() {
        Modul modul = new Modul("Algorithmen", 6, true, Semester.WS24_25);

        verwaltung.addModul(modul);

        assertEquals(modul, verwaltung.getModul(modul.getId()));
    }

    @Test
    void addModulMitNullWirftException() {
        assertThrows(IllegalArgumentException.class, () -> verwaltung.addModul(null));
    }

    @Test
    void getModulMitUnbekannterIdWirftException() {
        assertThrows(IllegalArgumentException.class, () -> verwaltung.getModul("unbekannt"));
    }

    @Test
    void updateModulAendertBestehendesModul() {
        Modul modul = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        verwaltung.addModul(modul);

        Modul aktualisiert = new Modul(modul.getId(), "Algorithmen 2", 8, false, Semester.SS25);
        verwaltung.updateModul(aktualisiert);

        Modul ergebnis = verwaltung.getModul(modul.getId());
        assertEquals("Algorithmen 2", ergebnis.getName());
        assertEquals(8, ergebnis.getEcts());
        assertFalse(ergebnis.istBenotet());
        assertEquals(Semester.SS25, ergebnis.getSemester());
    }

    @Test
    void deleteModulEntferntDasModul() {
        Modul modul = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        verwaltung.addModul(modul);

        verwaltung.deleteModul(modul.getId());

        assertThrows(IllegalArgumentException.class, () -> verwaltung.getModul(modul.getId()));
    }
}
