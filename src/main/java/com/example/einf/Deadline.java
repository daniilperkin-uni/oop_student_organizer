package com.example.einf;

import java.time.LocalDate;

/**
 * Repräsentiert eine Frist (Anmeldung, Abgabe oder Klausur) mit optionalem Modulbezug.
 */
public class Deadline {

    private String beschreibung;
    private LocalDate datum;
    private DeadlineTyp typ;
    private String modulName; // optional – kann leer sein

    public Deadline(String beschreibung, LocalDate datum, DeadlineTyp typ, String modulName) {
        this.beschreibung = beschreibung;
        this.datum = datum;
        this.typ = typ;
        this.modulName = modulName == null ? "" : modulName;
    }

    public String getBeschreibung() { return beschreibung; }
    public void setBeschreibung(String beschreibung) { this.beschreibung = beschreibung; }

    public LocalDate getDatum() { return datum; }
    public void setDatum(LocalDate datum) { this.datum = datum; }

    public DeadlineTyp getTyp() { return typ; }
    public void setTyp(DeadlineTyp typ) { this.typ = typ; }

    public String getModulName() { return modulName; }
    public void setModulName(String modulName) { this.modulName = modulName; }

    public boolean isAbgelaufen() {
        return datum.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Deadline{beschreibung='" + beschreibung + "', datum=" + datum + ", typ=" + typ + ", modul='" + modulName + "'}";
    }
}
