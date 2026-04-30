package repositories;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import domain.entities.Booking;
import domain.entities.Product;
import domain.users.Owner;
import domain.users.Renter;

public class BookingRepository {
	private final DatabaseHandler db = DatabaseHandler.getInstance();

	// 1. SAVE BOOKING
	public boolean save(Booking booking) {
		String sql = "INSERT INTO Booking (ProductId, OwnerUserId, RenterUserId, StartDate, EndDate, TotalPrice, Status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = db.connect();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, booking.getProduct().getProductId());
			stmt.setInt(2, booking.getProductOwner().getUserId());
			stmt.setInt(3, booking.getRenter().getUserId());
			stmt.setDate(4, Date.valueOf(booking.getStartDate()));
			stmt.setDate(5, Date.valueOf(booking.getEndDate()));
			stmt.setDouble(6, booking.getTotalPrice());
			stmt.setString(7, booking.getStatus());
			if (stmt.executeUpdate() > 0) {
				try (ResultSet rs = stmt.getGeneratedKeys()) {
					if (rs.next()) {
						booking.setBookingId(rs.getInt(1));
						return true;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 2. FIND BY ID
	public Booking findById(int bookingId) {
		String sql = buildBaseQuery() + "WHERE b.BookingId = ?";
		List<Booking> result = fetchBookings(sql, bookingId);
		return result.isEmpty() ? null : result.get(0);
	}

	// 3. FIND BY RENTER
	public List<Booking> findByRenter(int renterId) {
		String sql = buildBaseQuery() + "WHERE b.RenterUserId = ? ORDER BY b.CreatedAt DESC";
		return fetchBookings(sql, renterId);
	}

	// 4. FIND BY OWNER
	public List<Booking> findByOwner(int ownerId) {
		String sql = buildBaseQuery() + "WHERE b.OwnerUserId = ? ORDER BY b.CreatedAt DESC";
		return fetchBookings(sql, ownerId);
	}

	// 5. FIND ALL BY RENTER (admin)
	public List<Booking> findAllByRenter() {
		String sql = buildBaseQuery() + "ORDER BY b.CreatedAt DESC";
		return fetchAllBookings(sql);
	}

	// 6. FIND ALL BY OWNER (admin)
	public List<Booking> findAllByOwner() {
		String sql = buildBaseQuery() + "ORDER BY b.CreatedAt DESC";
		return fetchAllBookings(sql);
	}

	// 7. UPDATE STATUS
	public boolean updateStatus(int bookingId, String newStatus) {
		String sql = "UPDATE Booking SET Status = ? WHERE BookingId = ?";
		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, newStatus);
			stmt.setInt(2, bookingId);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ── BASE QUERY (single source of truth) ──────────────────────
	// ← Changed r.Email → r.Name, o.Email → o.Name
	private String buildBaseQuery() {
		return "SELECT b.*, p.Name AS ProductName, " + "r.Name AS RenterName, " // ← was r.Email
				+ "o.Name AS OwnerName " // ← was o.Email
				+ "FROM Booking b " + "JOIN Product p       ON b.ProductId    = p.ProductId "
				+ "JOIN Renters r       ON b.RenterUserId = r.RenterID "
				+ "JOIN ProductOwners o ON b.OwnerUserId  = o.OwnerID ";
	}

	// ── PRIVATE HELPERS ──────────────────────────────────────────
	private List<Booking> fetchBookings(String sql, int id) {
		List<Booking> list = new ArrayList<>();
		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next())
					list.add(mapBooking(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private List<Booking> fetchAllBookings(String sql) {
		List<Booking> list = new ArrayList<>();
		try (Connection conn = db.connect();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next())
				list.add(mapBooking(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private Booking mapBooking(ResultSet rs) throws SQLException {
		Booking b = new Booking();
		b.setBookingId(rs.getInt("BookingId"));

		Product p = new Product();
		p.setProductId(rs.getInt("ProductId"));
		p.setName(rs.getString("ProductName"));
		b.setProduct(p);

		// ← Now reads Name instead of Email for display
		b.setRenter(new Renter(rs.getInt("RenterUserId"), rs.getString("RenterName"), "", "", "", 3));
		b.setProductOwner(new Owner(rs.getInt("OwnerUserId"), rs.getString("OwnerName"), "", "", "", 2));

		b.setStartDate(rs.getDate("StartDate").toLocalDate());
		b.setEndDate(rs.getDate("EndDate").toLocalDate());
		b.setTotalPrice(rs.getDouble("TotalPrice"));
		b.setStatus(rs.getString("Status"));

		if (rs.getTimestamp("CreatedAt") != null)
			b.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());

		return b;
	}
}