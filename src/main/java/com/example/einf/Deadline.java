package com.example.einf;

import java.time.LocalDate;

/**
 * Repräsentiert eine Frist (Anmeldung, Abgabe oder Klausur) mit optionalem Modulbezug.
 */
/**
 * Note on the modul/modulName dual representation (accepted):
 * A Deadline can be created either with a {@link Modul} reference (then
 * modulName is derived from it) or with just a module name string (then
 * modul stays null). The two are parallel, intentionally not synced at
 * runtime: modul is the object link used in-memory, modulName is the
 * plain text persisted to CSV. This keeps persistence independent of
 * live Modul objects and is an accepted design trade-off, documented here.
 */
public class Deadline {
    
    private LocalDate datum;
    private boolean istErledigt;
    private Modul modul;   //Referenz auf zugehöriges Modul

    private String beschreibung;
    private DeadlineTyp typ;
    private String modulName; // optional – kann leer sein

    public Deadline(String titel, LocalDate datum, Modul modul){
        this.datum = datum;
        this.modul = modul;
        this.istErledigt = false;
        
        this.beschreibung = titel;
        this.modulName = modul != null ? modul.getName() : "";
    }

    public Deadline(String beschreibung, LocalDate datum, DeadlineTyp typ, String modulName) {
        this.beschreibung = beschreibung;
        this.datum = datum;
        this.typ = typ;
        this.modulName = modulName == null ? "" : modulName;

        this.istErledigt = false;
        this.modul = null;
    }

    /**
     * Convenience accessor returning the description as the title.
     * The former separate {@code titel} field was an alias for
     * {@code beschreibung} (setBeschreibung always updated both),
     * which was a real dual-identity smell. The field has been
     * removed; getTitel() now unambiguously delegates to beschreibung.
     */
    public String getTitel(){
        return this.beschreibung;
    }

    public LocalDate getDatum(){
        return this.datum;
    }

    public void setDatum(LocalDate datum) { 
        this.datum = datum; 
    }

    public boolean istErledigt(){
        return this.istErledigt;
    }

    public Modul getModul(){
        return this.modul;
    }

    public void setErledigt(boolean istErledigt){
        this.istErledigt = istErledigt;
    }

    public DeadlineStatus getStatus(){
        if(istErledigt){
            return DeadlineStatus.ERLEDIGT;
        }
        
        if(datum.isBefore(LocalDate.now())){
            return DeadlineStatus.UEBERFAELLIG;
        }

        return DeadlineStatus.OFFEN;
    }

    public String getBeschreibung() { return beschreibung; }
    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

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
