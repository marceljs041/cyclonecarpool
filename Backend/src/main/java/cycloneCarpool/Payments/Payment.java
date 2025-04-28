package cycloneCarpool.Payments;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Anthony Campana
 */

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String status; // e.g., "PENDING", "COMPLETED", "FAILED"

    private LocalDateTime transactionDate;

    private Long tripId;

    private Long passengerId;

    private String receiptUrl;

    public Payment(){}

    public Payment(Double amount, String status, LocalDateTime transactionDate, String receiptUrl, Long tripId, Long passengerId) {
        this.amount = amount;
        this.status = status;
        this.transactionDate = transactionDate;
        this.tripId = tripId;
        this.passengerId = passengerId;
        this.receiptUrl = receiptUrl;
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public Long getId() {
        return id;
    }
}
