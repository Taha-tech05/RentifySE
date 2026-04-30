// domain.payments/PaymentStrategy.java
package domain.payments;

import domain.entities.Payment;

public interface PaymentStrategy {
	boolean processPayment(Payment payment);

	String getMethodName();
}