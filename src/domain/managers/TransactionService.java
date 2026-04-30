package domain.managers;

import java.util.List;

import domain.entities.Transaction;
import repositories.TransactionRepository;

public class TransactionService {

	private final TransactionRepository repo = new TransactionRepository();

	/**
	 * Fetch all transactions for admin view.
	 */
	public static TransactionService getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private static class InstanceHolder {
		private static final TransactionService INSTANCE = new TransactionService();
	}

	public List<Transaction> getAllTransactionsForAdmin() {
		return repo.getAllForAdmin();
	}

	/**
	 * Fetch transaction history for a specific renter.
	 */
	public List<Transaction> getHistoryByRenter(int renterId) {
		return repo.getByRenter(renterId);
	}

	/**
	 * Fetch transaction history for a specific owner (earnings, completed
	 * bookings).
	 */
	public List<Transaction> getHistoryByOwner(int ownerId) {
		return repo.getByOwner(ownerId);
	}

	/**
	 * Fetch a single transaction by ID.
	 */
	public Transaction getTransactionById(int transactionId) {
		return repo.findById(transactionId);
	}

	/**
	 * Create a new transaction (booking/payment).
	 */
	public boolean createTransaction(Transaction t) {
		return repo.create(t);
	}

	/**
	 * Update status of a transaction (PaymentConfirmed, Returned, Cancelled, etc.)
	 */
	public boolean updateTransactionStatus(int transactionId, String newStatus) {
		return repo.updateStatus(transactionId, newStatus);
	}
}
