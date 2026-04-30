package domain.entities;

import java.time.LocalDateTime;

public class Review {
	private int reviewId;
	private int bookingId;
	private int reviewerUserId;
	private String reviewType; // "RenterToOwner" or "OwnerToRenter"
	private int rating; // 1-5
	private String comment;
	private int ratedUserId;
	private LocalDateTime createdAt;

	public Review() {
	}

	// Getters & Setters
	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public int getBookingId() {
		return bookingId;
	}

	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}

	public int getReviewerUserId() {
		return reviewerUserId;
	}

	public void setReviewerUserId(int reviewerUserId) {
		this.reviewerUserId = reviewerUserId;
	}

	public String getReviewType() {
		return reviewType;
	}

	public void setReviewType(String reviewType) {
		this.reviewType = reviewType;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getRatedUserId() {
		return ratedUserId;
	}

	public void setRatedUserId(int ratedUserId) {
		this.ratedUserId = ratedUserId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setReviewDate(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return String.format("Review#%d | Rating: %d★ | %s | By User#%d → User#%d", reviewId, rating, reviewType,
				reviewerUserId, ratedUserId);
	}
}