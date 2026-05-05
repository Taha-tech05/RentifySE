package domain.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.entities.Review;
import repositories.ReviewRepository;

/**
 * US-05 | Rate and Review Tests Tests ReviewRepository.save() and query methods
 * against the actual DB.
 *
 * BLACK-BOX: EP + BVA on rating range (1–5) and comment field. WHITE-BOX:
 * Branch coverage on duplicate check and null guards.
 */
@DisplayName("US-05 | Rate and Review Tests")
public class RateAndReviewTest {

	private ReviewRepository reviewRepo;

	// IDs that must exist in your test DB
	private static final int VALID_REVIEWER_ID = 100247; // a renter
	private static final int VALID_RATED_ID = 200381; // an owner
	private static final int COMPLETED_BOOKING_ID = 7; // status = Completed

	@BeforeEach
	void setUp() {
		reviewRepo = new ReviewRepository();
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Equivalence Class Partitioning
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-RR01 | VEC: Valid rating (3) with non-empty comment should be saved")
	void testValidReviewSaved() {
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(3);
		r.setComment("Good experience overall.");
		assertTrue(reviewRepo.save(r), "Valid review must be saved successfully");
	}

	@Test
	@DisplayName("BB-RR02 | IEC: Rating = 0 is below valid range — should be rejected")
	void testRatingZeroRejected() {
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(0); // invalid
		r.setComment("Zero rating test.");
		assertFalse(reviewRepo.save(r), "Rating of 0 is out of range and must be rejected");
	}

	@Test
	@DisplayName("BB-RR03 | IEC: Rating = 6 is above valid range — should be rejected")
	void testRatingAboveFiveRejected() {
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(6); // invalid
		r.setComment("Above max rating test.");
		assertFalse(reviewRepo.save(r), "Rating above 5 must be rejected");
	}

	@Test
	@DisplayName("BB-RR04 | IEC: Empty comment should be rejected")
	void testEmptyCommentRejected() {
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(4);
		r.setComment(""); // invalid
		assertFalse(reviewRepo.save(r), "Empty comment must be rejected");
	}

	@Test
	@DisplayName("BB-RR05 | IEC: Whitespace-only comment should be rejected")
	void testBlankCommentRejected() {
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(4);
		r.setComment("   "); // invalid — whitespace only
		assertFalse(reviewRepo.save(r), "Whitespace-only comment must be rejected");
	}

	@Test
	@DisplayName("BB-RR06 | IEC: Duplicate review for same booking + reviewer must be rejected")
	void testDuplicateReviewRejected() {
		// hasUserReviewedBooking() guard must block the second save
		assertTrue(reviewRepo.hasUserReviewedBooking(COMPLETED_BOOKING_ID, VALID_REVIEWER_ID) || true,
				"Pre-condition: first review may or may not exist");

		Review first = new Review();
		first.setBookingId(COMPLETED_BOOKING_ID);
		first.setReviewerUserId(VALID_REVIEWER_ID);
		first.setRatedUserId(VALID_RATED_ID);
		first.setReviewType("RenterToOwner");
		first.setRating(5);
		first.setComment("First review.");
		reviewRepo.save(first); // may succeed or fail depending on prior state

		Review duplicate = new Review();
		duplicate.setBookingId(COMPLETED_BOOKING_ID);
		duplicate.setReviewerUserId(VALID_REVIEWER_ID);
		duplicate.setRatedUserId(VALID_RATED_ID);
		duplicate.setReviewType("RenterToOwner");
		duplicate.setRating(2);
		duplicate.setComment("Second attempt — should be blocked.");
		assertFalse(reviewRepo.save(duplicate), "Duplicate review (same bookingId + reviewerUserId) must be rejected");
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Boundary Value Analysis
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-RR07 | BVA: Rating = 1 (minimum boundary) must be accepted")
	void testBoundaryRatingOne() {
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(1);
		r.setComment("Minimum rating.");
		assertTrue(reviewRepo.save(r), "Rating of 1 is the minimum valid boundary");
	}

	@Test
	@DisplayName("BB-RR08 | BVA: Rating = 5 (maximum boundary) must be accepted")
	void testBoundaryRatingFive() {
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(5);
		r.setComment("Maximum rating.");
		assertTrue(reviewRepo.save(r), "Rating of 5 is the maximum valid boundary");
	}

	// -----------------------------------------------------------------------
	// WHITE-BOX — Branch Coverage
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("WB-RR01 | Branch: hasUserReviewedBooking() returns true when duplicate exists")
	void testHasUserReviewedBookingTrue() {
		// First ensure a review exists for this pair
		Review r = new Review();
		r.setBookingId(COMPLETED_BOOKING_ID);
		r.setReviewerUserId(VALID_REVIEWER_ID);
		r.setRatedUserId(VALID_RATED_ID);
		r.setReviewType("RenterToOwner");
		r.setRating(3);
		r.setComment("Setup review.");
		reviewRepo.save(r); // insert if not already there

		assertTrue(reviewRepo.hasUserReviewedBooking(COMPLETED_BOOKING_ID, VALID_REVIEWER_ID),
				"Must return true when that reviewer has already reviewed that booking");
	}

	@Test
	@DisplayName("WB-RR02 | Branch: hasUserReviewedBooking() returns false for new pair")
	void testHasUserReviewedBookingFalse() {
		// Use an ID combination that definitely has no review
		assertFalse(reviewRepo.hasUserReviewedBooking(99999, 99999),
				"Must return false for a booking/reviewer pair that does not exist");
	}

	@Test
	@DisplayName("WB-RR03 | Path: getReviewsReceivedByUser() returns list where all ratedUserId match")
	void testGetReviewsReceivedByUser() {
		List<Review> received = reviewRepo.getReviewsReceivedByUser(VALID_RATED_ID);
		assertNotNull(received, "Received reviews list must not be null");
		for (Review rv : received) {
			assertEquals(VALID_RATED_ID, rv.getRatedUserId(), "Every review must be addressed to the queried user");
		}
	}

	@Test
	@DisplayName("WB-RR04 | Path: getReviewsWrittenByUser() returns list where all reviewerUserId match")
	void testGetReviewsWrittenByUser() {
		List<Review> written = reviewRepo.getReviewsWrittenByUser(VALID_REVIEWER_ID);
		assertNotNull(written, "Written reviews list must not be null");
		for (Review rv : written) {
			assertEquals(VALID_REVIEWER_ID, rv.getReviewerUserId(),
					"Every review must be authored by the queried user");
		}
	}

	@Test
	@DisplayName("WB-RR05 | Path: getAverageRatingForUser() returns value in range 0.0–5.0")
	void testAverageRatingInValidRange() {
		double avg = reviewRepo.getAverageRatingForUser(VALID_RATED_ID);
		assertTrue(avg >= 0.0 && avg <= 5.0, "Average rating must be between 0.0 and 5.0 inclusive");
	}

	@Test
	@DisplayName("WB-RR06 | Branch: getAverageRatingForUser() for user with no reviews returns 0.0")
	void testAverageRatingNoReviews() {
		double avg = reviewRepo.getAverageRatingForUser(99999); // non-existent user
		assertEquals(0.0, avg, "User with no reviews must return 0.0 (not null or exception)");
	}

	@Test
	@DisplayName("WB-RR07 | Path: getAllReviews() elements all have non-null reviewType and comment")
	void testAllReviewsFieldsNonNull() {
		List<Review> all = reviewRepo.getAllReviews();
		assertNotNull(all);
		for (Review rv : all) {
			assertNotNull(rv.getReviewType(), "reviewType must not be null");
			assertNotNull(rv.getComment(), "comment must not be null");
			assertTrue(rv.getRating() >= 1 && rv.getRating() <= 5, "Every persisted rating must be in range 1–5");
		}
	}
}