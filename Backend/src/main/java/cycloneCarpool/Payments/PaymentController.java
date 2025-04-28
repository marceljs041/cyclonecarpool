package cycloneCarpool.Payments;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;

import cycloneCarpool.Trips.PassengerTripStatusService;

/**
 * @author Anthony Campana
 */

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PassengerTripStatusService passengerTripStatusService;

    @Operation(summary = "Initates a Payment request to a specific passenger.", description = "Requests a payment from the passenger for a specific trip (tripId) for a predetermined amount. " +
            "Sets payment status to pending.")
    @PostMapping("/initiate")
    public PaymentResponseDTO initiatePayment(@RequestParam Double amount, @RequestParam Long tripId, @RequestParam Long passengerId) {
        if (amount == null || tripId == null || passengerId == null || amount <= 0) {
            throw new IllegalArgumentException("Invalid payment parameters.");
        }
        Payment payment = paymentService.initiatePayment(amount, tripId, passengerId);
        return new PaymentResponseDTO(payment);
    }

    @Operation(summary = "Marks Payment Status as Complete", description = "If a payment is successful then updates the current paymentID status to complete.")
    @PostMapping("/complete/{paymentId}")
    public Payment completePayment(@PathVariable Long paymentId) {
        return paymentService.completePayment(paymentId);
    }

    @Operation(summary = "Marks payment status as Failed", description = "If a payment is unsuccessful then updates the current paymentID status to failed.")
    @PostMapping("/fail/{paymentId}")
    public Payment failPayment(@PathVariable Long paymentId) {
        return paymentService.failPayment(paymentId);
    }

    @Operation(summary = "Process Payment with STRIPE API", description = "Uses Stripe API to process a payment. If successful then method will return a receipt in a JSON object. " +
            "Otherwise will return a response failed message.")
    @PostMapping("/process")
    public ResponseEntity<PaymentProcessResponse> processPayment(
            @RequestParam Long passengerId,
            @RequestParam Long tripId,
            @RequestParam Double amount,
            @RequestParam String paymentMethod) {

        try {
            // Process payment
            Payment payment = paymentService.processPayment(passengerId, tripId, amount, paymentMethod);
            passengerTripStatusService.updatePaymentStatus(tripId, passengerId, true);
            passengerTripStatusService.notifyDriverOfPayment(tripId, passengerId);

            // Build response DTO
            PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO(payment);
            PaymentProcessResponse response = new PaymentProcessResponse(
                    "Payment processed successfully.",
                    paymentResponseDTO
            );
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Build error response
            PaymentProcessResponse response = new PaymentProcessResponse(
                    "Payment failed: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse.toString());
    }

}
