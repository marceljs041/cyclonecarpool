package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity{

    private Button connectBtn1, connectBtn2, connectBtn3, backBtn1, backBtn2, backBtn3;
    private EditText serverEtx1, usernameEtx1, serverEtx2, usernameEtx2, serverEtx3, usernameEtx3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initialize UI elements */
        connectBtn1 = (Button) findViewById(R.id.connectBtn);
        connectBtn2 = (Button) findViewById(R.id.connectBtn2);
        connectBtn3 = (Button) findViewById(R.id.connectBtn3);
        backBtn1 = (Button) findViewById(R.id.backBtn);
        backBtn2 = (Button) findViewById(R.id.backBtn2);
        backBtn3 = (Button) findViewById(R.id.backBtn3);
        serverEtx1 = (EditText) findViewById(R.id.serverEdt);
        usernameEtx1 = (EditText) findViewById(R.id.unameEdt);
        serverEtx2 = (EditText) findViewById(R.id.serverEdt2);
        usernameEtx2 = (EditText) findViewById(R.id.unameEdt2);
        serverEtx3 = (EditText) findViewById(R.id.serverEdt3);
        usernameEtx3 = (EditText) findViewById(R.id.unameEdt3);

        /* connect1 button listener */
        connectBtn1.setOnClickListener(view -> {
            String serverUrl = serverEtx1.getText().toString() + usernameEtx1.getText().toString();

            // start Websocket service with key "chat1"
            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat1");
            serviceIntent.putExtra("url", serverUrl);
            startService(serviceIntent);

            // got to chat activity #1
            Intent intent = new Intent(this, ChatActivity1.class);
            startActivity(intent);
        });

        /* connect2 button listener */
        connectBtn2.setOnClickListener(view -> {
            String serverUrl = serverEtx2.getText().toString() + usernameEtx2.getText().toString();

            // start Websocket service with key "chat2"
            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat2");
            serviceIntent.putExtra("url", serverUrl);
            startService(serviceIntent);

            // got to chat activity #2
            Intent intent = new Intent(this, ChatActivity2.class);
            startActivity(intent);
        });

        /* connect3 button listener */
        connectBtn3.setOnClickListener(view -> {
            String serverUrl = serverEtx3.getText().toString() + usernameEtx3.getText().toString();

            // start Websocket service with key "chat3"
            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat3");
            serviceIntent.putExtra("url", serverUrl);
            startService(serviceIntent);

            // got to chat activity #2
            Intent intent = new Intent(this, ChatActivity3.class);
            startActivity(intent);
        });

        /* back button listener */
        backBtn1.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, ChatActivity1.class);
            startActivity(intent);
        });

        /* back2 button listener */
        backBtn2.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, ChatActivity2.class);
            startActivity(intent);
        });

        /* back3 button listener */
        backBtn3.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, ChatActivity3.class);
            startActivity(intent);
        });
    }
}