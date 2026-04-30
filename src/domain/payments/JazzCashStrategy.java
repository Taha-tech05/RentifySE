// domain.payments/JazzCashStrategy.java
package domain.payments;

import domain.entities.Payment;

public class JazzCashStrategy implements PaymentStrategy {
	@Override
	public boolean processPayment(Payment payment) {
		System.out.println("Processing JazzCash: Rs." + payment.getAmount());
		payment.setStatus("Paid");
		return true;
	}

	@Override
	public String getMethodName() {
		return "JazzCash";
	}
}