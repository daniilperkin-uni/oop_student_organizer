package com.example.einf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeadlineManager")
class DeadlineManagerTest {

    private DeadlineManager manager;
    private Deadline ueberfaellig;
    private Deadline offenBald;
    private Deadline offenSpaeter;
    private Deadline erledigt;

    @BeforeEach
    void setUp() {
        manager = new DeadlineManager();

        ueberfaellig = new Deadline("Alte Abgabe", LocalDate.now().minusDays(10), DeadlineTyp.ABGABE, "Mathe");
        offenBald = new Deadline("Bald fällig", LocalDate.now().plusDays(2), DeadlineTyp.ANMELDUNG, "Physik");
        offenSpaeter = new Deadline("Später fällig", LocalDate.now().plusDays(30), DeadlineTyp.KLAUSUR, "Chemie");
        erledigt = new Deadline("Schon erledigt", LocalDate.now().minusDays(5), DeadlineTyp.ABGABE, "Info");
        erledigt.setErledigt(true);

        // bewusst unsortiert einfügen
        manager.addDeadline(offenSpaeter);
        manager.addDeadline(erledigt);
        manager.addDeadline(ueberfaellig);
        manager.addDeadline(offenBald);
    }

    @Test
    @DisplayName("liefert anstehende Fristen ohne erledigte, nach Datum sortiert")
    void anstehendeSindSortiertUndOhneErledigte() {
        List<Deadline> anstehend = manager.getAnstehendeDeadlines();

        assertEquals(List.of(ueberfaellig, offenBald, offenSpaeter), anstehend);
        assertFalse(anstehend.contains(erledigt));
    }

    @Test
    @DisplayName("liefert nur überfällige Fristen")
    void ueberfaelligeGefiltert() {
        assertEquals(List.of(ueberfaellig), manager.getUeberfaelligeDeadlines());
    }

    @Test
    @DisplayName("sortiert die Gesamtliste nach Überfällig, Offen, Erledigt")
    void gesamtlisteNachStatus() {
        assertEquals(List.of(ueberfaellig, offenBald, offenSpaeter, erledigt),
                manager.getAllDeadlinesSortiert());
    }

    @Test
    @DisplayName("entfernt eine Frist aus dem Bestand")
    void loescheFrist() {
        manager.deleteDeadline(offenBald);

        assertAll(
                () -> assertFalse(manager.getAllDeadlinesSortiert().contains(offenBald)),
                () -> assertEquals(3, manager.getAllDeadlinesSortiert().size()));
    }

    @Test
    @DisplayName("lehnt null beim Löschen ab")
    void loeschenMitNullWirftException() {
        assertThrows(IllegalArgumentException.class, () -> manager.deleteDeadline(null));
    }

    @Test
    @DisplayName("markiert eine Frist als erledigt")
    void markiereAlsErledigt() {
        manager.markAsErledigt(offenBald);

        assertAll(
                () -> assertTrue(offenBald.istErledigt()),
                () -> assertEquals(DeadlineStatus.ERLEDIGT, offenBald.getStatus()),
                () -> assertFalse(manager.getAnstehendeDeadlines().contains(offenBald)));
    }

    @Test
    @DisplayName("kommt mit einer leeren Verwaltung zurecht")
    void leererManager() {
        DeadlineManager leer = new DeadlineManager();

        assertAll(
                () -> assertTrue(leer.getAnstehendeDeadlines().isEmpty()),
                () -> assertTrue(leer.getUeberfaelligeDeadlines().isEmpty()),
                () -> assertTrue(leer.getAllDeadlinesSortiert().isEmpty()));
    }

    @Test
    @DisplayName("arbeitet auf der übergebenen Liste weiter")
    void umschliesstUebergebeneListe() {
        List<Deadline> bestand = new ArrayList<>();
        DeadlineManager wrapper = new DeadlineManager(bestand);

        Deadline neu = new Deadline("Neu", LocalDate.now().plusDays(1), DeadlineTyp.ABGABE, "");
        wrapper.addDeadline(neu);

        assertEquals(List.of(neu), bestand,
                "Der Manager muss dieselbe Liste befüllen, die persistiert wird.");
    }

    @Test
    @DisplayName("gibt bei jedem Aufruf eine eigene Ergebnisliste zurück")
    void ergebnislisteIstKopie() {
        List<Deadline> ersteAbfrage = manager.getAllDeadlinesSortiert();
        ersteAbfrage.clear();

        assertEquals(4, manager.getAllDeadlinesSortiert().size(),
                "Das Leeren der Ergebnisliste darf den Bestand nicht verändern.");
    }
}
