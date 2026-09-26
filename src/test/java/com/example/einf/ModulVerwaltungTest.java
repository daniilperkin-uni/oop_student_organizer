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
    void updateModulErsetztDasModulVollstaendig() {
        Modul modul = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        verwaltung.addModul(modul);

        Modul aktualisiert = new Modul(modul.getId(), "Algorithmen 2", 8, true, Semester.SS25);
        aktualisiert.setLeistung(new Pruefungsleistung(aktualisiert, 1.7));
        verwaltung.updateModul(aktualisiert);

        // Regression: die frueher feldweise Kopie hielt die alte Instanz am
        // Leben und liess jedes neu hinzugefuegte Modul-Feld stillschweigend
        // fallen. Jetzt wird das uebergebene Objekt eingesetzt.
        Modul ergebnis = verwaltung.getModul(modul.getId());
        assertSame(aktualisiert, ergebnis);
        assertSame(aktualisiert, ergebnis.getLeistung().getModul());
        assertEquals(1.7, ergebnis.getLeistung().getErreichteNote(), 1e-9);
    }

    @Test
    void deleteModulEntferntDasModul() {
        Modul modul = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        verwaltung.addModul(modul);

        verwaltung.deleteModul(modul.getId());

        assertThrows(IllegalArgumentException.class, () -> verwaltung.getModul(modul.getId()));
    }
}
