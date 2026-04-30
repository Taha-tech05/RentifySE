package app;

import domain.managers.BookingService;
import domain.managers.PaymentService;
import domain.managers.ProductService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controllers.LoginController;

public class MainApp extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		// 1. Initialize Singleton Services
		// This ensures all controllers use the same logic/repository instances
		ProductService productService = ProductService.getInstance();
		BookingService bookingService = BookingService.getInstance();
		PaymentService paymentService = PaymentService.getInstance();

		// 2. Load Login FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/login.fxml"));
		Parent root = loader.load();

		// 3. Inject Services into LoginController
		// The LoginController needs these so it can pass them to the RenterDashboard
		LoginController loginCtrl = loader.getController();
		loginCtrl.setProductService(productService);
		loginCtrl.setBookingService(bookingService);
		loginCtrl.setPaymentService(paymentService);

		// 4. Setup Scene and CSS
		Scene scene = new Scene(root, 1280, 800); // Updated to match your FXML prefWidth
		String css = getClass().getResource("/ui/css/app.css").toExternalForm();
		if (css != null) {
			scene.getStylesheets().add(css);
		}

		// 5. Show the stage
		stage.setTitle("Rentify - Premium Rental Marketplace");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}