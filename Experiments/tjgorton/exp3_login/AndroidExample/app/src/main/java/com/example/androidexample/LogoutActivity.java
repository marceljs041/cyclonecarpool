package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LogoutActivity extends AppCompatActivity {

    private Button logoutButton;         // define login button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);            // link to Login activity XML

        /* initialize UI elements */
        logoutButton = findViewById(R.id.logout_logout_btn);

        /* click listener on login button pressed */
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when logout button is pressed, use intent to return to main Activity */
                Intent intent = new Intent(LogoutActivity.this, MainActivity.class);
                startActivity(intent);  // go to MainActivity with the key-value data
            }
        });
    }
}
