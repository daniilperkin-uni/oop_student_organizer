package com.example.einf;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
	@FXML
	private Label welcomeText;

	@FXML
	protected void onHelloButtonClick() {
		welcomeText.setText("Welcome to JavaFX Application!");
	}

	@FXML
	protected void onAddModuleClick() {
		welcomeText.setText("Modul hinzufügen – noch nicht implementiert.");
	}
}