package domain.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.users.User;
import repositories.UserRepository;

/**
 * US-03: Admin — Manage Users Tests that an Admin can view, update,
 * activate/deactivate, and delete users.
 *
 * BLACK-BOX: EP on valid vs. invalid user IDs and field values. WHITE-BOX:
 * Branch coverage on update and delete logic.
 */
@DisplayName("US-03 | Admin Manage Users Tests")
public class Adminmanageusertest {

	private UserRepository userRepo;

	// A user ID known to exist in the test database
	private static final int EXISTING_USER_ID = 200381;
	// A user ID that does not exist
	private static final int NONEXISTENT_USER_ID = 99999;

	@BeforeEach
	void setUp() {
		userRepo = new UserRepository();
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — View / List Users
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-AU01 | getAllUsers() should return a non-null, non-empty list")
	void testGetAllUsersReturnsData() {
		List<User> users = userRepo.getAllUsers();
		assertNotNull(users, "User list must not be null");
		assertFalse(users.isEmpty(), "There should be at least one user in the system");
	}

	@Test
	@DisplayName("BB-AU02 | findById() with valid ID should return correct User")
	void testGetUserByValidId() {
		User user = userRepo.findById(EXISTING_USER_ID);
		assertNotNull(user, "Valid user ID must return a User object");
		assertEquals(EXISTING_USER_ID, user.getUserId(), "Returned user's ID must match the requested ID");
	}

	@Test
	@DisplayName("BB-AU03 | findById() with invalid ID should return null (IEC)")
	void testGetUserByInvalidId() {
		User user = userRepo.findById(NONEXISTENT_USER_ID);
		assertNull(user, "Non-existent user ID must return null");
	}

	@Test
	@DisplayName("BB-AU04 | findById() with negative ID should return null (IEC — BVA)")
	void testGetUserByNegativeId() {
		User user = userRepo.findById(-1);
		assertNull(user, "Negative ID should return null");
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Update User
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-AU05 | Updating an existing user with valid data should succeed")
	void testUpdateUserValid() {
		User user = userRepo.findById(EXISTING_USER_ID);
		assertNotNull(user);
		user.setName("Updated Name");
		boolean result = userRepo.updateUser(user);
		assertTrue(result, "Updating a valid user should return true");
		// Verify persistence
		User updated = userRepo.findById(EXISTING_USER_ID);
		assertEquals("Updated Name", updated.getName(), "Updated name must be persisted in the database");
	}

	@Test
	@DisplayName("BB-AU06 | Updating a user with blank name should fail (IEC)")
	void testUpdateUserBlankName() {
		User user = userRepo.findById(EXISTING_USER_ID);
		assertNotNull(user);
		user.setName("");
		boolean result = userRepo.updateUser(user);
		assertFalse(result, "Blank name update must be rejected");
	}

	@Test
	@DisplayName("BB-AU07 | Updating a non-existent user should return false (IEC)")
	void testUpdateNonExistentUser() {
		User ghost = userRepo.findById(EXISTING_USER_ID);
		assertNotNull(ghost);
		ghost.setUserId(NONEXISTENT_USER_ID);
		boolean result = userRepo.updateUser(ghost);
		assertFalse(result, "Updating a non-existent user must return false");
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Activate / Deactivate
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// BLACK-BOX — Delete User
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-AU12 | Deleting a non-existent user should return false (IEC)")
	void testDeleteNonExistentUser() {
		boolean result = userRepo.deleteUser(NONEXISTENT_USER_ID);
		assertFalse(result, "Deleting a non-existent user must return false");
	}

	// -----------------------------------------------------------------------
	// WHITE-BOX — Branch Coverage
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("WB-AU01 | Branch: updateUser() with null User object must not throw NPE")
	void testUpdateNullUser() {
		assertDoesNotThrow(() -> {
			boolean result = userRepo.updateUser(null);
			assertFalse(result, "Null user update must return false, not throw");
		});
	}

	@Test
	@DisplayName("WB-AU02 | Branch: deleteUser() with zero ID should be rejected")
	void testDeleteZeroId() {
		boolean result = userRepo.deleteUser(0);
		assertFalse(result, "Deleting user with ID 0 must return false");
	}

	@Test
	@DisplayName("WB-AU03 | Path: getAllUsers() list elements all have non-null names")
	void testAllUsersHaveNames() {
		List<User> users = userRepo.getAllUsers();
		for (User u : users) {
			assertNotNull(u.getName(), "Every user must have a non-null name");
		}
	}
}