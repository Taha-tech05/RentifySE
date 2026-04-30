package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import domain.entities.Review;

public class ReviewRepository {

	private final DatabaseHandler db = DatabaseHandler.getInstance();

	// ── Save a review (Owner→Renter OR Renter→Owner) ──────────────────────────
	public boolean save(Review review) {
		if (hasUserReviewedBooking(review.getBookingId(), review.getReviewerUserId()))
			return false;

		String sql = """
				INSERT INTO Review
				    (BookingId, ReviewerUserId, RatedUserId, ReviewType, Rating, Comment, CreatedAt)
				VALUES (?, ?, ?, ?, ?, ?, GETDATE())
				""";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, review.getBookingId());
			s.setInt(2, review.getReviewerUserId());
			s.setInt(3, review.getRatedUserId());
			s.setString(4, review.getReviewType());
			s.setInt(5, review.getRating());
			s.setString(6, review.getComment());
			return s.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// ── Reviews RECEIVED by a user ─────────────────────────────────────────────
	// Reviewer can be either a Renter or an Owner so we LEFT JOIN both tables
	// and COALESCE picks whichever name is not null.
	public List<Review> getReviewsReceivedByUser(int ratedUserId) {
		List<Review> reviews = new ArrayList<>();
		String sql = """
				SELECT r.ReviewId, r.BookingId, r.ReviewerUserId, r.RatedUserId,
				       r.ReviewType, r.Rating, r.Comment, r.CreatedAt,
				       COALESCE(rn.Name, own.Name, 'Unknown') AS ReviewerName
				FROM Review r
				LEFT JOIN Renters       rn  ON r.ReviewerUserId = rn.RenterID
				LEFT JOIN ProductOwners own ON r.ReviewerUserId = own.OwnerID
				WHERE r.RatedUserId = ?
				ORDER BY r.CreatedAt DESC
				""";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, ratedUserId);
			try (ResultSet rs = s.executeQuery()) {
				while (rs.next())
					reviews.add(mapReview(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reviews;
	}

	// ── Reviews WRITTEN by a user ──────────────────────────────────────────────
	public List<Review> getReviewsWrittenByUser(int reviewerUserId) {
		List<Review> reviews = new ArrayList<>();
		String sql = """
				SELECT r.ReviewId, r.BookingId, r.ReviewerUserId, r.RatedUserId,
				       r.ReviewType, r.Rating, r.Comment, r.CreatedAt,
				       COALESCE(rn.Name, own.Name, 'Unknown') AS ReviewerName
				FROM Review r
				LEFT JOIN Renters       rn  ON r.ReviewerUserId = rn.RenterID
				LEFT JOIN ProductOwners own ON r.ReviewerUserId = own.OwnerID
				WHERE r.ReviewerUserId = ?
				ORDER BY r.CreatedAt DESC
				""";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, reviewerUserId);
			try (ResultSet rs = s.executeQuery()) {
				while (rs.next())
					reviews.add(mapReview(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reviews;
	}

	// ── Average rating for a user ──────────────────────────────────────────────
	public double getAverageRatingForUser(int ratedUserId) {
		String sql = "SELECT AVG(Rating * 1.0) FROM Review WHERE RatedUserId = ?";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, ratedUserId);
			try (ResultSet rs = s.executeQuery()) {
				if (rs.next() && rs.getObject(1) != null)
					return rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	// ── Duplicate check ────────────────────────────────────────────────────────
	public boolean hasUserReviewedBooking(int bookingId, int reviewerUserId) {
		String sql = "SELECT 1 FROM Review WHERE BookingId = ? AND ReviewerUserId = ?";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, bookingId);
			s.setInt(2, reviewerUserId);
			try (ResultSet rs = s.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			return false;
		}
	}

	// ── All reviews (admin use) ────────────────────────────────────────────────
	public List<Review> getAllReviews() {
		List<Review> reviews = new ArrayList<>();
		String sql = """
				SELECT r.ReviewId, r.BookingId, r.ReviewerUserId, r.RatedUserId,
				       r.ReviewType, r.Rating, r.Comment, r.CreatedAt,
				       COALESCE(rn.Name, own.Name, 'Unknown') AS ReviewerName
				FROM Review r
				LEFT JOIN Renters       rn  ON r.ReviewerUserId = rn.RenterID
				LEFT JOIN ProductOwners own ON r.ReviewerUserId = own.OwnerID
				ORDER BY r.CreatedAt DESC
				""";
		try (Connection c = db.connect();
				PreparedStatement s = c.prepareStatement(sql);
				ResultSet rs = s.executeQuery()) {
			while (rs.next())
				reviews.add(mapReview(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reviews;
	}

	// ── Private mapper ─────────────────────────────────────────────────────────
	private Review mapReview(ResultSet rs) throws SQLException {
		Review r = new Review();
		r.setReviewId(rs.getInt("ReviewId"));
		r.setBookingId(rs.getInt("BookingId"));
		r.setReviewerUserId(rs.getInt("ReviewerUserId"));
		r.setRatedUserId(rs.getInt("RatedUserId"));
		r.setReviewType(rs.getString("ReviewType"));
		r.setRating(rs.getInt("Rating"));
		r.setComment(rs.getString("Comment"));

		Timestamp ts = rs.getTimestamp("CreatedAt");
		if (ts != null)
			r.setReviewDate(ts.toLocalDateTime());

		return r;
	}
}