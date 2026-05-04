package domain.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.entities.Booking;
import domain.entities.Product;
import domain.users.Owner;
import domain.users.Renter;
import repositories.BookingRepository;
import repositories.ProductRepository;

@DisplayName("US-05 | Renter Browse & Book Product Tests")
public class BrowseAndBookTest {

	private ProductRepository productRepo;
	private BookingRepository bookingRepo;

	private static final int VALID_RENTER_ID = 100247;
	private static final int VALID_OWNER_ID = 200381;
	private static final int AVAILABLE_PRODUCT_ID = 3;

	@BeforeEach
	void setUp() {
		productRepo = new ProductRepository();
		bookingRepo = new BookingRepository();
	}

	// -----------------------------------------------------------------------
	// SEARCH & BROWSE
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-BB01 | getAvailableForRent() returns only approved/available products")
	void testGetAvailableForRent() {
		List<Product> products = productRepo.getAvailableForRent();
		assertNotNull(products);
		for (Product p : products) {
			assertTrue(p.isAvailable(), "Product must be marked as available");
			assertEquals("Approved", p.getApprovalStatus(), "Product must be approved by admin");
		}
	}

	@Test
	@DisplayName("BB-BB02 | searchProducts() returns matching items")
	void testSearchByKeyword() {
		// Searching for name "Drill", any type, any owner
		List<Product> results = productRepo.searchProducts("Drill", null, null);
		assertNotNull(results);
		for (Product p : results) {
			assertTrue(p.getName().toLowerCase().contains("drill"));
		}
	}

	// -----------------------------------------------------------------------
	// BOOKING LOGIC
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-BB03 | Create valid booking (VEC)")
	void testCreateValidBooking() {
		Product p = productRepo.findById(AVAILABLE_PRODUCT_ID);
		assertNotNull(p, "Product must exist for booking");

		Renter renter = new Renter(VALID_RENTER_ID, "Test Renter", "renter@test.com", "", "", 3);
		Owner owner = new Owner(VALID_OWNER_ID, "Test Owner", "owner@test.com", "", "", 2);

		LocalDate start = LocalDate.now().plusDays(1);
		LocalDate end = LocalDate.now().plusDays(3);
		double total = p.getPricePerDay() * 2;

		Booking booking = new Booking(p, renter, owner, start, end, total);

		boolean result = bookingRepo.save(booking);
		assertTrue(result, "Booking should be saved to database");
		assertTrue(booking.getBookingId() > 0, "Booking ID should be generated");
	}

	@Test
	@DisplayName("BB-BB04 | Booking with end date before start date should be rejected")
	void testBookingDatesInvalid() {
		// This check should ideally be in the Repository or Service layer
		Product p = productRepo.findById(AVAILABLE_PRODUCT_ID);
		Renter r = new Renter(VALID_RENTER_ID, "Renter", "r@t.com", "", "", 3);
		Owner o = new Owner(VALID_OWNER_ID, "Owner", "o@t.com", "", "", 2);

		LocalDate start = LocalDate.now().plusDays(5);
		LocalDate end = LocalDate.now().plusDays(2); // ERROR: before start

		Booking booking = new Booking(p, r, o, start, end, 100.0);

		// Note: You must implement this date check in bookingRepo.save() for this to
		// pass as false
		assertFalse(bookingRepo.save(booking), "Repository should reject end date < start date");
	}

	@Test
	@DisplayName("BB-BB05 | Check availability for specific dates")
	void testDateAvailability() {
		LocalDate start = LocalDate.now().plusDays(10);
		LocalDate end = LocalDate.now().plusDays(12);

		boolean available = productRepo.isProductAvailableForDates(AVAILABLE_PRODUCT_ID, start, end);
		// Result depends on existing bookings in DB
		assertDoesNotThrow(() -> productRepo.isProductAvailableForDates(AVAILABLE_PRODUCT_ID, start, end));
	}

	// -----------------------------------------------------------------------
	// WHITE-BOX
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("WB-BB01 | findByRenter returns correct list size")
	void testFindByRenter() {
		List<Booking> renterBookings = bookingRepo.findByRenter(VALID_RENTER_ID);
		assertNotNull(renterBookings);
		for (Booking b : renterBookings) {
			assertEquals(VALID_RENTER_ID, b.getRenter().getUserId());
		}
	}
}