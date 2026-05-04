package domain.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.users.User;
import repositories.UserRepository;

public class LoginTest {

	private UserRepository userRepo;

	@BeforeEach
	void setUp() {
		userRepo = new UserRepository();
	}

	// Valid login — should return a User object
	@Test
	void testValidLogin() {
		User user = userRepo.authenticateUser("admin@rentease.com", "admin123");
		assertNotNull(user, "Valid credentials should return a user");
		assertEquals(1, user.getRoleId(), "Admin role ID should be 1");
	}

	// Wrong password — should return null
	@Test
	void testWrongPassword() {
		User user = userRepo.authenticateUser("admin@rentease.com", "wrongpass");
		assertNull(user, "Wrong password should return null");
	}

	// Non-existent email — should return null
	@Test
	void testNonExistentEmail() {
		User user = userRepo.authenticateUser("nobody@test.com", "pass123");
		assertNull(user, "Non-existent email should return null");
	}

	// Empty email — should return null
	@Test
	void testEmptyEmail() {
		User user = userRepo.authenticateUser("", "admin123");
		assertNull(user, "Empty email should return null");
	}

	// Owner login — should return roleId 2
	@Test
	void testOwnerLogin() {
		User user = userRepo.authenticateUser("ali@owner.com", "ali123");
		assertNotNull(user);
		assertEquals(2, user.getRoleId(), "Owner role ID should be 2");
	}

	// Renter login — should return roleId 3
	@Test
	void testRenterLogin() {
		User user = userRepo.authenticateUser("usman@renter.com", "usman123");
		assertNotNull(user);
		assertEquals(3, user.getRoleId(), "Renter role ID should be 3");
	}
}