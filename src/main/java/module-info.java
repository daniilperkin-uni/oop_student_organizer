module com.example.einf {
	requires javafx.controls;
	requires javafx.fxml;

	// Für JUnit
	requires org.junit.jupiter.api;
	requires org.junit.platform.commons;

	opens com.example.einf to
			javafx.fxml,
			org.junit.platform.commons;

	exports com.example.einf;
}