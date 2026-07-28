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
}
