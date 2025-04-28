package com.example.cyclonecarpool;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;


import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cyclonecarpool.chat.ChatsPage;
import com.example.cyclonecarpool.chat.MessagingPage;
import com.example.cyclonecarpool.trips.CreateTripPage;
import com.example.cyclonecarpool.trips.MyTripPage;
import com.example.cyclonecarpool.trips.TripInfoPage;
import com.example.cyclonecarpool.trips.TripItem;
import com.example.cyclonecarpool.user.ProfilePage;
import com.example.cyclonecarpool.user.User;
import com.example.cyclonecarpool.utils.API;
import com.example.cyclonecarpool.utils.CircleBorderTransform;
import com.example.cyclonecarpool.utils.ImageHelper;
import com.example.cyclonecarpool.utils.VolleySingleton;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomePage extends AppCompatActivity implements View.OnClickListener {

    public String role = null;
    public Integer userId = null;
    private boolean userVerified = false;

    private Button homeNavBtn, tripsNavBtn, createTripBtn, messagesTripBtn, profilePageBtn;
    private LinearLayout tripContainer;
    private ImageView profileBtn;

    private View searchBarClosed, polygon2, polygon3, plus;
    private LinearLayout searchBarExpanded, searchBarToggle;
    private ScrollView tripsScrollView;
    private boolean isExpanded = false;

    private EditText fromSearchInput, toSearchInput;
    private Button searchBtn;

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
            userId = intent.getIntExtra("userId", -1); // Default to -1 if not found
            role = intent.getStringExtra("role");
        }

        setContentView(R.layout.home_screen);

        homeNavBtn = findViewById(R.id.house_navBtn_h);
        tripsNavBtn = findViewById(R.id.trips_navBtn);
        createTripBtn = findViewById(R.id.btnCreate);
        messagesTripBtn = findViewById(R.id.messages_navBtn);
        tripContainer = findViewById(R.id.trip_Container);
        profileBtn = findViewById(R.id.profileBtn);
        searchBarClosed = findViewById(R.id.searchBarClosed);
        searchBarExpanded = findViewById(R.id.searchBarExpanded);
        searchBarToggle = findViewById(R.id.searchBarToggle);
        polygon2 = findViewById(R.id.polygon_2);
        polygon3 = findViewById(R.id.polygon_3);
        tripsScrollView = findViewById(R.id.tripsScrollView);
        plus = findViewById(R.id.plus_solid);

        fromSearchInput = findViewById(R.id.fromSearchInput);
        toSearchInput = findViewById(R.id.toSearchInput);
        searchBtn = findViewById(R.id.searchBtn);

        homeNavBtn.setOnClickListener(this);
        tripsNavBtn.setOnClickListener(this);
        createTripBtn.setOnClickListener(this);
        messagesTripBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);
        searchBarToggle.setOnClickListener(v -> toggleSearchBar());
        searchBtn.setOnClickListener(this);

        setupRoleBasedUI();
        updateConstraints(false);
        makeProfilePicReq();
        makeTripsListReq();
        checkIsVerified();
    }

    private boolean isUserVerified() {
        return userVerified;
    }

    private void checkIsVerified() {
        String url = API.USER_URL + userId + "/isVerified";
        JsonObjectRequest verifyRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("IsVerified", "Response: "+ response.toString());
                    try {
                        // The response JSON: { "userId": 123, "verified": "true" or "false" }
                        String verifiedStr = response.getString("verified");
                        userVerified = verifiedStr.equalsIgnoreCase("true");
                    } catch (JSONException e) {
                        Log.e("IsVerified", "JSON parsing error", e);
                    }
                },
                error -> {
                    Log.e("IsVerified", "Error: " + error.toString());
                    // In case of error, userVerified remains false
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(verifyRequest);
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

    private void showVerificationPopup() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.verification_popup);
        dialog.setCancelable(false);

        // Set up the dialog components
        Button btnVerify = dialog.findViewById(R.id.btn_verify);
        Button btnClose = dialog.findViewById(R.id.btn_close_popup);

        btnVerify.setOnClickListener(v -> {
            // Navigate to ProfilePage
            Intent intent = new Intent(HomePage.this, ProfilePage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();

        // Set the dialog window size and background
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // Check verification before handling clicks
        if (!isUserVerified()) {
            showVerificationPopup();
            return;
        }

        if (id == R.id.house_navBtn_h) {
            tripContainer.removeAllViews();
            makeTripsListReq();
        } else if (id == R.id.trips_navBtn) {
            Intent intent = new Intent(HomePage.this, MyTripPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.btnCreate) {
            if (!role.equals("Passenger")) { // Only Drivers and Admins can create trips
                Intent intent = new Intent(HomePage.this, CreateTripPage.class);
                intent.putExtra("userId", userId);
                intent.putExtra("role", role);
                startActivity(intent);
            }
        } else if (id == R.id.messages_navBtn) {
            Intent intent = new Intent(HomePage.this, ChatsPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.profileBtn) {
            Intent intent = new Intent(this, ProfilePage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.searchBtn) {
            String fromLoco = fromSearchInput.getText().toString().trim();
            String toLoco = toSearchInput.getText().toString().trim();
            makeFilteredTripListReq(toLoco, fromLoco);
        }
    }


    private void toggleSearchBar() {
        if (isExpanded) {
            collapseSearchBar();
        } else {
            expandSearchBar();
        }
        isExpanded = !isExpanded;
        updateConstraints(isExpanded);
    }

    private void expandSearchBar() {
        searchBarExpanded.setVisibility(View.VISIBLE);
        searchBarClosed.setVisibility(View.GONE);

        // Animation for expansion
        searchBarExpanded.measure(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = searchBarExpanded.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            searchBarExpanded.getLayoutParams().height = (int) animation.getAnimatedValue();
            searchBarExpanded.requestLayout();
        });
        animator.setDuration(300);
        animator.start();

        // Rotate arrows to point upwards
        polygon2.animate().rotation(180).setDuration(300).start();
        polygon3.animate().rotation(180).setDuration(300).start();
    }

    private void collapseSearchBar() {
        final int initialHeight = searchBarExpanded.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            searchBarExpanded.getLayoutParams().height = (int) animation.getAnimatedValue();
            searchBarExpanded.requestLayout();
        });
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchBarExpanded.setVisibility(View.GONE);
                searchBarClosed.setVisibility(View.VISIBLE);
            }
        });
        animator.start();

        // Rotate arrows back to original position (pointing down)
        polygon2.animate().rotation(0).setDuration(300).start();
        polygon3.animate().rotation(0).setDuration(300).start();
    }

    private void updateConstraints(boolean isExpanded) {
        ConstraintLayout.LayoutParams toggleParams = (ConstraintLayout.LayoutParams) searchBarToggle.getLayoutParams();
        ConstraintLayout.LayoutParams scrollParams = (ConstraintLayout.LayoutParams) tripsScrollView.getLayoutParams();
        ViewGroup.LayoutParams scrollHeightParams = tripsScrollView.getLayoutParams();
        Integer height = 757;

        if (isExpanded) {
            toggleParams.bottomToBottom = searchBarExpanded.getId();
            scrollParams.topToBottom = searchBarExpanded.getId();
            height = 705;
            scrollHeightParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
        } else {
            toggleParams.bottomToBottom = searchBarClosed.getId();
            scrollParams.topToBottom = searchBarClosed.getId();
            height = 751;
            scrollHeightParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
        }

        searchBarToggle.setLayoutParams(toggleParams);
        tripsScrollView.setLayoutParams(scrollParams);
        tripsScrollView.setLayoutParams(scrollHeightParams);
        tripsScrollView.requestLayout();
    }

    private void populateTripContainer() {
        tripContainer.removeAllViews();

        for (TripItem trip : trips) {
            View tripItemView = LayoutInflater.from(this).inflate(R.layout.trip_item, tripContainer, false);

            Button deleteButton = tripItemView.findViewById(R.id.btnDelete);
            deleteButton.setVisibility(role.equals("Admin") ? View.VISIBLE : View.GONE); // Show delete only for Admin

            // Set up delete button action
            if (role.equals("Admin")) {
                deleteButton.setOnClickListener(view -> {
                    // Logic to delete trip goes here
                    makeDeleteTripRequest(trip.getTripId());
                });
            }

            TextView tripText = tripItemView.findViewById(R.id.tripText);
            TextView dateTime = tripItemView.findViewById(R.id.am_cst);
            TextView pickUpLoco = tripItemView.findViewById(R.id.pick_up_tbd);
            TextView seatsAvailable = tripItemView.findViewById(R.id.seats_avail);
            TextView priceText = tripItemView.findViewById(R.id.priceText);
            View roundTripIcon = tripItemView.findViewById(R.id.roundTrip);
            TextView roundTripText = tripItemView.findViewById(R.id.round_trip);
            View oneWayIcon = tripItemView.findViewById(R.id.oneWay);
            TextView oneWayText = tripItemView.findViewById(R.id.oneway_trip);
            View noSmokeIcon = tripItemView.findViewById(R.id.noSmoke);
            TextView noSmokeText = tripItemView.findViewById(R.id.nosmoke_text);
            View messageButton = tripItemView.findViewById(R.id.messageBtn);

            // Get driver info
            Integer driverId = trip.getCreatorUserId();
            reqDriver(driverId, tripItemView);

            String dateTimee = trip.getDateTime();
            String[] parts = dateTimee.split("T");

            if (parts.length < 2) {
                Log.e("DateTime Parsing", "Unexpected dateTime format: " + dateTimee);
                continue; // Skip this trip if the format is unexpected
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

            // Set onClickListener for the trip text
            tripText.setOnClickListener(v -> {
                if (!isUserVerified()) {
                    showVerificationPopup();
                    return;
                }

                Intent tripIntent = new Intent(HomePage.this, TripInfoPage.class);
                tripIntent.putExtra("tripId", trip.getTripId());
                tripIntent.putExtra("userId", userId);
                tripIntent.putExtra("role", role);
                tripIntent.putExtra("driverId", trip.getCreatorUserId());
                startActivity(tripIntent);
            });

            messageButton.setOnClickListener(view -> {
                if (!isUserVerified()) {
                    showVerificationPopup();
                    return;
                }

                Intent intent = new Intent(HomePage.this, MessagingPage.class);
                intent.putExtra("userId", userId);
                intent.putExtra("role", role);
                intent.putExtra("otherId", trip.getTripId());
                startActivity(intent);
            });

            tripContainer.addView(tripItemView);
        }
    }

    public void makeTripsListReq() {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/trips/home");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
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

                    // Parse the JSON response and update trips list
                    trips = parseTripsJson(response.toString());

                    runOnUiThread(this::populateTripContainer);

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

    public void makeFilteredTripListReq(String toLoco, String fromLoco) {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/trips/home/filter");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                // Create JSON object with the filter parameters
                JSONObject filterJson = new JSONObject();
                if (!fromLoco.isEmpty()) {
                    filterJson.put("startLocation", fromLoco);
                } else {
                    filterJson.put("startLocation", JSONObject.NULL);
                }
                if (!toLoco.isEmpty()) {
                    filterJson.put("endLocation", toLoco);
                } else {
                    filterJson.put("endLocation", JSONObject.NULL);
                }

                // Write JSON body to output stream
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = filterJson.toString().getBytes("utf-8");
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

                    // Clear container and populate with filtered trips on UI thread
                    runOnUiThread(() -> {
                        tripContainer.removeAllViews();
                        populateTripContainer();
                    });
                } else {
                    Log.e("HTTP Request", "Failed to fetch filtered trips. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("HTTP Request", "Error fetching filtered trips", e);
            }
        }).start();
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
                        Toast.makeText(HomePage.this, "Trip deleted successfully!", Toast.LENGTH_SHORT).show();
                        makeTripsListReq(); // Refresh the trip list
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
        String url = API.USER_URL + driverId;
        Log.d("Driver Info Request", "Requesting driver info from: " + url);

        JsonObjectRequest driverRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("Driver Info Request", "Response: " + response.toString());
                    try {
                        User driver = gson.fromJson(response.toString(), User.class);
                        populateDriverInfo(driver, tripItemView);
                    } catch (Exception e) {
                        Log.e("Driver Parsing Error", "Error parsing driver info", e);
                    }
                },
                error -> {
                    Log.e("Driver Info Request", "Error fetching driver", error);
                    if (error.networkResponse != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("Driver Info Request", "Response error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("Driver Info Request", "Error parsing error body", e);
                        }
                    }
                    Toast.makeText(HomePage.this, "Error fetching driver", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(driverRequest);
    }


    private void populateDriverInfo(User driver, View tripItemView) {
        ImageView driverImage = tripItemView.findViewById(R.id.driver_pic);
        TextView driverName = tripItemView.findViewById(R.id.driver_name);

        ImageHelper.loadProfilePic(this, driver.getProfilePicture(), driverImage);
        driverName.setText(driver.getFirstname());
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
                    String inputLine;
                    StringBuilder response = new StringBuilder();

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

