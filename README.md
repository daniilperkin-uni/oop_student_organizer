# Studentischer Organisationshelfer

[![CI](https://github.com/daniilperkin-uni/oop_student_organizer/actions/workflows/ci.yml/badge.svg)](https://github.com/daniilperkin-uni/oop_student_organizer/actions/workflows/ci.yml)

Ein Hilfswerkzeug für den studentischen Alltag, entwickelt in Java. 
Dieses Programm hilft Studierenden dabei, Fristen besser zu organisieren, Module zu verwalten und den eigenen Leistungsstand (Noten und ECTS) stets im Blick zu behalten. 

Entwickelt unter Anwendung aller gelernten Konzepte der objektorientierten Programmierung (OOP).

## Kernfunktionen (Die 5 Pfeiler)

* **Verwaltung von Modulen:** Fächer inkl. ECTS und Status anlegen, bearbeiten und verwalten.
* **Deadline-Tracking:** Anmelde- und Abgabefristen sowie Klausurtermine überwachen (mit Warnfunktion).
* **Leistungsüberwachung:** Automatische Berechnung des gewichteten Notendurchschnitts und des Studienfortschritts.
* **Persistente Datenhaltung:** Sicheres Speichern und Laden der Nutzerdaten auf der Festplatte.
* **Grafische Benutzeroberfläche (GUI):** Intuitive Bedienung durch eine moderne und übersichtliche Oberfläche.

## Technologien & Architektur

* **Sprache:** Java (JDK 21+)
* **GUI-Framework:** JavaFX (via FXML)
* **Build-Tool:** Maven
* **Architektur:** Model-View-Controller (MVC) zur sauberen Trennung von Daten, Logik und Oberfläche.

## Starten des Programms

Um das Programm lokal auszuführen, wird Maven benötigt.

```bash
# Projekt kompilieren und ausführen
.\mvnw.cmd clean javafx:run
```

## Datenhaltung

Alle Daten liegen im Verzeichnis `~/.studenthelfer` (unter Windows `%USERPROFILE%\.studenthelfer`):

* `module.csv` – Module inkl. ECTS, Semester und Leistung
* `deadlines.csv` – Fristen

Vor jedem Speichern wird die vorherige Version als `module.csv.bak` bzw. `deadlines.csv.bak` gesichert. Schlägt Laden oder Speichern fehl, zeigt die Anwendung einen Fehlerdialog.

## Kalender-Export (ICS)

Im Tab „Fristen“ exportiert „Als Kalender exportieren (.ics)“ alle Fristen als Ganztagestermine in die Datei `fristen.ics` im gewählten Ordner. Die Datei lässt sich in Outlook, Google Kalender oder Apple Kalender importieren.

## Tests

```bash
./mvnw -B verify   # Tests + JaCoCo-Report unter target/site/jacoco
```
