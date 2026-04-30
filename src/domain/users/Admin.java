package domain.users;

public class Admin extends User {

	public Admin() {
		super();
	}

	public Admin(int id, String name, String email, String password, String phone, int roleId) {
		super(id, name, email, password, phone, roleId);
	}

	@Override
	public String getRole() {
		return "ADMIN";
	}

	// Add Admin-specific methods here
}