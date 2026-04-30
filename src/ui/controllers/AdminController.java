package ui.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import domain.entities.Booking;
import domain.entities.Product;
import domain.entities.Transaction;
import domain.managers.BookingService;
import domain.managers.ProductService;
import domain.managers.ReportService;
import domain.managers.TransactionService;
import domain.users.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import repositories.UserRepository;
import ui.UiUtils;

public class AdminController {

	// ═══════════════════════════════════════════════════════════
	// SERVICES
	// ═══════════════════════════════════════════════════════════

	private ProductService productService;
	private TransactionService transactionService;
	private BookingService bookingService;
	private ReportService reportService;

	// ═══════════════════════════════════════════════════════════
	// USER MANAGEMENT
	// ═══════════════════════════════════════════════════════════

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
	private TableColumn<User, String> userRoleCol;
	@FXML
	private TableColumn<User, Void> userActionCol;

	private final UserRepository userRepo = new UserRepository();
	private final ObservableList<User> allUsers = FXCollections.observableArrayList();

	// ═══════════════════════════════════════════════════════════
	// MODERATE LISTINGS
	// ═══════════════════════════════════════════════════════════

	@FXML
	private Label pendingCountLabel;
	@FXML
	private Label approvedTodayLabel;
	@FXML
	private Label rejectedTodayLabel;
	@FXML
	private Label totalReviewedLabel;

	@FXML
	private TextField listingSearchField;
	@FXML
	private ComboBox<String> listingStatusFilter;

	@FXML
	private TableView<Product> listingsTable;
	@FXML
	private TableColumn<Product, Integer> listingIdCol;
	@FXML
	private TableColumn<Product, String> listingTitleCol;
	@FXML
	private TableColumn<Product, String> listingOwnerCol;
	@FXML
	private TableColumn<Product, String> listingCategoryCol;
	@FXML
	private TableColumn<Product, String> listingDateCol;
	@FXML
	private TableColumn<Product, String> listingStatusCol;
	@FXML
	private TableColumn<Product, Void> listingActionCol;

	@FXML
	private VBox reviewPanel;
	@FXML
	private Label selectedListingIdLabel;
	@FXML
	private Label selectedListingTitle;
	@FXML
	private Label selectedListingOwner;
	@FXML
	private Label selectedListingCategory;
	@FXML
	private Label selectedListingDate;
	@FXML
	private Button approveBtn;
	@FXML
	private Button rejectBtn;
	@FXML
	private TextArea rejectionReasonField;

	@FXML
	private ListView<String> auditLogList;

	private final ObservableList<Product> allProducts = FXCollections.observableArrayList();

	// ═══════════════════════════════════════════════════════════
	// VIEW TRANSACTIONS
	// ═══════════════════════════════════════════════════════════

	@FXML
	private Label statsLabel;
	@FXML
	private Label detailsLabel;
	@FXML
	private TextField txSearchField;
	@FXML
	private ComboBox<String> txStatusFilter;
	@FXML
	private TableView<Transaction> transactionsTable;
	@FXML
	private TableColumn<Transaction, Integer> txIdCol;
	@FXML
	private TableColumn<Transaction, Integer> txBookingIdCol;
	@FXML
	private TableColumn<Transaction, String> txProductCol;
	@FXML
	private TableColumn<Transaction, String> txRenterCol;
	@FXML
	private TableColumn<Transaction, String> txOwnerCol;
	@FXML
	private TableColumn<Transaction, Double> txAmountCol;
	@FXML
	private TableColumn<Transaction, String> txStatusCol;
	@FXML
	private TableColumn<Transaction, String> txCreatedAtCol;
	@FXML
	private VBox txDetailPanel;
	@FXML
	private Label txDetailIdLabel;

	private final ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();

	// ═══════════════════════════════════════════════════════════
	// RENTAL HISTORY
	// ═══════════════════════════════════════════════════════════

	@FXML
	private ListView<String> renterHistoryList;
	@FXML
	private ListView<String> ownerHistoryList;
	@FXML
	private Label historyDetailsLabel;

	private final List<Booking> renterBookingsList = new ArrayList<>();
	private final List<Booking> ownerBookingsList = new ArrayList<>();

	// ═══════════════════════════════════════════════════════════
	// STATISTICS AND REPORTS
	// ═══════════════════════════════════════════════════════════

	@FXML
	private ComboBox<String> reportTypeChoice;
	@FXML
	private ComboBox<String> periodChoice;
	@FXML
	private TextArea reportTextArea;
	@FXML
	private Label reportTitleLabel;

	// ═══════════════════════════════════════════════════════════
	// SESSION COUNTERS
	// ═══════════════════════════════════════════════════════════

	private int approvedToday = 0;
	private int rejectedToday = 0;
	private int totalReviewed = 0;

	// ═══════════════════════════════════════════════════════════
	// INITIALIZE
	// ═══════════════════════════════════════════════════════════

	@FXML
	public void initialize() {
		setupUserTable();
		setupListingsTable();
		setupStatusFilter();
		setupRowSelection();
		setupHistoryClickHandlers();
		setupReportControls();

		reviewPanel.setVisible(false);
		reviewPanel.setManaged(false);

		setupTransactionsTable();
		setupTxStatusFilter();
		setupTxRowSelection();

		txDetailPanel.setVisible(false);
		txDetailPanel.setManaged(false);

		loadAllUsers();
	}

	// ═══════════════════════════════════════════════════════════
	// USER MANAGEMENT
	// ═══════════════════════════════════════════════════════════

	private void setupUserTable() {
		userIdCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getUserId()));
		userNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
		userEmailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
		userPhoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
		userRoleCol.setCellValueFactory(c -> new SimpleStringProperty(getRoleName(c.getValue().getRoleId())));

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
				if (user.getRoleId() != 1) {
					hbox.getChildren().add(editBtn);
					toggleBtn.setText(user.isActive() ? "Deactivate" : "Reactivate");
					toggleBtn.getStyleClass().setAll(user.isActive() ? "btn-warning" : "btn-success");
					hbox.getChildren().add(toggleBtn);
					hbox.getChildren().add(deleteBtn);
				} else {
					hbox.getChildren().add(new Label("Super Admin"));
				}
				setGraphic(hbox);
			}
		});

		userSearchField.textProperty().addListener((obs, old, val) -> filterUsers(val));
	}

	private void loadAllUsers() {
		allUsers.setAll(userRepo.getAllUsers());
		usersTable.setItems(allUsers);
	}

	private void filterUsers(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			usersTable.setItems(allUsers);
			return;
		}
		String lower = keyword.toLowerCase();
		var filtered = allUsers.stream()
				.filter(u -> u.getName().toLowerCase().contains(lower) || u.getEmail().toLowerCase().contains(lower))
				.toList();
		usersTable.setItems(FXCollections.observableArrayList(filtered));
	}

	private void editUser(User user) {
		if (user == null)
			return;

		TextInputDialog nameDlg = new TextInputDialog(user.getName());
		nameDlg.setTitle("Edit Name");
		nameDlg.setHeaderText("Update name");
		nameDlg.showAndWait().ifPresent(user::setName);

		TextInputDialog phoneDlg = new TextInputDialog(user.getPhone());
		phoneDlg.setTitle("Edit Phone");
		phoneDlg.setHeaderText("Update phone");
		phoneDlg.showAndWait().ifPresent(user::setPhone);

		ChoiceDialog<String> roleDlg = new ChoiceDialog<>("Renter", "Renter", "Owner", "Admin");
		roleDlg.setTitle("Change Role");
		roleDlg.setHeaderText("New role");
		roleDlg.showAndWait()
				.ifPresent(role -> user.setRoleId(role.equals("Admin") ? 1 : role.equals("Owner") ? 2 : 3));

		if (userRepo.updateUser(user)) {
			UiUtils.showInfo("Success", "User updated!");
			loadAllUsers();
		} else {
			UiUtils.showError("Error", "Failed to update user.");
		}
	}

	private void toggleUserStatus(User user) {
		if (user == null || user.getRoleId() == 1)
			return;
		String action = user.isActive() ? "deactivate" : "reactivate";
		if (UiUtils.showConfirmation("Confirm", "Are you sure you want to " + action + " this user?")) {
			boolean success = user.isActive() ? userRepo.deactivateUser(user.getUserId())
					: userRepo.reactivateUser(user.getUserId());
			if (success) {
				user.setActive(!user.isActive());
				UiUtils.showInfo("Success", "User " + action + "d!");
				loadAllUsers();
			}
		}
	}

	private void deleteUser(User user) {
		if (user == null || user.getRoleId() == 1) {
			UiUtils.showError("Denied", "Cannot delete Super Admin!");
			return;
		}
		if (UiUtils.showConfirmation("DELETE USER", "PERMANENTLY delete this user?\n\nThis cannot be undone!")) {
			if (userRepo.deleteUser(user.getUserId())) {
				UiUtils.showInfo("Deleted", "User deleted permanently.");
				loadAllUsers();
			} else {
				UiUtils.showError("Error", "User has bookings and cannot be deleted.");
			}
		}
	}

	@FXML
	private void onSearchUsers() {
		filterUsers(userSearchField.getText().trim());
	}

	@FXML
	private void onRefreshUsers() {
		userSearchField.clear();
		loadAllUsers();
	}

	private String getRoleName(int roleId) {
		return switch (roleId) {
		case 1 -> "Admin";
		case 2 -> "Owner";
		case 3 -> "Renter";
		default -> "Unknown";
		};
	}

	// ═══════════════════════════════════════════════════════════
	// MODERATE LISTINGS
	// ═══════════════════════════════════════════════════════════

	private void setupListingsTable() {
		listingIdCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getProductId()));
		listingTitleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
		listingOwnerCol.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getOwnerName() != null ? c.getValue().getOwnerName()
						: "User#" + c.getValue().getOwnerUserId()));
		listingCategoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
		listingDateCol.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().toLocalDate().toString() : "—"));
		listingStatusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApprovalStatus()));

		listingActionCol.setCellFactory(col -> new TableCell<>() {
			private final Button qa = new Button("✓ Approve");
			private final Button qr = new Button("✗ Reject");
			private final HBox hbox = new HBox(6, qa, qr);

			{
				qa.setStyle("-fx-background-color:#16A34A;-fx-text-fill:white;"
						+ "-fx-font-size:11px;-fx-background-radius:5;-fx-cursor:hand;");
				qr.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;"
						+ "-fx-font-size:11px;-fx-background-radius:5;-fx-cursor:hand;");

				qa.setOnAction(e -> {
					Product p = getTableRow().getItem();
					if (p != null)
						doApprove(p);
				});
				qr.setOnAction(e -> {
					Product p = getTableRow().getItem();
					if (p != null) {
						listingsTable.getSelectionModel().select(p);
						rejectionReasonField.requestFocus();
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					return;
				}
				String status = getTableRow().getItem().getApprovalStatus();
				setGraphic("Pending".equalsIgnoreCase(status) ? hbox : null);
			}
		});
	}

	private void setupStatusFilter() {
		listingStatusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected"));
		listingStatusFilter.setValue("All");
	}

	private void setupRowSelection() {
		listingsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
			if (selected == null) {
				hideReviewPanel();
			} else {
				populateReviewPanel(selected);
				reviewPanel.setVisible(true);
				reviewPanel.setManaged(true);
			}
		});
	}

	private void populateReviewPanel(Product p) {
		selectedListingIdLabel.setText("Product #" + p.getProductId());
		selectedListingTitle.setText(p.getName());
		selectedListingOwner.setText(p.getOwnerName() != null ? p.getOwnerName() : "User#" + p.getOwnerUserId());
		selectedListingCategory.setText(p.getType());
		selectedListingDate.setText(p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate().toString() : "—");
		rejectionReasonField.clear();
	}

	private void loadAllProducts() {
		if (productService == null)
			return;
		allProducts.setAll(productService.getAllProducts());
		listingsTable.setItems(allProducts);
		refreshStatCards();
	}

	private void refreshStatCards() {
		long pending = allProducts.stream().filter(p -> "Pending".equalsIgnoreCase(p.getApprovalStatus())).count();
		pendingCountLabel.setText(String.valueOf(pending));
		approvedTodayLabel.setText(String.valueOf(approvedToday));
		rejectedTodayLabel.setText(String.valueOf(rejectedToday));
		totalReviewedLabel.setText(String.valueOf(totalReviewed));
	}

	@FXML
	private void onApproveListing() {
		Product selected = listingsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			UiUtils.showError("No Selection", "Please select a product to approve.");
			return;
		}
		doApprove(selected);
	}

	private void doApprove(Product product) {
		if (!UiUtils.showConfirmation("Approve Product",
				"Approve '" + product.getName() + "'?\nThis will make it visible to all renters."))
			return;

		if (productService.approveProduct(product.getProductId(), "Admin")) {
			product.setApprovalStatus("Approved");
			approvedToday++;
			totalReviewed++;
			addAuditEntry("APPROVED", product, null);
			refreshStatCards();
			listingsTable.refresh();
			hideReviewPanel();
			UiUtils.showInfo("Approved!", product.getName() + " is now live!");
		} else {
			UiUtils.showError("Failed", "Could not approve product.");
		}
	}

	@FXML
	private void onRejectListing() {
		Product selected = listingsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			UiUtils.showError("No Selection", "Please select a product to reject.");
			return;
		}
		String reason = rejectionReasonField.getText().trim();
		if (reason.isEmpty()) {
			UiUtils.showError("Reason Required", "Please provide a rejection reason.");
			rejectionReasonField.requestFocus();
			return;
		}
		if (productService.rejectProduct(selected.getProductId(), reason, "Admin")) {
			selected.setApprovalStatus("Rejected");
			rejectedToday++;
			totalReviewed++;
			addAuditEntry("REJECTED", selected, reason);
			refreshStatCards();
			listingsTable.refresh();
			hideReviewPanel();
			UiUtils.showInfo("Rejected", selected.getName() + " has been rejected.");
		} else {
			UiUtils.showError("Failed", "Could not reject product.");
		}
	}

	@FXML
	private void onSearchListings() {
		String keyword = listingSearchField.getText().trim().toLowerCase();
		String filter = listingStatusFilter.getValue();

		var filtered = allProducts.stream().filter(p -> {
			String owner = p.getOwnerName() != null ? p.getOwnerName() : "User#" + p.getOwnerUserId();
			boolean kw = keyword.isEmpty() || p.getName().toLowerCase().contains(keyword)
					|| owner.toLowerCase().contains(keyword);
			boolean st = "All".equalsIgnoreCase(filter) || p.getApprovalStatus().equalsIgnoreCase(filter);
			return kw && st;
		}).toList();

		listingsTable.setItems(FXCollections.observableArrayList(filtered));
		hideReviewPanel();
	}

	@FXML
	private void onRefreshListings() {
		listingSearchField.clear();
		listingStatusFilter.setValue("All");
		loadAllProducts();
		hideReviewPanel();
	}

	@FXML
	private void onClearAuditLog() {
		auditLogList.getItems().clear();
	}

	private void addAuditEntry(String action, Product p, String reason) {
		String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd HH:mm"));
		String entry = reason == null
				? String.format("[%s]  %s — \"%s\" (#%d)", ts, action, p.getName(), p.getProductId())
				: String.format("[%s]  %s — \"%s\" (#%d) | %s", ts, action, p.getName(), p.getProductId(), reason);
		auditLogList.getItems().add(0, entry);
	}

	private void hideReviewPanel() {
		reviewPanel.setVisible(false);
		reviewPanel.setManaged(false);
		listingsTable.getSelectionModel().clearSelection();
		rejectionReasonField.clear();
	}

	// ═══════════════════════════════════════════════════════════
	// VIEW TRANSACTIONS
	// ═══════════════════════════════════════════════════════════

	private void setupTransactionsTable() {
		txIdCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTransactionId()));
		txBookingIdCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getBookingId()));
		txProductCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
		txRenterCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRenterName()));
		txOwnerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerName()));
		txAmountCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTotalAmount()));
		txStatusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
		txCreatedAtCol.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().toLocalDate().toString() : "—"));
	}

	private void setupTxStatusFilter() {
		txStatusFilter.setItems(FXCollections.observableArrayList("All", "Paid", "Completed", "Returned", "Pending"));
		txStatusFilter.setValue("All");
	}

	private void setupTxRowSelection() {
		transactionsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
			if (selected == null) {
				txDetailPanel.setVisible(false);
				txDetailPanel.setManaged(false);
			} else {
				populateTxDetailPanel(selected);
				txDetailPanel.setVisible(true);
				txDetailPanel.setManaged(true);
			}
		});
	}

	private void populateTxDetailPanel(Transaction t) {
		txDetailIdLabel.setText("Transaction #" + t.getTransactionId());
		detailsLabel.setText(String.format(
				"Transaction ID : %d%n" + "Booking ID     : %d%n" + "Product        : %s%n" + "Renter         : %s%n"
						+ "Owner          : %s%n" + "Amount         : %.2f PKR%n" + "Status         : %s%n"
						+ "Created At     : %s",
				t.getTransactionId(), t.getBookingId(), t.getProductName(), t.getRenterName(), t.getOwnerName(),
				t.getTotalAmount(), t.getStatus(), t.getCreatedAt() != null ? t.getCreatedAt().toLocalDate() : "—"));
	}

	private void loadAllTransactions() {
		if (transactionService == null)
			return;
		List<Transaction> all = transactionService.getAllTransactionsForAdmin();
		allTransactions.setAll(all != null ? all : List.of());
		transactionsTable.setItems(allTransactions);
		updateStats();
	}

	private void updateStats() {
		if (allTransactions.isEmpty()) {
			statsLabel.setText("Total: 0  |  Paid: 0  |  Completed: 0");
			return;
		}
		long total = allTransactions.size();
		long paid = allTransactions.stream()
				.filter(t -> "Paid".equals(t.getStatus()) || "Completed".equals(t.getStatus())).count();
		long completed = allTransactions.stream()
				.filter(t -> "Completed".equals(t.getStatus()) || "Returned".equals(t.getStatus())).count();
		statsLabel.setText(String.format("Total: %d  |  Paid: %d  |  Completed: %d", total, paid, completed));
	}

	@FXML
	private void onSearchTransactions() {
		String keyword = txSearchField.getText().trim().toLowerCase();
		String filter = txStatusFilter.getValue();

		var filtered = allTransactions.stream().filter(t -> {
			boolean kw = keyword.isEmpty() || t.getProductName().toLowerCase().contains(keyword)
					|| t.getRenterName().toLowerCase().contains(keyword)
					|| t.getOwnerName().toLowerCase().contains(keyword);
			boolean st = "All".equalsIgnoreCase(filter) || t.getStatus().equalsIgnoreCase(filter);
			return kw && st;
		}).toList();

		transactionsTable.setItems(FXCollections.observableArrayList(filtered));
		txDetailPanel.setVisible(false);
		txDetailPanel.setManaged(false);
	}

	@FXML
	private void onRefreshTransactions() {
		txSearchField.clear();
		txStatusFilter.setValue("All");
		loadAllTransactions();
		txDetailPanel.setVisible(false);
		txDetailPanel.setManaged(false);
	}

	// ═══════════════════════════════════════════════════════════
	// RENTAL HISTORY
	// ═══════════════════════════════════════════════════════════

	private void setupHistoryClickHandlers() {
		renterHistoryList.setOnMouseClicked(e -> {
			String selected = renterHistoryList.getSelectionModel().getSelectedItem();
			if (selected != null && !selected.contains("No"))
				showHistoryDetails(selected, renterBookingsList);
		});
		ownerHistoryList.setOnMouseClicked(e -> {
			String selected = ownerHistoryList.getSelectionModel().getSelectedItem();
			if (selected != null && !selected.contains("No"))
				showHistoryDetails(selected, ownerBookingsList);
		});
	}

	private void loadRentalHistory() {
		if (bookingService == null) {
			renterHistoryList.getItems().setAll("Service not available");
			ownerHistoryList.getItems().setAll("Service not available");
			return;
		}

		renterBookingsList.clear();
		ownerBookingsList.clear();
		renterHistoryList.getItems().clear();
		ownerHistoryList.getItems().clear();

		List<Booking> renterBookings = bookingService.getRentalHistoryForRenter(-1);
		if (renterBookings.isEmpty()) {
			renterHistoryList.getItems().add("No renter bookings found.");
		} else {
			for (Booking b : renterBookings) {
				String line = String.format("#%d | %s → %s | %.0f PKR | %s", b.getBookingId(), b.getRenter().getName(),
						b.getProduct().getName(), b.getTotalPrice(), b.getStatus());
				renterHistoryList.getItems().add(line);
			}
			renterBookingsList.addAll(renterBookings);
		}

		List<Booking> ownerBookings = bookingService.getRentalHistoryForOwner(-1);
		if (ownerBookings.isEmpty()) {
			ownerHistoryList.getItems().add("No owner rentals found.");
		} else {
			for (Booking b : ownerBookings) {
				String line = String.format("#%d | %s ← %s | %.0f PKR | %s", b.getBookingId(), b.getProduct().getName(),
						b.getRenter().getName(), b.getTotalPrice(), b.getStatus());
				ownerHistoryList.getItems().add(line);
			}
			ownerBookingsList.addAll(ownerBookings);
		}
	}

	private void showHistoryDetails(String line, List<Booking> list) {
		try {
			int id = Integer.parseInt(line.substring(1, line.indexOf(" ", 1)));
			Booking b = list.stream().filter(booking -> booking.getBookingId() == id).findFirst().orElse(null);

			if (b == null) {
				historyDetailsLabel.setText("Booking not found.");
				return;
			}

			String details = String.format("""
					Booking ID: %d
					Product: %s
					Renter: %s
					Owner: %s
					Amount: %.0f PKR
					Status: %s
					Period: %s to %s
					Booked on: %s
					""", b.getBookingId(), b.getProduct().getName(), b.getRenter().getName(),
					b.getProductOwner().getName(), b.getTotalPrice(), b.getStatus(), b.getStartDate(), b.getEndDate(),
					b.getCreatedAt() != null ? b.getCreatedAt().toLocalDate() : "Unknown");

			historyDetailsLabel.setText(details);
		} catch (Exception ex) {
			historyDetailsLabel.setText("Error loading details.");
		}
	}

	// ═══════════════════════════════════════════════════════════
	// STATISTICS AND REPORTS
	// ═══════════════════════════════════════════════════════════

	private void setupReportControls() {
		reportTypeChoice.setItems(FXCollections.observableArrayList("Revenue Summary", "Total Bookings",
				"Revenue by Owner", "Most Rented Products", "Monthly Revenue Trend", "Platform Summary"));

		periodChoice.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));

		reportTypeChoice.getSelectionModel().selectFirst();
		periodChoice.getSelectionModel().select("Monthly");
	}

	@FXML
	private void onGenerateReport() {
		if (reportService == null) {
			reportTextArea.setText("Report service not available.");
			return;
		}

		String type = switch (reportTypeChoice.getValue()) {
		case "Revenue Summary" -> "revenue";
		case "Total Bookings" -> "bookings";
		case "Revenue by Owner" -> "ownerrevenue";
		case "Most Rented Products" -> "mostrented";
		case "Monthly Revenue Trend" -> "monthlyrevenue";
		case "Platform Summary" -> "platformsummary";
		default -> "revenue";
		};

		String period = periodChoice.getValue().toLowerCase();
		Map<String, Object> report = reportService.generateReport(type, period);

		reportTitleLabel.setText(String.format("%s Report (%s)", reportTypeChoice.getValue(), periodChoice.getValue()));
		reportTextArea.setText(formatReport(report));
	}

	private String formatReport(Map<String, Object> report) {
		StringBuilder sb = new StringBuilder();

		String type = (String) report.getOrDefault("type", "unknown");
		String period = (String) report.getOrDefault("period", "all time");
		String from = (String) report.getOrDefault("from", "N/A");
		String to = (String) report.getOrDefault("to", LocalDate.now().toString());

		sb.append("══════════════════════════════════════\n");
		sb.append("     RENTEASE PLATFORM REPORT         \n");
		sb.append("══════════════════════════════════════\n\n");
		sb.append(String.format("Report Type       : %s\n", formatReportTitle(type)));
		sb.append(String.format("Period            : %s (%s → %s)\n", period.toUpperCase(), from, to));
		sb.append(String.format("Generated on      : %s\n\n", LocalDate.now()));
		sb.append("──────────────────────────────────────\n\n");

		Object data = report.get("data");

		switch (type.toLowerCase()) {
		case "revenue" -> {
			double revenue = getDouble(data, 0.0);
			sb.append(String.format("TOTAL PLATFORM REVENUE: %.2f PKR\n", revenue));
		}

		case "bookings" -> {
			int count = getInt(data, 0);
			sb.append(String.format("TOTAL COMPLETED BOOKINGS: %d\n", count));
		}

		case "platformsummary" -> {
			@SuppressWarnings("unchecked")
			Map<String, Object> s = data instanceof Map ? (Map<String, Object>) data : Map.of();
			sb.append("PLATFORM OVERVIEW\n");
			sb.append("────────────────────────────\n");
			sb.append(String.format("Total Active Renters    : %d\n", getLong(s, "totalRenters")));
			sb.append(String.format("Total Active Owners     : %d\n", getLong(s, "totalOwners")));
			sb.append(String.format("Available Products      : %d\n", getLong(s, "availableProducts")));
			sb.append(String.format("Active Rentals          : %d\n", getLong(s, "activeBookings")));
			sb.append(String.format("Total Earnings (10%%)    : %.2f PKR\n", getDouble(s.get("totalEarnings"), 0.0)));
		}

		case "ownerrevenue" -> {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = data instanceof List ? (List<Map<String, Object>>) data : List.of();
			if (list.isEmpty()) {
				sb.append("No revenue recorded from owners yet.\n");
			} else {
				sb.append("REVENUE BY OWNER (Platform 10%% Fee)\n");
				sb.append("────────────────────────────────────\n");
				double total = 0;
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> row = list.get(i);
					String owner = getString(row, "owner", "Unknown Owner");
					double revenue = getDouble(row.get("revenue"), 0.0);
					total += revenue;
					sb.append(String.format(" %2d. %-28s → %.2f PKR\n", i + 1, owner, revenue));
				}
				sb.append("────────────────────────────────────\n");
				sb.append(String.format(" TOTAL PLATFORM EARNINGS     : %.2f PKR\n", total));
			}
		}

		case "mostrented" -> {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = data instanceof List ? (List<Map<String, Object>>) data : List.of();
			if (list.isEmpty()) {
				sb.append("No products have been rented yet.\n");
			} else {
				sb.append("TOP 10 MOST RENTED PRODUCTS\n");
				sb.append("────────────────────────────────────\n");
				for (Map<String, Object> row : list) {
					int rank = getInt(row.get("rank"), 0);
					String name = getString(row, "name", "Unknown Product");
					String typeName = getString(row, "type", "N/A");
					int count = getInt(row.get("count"), 0);
					sb.append(String.format(" #%d │ %-22s (%s) → %d times rented\n", rank, name, typeName, count));
				}
			}
		}

		case "monthlyrevenue" -> {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = data instanceof List ? (List<Map<String, Object>>) data : List.of();
			sb.append("MONTHLY REVENUE TREND (10%% Fee)\n");
			sb.append("────────────────────────────────────\n");
			if (list.isEmpty()) {
				sb.append("No monthly revenue data available.\n");
			} else {
				for (Map<String, Object> row : list) {
					String month = getString(row, "month", "Unknown");
					double revenue = getDouble(row.get("revenue"), 0.0);
					sb.append(String.format(" %s → %.2f PKR\n", month, revenue));
				}
			}
		}

		default -> sb.append("Report type not supported yet.\n");
		}

		sb.append("\n════════════════ END OF REPORT ════════════════\n");
		return sb.toString();
	}

	// ────────────────── SAFE GETTERS ──────────────────

	private String getString(Map<String, Object> map, String key, String fallback) {
		Object val = map.get(key);
		return (val != null) ? val.toString() : fallback;
	}

	private double getDouble(Object obj, double fallback) {
		if (obj instanceof Number n)
			return n.doubleValue();
		return fallback;
	}

	private int getInt(Object obj, int fallback) {
		if (obj instanceof Number n)
			return n.intValue();
		return fallback;
	}

	private long getLong(Map<String, Object> map, String key) {
		return getLong(map.get(key), 0L);
	}

	private long getLong(Object obj, long fallback) {
		if (obj instanceof Number n)
			return n.longValue();
		return fallback;
	}

	private String formatReportTitle(String type) {
		return switch (type.toLowerCase()) {
		case "revenue" -> "REVENUE SUMMARY";
		case "ownerrevenue" -> "REVENUE BY OWNER";
		case "mostrented" -> "MOST RENTED PRODUCTS";
		case "monthlyrevenue" -> "MONTHLY REVENUE TREND";
		case "platformsummary" -> "PLATFORM SUMMARY";
		case "bookings" -> "TOTAL BOOKINGS";
		default -> type.toUpperCase();
		};
	}

	// ═══════════════════════════════════════════════════════════
	// DEPENDENCY INJECTION
	// ═══════════════════════════════════════════════════════════

	public void setTransactionService(TransactionService ts) {
		this.transactionService = ts;
		Platform.runLater(this::loadAllTransactions);
	}

	public void setProductService(ProductService ps) {
		this.productService = ps;
		Platform.runLater(this::loadAllProducts);
	}

	public void setBookingService(BookingService bs) {
		this.bookingService = bs;
		Platform.runLater(() -> {
			loadRentalHistory();
			System.out.println("BookingService injected → Rental history loaded");
		});
	}

	public void setReportService(ReportService rs) {
		this.reportService = rs;
		Platform.runLater(() -> {
			if (reportTypeChoice != null && periodChoice != null) {
				reportTypeChoice.getSelectionModel().select("Revenue by Owner");
				periodChoice.getSelectionModel().select("Monthly");
				onGenerateReport();
			}
		});
	}

	// ═══════════════════════════════════════════════════════════
	// WINDOW CONTROLS
	// ═══════════════════════════════════════════════════════════

	@FXML
	private void onClose() {
		Stage stage = (Stage) usersTable.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void onMinimize() {
		Stage stage = (Stage) usersTable.getScene().getWindow();
		stage.setIconified(true);
	}

	@FXML
	private void onMaximize() {
		Stage stage = (Stage) usersTable.getScene().getWindow();
		stage.setMaximized(!stage.isMaximized());
	}
}