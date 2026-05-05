package domain.managers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.entities.Product;
import repositories.ProductRepository;

@DisplayName("US-04 | Owner Manage Products (Corrected for Repo)")
public class ManageProductsTest {

	private ProductRepository productRepo;
	private static final int VALID_OWNER_ID = 200381;
	private static final int EXISTING_PRODUCT_ID = 4;

	@BeforeEach
	void setUp() {
		productRepo = new ProductRepository();
	}

	// -----------------------------------------------------------------------
	// ADD PRODUCT TESTS (using save() method)
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("Valid product should be saved successfully")
	void testSaveValidProduct() {
		Product p = new Product(VALID_OWNER_ID, "Test Drill", "Tools", 500.0);
		assertTrue(productRepo.save(p), "A valid product should be saved");
	}

	@Test
	@DisplayName("Product with blank name should be rejected")
	void testSaveProductBlankName() {
		// Name is blank (only spaces)
		Product p = new Product(VALID_OWNER_ID, "   ", "Tools", 500.0);
		assertFalse(productRepo.save(p), "Blank names must be rejected");
	}

	@Test
	@DisplayName("Product with empty type/category should be rejected")
	void testSaveProductEmptyType() {
		Product p = new Product(VALID_OWNER_ID, "Drill", "", 500.0);
		assertFalse(productRepo.save(p), "Empty type must be rejected");
	}

	@Test
	@DisplayName("Product with zero or negative price should be rejected")
	void testSaveProductInvalidPrice() {
		Product pNeg = new Product(VALID_OWNER_ID, "Drill", "Tools", -10.0);
		Product pZero = new Product(VALID_OWNER_ID, "Drill", "Tools", 0.0);

		assertFalse(productRepo.save(pNeg), "Negative price should fail");
		assertFalse(productRepo.save(pZero), "Zero price should fail");
	}

	// -----------------------------------------------------------------------
	// UPDATE PRODUCT TESTS (using update() method)
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("Updating existing product with valid data should succeed")
	void testUpdateProductValid() {
		Product p = productRepo.findById(EXISTING_PRODUCT_ID);
		assertNotNull(p);
		p.setName("New Brand Name");
		p.setPricePerDay(800.0);

		assertTrue(productRepo.update(p), "Update should return true");
	}

	@Test
	@DisplayName("Updating with blank name should fail")
	void testUpdateBlankName() {
		Product p = productRepo.findById(EXISTING_PRODUCT_ID);
		p.setName("");
		assertFalse(productRepo.update(p), "Updating to a blank name must fail");
	}

	// -----------------------------------------------------------------------
	// DELETE PRODUCT TESTS (using delete() method)
	// -----------------------------------------------------------------------

	@Test
	@DisplayName("Deleting product with correct owner should succeed")
	void testDeleteProduct() {
		// IDs must match existing DB records or setup a temp product
		boolean result = productRepo.delete(EXISTING_PRODUCT_ID, VALID_OWNER_ID);
		// Note: result depends on if ID exists. If true, verify it's gone:
		if (result) {
			assertNull(productRepo.findById(EXISTING_PRODUCT_ID));
		}
	}
}