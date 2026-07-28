package com.example.einf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModulTest {

    @Test
    void konstruktorSetztAlleFelder() {
        Modul modul = new Modul("Algorithmen", 6, true, Semester.WS24_25);

        assertNotNull(modul.getId());
        assertEquals("Algorithmen", modul.getName());
        assertEquals(6, modul.getEcts());
        assertTrue(modul.istBenotet());
        assertEquals(Semester.WS24_25, modul.getSemester());
    }

    @Test
    void gettersUndSettersFunktionieren() {
        Modul modul = new Modul("Mathe", 5, false, Semester.SS24);

        modul.setName("Mathe 2");
        modul.setEcts(8);
        modul.setIstBenotet(true);
        modul.setSemester(Semester.WS25_26);

        assertEquals("Mathe 2", modul.getName());
        assertEquals(8, modul.getEcts());
        assertTrue(modul.istBenotet());
        assertEquals(Semester.WS25_26, modul.getSemester());
    }
}
