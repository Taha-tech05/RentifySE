// domain/entities/Product.java
package domain.entities;

import java.time.LocalDateTime;

public class Product {
	private int productId;
	private String name;
	private String type;
	private double pricePerDay;
	private boolean isAvailable = false;
	private int ownerUserId;
	private String ownerName;
	private String approvedBy; // Admix who approved
	private String approvalStatus = "Pending"; // Pending, Approved, Rejected, Changes_Requested
	private String rejectionReason; // If rejected
	private LocalDateTime moderatedAt;
	private LocalDateTime createdAt;

	// Default constructor
	public Product() {
		this.createdAt = LocalDateTime.now();
	}

	// Constructor to create a product with basic details
	public Product(int ownerUserId, String name, String type, double pricePerDay) {
		this.ownerUserId = ownerUserId;
		this.name = name;
		this.type = type;
		this.pricePerDay = pricePerDay;
		this.isAvailable = false; // default
		this.createdAt = LocalDateTime.now();
		this.approvalStatus = "Pending"; // default
	}

	// ====== GETTERS & SETTERS ======
	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getPricePerDay() {
		return pricePerDay;
	}

	public void setPricePerDay(double pricePerDay) {
		this.pricePerDay = pricePerDay;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean available) {
		this.isAvailable = available;
	}

	public int getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(int ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public LocalDateTime getModeratedAt() {
		return moderatedAt;
	}

	public void setModeratedAt(LocalDateTime moderatedAt) {
		this.moderatedAt = moderatedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	// ====== HELPER METHODS ======
	public boolean isApproved() {
		return "Approved".equalsIgnoreCase(approvalStatus);
	}

	public boolean isPending() {
		return "Pending".equalsIgnoreCase(approvalStatus);
	}

	public boolean isRejected() {
		return "Rejected".equalsIgnoreCase(approvalStatus);
	}

	public String getStatusBadge() {
		return switch (approvalStatus.toLowerCase()) {
		case "approved" -> "LIVE";
		case "pending" -> "PENDING REVIEW";
		case "rejected" -> "REJECTED";
		case "changes_requested" -> "ACTION REQUIRED";
		default -> approvalStatus.toUpperCase();
		};
	}

	@Override
	public String toString() {
		return String.format("%s | %s | %.0f PKR/day | %s | %s", name, type, pricePerDay, getStatusBadge(),
				ownerName != null ? ownerName : "Owner#" + ownerUserId);
	}
}