package com.example.einf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

	private final SpeicherManager speicherManager = new SpeicherManager();

	@Override
	public void start(Stage stage) throws IOException {
		// Gespeicherte Daten beim Programmstart laden
		try {
			speicherManager.ladeDaten();
		} catch (IOException e) {
			System.err.println("Fehler beim Laden der Daten: " + e.getMessage());
		}

		FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/einf/views/home-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		
		HomeController controller = fxmlLoader.getController();
		
		ModulVerwaltung modulVerwaltung = new ModulVerwaltung(speicherManager.getModule());
		controller.setModulVerwaltung(modulVerwaltung);
		
		DeadlineManager deadlineManager = new DeadlineManager(speicherManager.getDeadlines());
		controller.setDeadlineManager(deadlineManager);
		
		stage.setTitle("Studentischer Organisationshelfer");
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	@Override
	public void stop() {
		// Daten beim Schließen des Programms sichern
		try {
			speicherManager.speichereDaten();
		} catch (IOException e) {
			System.err.println("Fehler beim Speichern der Daten: " + e.getMessage());
		}
	}

	public SpeicherManager getSpeicherManager() {
		return speicherManager;
	}
}