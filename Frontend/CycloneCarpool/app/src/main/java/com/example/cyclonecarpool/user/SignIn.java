package com.example.cyclonecarpool.user;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;

import com.example.cyclonecarpool.HomePage;
import com.example.cyclonecarpool.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignIn extends AppCompatActivity {

    private Button signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.signin_screen);

        signInBtn = findViewById(R.id.btnConfirmSignIn);
        signInBtn.setOnClickListener((view) -> {
            EditText emailInput = findViewById(R.id.signin_emailInput);
            String email = emailInput.getText().toString();
            EditText passwordInput = findViewById(R.id.signin_passwordInput);
            String password = passwordInput.getText().toString();
            signInUser(email, password);
        });
    }

    public void errorInfo() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("The password or email doesn't match");
        dlgAlert.setTitle("Unauthorized");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void showErrorDialog(String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle("Error");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void signInUser(final String email, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/users/signin");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);

                    /* Create the JSON object */
                    JSONObject signInData = new JSONObject();
                    signInData.put("email", email);
                    signInData.put("password", password);

                    /* Write JSON data to the output stream */
                    OutputStream os = connection.getOutputStream();
                    os.write(signInData.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject responseJson = new JSONObject(response.toString());
                        int userId = responseJson.getInt("id");  // adjust this based on your JSON structure
                        String role = responseJson.getString("role"); // adjust this based on your JSON structure

                        Intent intent = new Intent(SignIn.this, HomePage.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("role", role);
                        startActivity(intent);
                    } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        runOnUiThread(() -> errorInfo());
                    } else {
                        runOnUiThread(() -> showErrorDialog("An unexpected error occurred. Please try again."));
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> showErrorDialog("An error occurred. Please check your network connection and try again."));
                }
            }
        }).start();
    }
}
