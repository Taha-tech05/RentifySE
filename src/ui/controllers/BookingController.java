package ui.controllers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import domain.entities.Booking;
import domain.entities.Payment;
import domain.entities.Product;
import domain.managers.BookingService;
import domain.managers.PaymentService;
import domain.managers.ProductService; // Added
import domain.users.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import repositories.UserRepository;
import ui.UiUtils;

public class BookingController {

	private BookingService bookingService;
	private PaymentService paymentService;
	private ProductService productService; // Added for availability check
	private Product product;
	private User loggedInUser;
	private final UserRepository userRepo = new UserRepository();

	@FXML
	private DatePicker fromDate;
	@FXML
	private DatePicker toDate;
	@FXML
	private Label infoLabel;
	@FXML
	private Label totalLabel;

	// Added setter for ProductService (called by RenterController)
	public void setProductService(ProductService s) {
		this.productService = s;
	}

	public void setBookingService(BookingService s) {
		this.bookingService = s;
	}

	public void setPaymentService(PaymentService s) {
		this.paymentService = s;
	}

	public void setLoggedInUser(User user) {
		this.loggedInUser = user;
	}

	public void setProduct(Product p) {
		this.product = p;
		if (infoLabel != null)
			infoLabel.setText("Booking: " + p.getName());
		updateTotal();
	}

	@FXML
	private void updateTotal() {
		LocalDate start = fromDate.getValue();
		LocalDate end = toDate.getValue();
		if (start != null && end != null && start.isBefore(end)) {
			long days = ChronoUnit.DAYS.between(start, end);
			double total = days * product.getPricePerDay();
			totalLabel.setText(String.format("Total: %.0f PKR (%d days)", total, days));
		} else {
			totalLabel.setText("Total: 0 PKR");
		}
	}

	@FXML
	public void onConfirm(ActionEvent e) {
		LocalDate start = fromDate.getValue();
		LocalDate end = toDate.getValue();

		// 1. Basic Validation
		if (loggedInUser == null) {
			UiUtils.showError("Login Required", "Please log in.");
			return;
		}
		if (start == null || end == null || !start.isBefore(end)) {
			UiUtils.showError("Invalid Dates", "Please select a valid date range.");
			return;
		}

		// ═══════════════════════════════════════════
		// 2. THE AVAILABILITY GATEKEEPER
		// ═══════════════════════════════════════════
		// Call the ProductService to check for overlapping bookings in the DB
		boolean isAvailable = productService.checkAvailability(product.getProductId(), start, end);

		if (!isAvailable) {
			// CRITICAL: Stop the flow here. Do not open the payment window.
			UiUtils.showError("Product Unavailable",
					"Sorry, this item is already booked for the selected dates. Please try another range.");
			return;
		}

		// 3. Resolve Owner
		User owner = userRepo.findById(product.getOwnerUserId());
		if (owner == null) {
			UiUtils.showError("Error", "Product owner could not be identified.");
			return;
		}

		// 4. Prepare Data for Handoff
		double amount = ChronoUnit.DAYS.between(start, end) * product.getPricePerDay();

		Booking booking = new Booking();
		booking.setProduct(product);
		booking.setRenter(loggedInUser);
		booking.setProductOwner(owner); // Set resolved owner
		booking.setStartDate(start);
		booking.setEndDate(end);
		booking.setTotalPrice(amount);
		booking.setStatus("In_Progress");

		Payment payment = new Payment();
		payment.setAmount(amount);
		payment.setStatus("Pending");

		// 5. Transition to Payment
		openPaymentWindow(booking, payment, amount);
	}

	private void openPaymentWindow(Booking b, Payment p, double amt) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/payment_view.fxml"));
			Parent root = loader.load();

			PaymentController pc = loader.getController();
			pc.setBooking(b);
			pc.setPayment(p);

			// Use Singletons if local references are null
			pc.setPaymentService(paymentService != null ? paymentService : PaymentService.getInstance());
			pc.setBookingService(bookingService != null ? bookingService : BookingService.getInstance());
			pc.setPaymentAmount(amt);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Secure Payment");
			stage.setScene(new Scene(root));
			stage.showAndWait();

			// Close the current booking dialog after payment attempt
			((Stage) fromDate.getScene().getWindow()).close();
		} catch (Exception ex) {
			ex.printStackTrace();
			UiUtils.showError("System Error", "The payment module failed to launch.");
		}
	}
}