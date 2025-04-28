package com.example.cyclonecarpool.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cyclonecarpool.utils.API;
import com.example.cyclonecarpool.utils.CircleBorderTransform;
import com.example.cyclonecarpool.HomePage;
import com.example.cyclonecarpool.trips.MyTripPage;
import com.example.cyclonecarpool.R;
import com.example.cyclonecarpool.utils.ImageHelper;
import com.example.cyclonecarpool.utils.VolleySingleton;
import com.example.cyclonecarpool.chat.ChatsPage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The ProfilePage class represents the activity for displaying and managing a user's profile.
 * This is where the user can edit their profile information, including changing their
 * username or email address. It is also where the user can see their average rating.
 *
 * @author Tyler Gorton
 */
public class ProfilePage extends AppCompatActivity implements View.OnClickListener {

    public String role = null;
    public Integer userId = null;
    public User user;

    private Button homeNavBtn, tripsNavBtn, messagesTripBtn, paymentBtn;
    private ImageView profileBtn;

    private ImageView profilePic;
    private TextView profileName, profileEmail, profileRating;
    private EditText profileEditFirst, profileEditLast, profileEditEmail;
    private Button profileEditBtn, profileConfirmBtn, profileDeleteBtn;

    private TextView verificationStatusText;
    private Button submitIdButton;
    private TextView verificationPendingText;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private String uploadedImageUrl; // To store the image URL after uploading
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private boolean isPending = false; // Simulate verification status
    private boolean isVerified = false; // Simulate final verification

    private boolean isEdit = false;

    private Gson gson = new Gson();

    /**
     * Performs initialization tasks when the activity is created. Sets up UI elements and event
     * listeners and then calls helper methods to fetch the user's profile information.
     *
     * @param savedInstanceState The saved state of the activity
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
        }

        // Initialize the gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImage(imageUri);
                        } else {
                            Toast.makeText(this, "Failed to pick image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Initialize the camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        if (photo != null) {
                            Uri imageUri = ImageHelperCamera.getImageUri(this, photo);
                            uploadImage(imageUri);
                        } else {
                            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        setContentView(R.layout.profile_screen);

        homeNavBtn = findViewById(R.id.house_navBtn);
        tripsNavBtn = findViewById(R.id.trips_navBtn);
        messagesTripBtn = findViewById(R.id.messages_navBtn);
        profileBtn = findViewById(R.id.profileBtn);
        paymentBtn = findViewById(R.id.profile_pay_btn);

        profilePic = findViewById(R.id.profile_pic);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profileRating = findViewById(R.id.profile_rating);
        profileEditBtn = findViewById(R.id.profile_edit_btn);

        profileEditFirst = findViewById(R.id.profile_edit_fname);
        profileEditLast = findViewById(R.id.profile_edit_lname);
        profileEditEmail = findViewById(R.id.profile_edit_email);
        profileConfirmBtn = findViewById(R.id.profile_confirm_btn);
        profileDeleteBtn = findViewById(R.id.profile_delete_btn);
        verificationStatusText = findViewById(R.id.profile_verification_status);
        submitIdButton = findViewById(R.id.btn_submit_id);
        verificationPendingText = findViewById(R.id.profile_verification_pending);

        submitIdButton.setOnClickListener(v -> showIdSubmissionPopup());

        homeNavBtn.setOnClickListener(this);
        tripsNavBtn.setOnClickListener(this);
        messagesTripBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);
        profileEditBtn.setOnClickListener(this);
        profileConfirmBtn.setOnClickListener(this);
        profileDeleteBtn.setOnClickListener(this);
        paymentBtn.setOnClickListener(this);

        makeProfilePicReq();
        reqProfile();
        fetchRating();
    }


    /**
     * Handles click events on UI elements within this activity. For the navigation buttons, this
     * method navigates to the corresponding activity. This method also handles button interactions
     * relating to editing or deleting the profile.
     *
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.house_navBtn) {
            Intent intent = new Intent(this, HomePage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if(id == R.id.trips_navBtn) {
            Intent intent = new Intent(this, MyTripPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if(id == R.id.profile_pay_btn) {
            Intent intent = new Intent(this, Payments.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.messages_navBtn) {
            Intent intent = new Intent(this, ChatsPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.profileBtn) {

        } else if (id == R.id.profile_edit_btn) {
            toggleEdit();
        } else if (id == R.id.profile_confirm_btn) {
            putProfile();
        } else if (id == R.id.profile_delete_btn) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteProfile())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    /**
     * Toggles the visibility of the profile edit fields and buttons.
     */
    private void toggleEdit() {
        if (isEdit) {
            profileName.setVisibility(View.VISIBLE);
            profileEmail.setVisibility(View.VISIBLE);

            profileEditFirst.setVisibility(View.INVISIBLE);
            profileEditLast.setVisibility(View.INVISIBLE);
            profileEditEmail.setVisibility(View.INVISIBLE);

            profileEditBtn.setVisibility(View.VISIBLE);
            profileDeleteBtn.setVisibility(View.INVISIBLE);
            profileConfirmBtn.setVisibility(View.INVISIBLE);

            isEdit = false;
        } else {
            profileName.setVisibility(View.INVISIBLE);
            profileEmail.setVisibility(View.INVISIBLE);

            profileEditFirst.setVisibility(View.VISIBLE);
            profileEditLast.setVisibility(View.VISIBLE);
            profileEditEmail.setVisibility(View.VISIBLE);

            profileEditBtn.setVisibility(View.INVISIBLE);
            profileDeleteBtn.setVisibility(View.VISIBLE);
            profileConfirmBtn.setVisibility(View.VISIBLE);

            isEdit = true;
        }
    }

    /**
     * Populates the profile fields with the user's information.
     *
     * @param profile The user object containing the profile information.
     */
    private void populateProfile(User profile) {
        profileName.setText(String.format("%s %s", profile.getFirstname(), profile.getLastname()));
        profileEmail.setText(profile.getEmail());
        ImageHelper.loadProfilePic(this, profile.getProfilePicture(), profilePic);
    }

    /**
     * Fetches the user's profile information from the server. Takes the ID of the user whose
     * profile is to be fetched as a parameter. Uses Volley to make the request and calls the
     * populateProfile method to populate the profile fields.
     *
     * @param userId the ID of the user profile to fetch
     */
    private void reqProfile(int userId) {
        JsonObjectRequest profileRequest = new JsonObjectRequest(
                Request.Method.GET,
                API.USER_URL + userId,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("[User] Volley Response", response.toString());
                        user = gson.fromJson(String.valueOf(response), User.class);
                        populateProfile(user);
                    }
                },
                error -> Log.e("[Profile] Volley Error", error.toString())
        );
        VolleySingleton.getInstance(getApplicationContext())
                       .addToRequestQueue(profileRequest);
    }

    public void reqProfile() {
        try {
            reqProfile(userId);
        } catch (Exception e) {}
    }

    /**
     * Fetches the user's average rating from the server. Takes the ID of the user for which to
     * fetch the rating as a parameter. Uses Volley to make the request.
     *
     * @param userId the ID of the user for which to fetch the rating
     */
    private void fetchRating(int userId) {
        StringRequest ratingRequest = new StringRequest(
                Request.Method.GET,
                API.USER_URL + userId + "/review",
                response -> {
                    double rating = Double.parseDouble(response);
                    profileRating.setText(String.format("%s%s", profileRating.getText().toString(), rating));
                },
                error -> {
                    if (error != null && error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        profileRating.setText(String.format("%s%s", profileRating.getText().toString(), "---"));
                    } else {
                        Log.e("Rating Request", "Error fetching rating", error);
                    }
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(ratingRequest);
    }

    public void fetchRating() {
        try {
            fetchRating(userId);
        } catch (Exception e) {}
    }

    /**
     * Updates the user's profile information on the server. Gets the updated information from the
     * input fields, then uses Volley to make the request and calls the populateProfile method in
     * the response handler to refresh the UI.
     */
    private void putProfile() {
        String newFirstName = (profileEditFirst.getText().length() > 0) ?
                profileEditFirst.getText().toString()
                : user.getFirstname();
        String newLastName = (profileEditLast.getText().length() > 0) ?
                profileEditLast.getText().toString()
                : user.getLastname();
        String newEmail = (profileEditEmail.getText().length() > 0) ?
                profileEditEmail.getText().toString()
                : user.getEmail();
        String newProfilePic = (user.getProfilePicture() == null) ?
                "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d"
                : user.getProfilePicture();
        User newUser = new User(user.getId(), newFirstName, newLastName, newEmail, user.getPassword(), user.getRole(), newProfilePic);
        Log.d("[Profile] Put User", gson.toJson(newUser));

        JsonObjectRequest profilePutRequest = new JsonObjectRequest(
                Request.Method.PUT,
                API.USER_URL + "editProfile/" + userId,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        user = gson.fromJson(response.toString(), User.class);
                        populateProfile(user);
                    }
                },
                error -> {
                    if (error.getMessage() != null) {
                        Log.e("[Profile] Volley Error", error.getMessage());
                    } else {
                        Log.e("[Profile] Volley Error", error.toString());
                    }
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
                return gson.toJson(newUser).getBytes(StandardCharsets.UTF_8);
            }
        };
        VolleySingleton.getInstance(getApplicationContext())
                       .addToRequestQueue(profilePutRequest);
        toggleEdit();
    }

    /**
     * Deletes the user's profile from the server. Uses standard HTTP methods to make the request
     * and navigates back to the WelcomePage activity after successful deletion.
     */
    private void deleteProfile() {
        new Thread(() -> {
            try {
                // Replace with your backend URL
                URL url = new URL(API.USER_URL + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    runOnUiThread(() -> {
                        // Profile deleted successfully, maybe show a toast or navigate away
                        Toast.makeText(this, "Profile deleted successfully.", Toast.LENGTH_SHORT).show();
                        // Navigate to a different activity if needed
                        Intent intent = new Intent(ProfilePage.this, WelcomePage.class);
                        startActivity(intent);
                        finish();
                    });
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Profile not found.", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Failed to delete profile. Try again.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("Delete Profile", "Error deleting profile", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error deleting profile. Try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
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

                    runOnUiThread(() -> ImageHelper.loadProfilePic(ProfilePage.this, profilePicUrl, profileBtn));
                } else {
                    Log.e("HTTP Request", "Failed to get profile picture. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("HTTP Request", "Error fetching profile picture", e);
            }
        }).start();
    }

    private void showIdSubmissionPopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.id_submission_popup);
        dialog.setCancelable(true);

        Button btnCamera = dialog.findViewById(R.id.btn_camera);
        Button btnGallery = dialog.findViewById(R.id.btn_gallery);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnCamera.setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });

        btnGallery.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                uploadImage(imageUri);
            } else if (requestCode == CAMERA_REQUEST_CODE && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Uri imageUri = ImageHelperCamera.getImageUri(this, photo);
                uploadImage(imageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference imageRef = storageReference.child("ids/" + System.currentTimeMillis() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String uploadedImageUrl = uri.toString();
                                sendIdToBackend(uploadedImageUrl);
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
                                Log.e("ImageUpload", "Error getting URL", e);
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                        Log.e("ImageUpload", "Upload failed", e);
                    });
        }
    }

    private void sendIdToBackend(String imageUrl) {
        String url = null;
        try {
            url = "http://coms-3090-029.class.las.iastate.edu:8080/api/identity/verify"
                    + "?imageUrl=" + imageUrl
                    + "&userId=" + userId
                    + "&firstName=" + URLEncoder.encode(user.getFirstname(),"UTF-8")
                    + "&lastName=" + URLEncoder.encode(user.getLastname(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        Log.d("IDSubmission", "Final URL: " + imageUrl);
        Log.d("IDSubmission", "Final URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    Log.d("IDSubmission", "Response: " + response.toString());
                    Toast.makeText(this, "ID verification submitted.", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("IDSubmission", "Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("IDSubmission", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("IDSubmission", "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Failed to submit ID for verification.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    private void submitId() {
        isPending = true;
        updateVerificationStatus();
        Toast.makeText(this, "ID Submitted. Pending verification.", Toast.LENGTH_SHORT).show();

        // Simulate backend processing
        new Handler().postDelayed(() -> {
            isPending = false;
            isVerified = true;
            updateVerificationStatus();
        }, 5000); // 5 seconds delay for simulation
    }

    private void updateVerificationStatus() {
        if (isVerified) {
            verificationStatusText.setText("Verification Status: Verified");
            verificationStatusText.setTextColor(Color.GREEN);
            submitIdButton.setVisibility(View.GONE);
            verificationPendingText.setVisibility(View.GONE);
        } else if (isPending) {
            verificationStatusText.setText("Verification Status: Pending");
            verificationStatusText.setTextColor(Color.YELLOW);
            verificationPendingText.setVisibility(View.VISIBLE);
            submitIdButton.setVisibility(View.GONE);
        } else {
            verificationStatusText.setText("Verification Status: Not Verified");
            verificationStatusText.setTextColor(Color.RED);
            submitIdButton.setVisibility(View.VISIBLE);
            verificationPendingText.setVisibility(View.GONE);
        }
    }

}
