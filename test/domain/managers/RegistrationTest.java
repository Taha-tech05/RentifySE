package domain.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.users.Owner;
import domain.users.Renter;
import repositories.UserRepository;
hello
/**
 * US-01: User Registration Tests that users (Renters and Owners) can register
 * with valid credentials, and that invalid/duplicate registrations are
 * rejected.
 *
 * BLACK-BOX tests: driven purely by inputs and expected outputs. WHITE-BOX
 * tests: target internal validation branches inside addUser().
 */
@DisplayName("US-01 | User Registration Tests")
public class RegistrationTest {

	private UserRepository userRepo;

	@BeforeEach
	void setUp() {
		userRepo = new UserRepository();
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Equivalence Class Partitioning
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-R01 | Valid Renter registration should succeed")
	void testValidRenterRegistration() {
		// VEC: all fields non-empty, unique email, valid roleId
		Renter newUser = new Renter(0, "Valid Renter", "validrenter@test.com", "pass123", "0300-1234567", 3);
		assertTrue(userRepo.addUser(newUser), "A fully valid Renter should be registered successfully");
	}

	@Test
	@DisplayName("BB-R02 | Valid Owner registration should succeed")
	void testValidOwnerRegistration() {
		Owner newOwner = new Owner(0, "Valid Owner", "validowner@test.com", "pass123", "0311-9876543", 2);
		assertTrue(userRepo.addUser(newOwner), "A fully valid Owner should be registered successfully");
	}

	@Test
	@DisplayName("BB-R03 | Duplicate email should be rejected (IEC)")
	void testDuplicateEmail() {
		// IEC: email already exists in DB
		Renter duplicate = new Renter(0, "Duplicate", "usman@renter.com", "pass123", "0300-0000000", 3);
		assertFalse(userRepo.addUser(duplicate), "Duplicate email must not be accepted");
	}

	@Test
	@DisplayName("BB-R04 | Empty name should be rejected (IEC)")
	void testEmptyName() {
		Renter noName = new Renter(0, "", "newuser@test.com", "pass123", "0300-0000001", 3);
		assertFalse(userRepo.addUser(noName), "Empty name should be rejected");
	}

	@Test
	@DisplayName("BB-R05 | Empty password should be rejected (IEC)")
	void testEmptyPassword() {
		Renter noPass = new Renter(0, "No Pass", "nopass@test.com", "", "0300-0000002", 3);
		assertFalse(userRepo.addUser(noPass), "Empty password should be rejected");
	}

	@Test
	@DisplayName("BB-R06 | Null email should be rejected (IEC)")
	void testNullEmail() {
		Renter nullEmail = new Renter(0, "Null Email", null, "pass123", "0300-0000003", 3);
		assertFalse(userRepo.addUser(nullEmail), "Null email should not be accepted");
	}

	// -----------------------------------------------------------------------
	// BLACK-BOX — Boundary Value Analysis
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("BB-R08 | BVA: Email without '@' should be rejected")
	void testInvalidEmailFormat() {
		Renter badEmail = new Renter(0, "Bad Email", "notanemail", "pass123", "0300-0000005", 3);
		assertFalse(userRepo.addUser(badEmail), "Email without '@' should be rejected");
	}

	// -----------------------------------------------------------------------
	// WHITE-BOX — Branch Coverage on addUser() validation logic
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("WB-R01 | Branch: null user object passed to addUser()")
	void testNullUserObject() {
		// Targets the null-guard branch at the top of addUser()
		assertFalse(userRepo.addUser(null), "Passing null to addUser() must return false, not throw NPE");
	}

	@Test
	@DisplayName("WB-R02 | Branch: invalid roleId (0) should be rejected")
	void testInvalidRoleId() {
		// Targets the roleId validation branch
		Renter badRole = new Renter(0, "Bad Role", "badrole@test.com", "pass123", "0300-0000006", 0);
		assertFalse(userRepo.addUser(badRole), "Invalid roleId (0) should be rejected");
	}

	@Test
	@DisplayName("WB-R03 | Path: successful registration updates the user count")
	void testUserCountIncreasesAfterRegistration() {
		int before = userRepo.getAllUsers().size();
		Renter newUser = new Renter(0, "Count Test", "counttest@test.com", "pass123", "0300-9999999", 3);
		userRepo.addUser(newUser);
		int after = userRepo.getAllUsers().size();
		assertEquals(before + 1, after, "User count should increase by 1 after successful registration");
	}
}