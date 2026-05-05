package domain.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.entities.Booking;
import domain.entities.Product;
import domain.users.Owner;
import domain.users.Renter;
import repositories.BookingRepository;

/**
 * US-06 | Return Product Tests
 *
 * Since return logic is handled via BookingRepository.updateStatus() +
 * BookingRepository.save() validation, tests target: - save() date-order guard
 * (Bug B-501 fix verification) - updateStatus() for marking a booking as
 * "Returned" - findByRenter() / findByOwner() to confirm status is persisted
 *
 * BLACK-BOX: EP and BVA on date inputs and status transitions. WHITE-BOX:
 * Branch coverage on the null-guard and date-order guard in save().
 */
@DisplayName("US-06 | Return Product Tests")
public class ReturnProductTest {

	private BookingRepository bookingRepo;

	// IDs that must exist in your test database
	private static final int VALID_RENTER_ID = 100247;
	private static final int VALID_OWNER_ID = 200381;
	private static final int AVAILABLE_PRODUCT_ID = 3;
	private static final int INPROGRESS_BOOKING_ID = 8; // status = 'In_Progress' in DB
	private static final int NONEXISTENT_ID = 99999;

	@BeforeEach
	void setUp() {
		bookingRepo = new BookingRepository();
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Equivalence Class Partitioning on updateStatus()
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-RP01 | VEC: updateStatus() to 'Returned' on existing booking succeeds")
	void testUpdateStatusToReturned() {
		// Marks an In_Progress booking as Returned — the core return action
		boolean result = bookingRepo.updateStatus(INPROGRESS_BOOKING_ID, "Returned");
		assertTrue(result, "updateStatus() must return true for a valid booking ID and status");
	}

	@Test
	@DisplayName("BB-RP02 | VEC: After updateStatus('Returned'), findById reflects new status")
	void testStatusPersistedAfterReturn() {
		bookingRepo.updateStatus(INPROGRESS_BOOKING_ID, "Returned");
		Booking updated = bookingRepo.findById(INPROGRESS_BOOKING_ID);
		assertNotNull(updated, "findById() must return the booking after update");
		assertEquals("Returned", updated.getStatus(), "Persisted status must be 'Returned' after updateStatus() call");
	}

	@Test
	@DisplayName("BB-RP03 | IEC: updateStatus() with non-existent booking ID returns false")
	void testUpdateStatusNonExistentBooking() {
		boolean result = bookingRepo.updateStatus(NONEXISTENT_ID, "Returned");
		assertFalse(result, "Non-existent booking ID must cause updateStatus() to return false");
	}

	@Test
	@DisplayName("BB-RP04 | IEC: updateStatus() with null status string — should not crash")
	void testUpdateStatusNullStatus() {
		assertDoesNotThrow(() -> {
			boolean result = bookingRepo.updateStatus(INPROGRESS_BOOKING_ID, null);
			// Behaviour (true/false) depends on DB null constraint; must not throw
			System.out.println("Null status result: " + result);
		}, "updateStatus() with null status must not throw an exception");
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — BVA on save() date-order guard (B-501 fix verification)
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-RP05 | BVA: Booking with end date one day before start must be rejected")
	void testEndDateOneBeforeStartRejected() {
		// Boundary: end = start - 1 day
		Product p = new Product(VALID_OWNER_ID, "Test Item", "Tools", 200.0);
		p.setProductId(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		LocalDate start = LocalDate.now().plusDays(5);
		LocalDate end = start.minusDays(1); // one day before start

		Booking booking = new Booking(p, r, o, start, end, 200.0);
		assertFalse(bookingRepo.save(booking), "End date one day before start must be rejected (B-501 fix)");
	}

	@Test
	@DisplayName("BB-RP06 | BVA: Booking with end date == start date — same-day edge case")
	void testEndDateEqualsStartDate() {
		// Boundary: end == start (same-day booking)
		Product p = new Product(VALID_OWNER_ID, "Same Day Item", "Tools", 200.0);
		p.setProductId(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		LocalDate day = LocalDate.now().plusDays(5);
		Booking booking = new Booking(p, r, o, day, day, 200.0);

		// isBefore() returns false for equal dates, so save() attempts DB insert
		// Result depends on your business rules — we assert it does NOT throw
		assertDoesNotThrow(() -> bookingRepo.save(booking),
				"Same-day booking must not throw; behaviour depends on business rules");
	}

	@Test
	@DisplayName("BB-RP07 | VEC: Booking with valid forward dates is accepted by save()")
	void testValidForwardDatesAccepted() {
		Product p = new Product(VALID_OWNER_ID, "Valid Return Item", "Tools", 300.0);
		p.setProductId(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		LocalDate start = LocalDate.now().plusDays(10);
		LocalDate end = LocalDate.now().plusDays(13);

		Booking booking = new Booking(p, r, o, start, end, 900.0);
		assertTrue(bookingRepo.save(booking), "Booking with start < end in the future must pass save()");
	}

	// -----------------------------------------------------------------------
	// WHITE-BOX — Branch Coverage on save() validation chain
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("WB-RP01 | Branch: null Booking object — null-guard at top of save()")
	void testNullBookingObject() {
		// Targets: if (booking == null ...) return false
		assertDoesNotThrow(() -> {
			boolean result = bookingRepo.save(null);
			assertFalse(result, "Null booking must return false, not throw NPE");
		});
	}

	@Test
	@DisplayName("WB-RP02 | Branch: Booking with null Product — null-guard in save()")
	void testNullProductInBooking() {
		// Targets: if (... booking.getProduct() == null ...) return false
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		Booking booking = new Booking(null, r, o, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 300.0);

		assertFalse(bookingRepo.save(booking), "Null product inside booking must be caught by null-guard");
	}

	@Test
	@DisplayName("WB-RP03 | Branch: Booking with null startDate — null-guard in save()")
	void testNullStartDate() {
		// Targets: if (... booking.getStartDate() == null ...) return false
		Product p = new Product(VALID_OWNER_ID, "Item", "Tools", 200.0);
		p.setProductId(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		Booking booking = new Booking(p, r, o, null, LocalDate.now().plusDays(3), 200.0);

		assertFalse(bookingRepo.save(booking), "Null startDate must be caught by null-guard before date comparison");
	}

	@Test
	@DisplayName("WB-RP04 | Branch: Booking with null endDate — null-guard in save()")
	void testNullEndDate() {
		// Targets: if (... booking.getEndDate() == null) return false
		Product p = new Product(VALID_OWNER_ID, "Item", "Tools", 200.0);
		p.setProductId(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		Booking booking = new Booking(p, r, o, LocalDate.now().plusDays(1), null, 200.0);

		assertFalse(bookingRepo.save(booking), "Null endDate must be caught by null-guard before date comparison");
	}

	@Test
	@DisplayName("WB-RP05 | Branch: date-order guard — endDate.isBefore(startDate) takes false branch")
	void testDateOrderGuardFalseBranch() {
		// Targets: if (endDate.isBefore(startDate)) — false branch (end > start)
		// Guard evaluates false → execution continues to SQL insert
		Product p = new Product(VALID_OWNER_ID, "Guard Test Item", "Tools", 100.0);
		p.setProductId(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		LocalDate start = LocalDate.now().plusDays(20);
		LocalDate end = LocalDate.now().plusDays(22);

		Booking b = new Booking(p, r, o, start, end, 200.0);
		assertTrue(bookingRepo.save(b), "False branch of date-order guard must allow save() to reach DB insert");
	}

	@Test
	@DisplayName("WB-RP06 | Branch: date-order guard — endDate.isBefore(startDate) takes true branch")
	void testDateOrderGuardTrueBranch() {
		// Targets: if (endDate.isBefore(startDate)) — true branch → return false
		Product p = new Product(VALID_OWNER_ID, "Guard Test Item", "Tools", 100.0);
		p.setProductId(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		LocalDate start = LocalDate.now().plusDays(10);
		LocalDate end = LocalDate.now().plusDays(7); // before start

		Booking b = new Booking(p, r, o, start, end, 200.0);
		assertFalse(bookingRepo.save(b), "True branch of date-order guard must return false immediately (B-501 fix)");
	}
}