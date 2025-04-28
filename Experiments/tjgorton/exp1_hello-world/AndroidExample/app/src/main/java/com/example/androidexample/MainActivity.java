package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

	private TextView greetingText; // define message textview variable
	private EditText nameText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // link to Main activity XML

		/* initialize UI elements */
		nameText = findViewById(R.id.plain_text_input);
		greetingText = findViewById(R.id.main_msg_txt); // link to message textview in the Main activity XML

		nameText.setText("World");
		greetingText.setText(String.format("Hello %s", nameText.getText()));

		/* Sync greeting to name */
		nameText.addTextChangedListener(new TextWatcher() {
			// No action needed before text is changed
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Update TextView with the text from EditText
				greetingText.setText(String.format("Hello %s", nameText.getText()));
			}

			// No action needed after text is changed
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// greetingText.setText("Hello World!");
	}
}