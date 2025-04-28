package cycloneCarpool.Payments;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Charge;
import com.stripe.param.ChargeListParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentGatewayService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public String createPayment(Double amount, String paymentMethod) {
        Stripe.apiKey = stripeApiKey;

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (amount * 100)) // Convert to the smallest currency unit
                    .setCurrency("usd")
                    .setDescription("Payment for trip")
                    .setPaymentMethod(paymentMethod)
                    .setConfirm(true)
                    .build();

            // Create and confirm the PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            // paymentIntent = paymentIntent.confirm();

            // Check if the PaymentIntent is successful
            if ("succeeded".equals(paymentIntent.getStatus())) {
                // Get the receipt URL from associated charges if available
                ChargeListParams chargeListParams = ChargeListParams.builder()
                        .setPaymentIntent(paymentIntent.getId())
                        .build();

                List<Charge> charges = Charge.list(chargeListParams).getData();
                if (!charges.isEmpty()) {
                    return charges.get(0).getReceiptUrl();
                }
            }
            throw new RuntimeException("Payment was not successful");
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }
}
