package com.example.cyclonecarpool.trips;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cyclonecarpool.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditTripPage extends AppCompatActivity implements View.OnClickListener {

    private Button editTripBtn;
    private EditText fromInput, toInput, pickupInput, dateInput, timeInput, seatsInput, priceInput;
    private CheckBox roundtripCheck, nosmokeCheck;

    private Integer userId;
    private String role;
    private Long tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.edit_trip_screen);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("tripId") && intent.hasExtra("userId")) {
            int tripIdInt = intent.getIntExtra("tripId", -1);
            tripId = (long) tripIdInt;
            userId = intent.getIntExtra("userId", -1);
            role = intent.getStringExtra("role");
            fetchTripDetails(tripId);
        }

        editTripBtn = findViewById(R.id.btnEditTrip);
        fromInput = findViewById(R.id.fromEditInput);
        toInput = findViewById(R.id.toEditInput);
        pickupInput = findViewById(R.id.pickupEditInput);
        dateInput = findViewById(R.id.dateEditInput);
        timeInput = findViewById(R.id.timeEditInput);
        seatsInput = findViewById(R.id.seatsEditInput);
        priceInput = findViewById(R.id.priceEditInput);
        roundtripCheck = findViewById(R.id.checkBoxRoundTrip);
        nosmokeCheck = findViewById(R.id.checkBoxNoSmoke);

        editTripBtn.setOnClickListener(this);
    }

    private void fetchTripDetails(Long tripId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/trips/" + tripId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject tripJson = new JSONObject(response.toString());
                    runOnUiThread(() -> populateFields(tripJson));
                } else {
                    runOnUiThread(() -> Toast.makeText(EditTripPage.this, "Failed to load trip details.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(EditTripPage.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void populateFields(JSONObject tripJson) {
        try {
            fromInput.setText(tripJson.getString("startLocation"));
            toInput.setText(tripJson.getString("endLocation"));
            pickupInput.setText(tripJson.getString("pickUp"));

            String dateTime = tripJson.getString("time");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = dateFormat.parse(dateTime);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

            dateInput.setText(dateFormatter.format(date));
            timeInput.setText(timeFormatter.format(date));

            seatsInput.setText(String.valueOf(tripJson.getInt("seat")));
            priceInput.setText(String.valueOf(tripJson.getInt("price")));
            roundtripCheck.setChecked(tripJson.getBoolean("roundTrip"));
            nosmokeCheck.setChecked(tripJson.getBoolean("noSmoke"));

        } catch (Exception e) {
            Toast.makeText(this, "Error parsing trip details.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnEditTrip) {
            String from = fromInput.getText().toString().trim();
            String to = toInput.getText().toString().trim();
            String pickup = pickupInput.getText().toString().trim();
            String date = dateInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            String seatsStr = seatsInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            Boolean roundTrip = roundtripCheck.isChecked();
            Boolean noSmoke = nosmokeCheck.isChecked();

            // Validate date and time
            String dateTime;
            try {
                dateTime = formatDateTime(date, time);
            } catch (ParseException pe) {
                Toast.makeText(this, "Invalid date/time format. Please use 'yyyy-MM-dd' for date and 'HH:mm' for time.", Toast.LENGTH_LONG).show();
                return;
            }

            // Validate seats
            int seats;
            try {
                seats = Integer.parseInt(seatsStr);
                if (seats <= 0) {
                    Toast.makeText(this, "Seats must be a positive number.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Seats must be a valid number.", Toast.LENGTH_LONG).show();
                return;
            }

            // Validate price
            int price;
            try {
                price = Integer.parseInt(priceStr);
                if (price < 0) {
                    Toast.makeText(this, "Price cannot be negative.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, "Price must be a valid number.", Toast.LENGTH_LONG).show();
                return;
            }

            // All validations passed
            TripItem updatedTrip = new TripItem(tripId.intValue(), to, from, dateTime, pickup, userId, seats, price, roundTrip, noSmoke);
            sendUpdatedTripToBackend(updatedTrip);
        }
    }

    private String formatDateTime(String date, String time) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date parsedDate = inputFormat.parse(date + " " + time);
        return outputFormat.format(parsedDate);
    }

    private void sendUpdatedTripToBackend(TripItem trip) {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/trips/mytrip/" + tripId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject tripJson = new JSONObject();
                tripJson.put("driverId", trip.getCreatorUserId());
                tripJson.put("startLocation", trip.getFromLoco());
                tripJson.put("endLocation", trip.getToLoco());
                tripJson.put("pickUp", trip.getPickUpLoco());
                tripJson.put("time", trip.getDateTime());
                tripJson.put("seat", trip.getSeatsAvailable());
                tripJson.put("price", trip.getPrice());
                tripJson.put("roundTrip", trip.getRoundTrip());
                tripJson.put("noSmoke", trip.getNoSmoke());

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = tripJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditTripPage.this, "Trip updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(EditTripPage.this, "Failed to update trip. Response Code: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(EditTripPage.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
