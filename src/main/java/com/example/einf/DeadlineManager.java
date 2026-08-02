package com.example.einf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeadlineManager {

    private final List<Deadline> deadlines;

    public DeadlineManager(){
        this.deadlines = new ArrayList<>();
    }

    public DeadlineManager(List<Deadline> deadlines){
        // Defensive copy: the manager owns its own list.
        this.deadlines = new ArrayList<>(deadlines);
    }

    /**
     * Returns an unmodifiable view of all deadlines (in insertion order).
     * Use addDeadline/deleteDeadline for changes. The controller syncs this
     * view back into the SpeicherManager before persisting.
     */
    public List<Deadline> getDeadlines() {
        return Collections.unmodifiableList(deadlines);
    }

    public void addDeadline(Deadline deadline){
        deadlines.add(deadline);
    }

    public void markAsErledigt(Deadline deadline){
        deadline.setErledigt(true);
    }

    public void deleteDeadline(Deadline deadline) {
        if (deadline == null) throw new IllegalArgumentException("Frist darf nicht null sein.");
        deadlines.remove(deadline);
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
