package com.example.einf;

import java.time.LocalDate;

/**
 * Repräsentiert eine Frist (Anmeldung, Abgabe oder Klausur) mit optionalem Modulbezug.
 */
public class Deadline {
    
    private String titel;
    private LocalDate datum;
    private boolean istErledigt;
    private Modul modul;   //Referenz auf zugehöriges Modul

    private String beschreibung;
    private DeadlineTyp typ;
    private String modulName; // optional – kann leer sein

    public Deadline(String titel, LocalDate datum, Modul modul){
        this.titel = titel;
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
        
        this.titel = beschreibung;
        this.istErledigt = false;
        this.modul = null;
    }

    public String getTitel(){
        return this.titel;
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
        this.titel = beschreibung;
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
