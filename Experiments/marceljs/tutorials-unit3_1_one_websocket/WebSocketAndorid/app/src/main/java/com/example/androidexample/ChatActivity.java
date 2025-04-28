package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements WebSocketListener {

    private Button sendBtn;
    private EditText msgEtx;
    private TextView msgTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* Initialize UI elements */
        sendBtn = findViewById(R.id.sendBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);

        /* Connect this activity to the WebSocket instance */
        WebSocketManager.getInstance().setWebSocketListener(ChatActivity.this);

        /* Send button listener */
        sendBtn.setOnClickListener(v -> {
            try {
                // Send message
                String message = msgEtx.getText().toString();
                WebSocketManager.getInstance().sendMessage(message);

                // Add the sent message to the UI with a single timestamp
                addMessageToView("You: " + message);

                // Clear the message input field
                msgEtx.setText("");
            } catch (Exception e) {
                Log.d("ExceptionSendMessage:", e.getMessage());
            }
        });
    }

    /* Adds only your sent messages to the TextView with a single timestamp */
    private void addMessageToView(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String formattedMessage = "[" + timestamp + "] " + message;
        runOnUiThread(() -> {
            String existingText = msgTv.getText().toString();
            msgTv.setText(existingText + "\n" + formattedMessage);
        });
    }

    @Override
    public void onWebSocketMessage(String message) {
        // Do nothing as we only want to display sent messages.
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nConnection closed by " + closedBy + "\nReason: " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n---\nConnection established with the server\n---");
        });
    }

    @Override
    public void onWebSocketError(Exception ex) {
        /* Show user-friendly error message */
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nError: Unable to connect\nDetails: " + ex.getMessage() + "\n---");
        });
        Log.e("WebSocketError:", ex.getMessage());
    }
}
