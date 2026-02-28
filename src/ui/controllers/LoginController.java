package ui.controllers;

import domain.users.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import repositories.UserRepository;

public class LoginController {

	@FXML
	private TextField emailField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Button loginButton;

	// =============================
	// LOGIN LOGIC
	// =============================

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

			// If using hashing, hash password here
			// String hashedPassword = HashUtil.hash(password);
			String hashedPassword = password; // Temporary (if not hashing yet)

			User loggedInUser = userRepo.authenticateUser(email, hashedPassword);

			if (loggedInUser == null) {
				showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
				return;
			}

			// Redirect based on detected role
			switch (loggedInUser.getRole()) {
			case "ADMIN" -> openAdminDashboard(loggedInUser, e);
			case "OWNER" -> openOwnerDashboard(loggedInUser, e);
			case "RENTER" -> openRenterDashboard(loggedInUser, e);
			default -> showAlert(Alert.AlertType.ERROR, "Error", "Unknown user role.");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "System Error", "Unable to login. Please try again.");
		}
	}

	// =============================
	// OPEN ADMIN DASHBOARD
	// =============================
	private void openAdminDashboard(User user, ActionEvent e) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/admin_dashboard.fxml"));
		Scene scene = new Scene(loader.load(), 1200, 800);
		AdminController adminCtrl = loader.getController();
		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		stage.setScene(scene);
		stage.setTitle("Rentify - Admin Dashboard");
		stage.show();
	}

	// =============================
	// OPEN OWNER DASHBOARD
	// =============================
	private void openOwnerDashboard(User user, ActionEvent e) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/owner_dashboard.fxml"));
		Scene scene = new Scene(loader.load(), 1200, 800);
		OwnerController ownerCtrl = loader.getController();
		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		stage.setScene(scene);
		stage.setTitle("Rentify - Owner Dashboard");
		stage.show();
	}

	// =============================
	// OPEN RENTER DASHBOARD
	// =============================
	private void openRenterDashboard(User user, ActionEvent e) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/renter_dashboard.fxml"));
		Scene scene = new Scene(loader.load(), 1200, 800);
		RenterController renterCtrl = loader.getController();
		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
		stage.setScene(scene);
		stage.setTitle("Rentify - Renter Dashboard");
		stage.show();
	}

	// =============================
	// ALERT UTILITY METHOD
	// =============================
	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	// ================================
	// Open registration screen
	// ================================
	@FXML
	private void onOpenRegistration(ActionEvent e) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/registration.fxml"));
			Scene scene = new Scene(loader.load(), 800, 600);
			Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle("Rentify - Registration");
			stage.show();
		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error", "Cannot open registration screen.");
		}
	}
}