package com.example.cyclonecarpool.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.cyclonecarpool.R;
import com.example.cyclonecarpool.utils.API;
import com.example.cyclonecarpool.utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class Payments extends AppCompatActivity {

    private EditText amountInput, tripIdInput;
    private Button initiatePaymentButton;

    private Integer userId;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Retrieve userId and role from Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId") && intent.hasExtra("role")) {
            userId = intent.getIntExtra("userId", -1);
            role = intent.getStringExtra("role");
        }

        amountInput = findViewById(R.id.amount_input);
        tripIdInput = findViewById(R.id.trip_id_input);
        initiatePaymentButton = findViewById(R.id.initiate_payment_button);

        initiatePaymentButton.setOnClickListener(v -> {
            if (validateInput()) {
                initiatePayment();
            }
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(amountInput.getText().toString())) {
            amountInput.setError("Amount is required");
            return false;
        }
        if (TextUtils.isEmpty(tripIdInput.getText().toString())) {
            tripIdInput.setError("Trip ID is required");
            return false;
        }
        return true;
    }

    private void initiatePayment() {
        double amount = Double.parseDouble(amountInput.getText().toString());
        long tripId = Long.parseLong(tripIdInput.getText().toString());
        long passengerId = userId;

        String url = API.BASE_URL + "payments/initiate?amount=" + amount + "&tripId=" + tripId + "&passengerId=" + passengerId;

        StringRequest initiateRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String status = responseObject.getString("status");
                        if ("PENDING".equals(status)) {
                            Toast.makeText(this, "Payment initiated successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Payment initiation failed.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Response parsing error.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Payment initiation error.", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(initiateRequest);
    }
}
