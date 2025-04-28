package cycloneCarpool.Payments;

import java.time.LocalDateTime;

public class PaymentResponseDTO {
    private Long id;
    private Double amount;
    private String status;
    private LocalDateTime transactionDate;
    private Long tripId;
    private Long passengerId;
    private String receiptUrl;

    public PaymentResponseDTO(Payment payment) {
        this.id = payment.getId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus();
        this.transactionDate = payment.getTransactionDate();
        this.tripId = payment.getTripId();
        this.passengerId = payment.getPassengerId();
        this.receiptUrl = payment.getReceiptUrl();
    }

    // Getters only
    public Long getId() { return id; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public Long getTripId() { return tripId; }
    public Long getPassengerId() { return passengerId; }
    public String getReceiptUrl() { return receiptUrl; }
}
