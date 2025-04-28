package cycloneCarpool.Payments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author Anthony Campana
 */

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;



    public Payment initiatePayment(Double amount, Long tripId, Long passengerId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0.");
        }
        Payment payment = new Payment(amount, "PENDING", LocalDateTime.now(), null, tripId, passengerId);
        return paymentRepository.save(payment);
    }

    public Payment completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus("COMPLETED");
        payment.setTransactionDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment failPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus("FAILED");
        payment.setTransactionDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment processPayment(Long passengerId, Long tripId, Double amount, String paymentMethod) {
        // Validate amount
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0.");
        }

        try {
            String receiptUrl = paymentGatewayService.createPayment(amount, paymentMethod);
            Payment payment = new Payment(amount, "COMPLETED", LocalDateTime.now(), receiptUrl, tripId, passengerId);

            return paymentRepository.save(payment);
        } catch (RuntimeException e) {
            // On failure
            Payment failedPayment = new Payment(amount, "FAILED", LocalDateTime.now(), null, tripId, passengerId);
            paymentRepository.save(failedPayment);
            throw e;
        }
    }
}
