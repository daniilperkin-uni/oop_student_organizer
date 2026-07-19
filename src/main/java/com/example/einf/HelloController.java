package com.example.einf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
	@FXML
	private Label welcomeText;


	@FXML
	public void onAddModuleClick(ActionEvent actionEvent) {
	}

	@FXML
	protected void onAddModuleClick() {
		welcomeText.setText("Modul hinzufügen – noch nicht implementiert.");
	}
}