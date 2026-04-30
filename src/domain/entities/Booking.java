// domain/entities/Booking.java
package domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import domain.users.User;

public class Booking {
	private int bookingId;
	private Product product; // Full Product object (with Name, PricePerDay)
	private User renter; // Who is renting
	private User productOwner; // Owner of the product (CRITICAL!)
	private LocalDate startDate;
	private LocalDate endDate;
	private double totalPrice;
	private String status = "Pending"; // Pending, Confirmed, In_Progress, Completed, Cancelled, etc.
	private LocalDateTime createdAt;

	// Default constructor
	public Booking() {
	}

	// Constructor used when creating a new booking
	public Booking(Product product, User renter, User productOwner, LocalDate startDate, LocalDate endDate,
			double totalPrice) {
		this.product = product;
		this.renter = renter;
		this.productOwner = productOwner;
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalPrice = totalPrice;
		this.status = "Pending";
	}

	// ====== GETTERS & SETTERS ======
	public int getBookingId() {
		return bookingId;
	}

	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public User getRenter() {
		return renter;
	}

	public void setRenter(User renter) {
		this.renter = renter;
	}

	public User getProductOwner() {
		return productOwner;
	}

	public void setProductOwner(User productOwner) {
		this.productOwner = productOwner;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	// ====== HELPER METHODS FOR UI & LOGIC ======
	public String getStatusBadge() {
		return switch (status == null ? "" : status.toLowerCase()) {
		case "confirmed" -> "Confirmed";
		case "in_progress" -> "In Progress";
		case "completed" -> "Completed";
		case "cancelled" -> "Cancelled";
		case "rejected" -> "Rejected";
		case "awaiting_payment" -> "Awaiting Payment";
		default -> "Pending";
		};
	}

	public boolean isPending() {
		return "Pending".equalsIgnoreCase(status);
	}

	public boolean isConfirmed() {
		return "Confirmed".equalsIgnoreCase(status);
	}

	public boolean isCompleted() {
		return "Completed".equalsIgnoreCase(status);
	}

	@Override
	public String toString() {
		String productName = product != null ? product.getName()
				: "Product#" + (product != null ? product.getProductId() : "?");
		String renterName = renter != null ? renter.getName() : "Renter#" + (renter != null ? renter.getUserId() : "?");
		String ownerName = productOwner != null ? productOwner.getName()
				: "Owner#" + (productOwner != null ? productOwner.getUserId() : "?");

		return String.format("Booking#%d | %s | %s → %s | %.0f PKR | %s | %s → %s", bookingId, productName, startDate,
				endDate, totalPrice, getStatusBadge(), renterName, ownerName);
	}
}