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
import domain.entities.Product;

public class ProductRepository {

	private final DatabaseHandler db = DatabaseHandler.getInstance();

	// ── BASE JOIN (single source of truth) ───────────────────────
	// ← Changed o.Email AS OwnerName → o.Name AS OwnerName
	private static final String BASE_JOIN = "SELECT p.*, o.Name AS OwnerName " // ← was o.Email
			+ "FROM Product p " + "JOIN ProductOwners o ON p.OwnerUserId = o.OwnerID ";

	// ═══════════════════════════════════════
	// 1. OWNER OPERATIONS
	// ═══════════════════════════════════════

	public boolean save(Product p) {
		// 1. Validation: Reject if Name or Type is null/blank, or if Price is invalid
		if (p == null || p.getName() == null || p.getName().isBlank() || p.getType() == null || p.getType().isBlank()
				|| p.getPricePerDay() <= 0) {

			System.err.println("Product Save Rejected: Missing required fields or invalid price.");
			return false;
		}

		// 2. Database Operation
		String sql = "INSERT INTO Product ([Name], [Type], PricePerDay, OwnerUserId, IsAvailable, ApprovalStatus) "
				+ "VALUES (?, ?, ?, ?, 1, 'Pending')";

		try (Connection c = db.connect();
				PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			s.setString(1, p.getName());
			s.setString(2, p.getType());
			s.setDouble(3, p.getPricePerDay());
			s.setInt(4, p.getOwnerUserId());

			if (s.executeUpdate() > 0) {
				try (ResultSet rs = s.getGeneratedKeys()) {
					if (rs.next()) {
						p.setProductId(rs.getInt(1));
						return true;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean update(Product p) {
		if (p == null || p.getName() == null || p.getName().isBlank() || p.getType() == null || p.getType().isBlank()
				|| p.getPricePerDay() <= 0) {

			System.err.println("Product Save Rejected: Missing required fields or invalid price.");
			return false;
		}

		String sql = "UPDATE Product SET [Name]=?, [Type]=?, PricePerDay=?, "
				+ "ApprovalStatus='Pending' WHERE ProductId=? AND OwnerUserId=?";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setString(1, p.getName());
			s.setString(2, p.getType());
			s.setDouble(3, p.getPricePerDay());
			s.setInt(4, p.getProductId());
			s.setInt(5, p.getOwnerUserId());
			return s.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean delete(int productId, int ownerUserId) {
		String sql = "DELETE FROM Product WHERE ProductId=? AND OwnerUserId=?";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, productId);
			s.setInt(2, ownerUserId);
			return s.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean requestChanges(int productId, String adminName) {
		String sql = "UPDATE Product SET ApprovalStatus = 'Changes_Requested', "
				+ "ModeratedBy = ?, ModeratedAt = GETDATE() WHERE ProductId = ?";
		return executeUpdate(sql, adminName, productId);
	}

	// ═══════════════════════════════════════
	// 2. ADMIN OPERATIONS
	// ═══════════════════════════════════════

	public boolean approve(int productId, String adminName) {
		String sql = "UPDATE Product SET ApprovalStatus='Approved', " + " WHERE ProductId=?";
		return executeUpdate(sql, adminName, productId);
	}

	public boolean reject(int productId, String reason, String adminName) {
		String sql = "UPDATE Product SET ApprovalStatus='Rejected'" + "WHERE ProductId=?";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, productId);
			return s.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<Product> getPendingApprovalProducts() {
		return fetchProducts(BASE_JOIN + "WHERE p.ApprovalStatus = 'Pending'"); // ← fixed
	}

	public List<Product> getAllProducts() {
		return fetchProducts(BASE_JOIN + "ORDER BY p.ProductId DESC");
	}

	// ═══════════════════════════════════════
	// 3. RENTER OPERATIONS
	// ═══════════════════════════════════════

	public boolean isProductAvailableForDates(int productId, java.time.LocalDate start, java.time.LocalDate end) {
		String sql = "SELECT COUNT(*) FROM Booking WHERE ProductId=? AND Status='Confirmed' "
				+ "AND NOT (EndDate <= ? OR StartDate >= ?)";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, productId);
			s.setDate(2, Date.valueOf(start));
			s.setDate(3, Date.valueOf(end));
			try (ResultSet rs = s.executeQuery()) {
				if (rs.next())
					return rs.getInt(1) == 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<Product> searchProducts(String name, String type, String ownerName) {
		StringBuilder sql = new StringBuilder(BASE_JOIN + "WHERE p.ApprovalStatus='Approved' and p.IsAvailable=1 ");
		List<Object> params = new ArrayList<>();
		if (name != null && !name.isEmpty()) {
			sql.append("AND p.[Name] LIKE ? ");
			params.add("%" + name + "%");
		}
		if (type != null && !type.isEmpty()) {
			sql.append("AND p.[Type] LIKE ? ");
			params.add("%" + type + "%");
		}
		if (ownerName != null && !ownerName.isEmpty()) {
			// ← Search by Name now, not Email
			sql.append("AND o.Name LIKE ? ");
			params.add("%" + ownerName + "%");
		}
		List<Product> list = new ArrayList<>();
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++)
				s.setObject(i + 1, params.get(i));
			try (ResultSet rs = s.executeQuery()) {
				while (rs.next())
					list.add(mapResultSet(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Product> getAvailableForRent() {
		return fetchProducts(BASE_JOIN + "WHERE  p.ApprovalStatus='Approved' AND p.IsAvailable=1");
	}

	public List<Product> findBookedProducts() {
		String sql = "SELECT DISTINCT p.*, o.Name AS OwnerName " // ← was o.Email
				+ "FROM Product p " + "JOIN ProductOwners o ON p.OwnerUserId = o.OwnerID "
				+ "JOIN Booking b ON p.ProductId = b.ProductId " + "WHERE b.Status='Completed'";
		return fetchProducts(sql);
	}

	public Product findById(int productId) {
		String sql = BASE_JOIN + "WHERE p.ProductId=?";
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setInt(1, productId);
			try (ResultSet rs = s.executeQuery()) {
				if (rs.next())
					return mapResultSet(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// ═══════════════════════════════════════
	// HELPERS
	// ═══════════════════════════════════════

	private boolean executeUpdate(String sql, String adminName, int productId) {
		try (Connection c = db.connect(); PreparedStatement s = c.prepareStatement(sql)) {
			s.setString(1, adminName);
			s.setInt(2, productId);
			return s.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private List<Product> fetchProducts(String sql) {
		List<Product> list = new ArrayList<>();
		try (Connection c = db.connect();
				PreparedStatement s = c.prepareStatement(sql);
				ResultSet rs = s.executeQuery()) {
			while (rs.next())
				list.add(mapResultSet(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private Product mapResultSet(ResultSet rs) throws SQLException {
		Product p = new Product();
		p.setProductId(rs.getInt("ProductId"));
		p.setName(rs.getString("Name"));
		p.setType(rs.getString("Type"));
		p.setPricePerDay(rs.getDouble("PricePerDay"));
		p.setAvailable(rs.getBoolean("IsAvailable"));
		p.setOwnerUserId(rs.getInt("OwnerUserId"));
		p.setOwnerName(rs.getString("OwnerName")); // ← now reads Name
		p.setApprovalStatus(rs.getString("ApprovalStatus"));
		return p;
	}
}
