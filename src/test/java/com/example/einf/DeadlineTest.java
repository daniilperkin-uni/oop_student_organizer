package com.example.einf;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DeadlineTest {

    @Test
    void zukuenftigeDeadlineIstOffen() {
        Deadline deadline = new Deadline("Abgabe", LocalDate.now().plusDays(7), DeadlineTyp.ABGABE, "Algorithmen");

        assertEquals(DeadlineStatus.OFFEN, deadline.getStatus());
        assertFalse(deadline.isAbgelaufen());
    }

    @Test
    void vergangeneDeadlineIstUeberfaellig() {
        Deadline deadline = new Deadline("Abgabe", LocalDate.now().minusDays(1), DeadlineTyp.ABGABE, "Algorithmen");

        assertEquals(DeadlineStatus.UEBERFAELLIG, deadline.getStatus());
        assertTrue(deadline.isAbgelaufen());
    }

    @Test
    void erledigteDeadlineIstImmerErledigt() {
        Deadline deadline = new Deadline("Abgabe", LocalDate.now().minusDays(1), DeadlineTyp.ABGABE, "Algorithmen");

        deadline.setErledigt(true);

        assertEquals(DeadlineStatus.ERLEDIGT, deadline.getStatus());
    }

    @Test
    void deadlineHeuteIstNochOffen() {
        Deadline deadline = new Deadline("Abgabe", LocalDate.now(), DeadlineTyp.ABGABE, "Algorithmen");

        assertEquals(DeadlineStatus.OFFEN, deadline.getStatus());
        assertFalse(deadline.isAbgelaufen());
    }

    @Test
    void deadlineAnSchaltjahrDatumWirdKorrektAusgewertet() {
        Deadline vergangeneSchaltjahresDeadline = new Deadline("Abgabe", LocalDate.of(2024, 2, 29), DeadlineTyp.ABGABE, "Algorithmen");
        Deadline zukuenftigeSchaltjahresDeadline = new Deadline("Abgabe", LocalDate.of(2028, 2, 29), DeadlineTyp.ABGABE, "Algorithmen");

        assertEquals(DeadlineStatus.UEBERFAELLIG, vergangeneSchaltjahresDeadline.getStatus());
        assertEquals(DeadlineStatus.OFFEN, zukuenftigeSchaltjahresDeadline.getStatus());
    }

    @Test
    void gleicheWerteBedeutenGleicheFrist() {
        Deadline a = new Deadline("Abgabe 1", LocalDate.of(2026, 9, 1), DeadlineTyp.ABGABE, "Mathe");
        Deadline b = new Deadline("Abgabe 1", LocalDate.of(2026, 9, 1), DeadlineTyp.ABGABE, "Mathe");
        Deadline c = new Deadline("Abgabe 2", LocalDate.of(2026, 9, 1), DeadlineTyp.ABGABE, "Mathe");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
