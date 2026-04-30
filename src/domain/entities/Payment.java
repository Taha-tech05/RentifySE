package domain.entities;

import java.time.LocalDateTime;

public class Payment {
	private int paymentId;
	private int bookingId;
	private double amount;
	private String paymentMethod;
	private String status = "Pending";
	private LocalDateTime paymentDate;

	public Payment() {
	}

	// Getters & Setters
	public int getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public int getBookingId() {
		return bookingId;
	}

	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	@Override
	public String toString() {
		return String.format("Payment#%d | Booking#%d | %.0f PKR | %s | %s", paymentId, bookingId, amount,
				paymentMethod, status);
	}
}