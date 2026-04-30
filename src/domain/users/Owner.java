package domain.users;

public class Owner extends User {

	public Owner() {
		super();
	}

	public Owner(int id, String name, String email, String password, String phone, int roleId) {
		super(id, name, email, password, phone, roleId);
	}

	@Override
	public String getRole() {
		return "OWNER";
	}

	// Add Owner-specific methods here
}