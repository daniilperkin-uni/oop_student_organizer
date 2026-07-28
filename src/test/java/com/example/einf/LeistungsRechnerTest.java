package com.example.einf;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeistungsRechnerTest {

    @Test
    void berechneNotendurchschnittGewichtetNachEcts() {
        Modul modul1 = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        modul1.setLeistung(new Pruefungsleistung(modul1, 2.0));

        Modul modul2 = new Modul("Mathe", 9, true, Semester.WS24_25);
        modul2.setLeistung(new Pruefungsleistung(modul2, 1.0));

        LeistungsRechner rechner = new LeistungsRechner(List.of(modul1, modul2));

        double erwarteterSchnitt = (2.0 * 6 + 1.0 * 9) / (6 + 9);
        assertEquals(erwarteterSchnitt, rechner.berechneNotendurchschnitt(), 0.0001);
    }

    @Test
    void berechneNotendurchschnittOhneBenoteteModuleIstNull() {
        LeistungsRechner rechner = new LeistungsRechner(List.of());

        assertEquals(0.0, rechner.berechneNotendurchschnitt());
    }

    @Test
    void berechneBestandeneEctsZaehltNurBestandeneModule() {
        Modul bestanden = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        bestanden.setLeistung(new Pruefungsleistung(bestanden, 2.0));

        Modul nichtBestanden = new Modul("Mathe", 9, true, Semester.WS24_25);
        nichtBestanden.setLeistung(new Pruefungsleistung(nichtBestanden, 5.0));

        LeistungsRechner rechner = new LeistungsRechner(List.of(bestanden, nichtBestanden));

        assertEquals(6, rechner.berechneBestandeneEcts());
    }

    @Test
    void istBestandenUnterscheidetBestandenUndNichtBestanden() {
        Modul bestanden = new Modul("Algorithmen", 6, true, Semester.WS24_25);
        bestanden.setLeistung(new Pruefungsleistung(bestanden, 4.0));

        Modul nichtBestanden = new Modul("Mathe", 9, true, Semester.WS24_25);
        nichtBestanden.setLeistung(new Pruefungsleistung(nichtBestanden, 4.3));

        LeistungsRechner rechner = new LeistungsRechner(List.of(bestanden, nichtBestanden));

        assertTrue(rechner.istBestanden(bestanden));
        assertFalse(rechner.istBestanden(nichtBestanden));
    }
}
