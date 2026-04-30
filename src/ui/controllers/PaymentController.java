package ui.controllers;

import domain.entities.Booking;
import domain.entities.Payment;
import domain.managers.BookingService;
import domain.managers.PaymentService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ui.UiUtils;

public class PaymentController {

	@FXML
	private Label amountLabel;

	@FXML
	private ChoiceBox<String> methodChoice;

	@FXML
	private Button payButton;

	private double paymentAmount;

	private PaymentService paymentService;
	private BookingService bookingService;

	private Booking booking;
	private Payment payment;

	public void setPaymentService(PaymentService s) {
		this.paymentService = s;
	}

	public void setBookingService(BookingService s) {
		this.bookingService = s;
	}

	public void setBooking(Booking b) {
		this.booking = b;
	}

	public void setPayment(Payment p) {
		this.payment = p;
	}

	public void setPaymentAmount(double a) {
		this.paymentAmount = a;
		amountLabel.setText("Amount: " + paymentAmount + " PKR");
	}

	@FXML
	public void initialize() {
		// Setup available payment methods
		methodChoice.setItems(FXCollections.observableArrayList("Credit Card", "JazzCash"));
		methodChoice.getSelectionModel().selectFirst();
	}

	@FXML
	public void onPay(ActionEvent event) {
		if (payment == null || booking == null || paymentService == null || bookingService == null) {
			UiUtils.showError("Error", "Required services or data not initialized.");
			return;
		}

		String method = methodChoice.getValue();
		if (method == null) {
			UiUtils.showError("Error", "Please select a payment method.");
			return;
		}

		// Use injected service, NOT static getInstance()!
		boolean paymentSuccess = paymentService.processPayment(payment, method);

		if (paymentSuccess) {
			boolean bookingSaved = bookingService.createBooking(booking, payment); // This now actually saves!

			if (bookingSaved) {
				UiUtils.showInfo("Success", "Booking and payment completed successfully!");
				((Stage) payButton.getScene().getWindow()).close();
			} else {
				UiUtils.showError("Error", "Booking failed after successful payment. Contact support.");
			}
		} else {
			UiUtils.showError("Payment Failed", "Payment could not be processed. Please try again.");
		}
	}
}
