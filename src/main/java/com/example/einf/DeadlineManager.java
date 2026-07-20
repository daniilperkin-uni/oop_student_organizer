package com.example.einf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeadlineManager {

    private List<Deadline> deadlines;

    public DeadlineManager(){
        this.deadlines = new ArrayList<>();
    }

    public DeadlineManager(List<Deadline> deadlines){
        this.deadlines = deadlines;
    }

    public void addDeadline(Deadline deadline){
        deadlines.add(deadline);
    }

    public void markAsErledigt(Deadline deadline){
        deadline.setErledigt(true);
    }

    public List<Deadline> getAnstehendeDeadlines(){
        
        List<Deadline> anstehend = new ArrayList<>();

        for(Deadline deadline: deadlines){
            if(!deadline.istErledigt()){
                anstehend.add(deadline);
            }
        }

        anstehend.sort(Comparator.comparing(Deadline::getDatum));
        return anstehend;
    }

    public List<Deadline> getUeberfaelligeDeadlines(){
        
        List<Deadline> ueberfaellig = new ArrayList<>();

        for(Deadline deadline: deadlines){
            if(deadline.getStatus() == DeadlineStatus.UEBERFAELLIG){
                ueberfaellig.add(deadline);
            }
        }

        ueberfaellig.sort(Comparator.comparing(Deadline::getDatum));
        return ueberfaellig;
    }

    public List<Deadline> getAllDeadlinesSortiert() {
        List<Deadline> sortiert = new ArrayList<>(deadlines);
        sortiert.sort(Comparator
                .comparingInt((Deadline d) -> switch (d.getStatus()) {
                    case UEBERFAELLIG -> 0;
                    case OFFEN -> 1;
                    case ERLEDIGT -> 2;
                })
                .thenComparing(Deadline::getDatum));
        return sortiert;
    }
}
