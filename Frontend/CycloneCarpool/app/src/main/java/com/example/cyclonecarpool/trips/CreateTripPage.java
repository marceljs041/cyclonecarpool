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

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.cyclonecarpool.HomePage;
import com.example.cyclonecarpool.R;

public class CreateTripPage extends AppCompatActivity implements View.OnClickListener {

    private Button createTripBtn;
    private EditText fromInput, toInput, pickupInput, dateInput, timeInput, seatsInput, priceInput;
    private CheckBox roundtripCheck, nosmokeCheck;

    private Integer userId;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.create_trip_screen);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId") && intent.hasExtra("role")) {
            int userId = intent.getIntExtra("userId", -1); // Default to -1 if not found
            String role = intent.getStringExtra("role");

            // Set the userId and role in HomePage
            this.userId = userId;
            this.role = role;
        }

        createTripBtn = findViewById(R.id.btnCreateTrip);
        fromInput = findViewById(R.id.fromCreateInput);
        toInput = findViewById(R.id.toCreateInput);
        pickupInput = findViewById(R.id.pickupCreateInput);
        dateInput = findViewById(R.id.dateCreateInput);
        timeInput = findViewById(R.id.timeCreateInput);
        seatsInput = findViewById(R.id.seatsCreateInput);
        priceInput = findViewById(R.id.priceCreateInput);
        roundtripCheck = findViewById(R.id.checkBoxRoundTrip);
        nosmokeCheck = findViewById(R.id.checkBoxNoSmoke);

        createTripBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnCreateTrip) {
            try {
                String from = fromInput.getText().toString();
                String to = toInput.getText().toString();
                String pickup = pickupInput.getText().toString();
                String date = dateInput.getText().toString();
                String time = timeInput.getText().toString();
                Integer seats = Integer.parseInt(seatsInput.getText().toString());
                Integer price = Integer.parseInt(priceInput.getText().toString());
                Boolean roundTrip = roundtripCheck.isChecked();
                Boolean noSmoke = nosmokeCheck.isChecked();

                // Combine date and time into a single datetime
                String dateTime = formatDateTime(date, time);

                // Check if the date is in the future
                if (!isDateInFuture(dateTime)) {
                    Toast.makeText(this, "Please select a future date and time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create the trip object
                TripItem newTrip = new TripItem(1, to, from, dateTime, pickup, userId, seats, price, roundTrip, noSmoke);

                // Send trip data to the backend
                sendTripToBackend(newTrip);

            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date/time format. Please use 'yyyy-MM-dd' for date and 'HH:mm' for time.", Toast.LENGTH_LONG).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Seats and Price must be valid numbers.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String formatDateTime(String date, String time) throws Exception {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date parsedDate = inputFormat.parse(date + " " + time);
        return outputFormat.format(parsedDate);
    }

    private boolean isDateInFuture(String dateTime) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = format.parse(dateTime);
        return date.after(Calendar.getInstance().getTime());
    }

    private void sendTripToBackend(TripItem trip) {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/trips/home/create");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Convert TripItem to JSON using JSONObject
                JSONObject tripJson = new JSONObject();
                tripJson.put("driverId", (long) trip.getCreatorUserId());
                tripJson.put("startLocation", trip.getFromLoco());
                tripJson.put("endLocation", trip.getToLoco());
                tripJson.put("pickUp", trip.getPickUpLoco());
                tripJson.put("time", trip.getDateTime());
                tripJson.put("seat", trip.getSeatsAvailable());
                tripJson.put("price", trip.getPrice());
                tripJson.put("roundTrip", trip.getRoundTrip());
                tripJson.put("noSmoke", trip.getNoSmoke());

                // Write JSON to output stream
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = tripJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Check the response code
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateTripPage.this, "Trip created successfully!", Toast.LENGTH_SHORT).show();
                        // Redirect to HomePage after successful creation
                        Intent intent = new Intent(CreateTripPage.this, HomePage.class);
                        intent.putExtra("userId", userId); // Pass userId back to HomePage
                        intent.putExtra("role", role);
                        startActivity(intent);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(CreateTripPage.this, "Failed to create trip. Response Code: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(CreateTripPage.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

}
