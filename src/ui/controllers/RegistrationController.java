package ui.controllers;

import domain.users.Admin;
import domain.users.Owner;
import domain.users.Renter;
import domain.users.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import repositories.UserRepository;

public class RegistrationController {

	@FXML
	private TextField nameField;
	@FXML
	private TextField emailField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private TextField phoneField;
	@FXML
	private ChoiceBox<String> roleChoiceBox;

	private final UserRepository userRepo = new UserRepository();

	@FXML
	private void onRegister(ActionEvent e) {
		String name = nameField.getText().trim();
		String email = emailField.getText().trim();
		String password = passwordField.getText().trim();
		String phone = phoneField.getText().trim();
		String role = roleChoiceBox.getValue();

		if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
			showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill all fields and select a role.");
			return;
		}

		try {
			String hashedPassword = password;

			User newUser;
			switch (role) {
			case "ADMIN" -> newUser = new Admin(0, name, email, hashedPassword, phone, 1);
			case "OWNER" -> newUser = new Owner(0, name, email, hashedPassword, phone, 2);
			case "RENTER" -> newUser = new Renter(0, name, email, hashedPassword, phone, 3);
			default -> {
				showAlert(Alert.AlertType.ERROR, "Error", "Invalid role selected.");
				return;
			}
			}

			boolean success = userRepo.addUser(newUser);
			if (success) {
				showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully!");

				// Redirect to dashboard immediately
				FXMLLoader loader = new FXMLLoader(getClass().getResource(switch (role) {
				case "ADMIN" -> "/ui/views/admin_dashboard.fxml";
				case "OWNER" -> "/ui/views/owner_dashboard.fxml";
				case "RENTER" -> "/ui/views/renter_dashboard.fxml";
				default -> null;
				}));
				Scene scene = new Scene(loader.load(), 1200, 800);
				Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
				stage.setScene(scene);
				stage.setTitle("Rentify - " + role + " Dashboard");
				stage.show();

			} else {
				showAlert(Alert.AlertType.ERROR, "Error", "Failed to register user. Email might already exist.");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "System Error", "Unable to register. Please try again.");
		}
	}

	@FXML
	private void onBackToLogin(ActionEvent e) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/login.fxml"));
			Scene scene = new Scene(loader.load(), 800, 600);
			Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle("Rentify - Login");
			stage.show();
		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error", "Cannot open login screen.");
		}
	}

	private void clearFields() {
		nameField.clear();
		emailField.clear();
		passwordField.clear();
		phoneField.clear();
		roleChoiceBox.setValue(null);
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}