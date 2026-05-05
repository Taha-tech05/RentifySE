package domain.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.entities.Booking;
import repositories.BookingRepository;

/**
 * US-07 | Admin Tracking History Tests
 *
 * BookingRepository exposes: findById(int) — fetch one booking by ID
 * findByRenter(int) — all bookings for a renter findByOwner(int) — all bookings
 * for an owner findAllByRenter() — every booking in the system (admin view)
 * findAllByOwner() — same, owner perspective updateStatus(int, str) — change
 * booking status
 *
 * BLACK-BOX: EP and Decision-Table tests on ID validity and status values.
 * WHITE-BOX: Branch coverage on fetchBookings() null-return path and
 * mapBooking() field-mapping (B-105 fix — names not emails).
 */
@DisplayName("US-07 | Admin Tracking History Tests")
public class TrackingHistoryTest {

	private BookingRepository bookingRepo;

	// Must exist in your test DB — align with your deliverable's TC-3.x data
	private static final int KNOWN_BOOKING_ID = 1; // Usman Tariq, Returned, 4500 PKR
	private static final int INPROGRESS_BOOKING_ID = 8; // Kamran Shah, In_Progress
	private static final int VALID_RENTER_ID = 100247;
	private static final int VALID_OWNER_ID = 200381;
	private static final int NONEXISTENT_ID = 99999;

	@BeforeEach
	void setUp() {
		bookingRepo = new BookingRepository();
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Equivalence Class Partitioning on findById()
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-TH01 | VEC: findById() with valid ID returns correct Booking object")
	void testFindByIdValidReturnsBooking() {
		Booking b = bookingRepo.findById(KNOWN_BOOKING_ID);
		assertNotNull(b, "Valid booking ID must return a non-null Booking");
		assertEquals(KNOWN_BOOKING_ID, b.getBookingId(), "Returned booking's ID must match the requested ID");
	}

	@Test
	@DisplayName("BB-TH02 | IEC: findById() with non-existent ID returns null")
	void testFindByIdNonExistentReturnsNull() {
		Booking b = bookingRepo.findById(NONEXISTENT_ID);
		assertNull(b, "Non-existent booking ID must return null");
	}

	@Test
	@DisplayName("BB-TH03 | IEC: findById() with negative ID returns null")
	void testFindByIdNegativeReturnsNull() {
		Booking b = bookingRepo.findById(-1);
		assertNull(b, "Negative booking ID must return null");
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Decision Table on status field values
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-TH04 | Decision Table: Known 'Returned' booking has correct status")
	void testReturnedBookingStatusIsCorrect() {
		Booking b = bookingRepo.findById(KNOWN_BOOKING_ID);
		assertNotNull(b);
		assertEquals("Returned", b.getStatus(), "Booking #1 must have status 'Returned' as per test DB");
	}

	@Test
	@DisplayName("BB-TH06 | VEC: findAllByRenter() returns non-null non-empty list")
	void testFindAllByRenterReturnsData() {
		List<Booking> all = bookingRepo.findAllByRenter();
		assertNotNull(all, "Admin booking list must not be null");
		assertFalse(all.isEmpty(), "There must be at least one booking in the system");
	}

	@Test
	@DisplayName("BB-TH07 | VEC: findAllByOwner() returns non-null non-empty list")
	void testFindAllByOwnerReturnsData() {
		List<Booking> all = bookingRepo.findAllByOwner();
		assertNotNull(all, "Admin owner-view booking list must not be null");
		assertFalse(all.isEmpty(), "There must be at least one booking in the system");
	}

	@Test
	@DisplayName("BB-TH08 | VEC: findByRenter() returns only bookings for that renter")
	void testFindByRenterFiltersCorrectly() {
		List<Booking> renterBookings = bookingRepo.findByRenter(VALID_RENTER_ID);
		assertNotNull(renterBookings);
		for (Booking b : renterBookings) {
			assertEquals(VALID_RENTER_ID, b.getRenter().getUserId(),
					"Every returned booking must belong to the requested renter");
		}
	}

	@Test
	@DisplayName("BB-TH09 | VEC: findByOwner() returns only bookings for that owner")
	void testFindByOwnerFiltersCorrectly() {
		List<Booking> ownerBookings = bookingRepo.findByOwner(VALID_OWNER_ID);
		assertNotNull(ownerBookings);
		for (Booking b : ownerBookings) {
			assertEquals(VALID_OWNER_ID, b.getProductOwner().getUserId(),
					"Every returned booking must belong to the requested owner");
		}
	}

	@Test
	@DisplayName("BB-TH10 | IEC: findByRenter() with non-existent renter returns empty list")
	void testFindByRenterNonExistentReturnsEmpty() {
		List<Booking> result = bookingRepo.findByRenter(NONEXISTENT_ID);
		assertNotNull(result, "Result must be an empty list, not null");
		assertTrue(result.isEmpty(), "Non-existent renter ID must return an empty list");
	}

	// -----------------------------------------------------------------------
	// WHITE-BOX — Branch Coverage on mapBooking() and fetchBookings()
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("WB-TH01 | Branch: mapBooking() — RenterName field is Name not Email (B-105 fix)")
	void testRenterNameIsNameNotEmail() {
		// Targets the fixed column alias: r.Name AS RenterName (was r.Email)
		// Verifies B-105: renter name must not look like an email address
		List<Booking> all = bookingRepo.findAllByRenter();
		assertFalse(all.isEmpty());
		for (Booking b : all) {
			String renterName = b.getRenter().getName();
			assertNotNull(renterName, "RenterName must not be null");
			assertFalse(renterName.contains("@"), "RenterName must be a name, not an email address (B-105 fix)");
		}
	}

	@Test
	@DisplayName("WB-TH02 | Branch: mapBooking() — OwnerName field is Name not Email (B-105 fix)")
	void testOwnerNameIsNameNotEmail() {
		// Targets the fixed column alias: o.Name AS OwnerName (was o.Email)
		List<Booking> all = bookingRepo.findAllByOwner();
		assertFalse(all.isEmpty());
		for (Booking b : all) {
			String ownerName = b.getProductOwner().getName();
			assertNotNull(ownerName, "OwnerName must not be null");
			assertFalse(ownerName.contains("@"), "OwnerName must be a name, not an email address (B-105 fix)");
		}
	}

	@Test
	@DisplayName("WB-TH03 | Path: fetchBookings() — all returned bookings have non-null Product")
	void testAllBookingsHaveNonNullProduct() {
		// Targets the JOIN mapping in mapBooking(): p.Name AS ProductName
		List<Booking> all = bookingRepo.findAllByRenter();
		for (Booking b : all) {
			assertNotNull(b.getProduct(), "Every booking must have a mapped Product object");
			assertNotNull(b.getProduct().getName(), "Every booking's Product must have a non-null name");
		}
	}

	@Test
	@DisplayName("WB-TH04 | Path: fetchBookings() — all bookings have non-null start and end dates")
	void testAllBookingsHaveDates() {
		// Targets mapBooking(): rs.getDate("StartDate") / rs.getDate("EndDate")
		List<Booking> all = bookingRepo.findAllByRenter();
		for (Booking b : all) {
			assertNotNull(b.getStartDate(), "StartDate must not be null");
			assertNotNull(b.getEndDate(), "EndDate must not be null");
		}
	}

	@Test
	@DisplayName("WB-TH05 | Branch: fetchBookings() with non-existent ID — returns empty list not null")
	void testFetchBookingsNonExistentReturnsEmptyNotNull() {
		// Targets the empty-list default path in fetchBookings()
		List<Booking> result = bookingRepo.findByRenter(NONEXISTENT_ID);
		assertNotNull(result, "fetchBookings() must return an empty list, never null");
		assertTrue(result.isEmpty(), "No bookings for non-existent renter");
	}

	@Test
	@DisplayName("WB-TH06 | Path: findById() — returned Booking has all core fields populated")
	void testFindByIdPopulatesAllFields() {
		// Full path through mapBooking(): verifies every mapped field is set
		Booking b = bookingRepo.findById(KNOWN_BOOKING_ID);
		assertNotNull(b);
		assertTrue(b.getBookingId() > 0, "BookingId must be positive");
		assertNotNull(b.getProduct(), "Product must be mapped");
		assertNotNull(b.getRenter(), "Renter must be mapped");
		assertNotNull(b.getProductOwner(), "Owner must be mapped");
		assertNotNull(b.getStartDate(), "StartDate must be mapped");
		assertNotNull(b.getEndDate(), "EndDate must be mapped");
		assertTrue(b.getTotalPrice() >= 0, "TotalPrice must be non-negative");
		assertNotNull(b.getStatus(), "Status must be mapped");
	}
}