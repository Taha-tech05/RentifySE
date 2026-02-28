package domain.users;

public class Renter extends User {
	public Renter(int id, String name, String email, String password, String phone, int roleid) {
		super(id, name, email, password, phone, roleid);
	}

	@Override
	public String getRole() {
		return "RENTER";
	}

	// Add Renter-specific methods here
}