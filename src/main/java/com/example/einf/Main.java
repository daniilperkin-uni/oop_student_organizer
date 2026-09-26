package com.example.einf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

	private final SpeicherManager speicherManager = new SpeicherManager();

	// Wired in start(); kept so stop() can flush through the controller's
	// synced save path (see HomeController.speichereAenderungen()).
	private HomeController controller;

	@Override
	public void start(Stage stage) throws IOException {
		try {
			speicherManager.ladeDaten();
		} catch (IOException e) {
			UiDialogs.zeigeFehler("Fehler", "Daten konnten nicht geladen werden",
					e.getMessage() + "\nDie Anwendung startet mit leeren Daten.");
		}

		FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/einf/views/home-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		scene.getStylesheets().add(
				Objects.requireNonNull(Main.class.getResource("/com/example/einf/styles/app.css")).toExternalForm());

		controller = fxmlLoader.getController();

		ModulVerwaltung modulVerwaltung = new ModulVerwaltung(speicherManager.getModule());
		controller.setModulVerwaltung(modulVerwaltung);

		DeadlineManager deadlineManager = new DeadlineManager(speicherManager.getDeadlines());
		controller.setDeadlineManager(deadlineManager);

		controller.setSpeicherManager(speicherManager);
		controller.initializeControllers();

		stage.setTitle("Studentischer Organisationshelfer");
		stage.setScene(scene);
		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.show();
	}

	@Override
	public void stop() {
		// Flush through the controller's synced save path instead of writing
		// the SpeicherManager snapshot directly: the ModulVerwaltung and
		// DeadlineManager own the live state and have to be pushed into the
		// store first, otherwise closing the app could persist a stale
		// snapshot as soon as a mutation forgets its saveChanges() call.
		if (controller != null) {
			controller.speichereAenderungen();
		}
	}

	public SpeicherManager getSpeicherManager() {
		return speicherManager;
	}
}
