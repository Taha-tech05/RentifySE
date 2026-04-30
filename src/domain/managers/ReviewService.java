package domain.managers;

import java.util.List;

import domain.entities.Review;
import repositories.ReviewRepository;

public class ReviewService {

	private final ReviewRepository repo = new ReviewRepository();

	private static class InstanceHolder {
		private static final ReviewService INSTANCE = new ReviewService();
	}

	private ReviewService() {
	} // Add this to prevent instantiation

	public static ReviewService getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Submit review after validation.
	 */
	public boolean submitReview(Review review) {
		if (review == null)
			return false;

		// Validation: Rating range
		if (review.getRating() < 1 || review.getRating() > 5)
			return false;

		// 3. Logic Check: Prevent self-review
		if (review.getReviewerUserId() == review.getRatedUserId())
			return false;

		// Validation: Prevent duplicates
		if (repo.hasUserReviewedBooking(review.getBookingId(), review.getReviewerUserId())) {
			return false;
		}

		return repo.save(review);
	}

	/**
	 * Get reviews received by a user. (Controller will convert List →
	 * ObservableList)
	 */
	public List<Review> getReviewsForUser(int userId) {
		return repo.getReviewsReceivedByUser(userId);
	}

	/**
	 * Get average rating for a user.
	 */
	public double getAverageRating(int userId) {
		return repo.getAverageRatingForUser(userId);
	}

	/**
	 * Check if a user has reviewed a specific booking.
	 */
	public boolean hasReviewed(int bookingId, int reviewerId) {
		return repo.hasUserReviewedBooking(bookingId, reviewerId);
	}
}
