package com.example.cyclonecarpool.reviews;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.cyclonecarpool.HomePage;
import com.example.cyclonecarpool.R;
import com.example.cyclonecarpool.chat.ChatsPage;
import com.example.cyclonecarpool.trips.MyTripPage;
import com.example.cyclonecarpool.trips.Trip;
import com.example.cyclonecarpool.user.ProfilePage;
import com.example.cyclonecarpool.user.User;
import com.example.cyclonecarpool.utils.API;
import com.example.cyclonecarpool.utils.ImageHelper;
import com.example.cyclonecarpool.utils.VolleySingleton;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The ReviewPage class represents the activity where a user can review the other participants
 * after a trip. This page can be opened from the TripInfoPage activity.
 *
 * @author Tyler Gorton
 */
public class ReviewPage extends AppCompatActivity {
    private Integer userId;
    private String role;

    private Long tripId;
    private Trip trip;

    private LinearLayout reviewFormContainer;

    private User user, receiver;

    private ImageView profileBtn;

    public static final String LOG_TAG = "ReviewPage";
    Gson gson = new Gson();

    /**
     * Performs initialization tasks when the activity is created. Sets up UI elements and event
     * listeners and then calls helper methods to fetch information about the current user and
     * trip from the server.
     *
     * @param savedInstanceState the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.review_screen);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("tripId") && intent.hasExtra("userId")) {
            tripId = (long) intent.getIntExtra("tripId", -1);
            userId = intent.getIntExtra("userId", -1);
            role = intent.getStringExtra("role");
        }

        reviewFormContainer = findViewById(R.id.reviewForm_Container);

        Button homeNavBtn = findViewById(R.id.house_navBtn);
        Button tripsNavBtn = findViewById(R.id.trips_navBtn);
        Button chatsNavBtn = findViewById(R.id.messages_navBtn);
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
        chatsNavBtn.setOnClickListener(view -> {
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

        fetchUser();
        fetchTripInfo();
    }

    /**
     * Fetches trip information from the server. Uses Volley to make the request and calls helper
     * methods in the response handler to populate the UI with review forms for each participant,
     * excluding the current user.
     */
    private void fetchTripInfo() {
        JsonObjectRequest tripRequest = new JsonObjectRequest(
                Request.Method.GET,
                API.TRIP_URL + tripId,
                null,
                response -> {
                    Log.d(LOG_TAG, "Trip Response: " + response.toString());
                    try {
                        trip = gson.fromJson(response.toString(), Trip.class);
                        if (userId != trip.getDriverId()) {
                            fetchDriver(trip.getDriverId());
                        }
                        for (User user : trip.getPassengers()) {
                            if (Objects.equals(userId, user.getIntId())) {
                                continue;
                            }
                            populateUser(user);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.e(LOG_TAG, "Error fetching trip", error);
                    Toast.makeText(ReviewPage.this, "Error fetching trip", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(tripRequest);
    }

    /**
     * Fetches user information from the server. Uses Volley to make the request and assigns the
     * <code>user</code> class variable in the response handler.
     */
    private void fetchUser() {
        JsonObjectRequest userRequest = new JsonObjectRequest(
                Request.Method.GET,
                API.USER_URL + userId,
                null,
                response -> {
                    Log.d(LOG_TAG, "User Response: " + response.toString());
                    user = gson.fromJson(response.toString(), User.class);
                    ImageHelper.loadProfilePic(this, user.getProfilePicture(), profileBtn);
                },
                error -> {
                    Log.e(LOG_TAG, "Error fetching user", error);
                    Toast.makeText(ReviewPage.this, "Error fetching user", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(userRequest);
    }

    /**
     * Fetches the driver of the trip from the server. Uses Volley to make the request and calls
     * a helper method in the response handler to populate the UI with the driver's information.
     */
    private void fetchDriver(long userId) {
        JsonObjectRequest driverRequest = new JsonObjectRequest(
                Request.Method.GET,
                API.USER_URL + userId,
                null,
                response -> {
                    Log.d(LOG_TAG, "Driver Response: " + response.toString());
                    User driver = gson.fromJson(response.toString(), User.class);
                    populateUser(driver);
                },
                error -> {
                    Log.e(LOG_TAG, "Error fetching driver", error);
                    Toast.makeText(ReviewPage.this, "Error fetching driver", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(driverRequest);
    }

    /**
     * Given a user, populates the UI with the user's information. The resulting UI includes
     * buttons to rate the user from 1-5 stars and a text input field for a review.
     *
     * @param receiver The user to populate the UI with.
     */
    private void populateUser(User receiver) {
        View reviewFormView = LayoutInflater.from(this).inflate(R.layout.review_form, reviewFormContainer, false);

        ImageView userImage = reviewFormView.findViewById(R.id.user_image);
        TextView userName = reviewFormView.findViewById(R.id.user_name);
        View star1 = reviewFormView.findViewById(R.id.star_1);
        View star2 = reviewFormView.findViewById(R.id.star_2);
        View star3 = reviewFormView.findViewById(R.id.star_3);
        View star4 = reviewFormView.findViewById(R.id.star_4);
        View star5 = reviewFormView.findViewById(R.id.star_5);
        EditText reviewInput = reviewFormView.findViewById(R.id.review_input);
        Button reviewSubmitButton = reviewFormView.findViewById(R.id.message_send);

        userName.setText(receiver.getFullName());
        ImageHelper.loadProfilePic(this, receiver.getProfilePicture(), userImage);

        AtomicReference<Integer> rating = new AtomicReference<>(0);

        star1.setOnClickListener(view -> {
            rating.set(1);
            star1.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star2.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
            star3.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
            star4.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
            star5.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
        });
        star2.setOnClickListener(view -> {
            rating.set(2);
            star1.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star2.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star3.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
            star4.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
            star5.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
        });
        star3.setOnClickListener(view -> {
            rating.set(3);
            star1.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star2.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star3.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star4.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
            star5.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
        });
        star4.setOnClickListener(view -> {
            rating.set(4);
            star1.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star2.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star3.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star4.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star5.setBackground(getResources().getDrawable(R.drawable.icon_star, getTheme()));
        });
        star5.setOnClickListener(view -> {
            rating.set(5);
            star1.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star2.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star3.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star4.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
            star5.setBackground(getResources().getDrawable(R.drawable.icon_star_h, getTheme()));
        });

        reviewSubmitButton.setOnClickListener(view -> {
            Review review = new Review(this.user, receiver, trip, reviewInput.getText().toString(), rating.get());
            postReview(review);
        });

        reviewFormContainer.addView(reviewFormView);
    }

    /**
     * Posts a given Review object to the server. Uses Volley to make the request.
     *
     * @param review The Review object to post.
     */
    private void postReview(Review review) {
        StringRequest reviewRequest = new StringRequest(
                Request.Method.POST,
                API.TRIP_URL + "review",
                response -> {
                    Log.d(LOG_TAG, "Post Review Response: " + response.toString());
                },
                error -> {
                    Log.e(LOG_TAG, "Post Review Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
            @Override
            public byte[] getBody() {
                return gson.toJson(review).getBytes(StandardCharsets.UTF_8);
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(reviewRequest);
    }
}
