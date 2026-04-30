package ui.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import domain.entities.Booking;
import domain.entities.Payment;
import domain.entities.Product;
import domain.managers.BookingService;
import domain.managers.PaymentService;
import domain.managers.ProductService;
import domain.users.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import repositories.BookingRepository;
import repositories.PaymentRepository;
import ui.UiUtils;

public class RenterController {

	// ═══════════════════════════════════════════
	// SERVICES & STATE
	// ═══════════════════════════════════════════

	private ProductService productService;
	private BookingService bookingService;
	private PaymentService paymentService;
	private BookingRepository bookingRepo;
	private PaymentRepository paymentRepo;

	private User loggedInUser;
	private int renterId = -1;
	private String userName = "Guest";

	private Booking selectedReturnBooking; // tracks which booking is being returned

	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

	// ═══════════════════════════════════════════
	// FXML — SHARED
	// ═══════════════════════════════════════════

	@FXML
	private Label userNameLabel;
	@FXML
	private Label avatarLabel;
	@FXML
	private Label pageTitle;
	@FXML
	private Label pageSubtitle;

	// sidebar nav items (for active-state highlighting)
	@FXML
	private HBox navDashboard;
	@FXML
	private HBox navBookings;

	// page containers
	@FXML
	private VBox dashboardPage;
	@FXML
	private VBox bookingsPage;

	// ═══════════════════════════════════════════
	// FXML — DASHBOARD PAGE
	// ═══════════════════════════════════════════

	@FXML
	private TextField searchField;
	@FXML
	private GridPane productGrid;
	@FXML
	private Label statusLabel;

	// ═══════════════════════════════════════════
	// FXML — BOOKINGS PAGE
	// ═══════════════════════════════════════════

	@FXML
	private Label bookingCountLabel;

	@FXML
	private TableView<Booking> bookingTable;
	@FXML
	private TableColumn<Booking, String> productCol;
	@FXML
	private TableColumn<Booking, String> startCol;
	@FXML
	private TableColumn<Booking, String> endCol;
	@FXML
	private TableColumn<Booking, String> priceCol;
	@FXML
	private TableColumn<Booking, String> statusCol;
	@FXML
	private TableColumn<Booking, String> paymentCol;
	@FXML
	private TableColumn<Booking, Void> actionCol;

	// ═══════════════════════════════════════════
	// FXML — RETURN PANEL
	// ═══════════════════════════════════════════

	@FXML
	private VBox returnPanel;
	@FXML
	private Label returnBookingIdLabel;
	@FXML
	private Label returnProductName;
	@FXML
	private Label returnStartDate;
	@FXML
	private Label returnEndDate;
	@FXML
	private Label returnTotalPrice;
	@FXML
	private Label earlyReturnWarning;
	@FXML
	private TextArea conditionNotesField;
	@FXML
	private Label conditionNotesError;
	@FXML
	private Button confirmReturnBtn;
	@FXML
	private Button cancelReturnBtn;
	@FXML
	private Label returnSuccessLabel;

	// ═══════════════════════════════════════════
	// INITIALIZE
	// ═══════════════════════════════════════════

	@FXML
	public void initialize() {
		setupBookingColumns();
	}

	// ═══════════════════════════════════════════
	// DEPENDENCY INJECTION
	// ═══════════════════════════════════════════

	public void setProductService(ProductService s) {
		this.productService = s;
	}

	public void setBookingService(BookingService s) {
		this.bookingService = s;
		this.bookingRepo = s.getRepo(); // pull repo from service
	}

	public void setPaymentService(PaymentService s) {
		this.paymentService = s;
		this.paymentRepo = s.getRepo(); // pull repo from service
	}

	/** Called by LoginController after authentication. */
	public void setLoggedInUser(User user) {
		this.loggedInUser = user;
		if (user != null) {
			this.renterId = user.getUserId();
			this.userName = user.getEmail(); // no Name column — use email
			if (userNameLabel != null)
				userNameLabel.setText(userName);
			if (avatarLabel != null)
				avatarLabel.setText(String.valueOf(userName.charAt(0)).toUpperCase());
		}
	}

	// ═══════════════════════════════════════════
	// DASHBOARD — PRODUCT GRID
	// ═══════════════════════════════════════════

	public void loadInitialProducts() {
		if (productService == null)
			return;

		List<Product> list = productService.getAvailableProducts().stream().filter(Product::isApproved).limit(12)
				.toList();

		updateGrid(list);
		statusLabel.setText("Showing " + list.size() + " available products");
	}

	@FXML
	private void onSearchClicked(ActionEvent e) {
		if (productService == null)
			return;
		String query = searchField.getText().trim();
		List<Product> results = productService.searchProducts(query, "", "");
		updateGrid(results);
		statusLabel.setText("Found " + results.size() + " results for '" + query + "'");
	}

	private void updateGrid(List<Product> products) {
		productGrid.getChildren().clear();
		int col = 0, row = 0;
		for (Product p : products) {
			productGrid.add(createProductCard(p), col, row);
			if (++col > 2) {
				col = 0;
				row++;
			}
		}
	}

	private VBox createProductCard(Product p) {
		VBox card = new VBox(12);
		card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; "
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4); -fx-cursor: hand;");

		Label name = new Label(p.getName());
		name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

		Label type = new Label(p.getType());
		type.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13px;");

		Label price = new Label(String.format("%.0f PKR / day", p.getPricePerDay()));
		price.setStyle("-fx-text-fill: #3498db; -fx-font-size: 19px; -fx-font-weight: 800;");

		Button bookBtn = new Button("View Availability");
		bookBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); "
				+ "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20;");

		bookBtn.setOnAction(e -> {
			Product fresh = productService.getProductById(p.getProductId());
			if (fresh == null || !fresh.isAvailable()) {
				UiUtils.showError("Not Available", "This product is no longer available.");
				loadInitialProducts();
				return;
			}
			openProductView(fresh);
		});

		card.getChildren().addAll(name, type, price, bookBtn);
		return card;
	}

	private void openProductView(Product product) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/booking_dialog.fxml"));
			Parent root = loader.load();

			BookingController ctrl = loader.getController();
			ctrl.setProductService(productService);
			ctrl.setBookingService(bookingService);
			ctrl.setPaymentService(paymentService);
			ctrl.setProduct(product);
			ctrl.setLoggedInUser(loggedInUser);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Booking: " + product.getName());
			stage.setScene(new Scene(root));
			stage.showAndWait();

			loadInitialProducts(); // refresh grid on return
		} catch (Exception ex) {
			ex.printStackTrace();
			UiUtils.showError("UI Error", "Could not load booking dialog.");
		}
	}

	// ═══════════════════════════════════════════
	// BOOKINGS PAGE — TABLE
	// ═══════════════════════════════════════════

	private void setupBookingColumns() {
		productCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProduct().getName()));
		startCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStartDate().format(fmt)));
		endCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEndDate().format(fmt)));
		priceCol.setCellValueFactory(
				cell -> new SimpleStringProperty(String.format("%.0f PKR", cell.getValue().getTotalPrice())));
		statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
		paymentCol.setCellValueFactory(
				cell -> new SimpleStringProperty(getPaymentStatus(cell.getValue().getBookingId())));

		actionCol.setCellFactory(col -> new TableCell<>() {
			private final Button cancelBtn = new Button("Cancel");
			private final Button returnBtn = new Button("Return Product");
			private final HBox hbox = new HBox(8);

			{
				cancelBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; "
						+ "-fx-font-size: 12px; -fx-background-radius: 5; -fx-cursor: hand;");
				returnBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; "
						+ "-fx-font-size: 12px; -fx-background-radius: 5; -fx-cursor: hand;");

				cancelBtn.setOnAction(e -> {
					Booking b = getTableView().getItems().get(getIndex());
					onCancelClicked(b);
				});
				returnBtn.setOnAction(e -> {
					Booking b = getTableView().getItems().get(getIndex());
					showReturnPanel(b);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					return;
				}
				Booking b = getTableRow().getItem();
				hbox.getChildren().clear();

				if ("In_Progress".equals(b.getStatus())) {
					hbox.getChildren().add(cancelBtn);
				}

				if ("Completed".equals(b.getStatus())) {
					hbox.getChildren().add(returnBtn);
				}

				setGraphic(hbox.getChildren().isEmpty() ? null : hbox);
			}
		});
	}

	public void loadBookings() {
		if (renterId <= 0 || bookingRepo == null)
			return;

		List<Booking> bookings = bookingRepo.findByRenter(renterId);
		bookingTable.setItems(FXCollections.observableArrayList(bookings));
		bookingCountLabel.setText("(" + bookings.size() + " booking" + (bookings.size() == 1 ? "" : "s") + ")");

		// hide return panel when reloading
		hideReturnPanel();
	}

	private void onCancelClicked(Booking b) {
		if (!UiUtils.showConfirmation("Cancel Booking", "Are you sure you want to cancel this booking?"))
			return;

		if (bookingService.cancelBooking(b.getBookingId())) {
			UiUtils.showInfo("Cancelled", "Booking cancelled successfully.");
			loadBookings();
		} else {
			UiUtils.showError("Error", "Failed to cancel booking.");
		}
	}

	private String getPaymentStatus(int bookingId) {
		if (paymentRepo == null)
			return "Unknown";
		Payment payment = paymentRepo.findByBookingId(bookingId);
		return payment == null ? "Unpaid" : (payment.getStatus() != null ? payment.getStatus() : "Unknown");
	}

	// ═══════════════════════════════════════════
	// RETURN PANEL
	// ═══════════════════════════════════════════

	private void showReturnPanel(Booking b) {
		selectedReturnBooking = b;

		returnBookingIdLabel.setText("Booking #" + b.getBookingId());
		returnProductName.setText(b.getProduct().getName());
		returnStartDate.setText(b.getStartDate().format(fmt));
		returnEndDate.setText(b.getEndDate().format(fmt));

		// show early return warning if returning before end date
		boolean isEarly = LocalDate.now().isBefore(b.getEndDate());
		earlyReturnWarning.setVisible(isEarly);
		earlyReturnWarning.setManaged(isEarly);

		// reset state
		conditionNotesField.clear();
		conditionNotesError.setVisible(false);
		conditionNotesError.setManaged(false);
		returnSuccessLabel.setVisible(false);
		returnSuccessLabel.setManaged(false);

		returnPanel.setVisible(true);
		returnPanel.setManaged(true);
	}

	private void hideReturnPanel() {
		returnPanel.setVisible(false);
		returnPanel.setManaged(false);
		selectedReturnBooking = null;
		conditionNotesField.clear();
	}

	@FXML
	private void onConfirmReturn(ActionEvent e) {
		if (selectedReturnBooking == null)
			return;

		String notes = conditionNotesField.getText().trim();

		// 1. Check if the field is empty
		if (notes.isEmpty()) {
			// Show the error message
			conditionNotesError.setText("Please describe the item condition before returning.");
			conditionNotesError.setVisible(true);
			conditionNotesError.setManaged(true);

			// Highlight the field and focus it for the user
			conditionNotesField.setStyle("-fx-border-color: #DC2626; -fx-border-width: 2px; -fx-background-radius: 5;");
			conditionNotesField.requestFocus();
			return;
		}

		// 2. Clear error state if input is valid
		conditionNotesError.setVisible(false);
		conditionNotesError.setManaged(false);
		conditionNotesField.setStyle(null); // Reset to default style

		// 3. Process the return
		if (bookingService.initiateReturn(selectedReturnBooking.getBookingId(), notes, 0.0)) {
			returnSuccessLabel.setVisible(true);
			returnSuccessLabel.setManaged(true);

			// Optionally disable the button to prevent double-clicks
			confirmReturnBtn.setDisable(true);

			loadBookings();
		} else {
			UiUtils.showError("Error", "Failed to process return. Please try again.");
		}
	}

	@FXML
	private void onCancelReturn(ActionEvent e) {
		hideReturnPanel();
	}

	// ═══════════════════════════════════════════
	// NAVIGATION — SIDEBAR
	// ═══════════════════════════════════════════

	@FXML
	private void onDashboardClicked(MouseEvent e) {
		showPage("dashboard");
		loadInitialProducts();
	}

	@FXML
	private void onBookingsClicked(MouseEvent e) {
		showPage("bookings");
		loadBookings();
	}

	@FXML
	private void onProfileClicked(MouseEvent e) {
		// placeholder
	}

	@FXML
	private void onLogoutClicked(MouseEvent e) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/login.fxml"));
			Parent root = loader.load();

			LoginController loginCtrl = loader.getController();
			loginCtrl.setProductService(productService);
			loginCtrl.setBookingService(bookingService);
			loginCtrl.setPaymentService(paymentService);

			Stage stage = (Stage) userNameLabel.getScene().getWindow();
			stage.setScene(new Scene(root, 1000, 700));
			stage.setTitle("Rentify - Login");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// ─── helper: swap visible page + highlight nav ───

	private void showPage(String page) {
		boolean isDash = "dashboard".equals(page);

		dashboardPage.setVisible(isDash);
		dashboardPage.setManaged(isDash);
		bookingsPage.setVisible(!isDash);
		bookingsPage.setManaged(!isDash);

		// update page header
		if (isDash) {
			pageTitle.setText("Dashboard");
			pageSubtitle.setText("Find your perfect rental product");
		} else {
			pageTitle.setText("My Bookings");
			pageSubtitle.setText("View and manage your rental bookings");
		}

		// highlight active nav item
		String activeStyle = "-fx-padding: 14 18; -fx-background-radius: 10; "
				+ "-fx-background-color: rgba(255,255,255,0.15); -fx-cursor: hand;";
		String inactiveStyle = "-fx-padding: 14 18; -fx-background-radius: 10; "
				+ "-fx-background-color: transparent; -fx-cursor: hand;";

		navDashboard.setStyle(isDash ? activeStyle : inactiveStyle);
		navBookings.setStyle(!isDash ? activeStyle : inactiveStyle);
	}

	@FXML
	private void onClose() {
		((Stage) searchField.getScene().getWindow()).close();
	}

	@FXML
	private void onMinimize() {
		((Stage) searchField.getScene().getWindow()).setIconified(true);
	}

	@FXML
	private void onMaximize() {
		Stage s = (Stage) searchField.getScene().getWindow();
		s.setMaximized(!s.isMaximized());
	}

}