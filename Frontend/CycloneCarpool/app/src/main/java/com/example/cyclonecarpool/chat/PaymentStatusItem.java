package com.example.cyclonecarpool.chat;

public class PaymentStatusItem {
    private final String passengerName;
    private final String paymentStatus;

    public PaymentStatusItem(String passengerName, String paymentStatus) {
        this.passengerName = passengerName;
        this.paymentStatus = paymentStatus;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
