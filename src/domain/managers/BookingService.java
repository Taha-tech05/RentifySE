package domain.managers;

import java.util.ArrayList;
import java.util.List;

import domain.entities.Booking;
import domain.entities.Payment;
import domain.entities.Transaction;
import repositories.BookingRepository;
import repositories.PaymentRepository;
import repositories.TransactionRepository;
import repositories.UserRepository;

public class BookingService {

	private final BookingRepository bookingRepo = new BookingRepository();
	private final PaymentRepository paymentRepo = new PaymentRepository();
	private final TransactionRepository transactionRepo = new TransactionRepository();
	private final UserRepository userRepo = new UserRepository();

	/**
	 * Save booking + payment + transaction after payment is successful
	 */
	private static class InstanceHolder {
		private static final BookingService INSTANCE = new BookingService();
	}

	public BookingRepository getRepo() {
		return this.bookingRepo;
	}

	public static BookingService getInstance() {
		return InstanceHolder.INSTANCE;
	}

	public boolean createBooking(Booking booking, Payment payment) {
		try {
			// Ensure product owner is set
			if (booking.getProductOwner() == null) {
				booking.setProductOwner(userRepo.findByName(booking.getProduct().getOwnerName()));
			}

			if (booking.getProductOwner() == null) {
				System.err.println("Product owner not found!");
				return false;
			}

			booking.setStatus("Confirmed");

			// STEP 1: SAVE BOOKING → This generates BookingId!
			boolean bookingSaved = bookingRepo.save(booking);
			if (!bookingSaved || booking.getBookingId() <= 0) {
				System.err.println("Failed to save booking or no ID generated");
				return false;
			}

			System.out.println("Booking saved successfully with ID: " + booking.getBookingId());

			// STEP 2: Save Payment with correct BookingId
			payment.setBookingId(booking.getBookingId());
			payment.setStatus("Paid");
			paymentRepo.save(payment);

			// STEP 3: Create Transaction
			Transaction txn = new Transaction();
			txn.setBookingId(booking.getBookingId());
			txn.setProductId(booking.getProduct().getProductId());
			txn.setProductName(booking.getProduct().getName());
			txn.setRenterId(booking.getRenter().getUserId());
			txn.setRenterName(booking.getRenter().getName());
			txn.setOwnerId(booking.getProductOwner().getUserId());
			txn.setOwnerName(booking.getProductOwner().getName());
			txn.setTotalAmount(booking.getTotalPrice());
			txn.setStatus("Confirmed");
			txn.setPaymentConfirmedAt(java.time.LocalDateTime.now());

			boolean txnCreated = transactionRepo.create(txn);
			if (!txnCreated) {
				System.err.println("Transaction creation failed");
			}

			return bookingSaved && txnCreated;

		} catch (Exception e) {
			e.printStackTrace();
			if (booking != null && booking.getBookingId() > 0) {
				booking.setStatus("Failed");
				bookingRepo.updateStatus(booking.getBookingId(), "Failed");
			}
			return false;
		}
	}

	// ── Added from old version ──
	public boolean cancelBooking(int bookingId) {
		Booking b = bookingRepo.findById(bookingId);
		if (b == null || !"Confirmed".equals(b.getStatus()))
			return false;
		b.setStatus("Cancelled");
		return bookingRepo.updateStatus(bookingId, "Cancelled")
				&& transactionRepo.updateStatus(b.getBookingId(), "Cancelled");
	}

	public boolean startRental(int bookingId) {
		return bookingRepo.updateStatus(bookingId, "In_Progress")
				&& transactionRepo.updateStatus(bookingId, "In_Progress");
	}

	public boolean completeRental(int bookingId) {
		return bookingRepo.updateStatus(bookingId, "Completed") && transactionRepo.updateStatus(bookingId, "Completed");
	}

	public boolean initiateReturn(int bookingId, String reason, double refundAmount) {
		boolean bookingUpdated = bookingRepo.updateStatus(bookingId, "Returned");
		boolean txnUpdated = transactionRepo.updateStatus(bookingId, "Returned");
		System.out.println("Refund of " + refundAmount + " PKR initiated for Booking#" + bookingId);
		return bookingUpdated && txnUpdated;
	}

	// ── Added from old version ──
	public List<Integer> getCompletedBookingIdsForOwner(int ownerUserId) {
		return bookingRepo.findByOwner(ownerUserId).stream().filter(b -> "Completed".equals(b.getStatus()))
				.map(Booking::getBookingId).toList();
	}

	// ── Added from old version ──
	public int getRenterUserIdByBookingId(int bookingId) {
		Booking b = bookingRepo.findById(bookingId);
		return b != null ? b.getRenter().getUserId() : -1;
	}

	// ── Added from old version ──
	public List<Booking> getRentalHistoryForRenter(int renterId) {
		if (renterId == -1) {
			return bookingRepo.findAllByRenter();
		}
		return bookingRepo.findByRenter(renterId);
	}

	// ── Added from old version ──
	public List<Booking> getRentalHistoryForOwner(int ownerId) {
		if (ownerId == -1) {
			return bookingRepo.findAllByOwner();
		}
		return bookingRepo.findByOwner(ownerId);
	}

	// ── Added from old version ──
	public List<Booking> getAllRentalHistoryForUser(int userId) {
		List<Booking> all = new ArrayList<>();
		all.addAll(getRentalHistoryForRenter(userId));
		all.addAll(getRentalHistoryForOwner(userId));
		all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
		return all;
	}
}