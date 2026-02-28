package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHandler;
import domain.users.Admin;
import domain.users.Owner;
import domain.users.Renter;
import domain.users.User;

public class UserRepository {

	private final DatabaseHandler db = DatabaseHandler.getInstance();

	// Authenticate user from any table
	public User authenticateUser(String email, String passwordHash) {

		try (Connection con = db.connect()) {

			// =============================
			// 1️⃣ CHECK ADMIN TABLE
			// =============================
			String adminQuery = """
					SELECT * FROM Admins
					WHERE Email=? AND PasswordHash=?
					""";

			try (PreparedStatement ps = con.prepareStatement(adminQuery)) {
				ps.setString(1, email);
				ps.setString(2, passwordHash);

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return new Admin(rs.getInt("AdminID"), rs.getString("Email"), // using Email as name? adjust if
																					// needed
							rs.getString("Email"), rs.getString("PasswordHash"), rs.getString("Phone"),
							rs.getInt("RoleID") // ✅ PASS ROLEID
					);
				}
			}

			// =============================
			// 2️⃣ CHECK PRODUCT OWNER TABLE
			// =============================
			String ownerQuery = """
					SELECT * FROM ProductOwners
					WHERE Email=? AND PasswordHash=?
					""";

			try (PreparedStatement ps = con.prepareStatement(ownerQuery)) {
				ps.setString(1, email);
				ps.setString(2, passwordHash);

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return new Owner(rs.getInt("OwnerID"), rs.getString("Email"), rs.getString("Email"),
							rs.getString("PasswordHash"), rs.getString("Phone"), rs.getInt("RoleID") // ✅ PASS ROLEID
					);
				}
			}

			// =============================
			// 3️⃣ CHECK RENTER TABLE
			// =============================
			String renterQuery = """
					SELECT * FROM Renters
					WHERE Email=? AND PasswordHash=?
					""";

			try (PreparedStatement ps = con.prepareStatement(renterQuery)) {
				ps.setString(1, email);
				ps.setString(2, passwordHash);

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return new Renter(rs.getInt("RenterID"), rs.getString("Email"), rs.getString("Email"),
							rs.getString("PasswordHash"), rs.getString("Phone"), rs.getInt("RoleID") // ✅ PASS ROLEID
					);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null; // No match found
	}

	// Get all users (as their concrete types)
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();

		try (Connection con = db.connect()) {

			// =============================
			// Admins
			// =============================
			String adminQuery = "SELECT * FROM Admins";
			try (PreparedStatement ps = con.prepareStatement(adminQuery); ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					users.add(new Admin(rs.getInt("AdminID"), rs.getString("Email"), // or rs.getString("Name") if you
																						// add a Name column
							rs.getString("Email"), null, // Don't fetch password
							rs.getString("Phone"), rs.getInt("RoleID") // ✅ PASS ROLEID
					));
				}
			}

			// =============================
			// Product Owners
			// =============================
			String ownerQuery = "SELECT * FROM ProductOwners";
			try (PreparedStatement ps = con.prepareStatement(ownerQuery); ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					users.add(new Owner(rs.getInt("OwnerID"), rs.getString("Email"), rs.getString("Email"), null,
							rs.getString("Phone"), rs.getInt("RoleID") // ✅ PASS ROLEID
					));
				}
			}

			// =============================
			// Renters
			// =============================
			String renterQuery = "SELECT * FROM Renters";
			try (PreparedStatement ps = con.prepareStatement(renterQuery); ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					users.add(new Renter(rs.getInt("RenterID"), rs.getString("Email"), rs.getString("Email"), null,
							rs.getString("Phone"), rs.getInt("RoleID") // ✅ PASS ROLEID
					));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return users;
	}

	// Update user info
	public boolean updateUser(User user) {
		String table;
		String idColumn;
		int userId;

		if (user instanceof Admin admin) {
			table = "Admins";
			idColumn = "AdminID";
			userId = admin.getUserId();
		} else if (user instanceof Owner owner) {
			table = "ProductOwners";
			idColumn = "OwnerID";
			userId = owner.getUserId();
		} else if (user instanceof Renter renter) {
			table = "Renters";
			idColumn = "RenterID";
			userId = renter.getUserId();
		} else {
			return false;
		}

		String query = String.format("UPDATE %s SET Email=?, Phone=? WHERE %s=?", table, idColumn);

		try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(query)) {

			ps.setString(1, user.getEmail());
			ps.setString(2, user.getPhone());
			ps.setInt(3, userId);

			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addUser(User user) {
		String table;
		String idColumn;

		if (user instanceof Admin) {
			table = "Admins";
			idColumn = "AdminID";
		} else if (user instanceof Owner) {
			table = "ProductOwners";
			idColumn = "OwnerID";
		} else if (user instanceof Renter) {
			table = "Renters";
			idColumn = "RenterID";
		} else {
			return false;
		}

		String query = String.format("INSERT INTO %s (Email, PasswordHash, Phone, RoleID) VALUES (?, ?, ?, ?)", table);

		try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, user.getEmail());
			ps.setString(2, user.getPassword());
			ps.setString(3, user.getPhone());
			ps.setInt(4, user.getRoleId());

			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Toggle user active status
	public boolean setUserActive(User user, boolean isActive) {
		String table;
		String idColumn;
		int userId;

		if (user instanceof Admin admin) {
			table = "Admins";
			idColumn = "AdminID";
			userId = admin.getUserId();
		} else if (user instanceof Owner owner) {
			table = "ProductOwners";
			idColumn = "OwnerID";
			userId = owner.getUserId();
		} else if (user instanceof Renter renter) {
			table = "Renters";
			idColumn = "RenterID";
			userId = renter.getUserId();
		} else {
			return false;
		}

		String query = String.format("UPDATE %s SET isActive=? WHERE %s=?", table, idColumn);

		try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(query)) {

			ps.setBoolean(1, isActive);
			ps.setInt(2, userId);

			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Delete user
	public boolean deleteUser(User user) {
		String table;
		String idColumn;
		int userId;

		if (user instanceof Admin admin) {
			table = "Admins";
			idColumn = "AdminID";
			userId = admin.getUserId();
		} else if (user instanceof Owner owner) {
			table = "ProductOwners";
			idColumn = "OwnerID";
			userId = owner.getUserId();
		} else if (user instanceof Renter renter) {
			table = "Renters";
			idColumn = "RenterID";
			userId = renter.getUserId();
		} else {
			return false;
		}

		String query = String.format("DELETE FROM %s WHERE %s=?", table, idColumn);

		try (Connection con = db.connect(); PreparedStatement ps = con.prepareStatement(query)) {

			ps.setInt(1, userId);
			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<User> searchUsers(String keyword) {
		List<User> users = new ArrayList<>();
		String searchPattern = "%" + keyword + "%";

		try (Connection con = db.connect()) {

			// Admins
			String adminQuery = "SELECT AdminID, Email, Phone FROM Admins WHERE Email LIKE ?";
			try (PreparedStatement ps = con.prepareStatement(adminQuery)) {
				ps.setString(1, searchPattern);
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					users.add(new Admin(rs.getInt("AdminID"), null, rs.getString("Email"), null, rs.getString("Phone"),
							rs.getInt("RoleID")));
				}
			}

			// Owners
			String ownerQuery = "SELECT OwnerID, Email, Phone FROM ProductOwners WHERE Email LIKE ?";
			try (PreparedStatement ps = con.prepareStatement(ownerQuery)) {
				ps.setString(1, searchPattern);
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					users.add(new Owner(rs.getInt("OwnerID"), null, rs.getString("Email"), null, rs.getString("Phone"),
							rs.getInt("RoleID")));
				}
			}

			// Renters
			String renterQuery = "SELECT RenterID, Email, Phone FROM Renters WHERE Email LIKE ?";
			try (PreparedStatement ps = con.prepareStatement(renterQuery)) {
				ps.setString(1, searchPattern);
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					users.add(new Renter(rs.getInt("RenterID"), null, rs.getString("Email"), null,
							rs.getString("Phone"), rs.getInt("RoleID")));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return users;
	}
}