package ui.controllers;

import domain.managers.BookingService;
import domain.managers.PaymentService;
import domain.managers.ProductService;
import domain.managers.ReportService;
import domain.managers.ReviewService;
import domain.managers.TransactionService;
import domain.users.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import repositories.UserRepository;

public class LoginController {

	@FXML
	private TextField emailField;
	@FXML
	private PasswordField passwordField;

	// Services injected from MainApp
	private ProductService productService;
	private BookingService bookingService;
	private PaymentService paymentService;

	// Setters for Dependency Injection
	public void setProductService(ProductService ps) {
		this.productService = ps;
	}

	public void setBookingService(BookingService bs) {
		this.bookingService = bs;
	}

	public void setPaymentService(PaymentService pys) {
		this.paymentService = pys;
	}

	@FXML
	public void onLogin(ActionEvent e) {
		String email = emailField.getText().trim();
		String password = passwordField.getText().trim();

		if (email.isEmpty() || password.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both email and password.");
			return;
		}

		try {
			UserRepository userRepo = new UserRepository();
			User loggedInUser = userRepo.authenticateUser(email, password);

			if (loggedInUser == null) {
				showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
				return;
			}

			// Redirect with Service Injection
			switch (loggedInUser.getRole().toUpperCase()) {
			case "ADMIN" -> openAdminDashboard(loggedInUser, e);
			case "OWNER" -> openOwnerDashboard(loggedInUser, e);
			case "RENTER" -> openRenterDashboard(loggedInUser, e);
			default -> showAlert(Alert.AlertType.ERROR, "Error", "Unknown user role: " + loggedInUser.getRole());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "System Error", "Unable to login. Please try again.");
		}
	}

	private void openRenterDashboard(User user, ActionEvent e) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/renter_dashboard.fxml"));
		Scene scene = new Scene(loader.load(), 1280, 800);

		// KEY STEP: Get the controller and inject the services
		RenterController renterCtrl = loader.getController();

		// Pass services so the dashboard can actually fetch products
		renterCtrl.setProductService(productService != null ? productService : ProductService.getInstance());
		renterCtrl.setBookingService(bookingService != null ? bookingService : BookingService.getInstance());
		renterCtrl.setPaymentService(paymentService != null ? paymentService : PaymentService.getInstance());

		// Pass user info
		renterCtrl.setLoggedInUser(user);

		// Initialize the data
		renterCtrl.loadInitialProducts();

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		stage.setScene(scene);
		stage.setTitle("Rentify - Renter Dashboard");
		stage.centerOnScreen();
	}

	private void openOwnerDashboard(User user, ActionEvent e) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/owner_dashboard.fxml"));
		Scene scene = new Scene(loader.load(), 1280, 800);

		Object controller = loader.getController();
		if (controller instanceof OwnerController ownerCtrl) {
			ownerCtrl.setProductService(productService);
			ownerCtrl.setOwnerName(user.getName()); // pass ID too
			ownerCtrl.setOwnerUserId(user.getUserId()); // pass ID too
			ownerCtrl.setReviewService(ReviewService.getInstance());
			ownerCtrl.setBookingService(BookingService.getInstance());

			ownerCtrl.initialize();
		}

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		stage.setScene(scene);
		stage.setTitle("Rentify - Owner Dashboard");
	}

	private void openAdminDashboard(User user, ActionEvent e) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/admin_dashboard.fxml"));
		Scene scene = new Scene(loader.load(), 1280, 800);

		Object controller = loader.getController();

		if (controller instanceof ui.controllers.AdminController adminCtrl) {
			adminCtrl.setProductService(productService);
			adminCtrl.setTransactionService(TransactionService.getInstance()); // ← CRITICAL!
			adminCtrl.setBookingService(BookingService.getInstance());
			adminCtrl.setReportService(ReportService.getInstance());

			adminCtrl.initialize();
		}

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		stage.setScene(scene);
		stage.setTitle("Rentify - Admin Dashboard");
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	@FXML
	private void onOpenRegistration(ActionEvent e) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/registration.fxml"));
			Scene scene = new Scene(loader.load(), 800, 600);
			Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle("Rentify - Registration");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}