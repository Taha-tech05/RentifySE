package domain.entities;

import java.time.LocalDateTime;

public class Transaction {

	private int transactionId;
	private int bookingId; // added — maps to SQL BookingId
	private int productId;
	private String productName;
	private int renterId;
	private String renterName;
	private int ownerId;
	private String ownerName;
	private double totalAmount;
	private String status = "Pending"; // Pending, Confirmed, Paid, In_Progress, Completed, Cancelled, Returned
	private LocalDateTime paymentConfirmedAt;
	private LocalDateTime createdAt;

	// Default constructor
	public Transaction() {
		this.createdAt = LocalDateTime.now();
	}

	// ====== GETTERS & SETTERS ======

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public int getBookingId() {
		return bookingId;
	}

	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getRenterId() {
		return renterId;
	}

	public void setRenterId(int renterId) {
		this.renterId = renterId;
	}

	public String getRenterName() {
		return renterName;
	}

	public void setRenterName(String renterName) {
		this.renterName = renterName;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getPaymentConfirmedAt() {
		return paymentConfirmedAt;
	}

	public void setPaymentConfirmedAt(LocalDateTime paymentConfirmedAt) {
		this.paymentConfirmedAt = paymentConfirmedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	// ====== HELPER METHODS ======

	public String getStatusBadge() {
		if (status == null)
			return "PENDING";
		return switch (status.toLowerCase()) {
		case "paid", "confirmed" -> "PAID";
		case "in_progress" -> "IN PROGRESS";
		case "completed" -> "COMPLETED";
		case "cancelled" -> "CANCELLED";
		case "returned" -> "RETURNED";
		default -> "PENDING";
		};
	}

	public boolean isPaid() {
		return paymentConfirmedAt != null;
	}

	public boolean isCompleted() {
		return "Completed".equalsIgnoreCase(status);
	}

	public boolean isReturned() {
		return "Returned".equalsIgnoreCase(status);
	}

	@Override
	public String toString() {
		return String.format("TXN#%d | BK#%d | %s | %.0f PKR | %s | %s → %s", transactionId, bookingId,
				productName != null ? productName : "Product#" + productId, totalAmount, getStatusBadge(),
				renterName != null ? renterName : "Renter#" + renterId,
				ownerName != null ? ownerName : "Owner#" + ownerId);
	}

	public String getDetailedSummary() {
		return String.format("""
				Transaction ID : %d
				Booking ID     : %d
				Product        : %s
				Amount         : %.2f PKR
				Status         : %s
				Renter         : %s
				Owner          : %s
				Created At     : %s
				Payment At     : %s
				""", transactionId, bookingId, productName, totalAmount, getStatusBadge(), renterName, ownerName,
				createdAt != null ? createdAt.toLocalDate() : "N/A",
				paymentConfirmedAt != null ? paymentConfirmedAt.toLocalDate() : "Not paid");
	}
}