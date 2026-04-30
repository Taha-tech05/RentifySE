package domain.managers;

import domain.entities.Payment;
import domain.payments.CreditCardStrategy;
import domain.payments.JazzCashStrategy;
import domain.payments.PaymentStrategy;
import repositories.PaymentRepository;

public class PaymentService {

	private final PaymentRepository paymentRepo = new PaymentRepository();

	// Singleton pattern
	private PaymentService() {
	}

	private static class InstanceHolder {
		private static final PaymentService INSTANCE = new PaymentService();
	}

	public PaymentRepository getRepo() {
		return this.paymentRepo;
	}

	public static PaymentService getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Process payment using strategy pattern
	 */
	public boolean processPayment(Payment payment, String method) {
		if (payment == null || method == null || method.isBlank())
			return false;

		PaymentStrategy strategy = switch (method.toLowerCase().trim()) {
		case "creditcard", "credit card", "card" -> new CreditCardStrategy();
		case "jazzcash", "jazz cash", "mobile" -> new JazzCashStrategy();
		default -> throw new IllegalArgumentException("Unsupported payment method: " + method);
		};

		boolean success = strategy.processPayment(payment);

		if (!success) {
			payment.setStatus("Failed");
			savePayment(payment);
			return false;
		}

		payment.setPaymentMethod(strategy.getMethodName());
		payment.setStatus("Paid");
		payment.setPaymentDate(java.time.LocalDateTime.now());

		savePayment(payment);
		return true;
	}

	private void savePayment(Payment payment) {
		paymentRepo.save(payment);
	}
}
