package com.example.cyclonecarpool.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

import com.example.cyclonecarpool.R;

public class WelcomePage extends AppCompatActivity implements View.OnClickListener {

    private Button signInBtn, signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.welcome_screen);

        signInBtn = findViewById(R.id.btnSignIn);
        signUpBtn = findViewById(R.id.btnSignUp);

        signInBtn.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSignIn) {
            startActivity(new Intent(WelcomePage.this, SignIn.class));
        }
        else if (id == R.id.btnSignUp) {
            startActivity(new Intent(WelcomePage.this, SignUp.class));
        }
    }
}