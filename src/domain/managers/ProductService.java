package domain.managers;

import java.util.List;

import domain.entities.Product;
import repositories.ProductRepository;

public class ProductService {

	private final ProductRepository repo = new ProductRepository();

	// ═══════════════════════════════════════════
	// 1. SINGLETON (Optional but recommended)
	// ═══════════════════════════════════════════
	private static class InstanceHolder {
		private static final ProductService INSTANCE = new ProductService();
	}

	public static ProductService getInstance() {
		return InstanceHolder.INSTANCE;
	}

	// ═══════════════════════════════════════════
	// 2. OWNER OPERATIONS
	// ═══════════════════════════════════════════
	public boolean addProduct(Product p) {
		return repo.save(p);
	}

	public boolean updateProduct(Product p) {
		// Existing logic: update and reset to pending
		return repo.update(p);
	}

	public boolean deleteProduct(int productId, int ownerUserId) {
		return repo.delete(productId, ownerUserId);
	}

	public Product getProductById(int productId) {
		return repo.findById(productId);
	}

	// ═══════════════════════════════════════════
	// 3. ADMIN OPERATIONS
	// ═══════════════════════════════════════════
	public boolean approveProduct(int productId, String adminName) {
		return repo.approve(productId, adminName);
	}

	public boolean rejectProduct(int productId, String reason, String adminName) {
		return repo.reject(productId, reason, adminName);
	}

	public List<Product> getAllProducts() {
		return repo.getAllProducts();
	}

	public boolean requestChanges(int productId, String adminName) {
		return repo.requestChanges(productId, adminName);
	}

	public List<Product> getPendingProducts() {
		return repo.getPendingApprovalProducts();
	}

	// ═══════════════════════════════════════════
	// 4. RENTER OPERATIONS (THE USER STORY)
	// ═══════════════════════════════════════════

	/**
	 * Checks if the product is free from conflicting bookings for the chosen dates.
	 * Directly supports the "Check Availability" user story step.
	 */
	public boolean checkAvailability(int productId, java.time.LocalDate start, java.time.LocalDate end) {
		if (start == null || end == null || !start.isBefore(end)) {
			return false;
		}
		return repo.isProductAvailableForDates(productId, start, end);
	}

	public List<Product> getAvailableProducts() {
		return repo.getAvailableForRent();
	}

	public List<Product> searchProducts(String name, String type, String ownerName) {
		// Corrected parameters to match repository search implementation
		return repo.searchProducts(name, type, ownerName);
	}

	/**
	 * Retrieves products that have been successfully booked (Completed history)
	 */
	public List<Product> getBookedProductsHistory() {
		return repo.findBookedProducts();
	}
}