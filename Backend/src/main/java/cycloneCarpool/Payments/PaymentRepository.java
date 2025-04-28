package cycloneCarpool.Payments;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Anthony Campana
 */

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Add custom methods here
}
