package com.example.cyclonecarpool.trips;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cyclonecarpool.chat.MessagingPage;
import com.example.cyclonecarpool.user.User;
import com.example.cyclonecarpool.utils.API;
import com.example.cyclonecarpool.utils.CircleBorderTransform;
import com.example.cyclonecarpool.HomePage;
import com.example.cyclonecarpool.R;
import com.example.cyclonecarpool.chat.ChatsPage;
import com.example.cyclonecarpool.user.ProfilePage;
import com.example.cyclonecarpool.utils.ImageHelper;
import com.example.cyclonecarpool.utils.VolleySingleton;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyTripPage extends AppCompatActivity implements View.OnClickListener {

    public String role = null;
    public Integer userId = null;

    private Button homeNavBtn, tripsNavBtn, createTripBtn, messagesTripBtn, profilePageBtn;
    private LinearLayout tripContainer;
    private ImageView profileBtn;
    private View plus;

    private ScrollView tripsScrollView;
    private List<TripItem> trips = new ArrayList<>();

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId") && intent.hasExtra("role")) {
            userId = intent.getIntExtra("userId", -1);
            role = intent.getStringExtra("role");
        }

        setContentView(R.layout.mytrips_screen);

        homeNavBtn = findViewById(R.id.house_navBtn);
        tripsNavBtn = findViewById(R.id.trips_navBtn_h);
        createTripBtn = findViewById(R.id.btnCreate);
        messagesTripBtn = findViewById(R.id.messages_navBtn);
        tripContainer = findViewById(R.id.trip_Container);
        profileBtn = findViewById(R.id.profileBtn);
        plus = findViewById(R.id.plus_solid);

        homeNavBtn.setOnClickListener(this);
        tripsNavBtn.setOnClickListener(this);
        createTripBtn.setOnClickListener(this);
        messagesTripBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);

        setupRoleBasedUI();
        makeProfilePicReq();
        makeMyTripsListReq();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.house_navBtn) {
            Intent intent = new Intent(MyTripPage.this, HomePage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.trips_navBtn_h) {
            // Already on MyTripPage, no action needed or refresh
            makeMyTripsListReq();
        } else if (id == R.id.btnCreate) {
            if (!role.equals("Passenger")) {
                Intent intent = new Intent(MyTripPage.this, CreateTripPage.class);
                intent.putExtra("userId", userId);
                intent.putExtra("role", role);
                startActivity(intent);
            }
        } else if (id == R.id.messages_navBtn) {
            Intent intent = new Intent(MyTripPage.this, ChatsPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.profileBtn) {
            Intent intent = new Intent(this, ProfilePage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        }
    }

    private void setupRoleBasedUI() {
        // Hide the Create button if role is Passenger
        if (role.equals("Passenger")) {
            createTripBtn.setVisibility(View.GONE);
            plus.setVisibility(View.GONE);
        }

        // Enable delete functionality if role is Admin
        else if (role.equals("Admin")) {
            createTripBtn.setVisibility(View.VISIBLE); // Admins can create trips

        } else if (role.equals("Driver")) {
            createTripBtn.setVisibility(View.VISIBLE); // Drivers can create trips
        }
    }

    private void makeMyTripsListReq() {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/trips/mytrip");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                // Create JSON object with driverId for filtering trips
                JSONObject driverIdJson = new JSONObject();
                driverIdJson.put("driverId", userId);

                // Write JSON body to output stream
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = driverIdJson.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse the JSON response and update trips list
                    trips = parseTripsJson(response.toString());

                    runOnUiThread(() -> {
                        tripContainer.removeAllViews();
                        populateTripContainer();
                    });
                } else {
                    Log.e("HTTP Request", "Failed to fetch trips. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("HTTP Request", "Error fetching trips", e);
            }
        }).start();
    }

    private List<TripItem> parseTripsJson(String json) throws Exception {
        List<TripItem> tripsList = new ArrayList<>();
        JSONArray tripsArray = new JSONArray(json);

        for (int i = 0; i < tripsArray.length(); i++) {
            JSONObject tripObject = tripsArray.getJSONObject(i);

            long tripId = tripObject.getLong("tripId");
            long driverId = tripObject.getLong("driverId");
            String startLocation = tripObject.getString("startLocation");
            String endLocation = tripObject.getString("endLocation");
            String pickUp = tripObject.getString("pickUp");
            String time = tripObject.getString("time");
            int seat = tripObject.getInt("seat");
            int price = tripObject.getInt("price");
            boolean roundTrip = tripObject.getBoolean("roundTrip");
            boolean noSmoke = tripObject.getBoolean("noSmoke");

            TripItem trip = new TripItem(
                    (int) tripId, startLocation, endLocation, time, pickUp, (int) driverId, seat, price, roundTrip, noSmoke
            );

            tripsList.add(trip);
        }
        return tripsList;
    }

    private void populateTripContainer() {
        tripContainer.removeAllViews();

        for (TripItem trip : trips) {
            View tripItemView = LayoutInflater.from(this).inflate(R.layout.trip_item, tripContainer, false);

            View tripCard = tripItemView.findViewById(R.id.trip_card);
            TextView tripText = tripItemView.findViewById(R.id.tripText);
            TextView dateTime = tripItemView.findViewById(R.id.am_cst);
            TextView pickUpLoco = tripItemView.findViewById(R.id.pick_up_tbd);
            TextView seatsAvailable = tripItemView.findViewById(R.id.seats_avail);
            TextView priceText = tripItemView.findViewById(R.id.priceText);
            View roundTripIcon = tripItemView.findViewById(R.id.roundTrip);
            View oneWayIcon = tripItemView.findViewById(R.id.oneWay);
            View noSmokeIcon = tripItemView.findViewById(R.id.noSmoke);
            Button editBtn = tripItemView.findViewById(R.id.btnEdit);
            Button deleteBtn = tripItemView.findViewById(R.id.btnDelete); // Reference to the Delete button

            reqDriver(trip.getCreatorUserId(), tripItemView);

            tripText.setText(trip.getToLoco() + " -> " + trip.getFromLoco());
            dateTime.setText(trip.getDateTime());
            pickUpLoco.setText("Pick Up: " + trip.getPickUpLoco());
            seatsAvailable.setText(trip.getSeatsAvailable() + " Seats Available");
            priceText.setText("$" + trip.getPrice());

            editBtn.setVisibility(View.VISIBLE);
            editBtn.setTranslationZ(5);
            deleteBtn.setVisibility(View.VISIBLE); // Make the Delete button visible
            deleteBtn.setTranslationZ(5);

            roundTripIcon.setVisibility(trip.getRoundTrip() ? View.VISIBLE : View.INVISIBLE);
            oneWayIcon.setVisibility(trip.getRoundTrip() ? View.INVISIBLE : View.VISIBLE);
            noSmokeIcon.setVisibility(trip.getNoSmoke() ? View.VISIBLE : View.INVISIBLE);

            // Set onClickListener for the trip text
            tripText.setOnClickListener(v -> {
                Intent tripIntent = new Intent(MyTripPage.this, TripInfoPage.class);
                tripIntent.putExtra("tripId", trip.getTripId());
                tripIntent.putExtra("userId", userId);
                tripIntent.putExtra("role", role);
                tripIntent.putExtra("driverId", trip.getCreatorUserId());
                startActivity(tripIntent);
            });

            // Set OnClickListener for the Edit button
            editBtn.setOnClickListener(v -> {
                Intent editIntent = new Intent(MyTripPage.this, EditTripPage.class);
                editIntent.putExtra("tripId", trip.getTripId());
                editIntent.putExtra("userId", userId);
                editIntent.putExtra("role", role);
                startActivity(editIntent);
            });

            // Set OnClickListener for the Delete button
            deleteBtn.setOnClickListener(v -> makeDeleteTripRequest(trip.getTripId()));

            tripContainer.addView(tripItemView);
        }
    }

    private void makeDeleteTripRequest(int tripId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/trips/mytrip/" + tripId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {  // 204 No Content for successful delete
                    runOnUiThread(() -> {
                        Toast.makeText(MyTripPage.this, "Trip deleted successfully!", Toast.LENGTH_SHORT).show();
                        makeMyTripsListReq(); // Refresh the trip list
                    });
                } else {
                    Log.e("HTTP Request", "Failed to delete trip. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("HTTP Request", "Error deleting trip", e);
            }
        }).start();
    }

    public void reqDriver(Integer driverId, View tripItemView) {
        JsonObjectRequest driverRequest = new JsonObjectRequest(
                Request.Method.GET,
                API.USER_URL + driverId,
                null,
                response -> {
                    Log.d("Driver Info Request", "Response: " + response.toString());
                    try {
                        User driver = gson.fromJson(response.toString(), User.class);
                        populateDriverInfo(driver, tripItemView);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.e("Driver Info Request", "Error fetching driver", error);
                    Toast.makeText(MyTripPage.this, "Error fetching driver", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(driverRequest);
    }

    private void populateDriverInfo(User driver, View tripItemView) {
        ImageView driverImage = tripItemView.findViewById(R.id.driver_pic);
        TextView driverName = tripItemView.findViewById(R.id.driver_name);
        View messageButton = tripItemView.findViewById(R.id.messageBtn);

        ImageHelper.loadProfilePic(this, driver.getProfilePicture(), driverImage);
        driverName.setText(driver.getFirstname());
        messageButton.setOnClickListener(view -> {
            Intent intent = new Intent(MyTripPage.this, MessagingPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            intent.putExtra("otherId", driver.getIntId());
            startActivity(intent);
        });
    }

    private void makeProfilePicReq() {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/users/profilePic/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String profilePicUrl = response.toString().trim();

                    runOnUiThread(() -> loadProfilePic(profilePicUrl));
                } else {
                    Log.e("HTTP Request", "Failed to get profile picture. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("HTTP Request", "Error fetching profile picture", e);
            }
        }).start();
    }

    private void loadProfilePic(String profilePicUrl) {
        int borderColor = Color.BLACK;
        int borderWidth = 6;

        Glide.with(this)
                .load(profilePicUrl)
                .apply(RequestOptions.bitmapTransform(new CircleBorderTransform(borderColor, borderWidth))
                        .placeholder(R.drawable.icon_round_trip)
                        .error(R.drawable.icon_one_way))
                .into(profileBtn);
    }
}
