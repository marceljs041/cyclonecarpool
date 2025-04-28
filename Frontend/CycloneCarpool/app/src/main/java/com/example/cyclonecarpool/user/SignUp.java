package com.example.cyclonecarpool.user;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cyclonecarpool.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignUp extends AppCompatActivity implements View.OnClickListener{

    private Button confirmSignUpBtn;
    private Spinner roleSpinner;
    private JSONArray emailsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.signup_screen);

        makeEmailListReq();

        // Modify the Spinner setup
        roleSpinner = findViewById(R.id.roleSpinner);

        // Create a list of options with a placeholder
        List<String> roleOptions = new ArrayList<>();
        roleOptions.add("Click for dropdown"); // Placeholder
        roleOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.role_options))); // Actual roles

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, roleOptions) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item (placeholder) so it cannot be selected
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                // Gray out the placeholder text
                if (position == 0) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.WHITE);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        roleSpinner.setAdapter(adapter);
        roleSpinner.setSelection(0); // Set the placeholder as the default selection

        confirmSignUpBtn = findViewById(R.id.btnConfirmSignUp);
        confirmSignUpBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnConfirmSignUp) {
            EditText passwordInput = findViewById(R.id.password1Input);
            EditText passwordConfirmInput = findViewById(R.id.password2Input);
            String password = passwordInput.getText().toString();
            String passwordConfirm = passwordConfirmInput.getText().toString();

            if (password.equals(passwordConfirm)) {
                EditText emailInput = findViewById(R.id.emailInput);
                String email = emailInput.getText().toString();

                if (emailsArray != null) {
                    boolean emailExists = false;
                    try {
                        for (int i = 0; i < emailsArray.length(); i++) {
                            String emailList = emailsArray.getString(i);
                            if (email.equals(emailList)) {
                                emailExists = true;
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (emailExists) {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                        dlgAlert.setMessage("There is already an account associated with this email!");
                        dlgAlert.setTitle("Error Message...");
                        dlgAlert.setPositiveButton("OK", null);
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                    } else {
                        EditText firstNameInput = findViewById(R.id.firstNameInput);
                        EditText lastNameInput = findViewById(R.id.lastNameInput);
                        Spinner spinnerInput = findViewById(R.id.roleSpinner);

                        String firstName = firstNameInput.getText().toString();
                        String lastName = lastNameInput.getText().toString();
                        String spinnerValue = spinnerInput.getSelectedItem().toString();

                        // Validate the spinner selection
                        if (spinnerValue.equals("Click for dropdown")) {
                            // Show error if no valid role is selected
                            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                            dlgAlert.setMessage("Please select a valid role from the dropdown.");
                            dlgAlert.setTitle("Error Message...");
                            dlgAlert.setPositiveButton("OK", null);
                            dlgAlert.setCancelable(true);
                            dlgAlert.create().show();
                        } else {
                            // Proceed with account creation
                            createAccount(firstName, lastName, email, password, spinnerValue, "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d");
                            makeEmailListReq();
                        }
                    }
                } else {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                    dlgAlert.setMessage("Unable to retrieve email list. Please try again!");
                    dlgAlert.setTitle("Error Message...");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            } else {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage("The passwords do not match!");
                dlgAlert.setTitle("Error Message...");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        }
    }

    public void makeEmailListReq() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/users/emails");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();

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


                        emailsArray = new JSONArray(response.toString());
                        System.out.println(emailsArray);

                    } else {
                        System.out.println("Error: " + responseCode);
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void createAccount(final String firstName, final String lastName, final String email, final String password, final String role, final String profileUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://coms-3090-029.class.las.iastate.edu:8080/api/users/signup");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);

                    // Create the JSON object with the correct keys
                    JSONObject accountData = new JSONObject();
                    accountData.put("firstname", firstName);  // Lowercase "firstname"
                    accountData.put("lastname", lastName);    // Lowercase "lastname"
                    accountData.put("email", email);          // Lowercase "email"
                    accountData.put("password", password);    // Lowercase "password"
                    accountData.put("role", role);            // Lowercase "role"
                    accountData.put("profilePicture", profileUrl);

                    System.out.println("Sending JSON: " + accountData.toString());

                    // Write JSON data to the output stream
                    OutputStream os = connection.getOutputStream();
                    os.write(accountData.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        runOnUiThread(() -> Toast.makeText(SignUp.this, "Created account successfully!! " + responseCode, Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(SignUp.this, SignIn.class);
                        startActivity(intent);
                    } else {
                        System.out.println("Error: " + responseCode);
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}