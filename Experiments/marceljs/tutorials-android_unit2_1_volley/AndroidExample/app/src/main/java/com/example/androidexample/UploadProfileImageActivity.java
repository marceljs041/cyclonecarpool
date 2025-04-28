package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64; // Added missing import
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request; // Added missing import
import com.android.volley.toolbox.StringRequest; // Added missing import
import com.android.volley.toolbox.Volley; // Added missing import

import java.io.ByteArrayOutputStream;
import java.io.IOException; // Added missing import
import java.util.HashMap;
import java.util.Map;

public class UploadProfileImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int STORAGE_PERMISSION_CODE = 3; // Added constant for storage permission
    private ImageView profileImageView;
    private Button chooseImageButton, uploadImageButton;
    private Uri imageUri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_image);

        profileImageView = findViewById(R.id.profileImageView);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);

        // Choose image from gallery or camera
        chooseImageButton.setOnClickListener(v -> showImagePickerOptions());

        // Upload image to server
        uploadImageButton.setOnClickListener(v -> uploadImage());
    }

    private void showImagePickerOptions() {
        // Check for camera and storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    CAMERA_REQUEST);
        } else {
            // Show options to pick image
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            String pickTitle = "Select or capture a new profile picture";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == CAMERA_REQUEST){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImagePickerOptions();
            } else {
                Toast.makeText(this, "Camera and storage permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void uploadImage() {
        if (bitmap == null && imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert image to Base64 string
        Bitmap imageToUpload = bitmap;
        if (imageToUpload == null) {
            try {
                imageToUpload = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to get image from URI", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageToUpload.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        final String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Send the image to the server
        String url = "https://yourserver.com/upload";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Handle server response
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Handle error
                    Toast.makeText(this, "Upload failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", encodedImage);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(this).add(request);
    }

    // Handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                // Image picked from gallery
                imageUri = data.getData();
                profileImageView.setImageURI(imageUri);
                bitmap = null; // Reset bitmap
            } else if (data != null && data.getExtras() != null) {
                // Image captured from camera
                bitmap = (Bitmap) data.getExtras().get("data");
                profileImageView.setImageBitmap(bitmap);
                imageUri = null; // Reset imageUri
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
