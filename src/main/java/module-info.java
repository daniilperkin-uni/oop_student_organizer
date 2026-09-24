module com.example.einf {
	requires javafx.controls;
	requires javafx.fxml;

	opens com.example.einf to javafx.fxml;

	exports com.example.einf;
}
