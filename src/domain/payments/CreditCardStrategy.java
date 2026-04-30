// domain.payments/CreditCardStrategy.java
package domain.payments;

import domain.entities.Payment;

public class CreditCardStrategy implements PaymentStrategy {
	@Override
	public boolean processPayment(Payment payment) {
		System.out.println("Processing Credit Card: Rs." + payment.getAmount());
		payment.setStatus("Paid");
		return true;
	}

	@Override
	public String getMethodName() {
		return "Credit Card";
	}
}