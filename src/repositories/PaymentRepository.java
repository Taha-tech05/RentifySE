package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import domain.entities.Payment;

public class PaymentRepository {

	private final DatabaseHandler db;

	public PaymentRepository() {
		this.db = DatabaseHandler.getInstance();
	}

	// =========================================================
	// 1. SAVE PAYMENT (after booking is created)
	// =========================================================
	public void save(Payment payment) {
		String sql = """
				INSERT INTO Payment (BookingId, Amount, Method, Status, PaidAt)
				VALUES (?, ?, ?, ?, ?)
				""";

		try (Connection conn = db.connect();
				PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

			stmt.setInt(1, payment.getBookingId());
			stmt.setDouble(2, payment.getAmount());
			stmt.setString(3, payment.getPaymentMethod()); // "CreditCard", "JazzCash", etc.
			stmt.setString(4, payment.getStatus()); // "Pending", "Success", "Failed"
			stmt.setTimestamp(5, Timestamp.valueOf(payment.getPaymentDate()));

			int affectedRows = stmt.executeUpdate();
			if (affectedRows > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					payment.setPaymentId(rs.getInt(1));
				}
			}
			System.out.println("Payment saved: ID=" + payment.getPaymentId() + ", Status=" + payment.getStatus());

		} catch (SQLException e) {
			System.err.println("Error saving payment: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// =========================================================
	// 2. FIND PAYMENT BY BOOKING ID (most common use case)
	// =========================================================
	public Payment findByBookingId(int bookingId) {
		String sql = """
				SELECT p.*, b.Status AS BookingStatus
				FROM Payment p
				JOIN Booking b ON p.BookingId = b.BookingId
				WHERE p.BookingId = ?
				""";

		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, bookingId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return mapResultSetToPayment(rs);
			}
		} catch (SQLException e) {
			System.err.println("Error finding payment for booking " + bookingId);
			e.printStackTrace();
		}
		return null;
	}

	// =========================================================
	// 3. UPDATE PAYMENT STATUS (after authorization success/fail)
	// =========================================================
	public boolean updateStatus(int paymentId, String status) {
		String sql = "UPDATE Payment SET Status = ?, PaidAt = GETDATE() WHERE PaymentId = ?";

		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, status); // "Success", "Failed", "Refunded"
			stmt.setInt(2, paymentId);

			int rows = stmt.executeUpdate();
			return rows > 0;

		} catch (SQLException e) {
			System.err.println("Error updating payment status: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// =========================================================
	// 4. FIND ALL PAYMENTS BY USER (renter's payment history)
	// =========================================================
	public List<Payment> findByRenter(int renterUserId) {
		List<Payment> payments = new ArrayList<>();
		String sql = """
				SELECT p.*, b.BookingId, b.TotalPrice, b.StartDate, b.EndDate,
				       prod.Name AS ProductName, u.Name AS RenterName
				FROM Payment p
				JOIN Booking b ON p.BookingId = b.BookingId
				JOIN Product prod ON b.ProductId = prod.ProductId
				JOIN [User] u ON b.RenterUserId = u.UserId
				WHERE b.RenterUserId = ?
				ORDER BY p.PaidAt DESC
				""";

		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, renterUserId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				payments.add(mapResultSetToPayment(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return payments;
	}

	// =========================================================
	// 6. HELPER: Map ResultSet to Payment object
	// =========================================================
	private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
		Payment payment = new Payment();
		payment.setPaymentId(rs.getInt("PaymentId"));
		payment.setBookingId(rs.getInt("BookingId"));
		payment.setAmount(rs.getDouble("Amount"));
		payment.setPaymentMethod(rs.getString("Method"));
		payment.setStatus(rs.getString("Status"));
		payment.setPaymentDate(rs.getTimestamp("PaidAt").toLocalDateTime());
		return payment;
	}
}