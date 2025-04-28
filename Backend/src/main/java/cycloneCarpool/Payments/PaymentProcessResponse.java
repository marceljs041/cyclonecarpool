package cycloneCarpool.Payments;


public class PaymentProcessResponse {
    private String message;
    private PaymentResponseDTO paymentDetails;

    public PaymentProcessResponse(String message, PaymentResponseDTO paymentDetails) {
        this.message = message;
        this.paymentDetails = paymentDetails;
    }

    // Getters
    public String getMessage() { return message; }
    public PaymentResponseDTO getPaymentDetails() { return paymentDetails; }
}