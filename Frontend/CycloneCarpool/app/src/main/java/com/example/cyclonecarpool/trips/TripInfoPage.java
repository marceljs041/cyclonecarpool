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
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cyclonecarpool.chat.ChatItem;
import com.example.cyclonecarpool.chat.MessagingPage;
import com.example.cyclonecarpool.reviews.ReviewPage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The TripInfoPage class represents the activity for displaying detailed information about a trip.
 * This includes information about the driver and passengers, such as their names and ratings.
 * This activity is also where the user can request to join the trip and where the driver can
 * confirm or deny the request.
 *
 * @author Tyler Gorton
 */
public class TripInfoPage extends AppCompatActivity {

    public String role = null;
    public Integer userId = null;

    private Integer tripId;
    private TripItem trip;
    private Long driverId;
    private User driver;
    private List<User> passengers = new ArrayList<>();;

    private LinearLayout passengerContainer;
    private ImageView profileBtn;
    private Button joinButton;

    private List<User> pendingRequests = new ArrayList<>(); // Pending requests


    Gson gson = new Gson();

    /**
     * Performs initialization tasks when the activity is created. Sets up UI elements and event
     * listeners and then calls helper methods to fetch information about the trip and driver from
     * the server.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId") && intent.hasExtra("role")) {
            int userId = intent.getIntExtra("userId", -1); // Default to -1 if not found
            String role = intent.getStringExtra("role");

            // Set the userId and role in HomePage
            this.userId = userId;
            this.role = role;
            if (intent.hasExtra("tripId")) {
                this.tripId = intent.getIntExtra("tripId", -1);
            }
            if (intent.hasExtra("driverId")) {
                this.driverId = (long) intent.getIntExtra("driverId", -1);
            }
        }

        setContentView(R.layout.trip_info_screen);

        passengerContainer = findViewById(R.id.passenger_Container);

        Button reviewButton = findViewById(R.id.btn_trip_review);
        if (userId != null && userId.equals(driverId.intValue())) { // Check if userId matches driverId
            reviewButton.setVisibility(View.GONE);
        }
        reviewButton.setOnClickListener(view -> {
            Intent reviewIntent = new Intent(this, ReviewPage.class);
            reviewIntent.putExtra("tripId", tripId);
            reviewIntent.putExtra("userId", userId);
            reviewIntent.putExtra("role", role);
            startActivity(reviewIntent);
        });

        if (userId != null && !(userId.equals(driverId.intValue()))) { // Check if userId matches driverId
            LinearLayout container = findViewById(R.id.pending_requests_container);
            TextView pending = findViewById(R.id.pendingPassengers);

            container.setVisibility(View.GONE);
            pending.setVisibility(View.GONE);
        }

        joinButton = findViewById(R.id.btn_trip_join);
        if (userId != null && userId.equals(driverId.intValue())) { // Check if userId matches driverId
            joinButton.setVisibility(View.GONE);
        }

        joinButton.setOnClickListener(view -> joinTrip());

        Button homeNavBtn = findViewById(R.id.house_navBtn);
        Button tripsNavBtn = findViewById(R.id.trips_navBtn);
        Button createTripBtn = findViewById(R.id.btnCreate);
        Button messagesTripBtn = findViewById(R.id.messages_navBtn);
        profileBtn = findViewById(R.id.profileBtn);

        homeNavBtn.setOnClickListener(view -> {
            Intent homeIntent = new Intent(this, HomePage.class);
            homeIntent.putExtra("userId", userId);
            homeIntent.putExtra("role", role);
            startActivity(homeIntent);
        });
        tripsNavBtn.setOnClickListener(view -> {
            Intent tripsIntent = new Intent(this, MyTripPage.class);
            tripsIntent.putExtra("userId", userId);
            tripsIntent.putExtra("role", role);
            startActivity(tripsIntent);
        });
        createTripBtn.setOnClickListener(view -> {
            Intent createIntent = new Intent(this, CreateTripPage.class);
            createIntent.putExtra("userId", userId);
            createIntent.putExtra("role", role);
            startActivity(createIntent);
        });
        messagesTripBtn.setOnClickListener(view -> {
            Intent msgIntent = new Intent(this, ChatsPage.class);
            msgIntent.putExtra("userId", userId);
            msgIntent.putExtra("role", role);
            startActivity(msgIntent);
        });
        profileBtn.setOnClickListener(view -> {
            Intent profileIntent = new Intent(this, ProfilePage.class);
            profileIntent.putExtra("userId", userId);
            profileIntent.putExtra("role", role);
            startActivity(profileIntent);
        });

        populatePassengerContainer();
        fetchPendingRequests();

        makeProfilePicReq();
        reqTrip();
        reqDriver();
    }

    /**
     * Fetches trip information from the server. Uses Volley to make the request and calls helper
     * methods in the response handler to parse the JSON response and populate the UI.
     */
    private void reqTrip() {
        JsonObjectRequest tripRequest = new JsonObjectRequest(
                Request.Method.GET,
                API.TRIP_URL + tripId,
                null,
                response -> {
                    Log.d("Trip Info Request", "Response: " + response.toString());
                    try {
                        trip = parseTripJson(response);
                        populateTripInfo();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        JSONArray passengersArray = response.getJSONArray("passengers");
                        passengers = parsePassengersJson(passengersArray);
                        populatePassengerContainer();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.e("Trip Info Request", "Error fetching trip", error);
                    Toast.makeText(TripInfoPage.this, "Error fetching trip", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(tripRequest);
    }

    /**
     * Parses a JSON object representing a trip and returns a TripItem object.
     *
     * @param tripJson The JSON object representing the trip.
     * @return a TripItem object containing the trip details.
     * @throws JSONException If the JSON object does not contain one or more of the required fields.
     */
    private TripItem parseTripJson(JSONObject tripJson) throws JSONException {
        long tripId = tripJson.getLong("tripId");
        long driverId = tripJson.getLong("driverId");
        String startLocation = tripJson.getString("startLocation");
        String endLocation = tripJson.getString("endLocation");
        String pickUp = tripJson.getString("pickUp");
        String time = tripJson.getString("time");
        int seat = tripJson.getInt("seat");
        int price = tripJson.getInt("price");
        boolean roundTrip = tripJson.getBoolean("roundTrip");
        boolean noSmoke = tripJson.getBoolean("noSmoke");

        TripItem trip = new TripItem(
                (int) tripId, startLocation, endLocation, time, pickUp, (int) driverId, seat, price, roundTrip, noSmoke
        );

        return trip;
    }

    /**
     * Populates the UI with the trip details. Uses the <code>trip</code> class variable to access
     * the trip information instead of taking a parameter.
     */
    private void populateTripInfo() {
        TextView tripText = findViewById(R.id.tripText);
        TextView dateTime = findViewById(R.id.am_cst);
        TextView pickUpLoco = findViewById(R.id.pick_up_tbd);
        TextView seatsAvailable = findViewById(R.id.seats_avail);
        TextView priceText = findViewById(R.id.priceText);
        View roundTripIcon = findViewById(R.id.roundTrip);
        TextView roundTripText = findViewById(R.id.round_trip);
        View oneWayIcon = findViewById(R.id.oneWay);
        TextView oneWayText = findViewById(R.id.oneway_trip);
        View noSmokeIcon = findViewById(R.id.noSmoke);
        TextView noSmokeText = findViewById(R.id.nosmoke_text);

        String dateTimeStr = trip.getDateTime();
        String[] parts = dateTimeStr.split("T");

        if (parts.length < 2) {
            Log.e("DateTime Parsing", "Unexpected dateTime format: " + dateTimeStr);
            return;
        }

        String date = parts[0];  // Date part "yyyy-MM-dd"
        String time24 = parts[1];  // Time part "HH:mm:ss"

        // Split the time part and convert to 12-hour format
        String[] timeParts = time24.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        String amPm = (hour >= 12) ? "PM" : "AM";
        hour = (hour == 0) ? 12 : (hour > 12) ? hour - 12 : hour;
        String time12 = String.format("%02d:%02d %s", hour, minute, amPm);


        tripText.setText(trip.getToLoco() + " -> " + trip.getFromLoco());
        dateTime.setText(date + " @ " + time12);
        pickUpLoco.setText("Pick Up: " + trip.getPickUpLoco());
        seatsAvailable.setText(trip.getSeatsAvailable() + " Seats Available");
        priceText.setText("$" + trip.getPrice() + " /");

        // Handle visibility for round trip and one-way
        if (trip.getRoundTrip()) {
            // Show round trip, hide one-way
            roundTripIcon.setVisibility(View.VISIBLE);
            roundTripText.setVisibility(View.VISIBLE);
            oneWayIcon.setVisibility(View.INVISIBLE);
            oneWayText.setVisibility(View.INVISIBLE);
        } else {
            // Show one-way, hide round trip
            roundTripIcon.setVisibility(View.INVISIBLE);
            roundTripText.setVisibility(View.INVISIBLE);
            oneWayIcon.setVisibility(View.VISIBLE);
            oneWayText.setVisibility(View.VISIBLE);
        }

        // Handle visibility for no smoking
        if (trip.getNoSmoke()) {
            noSmokeIcon.setVisibility(View.VISIBLE);
            noSmokeText.setVisibility(View.VISIBLE);
        } else {
            noSmokeIcon.setVisibility(View.INVISIBLE);
            noSmokeText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Parses a JSON array of passengers and returns a list of User objects.
     *
     * @param passengersArray The JSON array representing the passengers.
     * @return a list of User objects representing the passengers.
     * @throws JSONException If any of the objects in the JSONArray do not contain the required fields.
     */
    private List<User> parsePassengersJson(JSONArray passengersArray) throws JSONException {
        List<User> passengers = new ArrayList<>();
        for (int i = 0; i < passengersArray.length(); i++) {
            JSONObject passengerJson = passengersArray.getJSONObject(i);
            User passenger = gson.fromJson(passengerJson.toString(), User.class);
            passengers.add(passenger);
        }
        return passengers;
    }

    /**
     * Populates the UI with the list of passengers. Uses the <code>passengers</code> class variable
     * to access the passenger information instead of taking a parameter.
     */
    private void populatePassengerContainer() {
        passengerContainer.removeAllViews();
        for (User user : passengers) {
            View profileView = LayoutInflater.from(this)
                    .inflate(R.layout.profile_card, passengerContainer, false);

            ImageView userImage = profileView.findViewById(R.id.user_image);
            TextView chatName = profileView.findViewById(R.id.user_name);
            TextView userRating = profileView.findViewById(R.id.user_rating_text);

            String otherUserName = user.getFirstname() + " " + user.getLastname();
            chatName.setText(otherUserName);

            fetchUserRating(user.getIntId(), userRating);

            ImageHelper.loadProfilePic(this, user.getProfilePicture(), userImage);

            Button removeBtn = profileView.findViewById(R.id.remove_passenger_button);
            removeBtn.setOnClickListener(v -> removePassenger(user));
            if (this.userId != null && this.userId.equals(driverId.intValue())) {
                removeBtn.setVisibility(View.VISIBLE);
            }
            else {
                removeBtn.setVisibility(View.GONE);
            }

            passengerContainer.addView(profileView);
        }
    }

    private void removePassenger(User passenger) {
        String removeUrl = "http://coms-3090-029.class.las.iastate.edu:8080/passenger-trip-status/remove/"
                + tripId + "/" + passenger.getIntId();

        StringRequest removeRequest = new StringRequest(
                Request.Method.DELETE,
                removeUrl,
                response -> {
                    Toast.makeText(this, "Passenger removed successfully.", Toast.LENGTH_SHORT).show();
                    // Remove the passenger from the local list and refresh UI
                    passengers.remove(passenger);
                    populatePassengerContainer();
                },
                error -> {
                    Toast.makeText(this, "Failed to remove passenger.", Toast.LENGTH_SHORT).show();
                    Log.e("Remove Passenger", "Error: " + error.toString());
                    if (error.networkResponse != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("Remove Passenger", "Response error body: " + errorBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(removeRequest);
    }

    /**
     * Fetches driver information from the server. Uses Volley to make the request and calls a
     * helper method in the response handler to populate the UI with the driver information.
     */
    private void reqDriver() {
        JsonObjectRequest driverRequest = new JsonObjectRequest(
                Request.Method.GET,
                API.USER_URL + driverId,
                null,
                response -> {
                    Log.d("Driver Info Request", "Response: " + response.toString());
                    try {
                        driver = gson.fromJson(response.toString(), User.class);
                        populateDriverInfo();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.e("Driver Info Request", "Error fetching driver", error);
                    Toast.makeText(TripInfoPage.this, "Error fetching driver", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(driverRequest);
    }

    /**
     * Populates the UI with the driver information. Uses the <code>driver</code> class variable
     * to access the driver information instead of taking a parameter.
     */
    private void populateDriverInfo() {
        ImageView driverImage = findViewById(R.id.driver_image);
        TextView driverName = findViewById(R.id.driver_name);
        TextView driverRating = findViewById(R.id.driver_rating_text);

        driverName.setText(driver.getFullName());

        fetchUserRating(driver.getIntId(), driverRating);

        ImageHelper.loadProfilePic(this, driver.getProfilePicture(), driverImage);
    }

    /**
     * Fetches a user's average rating from the server. Uses Volley to make the request. Takes the
     * ID of the user and the TextView in which to display the rating as parameters.
     *
     * @param userId        The ID of the user to fetch the rating for.
     * @param ratingText    The TextView in which to display the rating.
     */
    private void fetchUserRating(Integer userId, TextView ratingText) {
        StringRequest ratingRequest = new StringRequest(
                Request.Method.GET,
                API.USER_URL + userId + "/review",
                response -> {
                    double rating = Double.parseDouble(response);
                    ratingText.setText(String.valueOf(rating));
                },
                error -> {
                    if (error != null && error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        ratingText.setText("---");
                    } else {
                        Log.e("Rating Request", "Error fetching rating", error);
                        Toast.makeText(TripInfoPage.this, "Error fetching rating", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(ratingRequest);
    }

    /**
     * Attempts to add the current user to the trip. Uses Volley to make the request. References
     * the <code>tripId</code> and <code>userId</code> class variables, so takes no parameters.
     * Assigns the <code>trip</code> and <code>passengers</code> class variables and calls
     * helper methods to populate the UI with the updated information.
     */
    private void joinTrip() {
        // Check if the user is the driver
        if (userId.equals(driverId)) {
            Toast.makeText(this, "Drivers cannot join their own trip.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user is already a passenger
        for (User passenger : passengers) {
            if (passenger.getIntId().equals(userId)) {
                Toast.makeText(this, "You are already a passenger in this trip.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Check if the user already has a pending request
        for (User pendingUser : pendingRequests) {
            if (pendingUser.getIntId().equals(userId)) {
                Toast.makeText(this, "You already have a pending request.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Make a POST request to the backend /request endpoint
        String url = API.PASS_URL + "/request/" + tripId + "/" + userId;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Toast.makeText(this, "Request sent to the driver.", Toast.LENGTH_SHORT).show();
                    joinButton.setVisibility(View.GONE); // Disable the button after requesting
                },
                error -> {
                    Log.e("Join Trip", "Error requesting to join trip", error);
                    Toast.makeText(this, "Failed to send request. Please try again.", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }



    private void fetchPendingRequests() {
        String url = API.PASS_URL + "/pending/" + tripId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        // Attempt to parse as JSON
                        if (response.startsWith("[")) { // Check if it's a JSON array
                            JSONArray pendingArray = new JSONArray(response);
                            pendingRequests.clear();
                            for (int i = 0; i < pendingArray.length(); i++) {
                                JSONObject passengerJson = pendingArray.getJSONObject(i);
                                User pendingUser = gson.fromJson(passengerJson.toString(), User.class);
                                pendingRequests.add(pendingUser);
                            }
                            populatePendingRequests();
                        } else {
                            Log.e("Pending Requests", "Received non-JSON response: " + response);
                            Toast.makeText(this, "No pending requests.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Pending Requests", "Error parsing pending requests", e);
                    }
                },
                error -> Log.e("Pending Requests", "Error fetching pending requests", error)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }



    private void populatePendingRequests() {
        LinearLayout pendingContainer = findViewById(R.id.pending_requests_container);
        pendingContainer.removeAllViews();

        for (User user : pendingRequests) {
            View requestView = LayoutInflater.from(this)
                    .inflate(R.layout.pending_request_item, pendingContainer, false);

            TextView nameText = requestView.findViewById(R.id.pending_user_name);
            Button approveBtn = requestView.findViewById(R.id.approve_request_btn);
            Button denyBtn = requestView.findViewById(R.id.deny_request_btn);

            nameText.setText(user.getFullName());

            approveBtn.setOnClickListener(v -> handleRequest(user, true));
            denyBtn.setOnClickListener(v -> handleRequest(user, false));

            pendingContainer.addView(requestView);
        }
    }


    private void handleRequest(User user, boolean approve) {
        String url = approve ? API.PASS_URL + "/accept" : API.PASS_URL + "/decline";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url + "?tripId=" + tripId + "&passengerId=" + user.getIntId(),
                response -> {
                    if (approve) {
                        passengers.add(user); // Add to passengers list
                        Toast.makeText(this, "Request approved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Request denied.", Toast.LENGTH_SHORT).show();
                    }
                    pendingRequests.remove(user); // Remove from pending list
                    populatePassengerContainer(); // Update passengers UI
                    populatePendingRequests(); // Update pending requests UI
                },
                error -> {
                    Log.e("Handle Request", "Error processing request", error);
                    try {
                        String responseBody = new String(error.networkResponse.data, "UTF-8");
                        Log.e("Handle Request", "Response error: " + responseBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }







    /**
     * Fetches the user's profile picture URL from the server. Uses standard HTTP methods to make
     * the request and calls <code>ImageHelper.loadProfilePic</code> to display the profile picture
     * in the UI.
     */
    private void makeProfilePicReq() {
        new Thread(() -> {
            try {
                URL url = new URL(API.USER_URL + "profilePic/" + userId);
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

                    runOnUiThread(() -> ImageHelper.loadProfilePic(TripInfoPage.this, profilePicUrl, profileBtn));
                } else {
                    Log.e("HTTP Request", "Failed to get profile picture. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("HTTP Request", "Error fetching profile picture", e);
            }
        }).start();
    }
}
