package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controllers.LoginController;

public class MainApp extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		// Load login FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/login.fxml"));
		Scene scene = new Scene(loader.load(), 1000, 700);
		scene.getStylesheets().add(getClass().getResource("/ui/css/app.css").toExternalForm());

		// Initialize services

		// Inject services into login controller
		LoginController loginCtrl = loader.getController();

		// Show the stage
		stage.setTitle("RentEase");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
