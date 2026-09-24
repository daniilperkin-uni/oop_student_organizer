package com.example.einf;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EctsFortschrittTest {

    @Test
    void teiltEctsNachErgebnisAuf() {
        Modul ok = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        ok.setLeistung(new Pruefungsleistung(ok, 2.0));
        Modul fail = new Modul("Mathe", 9, true, Semester.WS24_25);
        fail.setLeistung(new Pruefungsleistung(fail, 5.0));
        Modul offen = new Modul("OOP", 5, true, Semester.WS24_25);

        EctsFortschritt f = EctsFortschritt.berechne(List.of(ok, fail, offen));

        assertEquals(new EctsFortschritt(6, 9, 5), f);
        assertEquals(20, f.gesamt());
        assertEquals(30.0, f.prozentBestanden(), 1e-9);
    }

    @Test
    void leereListeErgibtNull() {
        EctsFortschritt f = EctsFortschritt.berechne(List.of());
        assertEquals(0, f.gesamt());
        assertEquals(0.0, f.prozentBestanden());
    }
}
