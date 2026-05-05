package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.DatabaseHandler;

public class ReportRepository {

	private final DatabaseHandler db = DatabaseHandler.getInstance();

	// ── 1. Total Revenue ──────────────────────────────────────────
	public double getTotalRevenue(LocalDate from, LocalDate to) {
		if (from == null && to == null) {
			return -1;
		}

		String sql = """
				SELECT COALESCE(SUM(p.Amount), 0)
				FROM Payment p
				WHERE p.Status = 'Paid'
				  AND p.PaidAt >= ?
				  AND p.PaidAt < ?
				""";
		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
			stmt.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return rs.getDouble(1);
		} catch (SQLException e) {
			System.err.println("Revenue query failed: " + e.getMessage());
			e.printStackTrace();
		}
		return 0.0;
	}

	// ── 2. Total Bookings ─────────────────────────────────────────
	public int getTotalBookings(LocalDate from, LocalDate to) {
		String sql = """
				SELECT COUNT(*)
				FROM Booking
				WHERE Status IN ('Confirmed', 'Completed', 'In_Progress')
				""";
		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// ── 3. Revenue by Owner ───────────────────────────────────────
	// FIXED: use o.Name instead of o.Email
	// FIXED: keys are "owner" and "revenue" to match formatReport in
	// AdminController
	public List<Map<String, Object>> getRevenueByOwner(LocalDate from, LocalDate to) {
		List<Map<String, Object>> result = new ArrayList<>();
		String sql = """
				SELECT o.Name,
				       COALESCE(SUM(p.Amount), 0) AS Earnings
				FROM ProductOwners o
				LEFT JOIN Booking b  ON b.OwnerUserId = o.OwnerID
				LEFT JOIN Payment p  ON p.BookingId   = b.BookingId
				                    AND p.Status = 'Paid'
				GROUP BY o.OwnerID, o.Name
				ORDER BY Earnings DESC
				""";
		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("owner", rs.getString("Name")); // ← matches formatReport
				row.put("revenue", rs.getDouble("Earnings")); // ← matches formatReport
				result.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	// ── 4. Most Rented Products ───────────────────────────────────
	// FIXED: added rank, type columns
	// FIXED: keys are "rank","name","type","count" to match formatReport in
	// AdminController
	public List<Map<String, Object>> getMostRentedProducts(int limit) {
		List<Map<String, Object>> result = new ArrayList<>();
		String sql = """
				SELECT TOP (?)
				    ROW_NUMBER() OVER (ORDER BY COUNT(b.BookingId) DESC) AS Rank,
				    p.Name  AS ProductName,
				    p.Type  AS ProductType,
				    COUNT(b.BookingId) AS Rentals
				FROM Product p
				JOIN Booking b ON b.ProductId = p.ProductId
				WHERE b.Status IN ('Completed', 'Returned')
				GROUP BY p.ProductId, p.Name, p.Type
				ORDER BY Rentals DESC
				""";
		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, limit);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("rank", rs.getInt("Rank")); // ← matches formatReport
				row.put("name", rs.getString("ProductName")); // ← matches formatReport
				row.put("type", rs.getString("ProductType")); // ← matches formatReport
				row.put("count", rs.getInt("Rentals")); // ← matches formatReport
				result.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	// ── 5. Monthly Revenue ────────────────────────────────────────
	public List<Map<String, Object>> getMonthlyRevenue(int year) {
		List<Map<String, Object>> result = new ArrayList<>();
		String sql = """
				SELECT
				    MONTH(p.PaidAt)            AS MonthNum,
				    DATENAME(MONTH, p.PaidAt)  AS MonthName,
				    COALESCE(SUM(p.Amount), 0) AS Revenue
				FROM Payment p
				WHERE p.Status = 'Paid'
				  AND YEAR(p.PaidAt) = ?
				GROUP BY MONTH(p.PaidAt), DATENAME(MONTH, p.PaidAt)
				ORDER BY MonthNum
				""";
		try (Connection conn = db.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, year);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("month", rs.getString("MonthName"));
				row.put("revenue", rs.getDouble("Revenue"));
				result.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	// ── 6. Platform Summary ───────────────────────────────────────
	public Map<String, Object> getPlatformSummary() {
		Map<String, Object> summary = new HashMap<>();
		String sql = """
				SELECT
				    (SELECT COUNT(*) FROM Renters)                                               AS TotalRenters,
				    (SELECT COUNT(*) FROM ProductOwners)                                         AS TotalOwners,
				    (SELECT COUNT(*) FROM Product WHERE IsAvailable = 1)                         AS AvailableProducts,
				    (SELECT COUNT(*) FROM Booking WHERE Status IN ('Confirmed', 'In_Progress'))  AS ActiveBookings,
				    (SELECT COALESCE(SUM(Amount), 0) FROM Payment WHERE Status = 'Paid')        AS TotalEarnings
				""";
		try (Connection conn = db.connect();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				summary.put("totalRenters", rs.getInt("TotalRenters"));
				summary.put("totalOwners", rs.getInt("TotalOwners"));
				summary.put("availableProducts", rs.getInt("AvailableProducts"));
				summary.put("activeBookings", rs.getInt("ActiveBookings"));
				summary.put("totalEarnings", rs.getDouble("TotalEarnings"));
			}
		} catch (SQLException e) {
			System.err.println("Error in getPlatformSummary(): " + e.getMessage());
			e.printStackTrace();
		}

		// Safety defaults if query fails
		summary.putIfAbsent("totalRenters", 0);
		summary.putIfAbsent("totalOwners", 0);
		summary.putIfAbsent("availableProducts", 0);
		summary.putIfAbsent("activeBookings", 0);
		summary.putIfAbsent("totalEarnings", 0.0);

		return summary;
	}
}