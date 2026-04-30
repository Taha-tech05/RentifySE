package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import domain.entities.Transaction;

public class TransactionRepository {

	private final DatabaseHandler db = DatabaseHandler.getInstance();

	// ── BASE SELECT (single source of truth) ─────────────────────
	// ← Changed r.Email → r.Name, o.Email → o.Name
	private static final String BASE_SELECT = """
			SELECT
			    t.TransactionId,
			    t.BookingId,
			    t.ProductId,
			    p.Name          AS ProductName,
			    t.RenterId,
			    r.Name          AS RenterName,
			    t.OwnerId,
			    o.Name          AS OwnerName,
			    t.TotalAmount,
			    t.Status,
			    t.PaymentConfirmedAt,
			    t.CreatedAt
			FROM [Transaction] t
			JOIN Product       p ON t.ProductId = p.ProductId
			JOIN Renters       r ON t.RenterId  = r.RenterID
			JOIN ProductOwners o ON t.OwnerId   = o.OwnerID
			""";

	// ── CREATE ────────────────────────────────────────────────────
	public boolean create(Transaction t) {
		String sql = """
				INSERT INTO [Transaction]
				(BookingId, ProductId, ProductName, RenterId, RenterName,
				 OwnerId, OwnerName, TotalAmount, Status, PaymentConfirmedAt)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try (Connection c = db.connect();
				PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			s.setInt(1, t.getBookingId());
			s.setInt(2, t.getProductId());
			s.setString(3, t.getProductName());
			s.setInt(4, t.getRenterId());
			s.setString(5, t.getRenterName());
			s.setInt(6, t.getOwnerId());
			s.setString(7, t.getOwnerName());
			s.setDouble(8, t.getTotalAmount());
			s.setString(9, t.getStatus());
			s.setTimestamp(10, t.getPaymentConfirmedAt() != null ? Timestamp.valueOf(t.getPaymentConfirmedAt()) : null);
			if (s.executeUpdate() > 0) {
				ResultSet rs = s.getGeneratedKeys();
				if (rs.next())
					t.setTransactionId(rs.getInt(1));
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ── UPDATE STATUS ─────────────────────────────────────────────
	public boolean updateStatus(int bookingId, String newStatus) {
		String sql = """
				UPDATE [Transaction]
				SET Status = ?, PaymentConfirmedAt = ?
				WHERE BookingId = ?
				""";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setString(1, newStatus);
			s.setTimestamp(2,
					("Paid".equalsIgnoreCase(newStatus) || "Confirmed".equalsIgnoreCase(newStatus))
							? Timestamp.valueOf(LocalDateTime.now())
							: null);
			s.setInt(3, bookingId);
			return s.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ── ALL (admin) ───────────────────────────────────────────────
	public List<Transaction> getAllForAdmin() {
		return fetchTransactions(BASE_SELECT + "ORDER BY t.CreatedAt DESC");
	}

	// kept for backward compat with TransactionService
	public List<Transaction> getAllTransactionsForAdmin() {
		return getAllForAdmin();
	}

	// ── BY RENTER ─────────────────────────────────────────────────
	public List<Transaction> getByRenter(int renterId) {
		return fetchTransactions(BASE_SELECT + "WHERE t.RenterId = ? ORDER BY t.CreatedAt DESC", renterId);
	}

	// ── BY OWNER ──────────────────────────────────────────────────
	public List<Transaction> getByOwner(int ownerId) {
		return fetchTransactions(BASE_SELECT + "WHERE t.OwnerId = ? ORDER BY t.CreatedAt DESC", ownerId);
	}

	// ── FIND BY ID ────────────────────────────────────────────────
	public Transaction findById(int id) {
		List<Transaction> list = fetchTransactions(BASE_SELECT + "WHERE t.TransactionId = ?", id);
		return list.isEmpty() ? null : list.get(0);
	}

	// ── PRIVATE HELPER ────────────────────────────────────────────
	private List<Transaction> fetchTransactions(String sql, Object... params) {
		List<Transaction> list = new ArrayList<>();
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++)
				s.setObject(i + 1, params[i]);
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				Transaction t = new Transaction();
				t.setTransactionId(rs.getInt("TransactionId"));
				t.setBookingId(rs.getInt("BookingId"));
				t.setProductId(rs.getInt("ProductId"));
				t.setProductName(rs.getString("ProductName"));
				t.setRenterId(rs.getInt("RenterId"));
				t.setRenterName(rs.getString("RenterName")); // ← now Name
				t.setOwnerId(rs.getInt("OwnerId"));
				t.setOwnerName(rs.getString("OwnerName")); // ← now Name
				t.setTotalAmount(rs.getDouble("TotalAmount"));
				t.setStatus(rs.getString("Status"));
				Timestamp ca = rs.getTimestamp("CreatedAt");
				if (ca != null)
					t.setCreatedAt(ca.toLocalDateTime());
				Timestamp pa = rs.getTimestamp("PaymentConfirmedAt");
				if (pa != null)
					t.setPaymentConfirmedAt(pa.toLocalDateTime());
				list.add(t);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}
