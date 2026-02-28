package ui.controllers;

import java.util.List;

import domain.users.User;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import repositories.UserRepository;

public class AdminController {

	// ==================== USER MANAGEMENT ====================
	@FXML
	private TextField userSearchField;
	@FXML
	private TableView<User> usersTable;
	@FXML
	private TableColumn<User, Integer> userIdCol;
	@FXML
	private TableColumn<User, String> userNameCol;
	@FXML
	private TableColumn<User, String> userEmailCol;
	@FXML
	private TableColumn<User, String> userPhoneCol;

	@FXML
	private TableColumn<User, Void> userActionCol;
	@FXML
	private TableColumn<User, Integer> userRoleCol; // <-- RoleID column

	@FXML

	private final UserRepository userRepo = new UserRepository();
	private final ObservableList<User> allUsers = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		setupUserTable();
		loadAllUsers();
	}

	// ------------------- USER TABLE SETUP -------------------
	private void setupUserTable() {
		userIdCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getUserId()));
		userNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
		userEmailCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
		userPhoneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
		userRoleCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getRoleId())); // RoleID

		userActionCol.setCellFactory(col -> new TableCell<>() {
			private final Button editBtn = new Button("Edit");
			private final Button toggleBtn = new Button();
			private final Button deleteBtn = new Button("Delete");
			private final HBox hbox = new HBox(8);

			{
				editBtn.setOnAction(e -> editUser(getTableRow().getItem()));
				toggleBtn.setOnAction(e -> toggleUserStatus(getTableRow().getItem()));
				deleteBtn.setOnAction(e -> deleteUser(getTableRow().getItem()));
				editBtn.getStyleClass().add("btn-primary");
				deleteBtn.getStyleClass().add("btn-danger");
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					return;
				}

				User user = getTableRow().getItem();
				hbox.getChildren().clear();

				hbox.getChildren().add(editBtn);
				toggleBtn.setText(user.isActive() ? "Deactivate" : "Reactivate");
				toggleBtn.getStyleClass().setAll(user.isActive() ? "btn-warning" : "btn-success");
				hbox.getChildren().add(toggleBtn);
				hbox.getChildren().add(deleteBtn);
				setGraphic(hbox);
			}
		});

		userSearchField.textProperty().addListener((obs, old, newVal) -> filterUsers(newVal));
	}

	private void loadAllUsers() {
		List<User> users = userRepo.getAllUsers();
		allUsers.setAll(users);
		usersTable.setItems(allUsers);
	}

	private void filterUsers(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			usersTable.setItems(allUsers);
		} else {
			String lower = keyword.toLowerCase();
			var filtered = allUsers.stream().filter(
					u -> u.getName().toLowerCase().contains(lower) || u.getEmail().toLowerCase().contains(lower))
					.toList();
			usersTable.setItems(FXCollections.observableArrayList(filtered));
		}
	}

	// ====================== UPDATED METHODS ======================

	// Edit user
	private void editUser(User user) {
		if (user == null)
			return;

		// Restrict editing other admins
		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Action Restricted");
			alert.setHeaderText(null);
			alert.setContentText("You cannot edit another admin!");
			alert.showAndWait();
			return;
		}

		// Edit Email
		TextInputDialog nameDialog = new TextInputDialog(user.getEmail());
		nameDialog.setTitle("Edit Email");
		nameDialog.setHeaderText("Update Email");
		nameDialog.setContentText("Enter new email:");
		nameDialog.showAndWait().ifPresent(user::setEmail);

		// Edit Phone
		TextInputDialog phoneDialog = new TextInputDialog(user.getPhone());
		phoneDialog.setTitle("Edit Phone");
		phoneDialog.setHeaderText("Update Phone");
		phoneDialog.setContentText("Enter new phone:");
		phoneDialog.showAndWait().ifPresent(user::setPhone);

		if (userRepo.updateUser(user)) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Success");
			alert.setHeaderText(null);
			alert.setContentText("User updated!");
			alert.showAndWait();
			loadAllUsers();
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Failed to update user.");
			alert.showAndWait();
		}
	}

	// Toggle user status (activate/deactivate)
	private void toggleUserStatus(User user) {
		if (user == null)
			return;

		// Restrict deactivating other admins
		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Action Restricted");
			alert.setHeaderText(null);
			alert.setContentText("You cannot deactivate another admin!");
			alert.showAndWait();
			return;
		}

		String action = user.isActive() ? "deactivate" : "reactivate";
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to " + action + " this user?");
		confirm.showAndWait().ifPresent(res -> {
			boolean success = user.isActive() ? userRepo.setUserActive(user, false)
					: userRepo.setUserActive(user, true);
			if (success) {
				user.setActive(!user.isActive());
				Alert info = new Alert(Alert.AlertType.INFORMATION);
				info.setTitle("Success");
				info.setHeaderText(null);
				info.setContentText("User " + action + "d!");
				info.showAndWait();
				loadAllUsers();
			}
		});
	}

	// Delete user
	private void deleteUser(User user) {
		if (user == null) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("No user selected!");
			alert.showAndWait();
			return;
		}

		// Restrict deleting other admins
		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Action Restricted");
			alert.setHeaderText(null);
			alert.setContentText("You cannot delete another admin!");
			alert.showAndWait();
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"PERMANENTLY delete this user?\n\nThis cannot be undone!");
		confirm.showAndWait().ifPresent(res -> {
			if (userRepo.deleteUser(user)) {
				Alert info = new Alert(Alert.AlertType.INFORMATION);
				info.setTitle("Deleted");
				info.setHeaderText(null);
				info.setContentText("User deleted permanently.");
				info.showAndWait();
				loadAllUsers();
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.setContentText("User cannot be deleted.");
				alert.showAndWait();
			}
		});
	}

	@FXML
	private void onSearchUsers() {
		String keyword = userSearchField.getText().trim();

		if (keyword.isEmpty()) {
			usersTable.getItems().setAll(userRepo.getAllUsers());
			return;
		}

		List<User> filteredUsers = userRepo.searchUsers(keyword);
		usersTable.getItems().setAll(filteredUsers);
	}

	@FXML
	private void onRefreshUsers() {
		userSearchField.clear();
		usersTable.getItems().setAll(userRepo.getAllUsers());
	}

}