package ui.controllers;

import java.util.List;

import domain.entities.Product;
import domain.entities.Review;
import domain.managers.BookingService;
import domain.managers.ProductService;
import domain.managers.ReviewService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OwnerController {

	// ═══════════════════════════════════════════════════════════
	// SERVICES & STATE
	// ═══════════════════════════════════════════════════════════

	private ProductService productService;
	private ReviewService reviewService;
	private BookingService bookingService;
	private int ownerUserId;
	private String ownerName;
	private int selectedRating = 0;

	// ═══════════════════════════════════════════════════════════
	// FXML — PRODUCTS PAGE
	// ═══════════════════════════════════════════════════════════

	@FXML
	private VBox productsPage;
	@FXML
	private Label welcomeLabel;
	@FXML
	private VBox productContainer;
	@FXML
	private TextField searchBar;

	// ═══════════════════════════════════════════════════════════
	// FXML — REVIEWS PAGE
	// ═══════════════════════════════════════════════════════════

	@FXML
	private VBox reviewsPage;
	@FXML
	private VBox reviewBox;
	@FXML
	private VBox reviewContainer;
	@FXML
	private Label reviewCountLabel;
	@FXML
	private Label avgRatingLabel;
	@FXML
	private ChoiceBox<Integer> bookingChoice;
	@FXML
	private TextArea reviewTextField;
	@FXML
	private ChoiceBox<Integer> ratingChoice;
	@FXML
	private Label star1;
	@FXML
	private Label star2;
	@FXML
	private Label star3;
	@FXML
	private Label star4;
	@FXML
	private Label star5;
	@FXML
	private Label ratingHintLabel;
	@FXML
	private Label charCountLabel;
	@FXML
	private Label statusLabel;
	@FXML
	private Button submitReviewBtn;

	// ═══════════════════════════════════════════════════════════
	// DEPENDENCY INJECTION
	// ═══════════════════════════════════════════════════════════

	public void setProductService(ProductService ps) {
		this.productService = ps;
	}

	public void setReviewService(ReviewService rs) {
		this.reviewService = rs;
	}

	public void setBookingService(BookingService bs) {
		this.bookingService = bs;
	}

	public void setOwnerName(String name) {
		this.ownerName = name;
		if (welcomeLabel != null)
			welcomeLabel.setText("Welcome, " + name);
	}

	public void setOwnerUserId(int id) {
		this.ownerUserId = id;
		if (productService != null)
			loadProductInventory();
		if (bookingService != null)
			loadBookingChoices();
		if (reviewService != null)
			loadReceivedReviews();
	}

	public int getOwnerUserId() {
		return this.ownerUserId;
	}

	// ═══════════════════════════════════════════════════════════
	// INITIALIZE
	// ═══════════════════════════════════════════════════════════

	@FXML
	public void initialize() {
		// Product search
		if (searchBar != null)
			searchBar.textProperty().addListener((obs, o, n) -> filterProducts(n));

		// Character counter
		if (reviewTextField != null)
			reviewTextField.textProperty().addListener((obs, o, n) -> {
				int len = n.length();
				if (charCountLabel != null)
					charCountLabel.setText(len + " / 500 characters");
				if (len > 500)
					reviewTextField.setText(o);
			});

		// Stars
		setupStars();

		if (ratingChoice != null)
			ratingChoice.getItems().addAll(1, 2, 3, 4, 5);

		if (reviewBox != null)
			reviewBox.setFillWidth(true);
		if (reviewContainer != null)
			reviewContainer.setFillWidth(true);
	}

	// ═══════════════════════════════════════════════════════════
	// PAGE SWITCHING — sidebar buttons
	// ═══════════════════════════════════════════════════════════

	@FXML
	public void handleViewListings() {
		showPage(true);
	}

	@FXML
	public void handleViewReviews() {
		// Load fresh data every time the page is opened
		if (bookingService != null)
			loadBookingChoices();
		if (reviewService != null)
			loadReceivedReviews();
		showPage(false);
	}

	private void showPage(boolean showProducts) {
		if (productsPage != null) {
			productsPage.setVisible(showProducts);
			productsPage.setManaged(showProducts);
		}
		if (reviewsPage != null) {
			reviewsPage.setVisible(!showProducts);
			reviewsPage.setManaged(!showProducts);
		}
	}

	// ═══════════════════════════════════════════════════════════
	// WINDOW CONTROLS
	// ═══════════════════════════════════════════════════════════

	@FXML
	private void onClose() {
		Stage s = getStage();
		if (s != null)
			s.close();
	}

	@FXML
	private void onMinimize() {
		Stage s = getStage();
		if (s != null)
			s.setIconified(true);
	}

	@FXML
	private void onMaximize() {
		Stage s = getStage();
		if (s != null)
			s.setMaximized(!s.isMaximized());
	}

	private Stage getStage() {
		if (welcomeLabel != null && welcomeLabel.getScene() != null)
			return (Stage) welcomeLabel.getScene().getWindow();
		if (productContainer != null && productContainer.getScene() != null)
			return (Stage) productContainer.getScene().getWindow();
		return null;
	}

	// ═══════════════════════════════════════════════════════════
	// PRODUCTS
	// ═══════════════════════════════════════════════════════════

	public void loadProductInventory() {
		if (productService == null || productContainer == null)
			return;
		productContainer.getChildren().clear();

		List<Product> myProducts = productService.getAllProducts().stream()
				.filter(p -> p.getOwnerUserId() == ownerUserId).toList();

		if (myProducts.isEmpty()) {
			displayEmptyState();
			return;
		}
		myProducts.forEach(this::renderProductCard);
	}

	public void renderProductCard(Product p) {
		VBox card = new VBox(12);
		card.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 12; "
				+ "-fx-border-color: #e1e8ed; -fx-border-width: 1; -fx-border-radius: 12; "
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3);");

		HBox topRow = new HBox(12);
		topRow.setStyle("-fx-alignment: center-left;");

		Label nameLabel = new Label(p.getName());
		nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

		Label statusBadge = new Label(p.getStatusBadge());
		String badgeColor = switch (p.getApprovalStatus().toLowerCase()) {
		case "approved" -> "#27ae60";
		case "pending" -> "#f39c12";
		case "rejected" -> "#e74c3c";
		default -> "#95a5a6";
		};
		statusBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; "
				+ "-fx-padding: 4 10; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");

		Label availBadge = new Label(p.isAvailable() ? "AVAILABLE" : "RENTED/BUSY");
		availBadge.setStyle("-fx-text-fill: " + (p.isAvailable() ? "#27ae60" : "#e67e22")
				+ "; -fx-font-size: 11px; -fx-font-weight: bold;");

		topRow.getChildren().addAll(nameLabel, statusBadge, availBadge);

		Label detailsLabel = new Label(String.format("%s  |  💰 %.0f PKR/day", p.getType(), p.getPricePerDay()));
		detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

		Button updateBtn = new Button("✏️ Edit");
		updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; "
				+ "-fx-cursor: hand; -fx-background-radius: 8;");
		updateBtn.setOnAction(e -> onUpdateProduct(p));

		Button deleteBtn = new Button("🗑️ Delete");
		deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
				+ "-fx-cursor: hand; -fx-background-radius: 8;");
		deleteBtn.setOnAction(e -> onDeleteProduct(p));

		HBox actions = new HBox(10, updateBtn, deleteBtn);
		card.getChildren().addAll(topRow, detailsLabel, actions);
		productContainer.getChildren().add(card);
	}

	private void filterProducts(String query) {
		if (productService == null || productContainer == null)
			return;
		productContainer.getChildren().clear();
		productService.getAllProducts().stream().filter(
				p -> p.getOwnerUserId() == ownerUserId && p.getName().toLowerCase().contains(query.toLowerCase()))
				.forEach(this::renderProductCard);
	}

	private void displayEmptyState() {
		Label e = new Label("📦 No products found. Click '+ Add New Product' to start.");
		e.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 20;");
		productContainer.getChildren().add(e);
	}

	@FXML
	public void onAddProductClicked() {
		openProductForm(null);
	}

	private void onUpdateProduct(Product p) {
		openProductForm(p);
	}

	private void openProductForm(Product productToEdit) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/product_form.fxml"));
			Stage stage = new Stage();
			stage.setScene(new Scene(loader.load()));
			ProductFormController ctrl = loader.getController();
			ctrl.setDependencies(productService, this);
			if (productToEdit != null) {
				ctrl.setEditingProduct(productToEdit);
				stage.setTitle("Update Product");
			} else {
				stage.setTitle("Add New Product");
			}
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onDeleteProduct(Product product) {
		// Original guard: !isAvailable && isApproved → blocks delete when
		// rented+approved
		// But "not available" alone should be enough to block if it's actively rented
		if (!product.isAvailable()) {
			System.out.println("Cannot delete: Item is currently rented.");
			return;
		}
		if (productService.deleteProduct(product.getProductId(), ownerUserId))
			loadProductInventory();
	}

	// ═══════════════════════════════════════════════════════════
	// REVIEWS
	// ═══════════════════════════════════════════════════════════

	private void setupStars() {
		Label[] stars = { star1, star2, star3, star4, star5 };
		for (int i = 0; i < stars.length; i++) {
			if (stars[i] == null)
				continue;
			final int rating = i + 1;
			stars[i].setOnMouseClicked(e -> setStarRating(rating));
			stars[i].setOnMouseEntered(e -> highlightStars(rating));
			stars[i].setOnMouseExited(e -> highlightStars(selectedRating));
		}
	}

	private void setStarRating(int rating) {
		selectedRating = rating;
		if (ratingChoice != null)
			ratingChoice.setValue(rating);
		highlightStars(rating);
		if (ratingHintLabel != null)
			ratingHintLabel.setText(rating + " star" + (rating > 1 ? "s" : "") + " selected");
	}

	private void highlightStars(int upTo) {
		Label[] stars = { star1, star2, star3, star4, star5 };
		for (int i = 0; i < stars.length; i++) {
			if (stars[i] == null)
				continue;
			stars[i].setStyle("-fx-font-size: 30px; -fx-cursor: hand; -fx-text-fill: "
					+ (i < upTo ? "#f39c12" : "#bdc3c7") + ";");
		}
	}

	private void loadBookingChoices() {
		if (bookingService == null || bookingChoice == null)
			return;
		bookingChoice.getItems().clear();
		List<Integer> ids = bookingService.getCompletedBookingIdsForOwner(ownerUserId);
		bookingChoice.getItems().addAll(ids);
		if (!ids.isEmpty())
			bookingChoice.setValue(ids.get(0));
	}

	private void loadReceivedReviews() {
		if (reviewService == null || reviewBox == null)
			return;
		reviewBox.getChildren().clear();

		List<Review> reviews = reviewService.getReviewsForUser(ownerUserId);

		if (reviewCountLabel != null)
			reviewCountLabel.setText(reviews.size() + " review" + (reviews.size() != 1 ? "s" : ""));

		if (avgRatingLabel != null) {
			if (reviews.isEmpty()) {
				avgRatingLabel.setText("⭐ — / 5");
			} else {
				double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
				avgRatingLabel.setText(String.format("⭐ %.1f / 5", avg));
			}
		}

		if (reviews.isEmpty()) {
			Label empty = new Label("📭 No reviews received yet.");
			empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 20;");
			reviewBox.getChildren().add(empty);
			return;
		}

		for (Review r : reviews) {
			VBox card = new VBox(10);
			card.setStyle("-fx-padding: 18; -fx-background-color: white; "
					+ "-fx-background-radius: 10; -fx-border-color: #e1e8ed; "
					+ "-fx-border-width: 1; -fx-border-radius: 10; "
					+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

			Label rating = new Label("⭐".repeat(r.getRating()) + "  " + r.getRating() + "/5");
			rating.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

			Label comment = new Label(r.getComment());
			comment.setWrapText(true);
			comment.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-padding: 6 0;");

			Label byUser = new Label("👤 By User #" + r.getReviewerUserId());
			byUser.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

			card.getChildren().addAll(rating, comment, byUser);
			reviewBox.getChildren().add(card);
		}
	}

	@FXML
	private void onSubmitReview() {
		if (reviewService == null || bookingService == null) {
			setStatus("❌ Service not available.", false);
			return;
		}
		Integer bookingId = bookingChoice != null ? bookingChoice.getValue() : null;
		String text = reviewTextField != null ? reviewTextField.getText().trim() : "";

		if (bookingId == null) {
			setStatus("⚠ Please select a completed booking.", false);
			return;
		}
		if (selectedRating == 0) {
			setStatus("⚠ Please select a star rating.", false);
			return;
		}
		if (text.isBlank()) {
			setStatus("⚠ Please write a review before submitting.", false);
			return;
		}

		int renterUserId = bookingService.getRenterUserIdByBookingId(bookingId);

		Review r = new Review();
		r.setReviewerUserId(ownerUserId);
		r.setRatedUserId(renterUserId);
		r.setBookingId(bookingId);
		r.setComment(text);
		r.setRating(selectedRating);
		r.setReviewType("Owner");
		r.setReviewDate(java.time.LocalDateTime.now());

		if (reviewService.submitReview(r)) {
			setStatus("✅ Review submitted successfully!", true);
			reviewTextField.clear();
			selectedRating = 0;
			highlightStars(0);
			if (ratingHintLabel != null)
				ratingHintLabel.setText("Click a star to rate");
			if (ratingChoice != null)
				ratingChoice.setValue(null);
			loadReceivedReviews();
		} else {
			setStatus("❌ Failed. You may have already reviewed this booking.", false);
		}
	}

	private void setStatus(String msg, boolean success) {
		if (statusLabel == null)
			return;
		statusLabel.setText(msg);
		statusLabel.setStyle(
				"-fx-font-size: 12px; -fx-wrap-text: true; -fx-text-fill: " + (success ? "#27ae60" : "#e74c3c") + ";");
	}

	// ═══════════════════════════════════════════════════════════
	// LOGOUT
	// ═══════════════════════════════════════════════════════════

	@FXML
	private void onLogoutClicked() {
		Stage s = getStage();
		if (s != null)
			s.close();
	}
}