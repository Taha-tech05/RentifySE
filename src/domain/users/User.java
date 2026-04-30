package domain.users;

import java.time.LocalDateTime;

// Abstract base class
public abstract class User {
	protected int userId;
	protected String name;
	protected String email;
	protected String phone;
	protected String password; // store hashed passwords later
	protected boolean isActive = true;
	protected LocalDateTime createdAt;
	protected int roleId;

	public User() {
	}

	public User(int userid, String name, String email, String password, String phone, int roleId) {
		this.userId = userid;
		this.name = name;
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.isActive = true;
		this.createdAt = LocalDateTime.now();
		this.roleId = roleId;
	}

	// Getters & Setters
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		this.isActive = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setRoleId(int id) {
		this.roleId = id;
	}

	// Abstract method for getting role name
	public abstract String getRole();

	public int getRoleId() {
		return roleId;
	}

	@Override
	public String toString() {
		return String.format("User#%d | %s | %s | Role:%s | Active:%s | Created:%s", userId, name, email, getRole(),
				isActive ? "YES" : "NO", createdAt != null ? createdAt.toLocalDate() : "N/A");
	}
}