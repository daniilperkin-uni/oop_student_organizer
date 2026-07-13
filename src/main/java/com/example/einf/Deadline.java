package com.example.einf;
import java.time.LocalDate;

public class Deadline {
    
    private String titel;
    private LocalDate datum;
    private boolean istErledigt;
    private Modul modul;   //Referenz auf zugehöriges Modul

    public Deadline(String titel, LocalDate datum, Modul modul){
        this.titel=titel;
        this.datum=datum;
        this.modul=modul;
        this.istErledigt=false;
    }

    public String getTitel(){
        return this.titel;
    }

    public LocalDate getDatum(){
        return this.datum;
    }

    public boolean istErledigt(){
        return this.istErledigt;
    }

    public Modul getModul(){
        return this.modul;
    }

    public void setErledigt(boolean istErledigt){
        this.istErledigt=istErledigt;
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
}
