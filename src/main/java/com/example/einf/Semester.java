package com.example.einf;

public enum Semester {
    WS23_24("Wintersemester 2023/24"),
    SS24("Sommersemester 2024"),
    WS24_25("Wintersemester 2024/25"),
    SS25("Sommersemester 2025");

    private final String bezeichnung;

    Semester(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}
