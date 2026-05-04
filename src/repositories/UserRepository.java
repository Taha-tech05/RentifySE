package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import domain.users.Admin;
import domain.users.Owner;
import domain.users.Renter;
import domain.users.User;

public class UserRepository {

	private final DatabaseHandler db = DatabaseHandler.getInstance();

	// ============================================================
	// 1. AUTHENTICATION
	// ============================================================
	public User authenticateUser(String email, String passwordHash) {
		try (Connection con = db.connect()) {
			User u;
			if ((u = checkTable(con, "Admins", "AdminID", email, passwordHash, 1)) != null)
				return u;
			if ((u = checkTable(con, "ProductOwners", "OwnerID", email, passwordHash, 2)) != null)
				return u;
			if ((u = checkTable(con, "Renters", "RenterID", email, passwordHash, 3)) != null)
				return u;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private User checkTable(Connection con, String table, String idCol, String email, String pw, int roleId)
			throws Exception {
		String sql = "SELECT * FROM " + table + " WHERE Email=? AND PasswordHash=? AND isActive=1";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, email);
			ps.setString(2, pw);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return mapUser(rs, idCol, roleId);
		}
		return null;
	}

	// ============================================================
	// 2. DATA RETRIEVAL
	// ============================================================
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		users.addAll(fetchFromTable("Admins", "AdminID", 1));
		users.addAll(fetchFromTable("ProductOwners", "OwnerID", 2));
		users.addAll(fetchFromTable("Renters", "RenterID", 3));
		return users;
	}

	private List<User> fetchFromTable(String table, String idCol, int roleId) {
		List<User> list = new ArrayList<>();
		String sql = "SELECT * FROM " + table;
		try (Connection con = db.connect();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				list.add(mapUser(rs, idCol, roleId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// ============================================================
	// 3. STATUS & DELETE
	// ============================================================
	public boolean deactivateUser(int userId) {
		return updateStatusInAllTables(userId, false);
	}

	public boolean reactivateUser(int userId) {
		return updateStatusInAllTables(userId, true);
	}

	private boolean updateStatusInAllTables(int id, boolean active) {
		String[] tables = { "Admins", "ProductOwners", "Renters" };
		String[] idCols = { "AdminID", "OwnerID", "RenterID" };
		for (int i = 0; i < tables.length; i++) {
			String sql = "UPDATE " + tables[i] + " SET isActive=? WHERE " + idCols[i] + "=?";
			try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setBoolean(1, active);
				ps.setInt(2, id);
				if (ps.executeUpdate() > 0)
					return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean deleteUser(int userId) {
		String[] tables = { "ProductOwners", "Renters" };
		String[] idCols = { "OwnerID", "RenterID" };
		for (int i = 0; i < tables.length; i++) {
			String sql = "DELETE FROM " + tables[i] + " WHERE " + idCols[i] + "=?";
			try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setInt(1, userId);
				if (ps.executeUpdate() > 0)
					return true;
			} catch (Exception e) {
				/* FK violation — has active bookings */ }
		}
		return false;
	}

	// ============================================================
	// 4. UPDATE & ADD
	// ============================================================
	public boolean updateUser(User user) {
		if (user == null) {
			return false;
		}
		// 1. Validation: Prevent blank Name or Email
		if (user.getName() == null || user.getName().isBlank()) {
			System.err.println("Update Failed: Name cannot be empty.");
			return false;
		}

		if (user.getEmail() == null || !user.getEmail().contains("@")) {
			System.err.println("Update Failed: Valid email is required.");
			return false;
		}

		// 2. Logic to determine table and column
		String table = (user instanceof Admin) ? "Admins" : (user instanceof Owner) ? "ProductOwners" : "Renters";
		String idCol = (user instanceof Admin) ? "AdminID" : (user instanceof Owner) ? "OwnerID" : "RenterID";

		String sql = "UPDATE " + table + " SET Name=?, Email=?, Phone=?, RoleID=? WHERE " + idCol + "=?";

		try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, user.getName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getPhone());
			ps.setInt(4, user.getRoleId());
			ps.setInt(5, user.getUserId());

			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addUser(User user) {
		// 1. Validation (The part we added)
		if (user.getEmail() == null || !user.getEmail().contains("@")) {
			System.err.println("Validation Failed: Invalid email format.");
			return false;
		}

		String table = (user instanceof Admin) ? "Admins" : (user instanceof Owner) ? "ProductOwners" : "Renters";
		String sql = "INSERT INTO " + table
				+ " (Name, Email, PasswordHash, Phone, RoleID, isActive) VALUES (?, ?, ?, ?, ?, 1)";

		try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, user.getName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getPassword());
			ps.setString(4, user.getPhone());
			ps.setInt(5, user.getRoleId());

			return ps.executeUpdate() > 0;

		} catch (com.microsoft.sqlserver.jdbc.SQLServerException e) {
			if (e.getMessage().contains("UNIQUE KEY constraint")) {
				System.err.println("Error: A user with this email already exists.");
			} else {
				e.printStackTrace();
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// ============================================================
	// 5. FIND BY ID / NAME
	// ============================================================
	public User findById(int userId) {
		try (Connection con = db.connect()) {
			User u;
			if ((u = fetchById(con, "ProductOwners", "OwnerID", userId, 2)) != null)
				return u;
			if ((u = fetchById(con, "Renters", "RenterID", userId, 3)) != null)
				return u;
			if ((u = fetchById(con, "Admins", "AdminID", userId, 1)) != null)
				return u;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private User fetchById(Connection con, String table, String idCol, int id, int roleId) throws SQLException {
		String sql = "SELECT * FROM " + table + " WHERE " + idCol + " = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return mapUser(rs, idCol, roleId);
		}
		return null;
	}

	public User findByName(String name) {
		if (name == null || name.isEmpty())
			return null;
		try (Connection con = db.connect()) {
			String[] tables = { "ProductOwners", "Renters", "Admins" };
			String[] idCols = { "OwnerID", "RenterID", "AdminID" };
			int[] roles = { 2, 3, 1 };
			for (int i = 0; i < tables.length; i++) {
				// Search by Email (unique identifier)
				String sql = "SELECT * FROM " + tables[i] + " WHERE Email = ?";
				try (PreparedStatement ps = con.prepareStatement(sql)) {
					ps.setString(1, name);
					ResultSet rs = ps.executeQuery();
					if (rs.next())
						return mapUser(rs, idCols[i], roles[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// ============================================================
	// 6. MAPPING HELPER
	// ============================================================
	private User mapUser(ResultSet rs, String idCol, int roleId) throws SQLException {
		int id = rs.getInt(idCol);
		String name = rs.getString("Name"); // ← NEW
		String email = rs.getString("Email");
		String phone = rs.getString("Phone");
		String pw = rs.getString("PasswordHash");
		boolean active = rs.getBoolean("isActive");

		// Constructor: (id, name, email, password, phone, roleId)
		User u = switch (roleId) {
		case 1 -> new Admin(id, name, email, pw, phone, 1);
		case 2 -> new Owner(id, name, email, pw, phone, 2);
		default -> new Renter(id, name, email, pw, phone, 3);
		};
		u.setActive(active);
		return u;
	}
}