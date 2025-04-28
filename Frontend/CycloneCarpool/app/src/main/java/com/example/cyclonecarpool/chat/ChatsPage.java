package com.example.cyclonecarpool.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cyclonecarpool.utils.API;
import com.example.cyclonecarpool.utils.CircleBorderTransform;
import com.example.cyclonecarpool.trips.CreateTripPage;
import com.example.cyclonecarpool.HomePage;
import com.example.cyclonecarpool.trips.MyTripPage;
import com.example.cyclonecarpool.user.ProfilePage;
import com.example.cyclonecarpool.R;
import com.example.cyclonecarpool.user.User;
import com.example.cyclonecarpool.utils.ImageHelper;
import com.example.cyclonecarpool.utils.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatsPage extends AppCompatActivity implements View.OnClickListener, WebSocketHelper.WebSocketListener {

    public static String role = null;
    public Integer userId = null;

    private Button homeNavBtn, tripsNavBtn, createTripBtn, messagesTripBtn, profilePageBtn;
    private LinearLayout chatContainer;
    private ImageView profileBtn;
    private ScrollView chatScrollView;
    private View plus;

    private Gson gson = new Gson();
    private WebSocketHelper webSocketHelper;
    private String webSocketUrl;

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

        setContentView(R.layout.chats_screen);

        chatContainer = findViewById(R.id.chat_Container);
        chatScrollView = findViewById(R.id.chatsScrollView);

        homeNavBtn = findViewById(R.id.house_navBtn);
        tripsNavBtn = findViewById(R.id.trips_navBtn);
        createTripBtn = findViewById(R.id.btnCreate);
        messagesTripBtn = findViewById(R.id.messages_navBtn);
        profileBtn = findViewById(R.id.profileBtn);
        plus = findViewById(R.id.plus_solid);

        homeNavBtn.setOnClickListener(this);
        tripsNavBtn.setOnClickListener(this);
        createTripBtn.setOnClickListener(this);
        messagesTripBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);

        reqChats();

        webSocketUrl = "ws://coms-3090-029.class.las.iastate.edu:8080/chats/" + userId;
        webSocketHelper = new WebSocketHelper(webSocketUrl, this);
        webSocketHelper.connect();

        setupRoleBasedUI();
        makeProfilePicReq();
    }

    @Override
    public void onMessageReceived(String message) {
        runOnUiThread(() -> {
            try {
                JSONObject msgObj = new JSONObject(message);
                String content = msgObj.getString("content");
                Integer otherUserId = msgObj.getInt("senderId") == userId ? msgObj.getInt("receiverId") : msgObj.getInt("senderId");
                String otherUserName = msgObj.getString("senderName");  // Assuming the name is provided
                updateChatPreview(otherUserId, otherUserName, content);
            } catch (JSONException e) {
                Log.e("ChatsPage", "Error parsing WebSocket message", e);
            }
        });
    }

    private void updateChatPreview(Integer otherUserId, String otherUserName, String lastMessage) {
        for (int i = 0; i < chatContainer.getChildCount(); i++) {
            View chatItemView = chatContainer.getChildAt(i);
            TextView chatName = chatItemView.findViewById(R.id.chat_name);
            if (chatName.getTag().equals(otherUserId)) {
                TextView chatPreview = chatItemView.findViewById(R.id.chat_preview);
                chatPreview.setText(lastMessage);
                return;
            }
        }
        // If chat not found, create a new chat item
        View chatItemView = LayoutInflater.from(this).inflate(R.layout.chat_item, chatContainer, false);
        TextView chatName = chatItemView.findViewById(R.id.chat_name);
        chatName.setText(otherUserName);
        chatName.setTag(otherUserId);
        TextView chatPreview = chatItemView.findViewById(R.id.chat_preview);
        chatPreview.setText(lastMessage);

        chatItemView.setOnClickListener(v -> {
            Intent intent = new Intent(ChatsPage.this, MessagingPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("otherId", otherUserId);
            System.out.println("1");
            startActivity(intent);
        });
        chatContainer.addView(chatItemView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketHelper.close();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.house_navBtn) {
            Intent intent = new Intent(this, HomePage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.trips_navBtn) {
            Intent intent = new Intent(this, MyTripPage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        } else if (id == R.id.btnCreate) {
            if (!role.equals("Passenger")) {
                Intent intent = new Intent(ChatsPage.this, CreateTripPage.class);
                intent.putExtra("userId", userId); // Pass userId to CreateTripPage
                startActivity(intent);
            }
        } else if (id == R.id.messages_navBtn) {
            chatContainer.removeAllViews();
//            populateChatContainer();
            reqChats();
        } else if (id == R.id.profileBtn) {
            Intent intent = new Intent(this, ProfilePage.class);
            intent.putExtra("userId", userId);
            intent.putExtra("role", role);
            startActivity(intent);
        }
    }

    private void setupRoleBasedUI() {
        // Hide the Create button if role is Passenger
        if (role.equals("Passenger")) {
            createTripBtn.setVisibility(View.GONE);
            plus.setVisibility(View.GONE);
        }

        // Enable delete functionality if role is Admin
        else if (role.equals("Admin")) {
            createTripBtn.setVisibility(View.VISIBLE); // Admins can create trips

        } else if (role.equals("Driver")) {
            createTripBtn.setVisibility(View.VISIBLE); // Drivers can create trips
        }
    }

    private void populateChatContainer() {
        List<ChatItem> chats = getChats();

        populateChatContainer(chats);
    }

    private void populateChatContainer(List<ChatItem> chats) {
        for (ChatItem chat : chats) {
            View chatItemView = LayoutInflater.from(this)
                    .inflate(R.layout.chat_item, chatContainer, false);

            View chatImage = chatItemView.findViewById(R.id.chat_image);
            TextView chatName = chatItemView.findViewById(R.id.chat_name);
            TextView chatPreview = chatItemView.findViewById(R.id.chat_preview);
            Button chatButton = chatItemView.findViewById(R.id.chat_open);

            String otherUserName = chat.getOtherName();
            chatName.setText(otherUserName);

            String lastMessage = chat.getLastMessage();
            chatPreview.setText(lastMessage);

            chatButton.setOnClickListener(view -> {
                Intent intent = new Intent(ChatsPage.this, MessagingPage.class);
                intent.putExtra("userId", userId);
                intent.putExtra("role", role);
                intent.putExtra("otherId", chat.getOtherId());
                startActivity(intent);
            });

            chatContainer.addView(chatItemView);
        }
    }

    private List<ChatItem> getChats() {
        List<ChatItem> chats = new ArrayList<>();
        chats.add(new ChatItem(
                0,
                "Marcel S.",
                "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d",
                "Knowledge is what remains after one has forgotten..."
        ));
        chats.add(new ChatItem(
                0,
                "Marcel S.",
                "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d",
                "Knowledge is what remains after one has forgotten..."
        ));
        chats.add(new ChatItem(
                0,
                "Marcel S.",
                "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d",
                "Knowledge is what remains after one has forgotten..."
        ));
        chats.add(new ChatItem(
                0,
                "Marcel S.",
                "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d",
                "Knowledge is what remains after one has forgotten..."
        ));
        chats.add(new ChatItem(
                0,
                "Marcel S.",
                "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d",
                "Knowledge is what remains after one has forgotten..."
        ));
        chats.add(new ChatItem(
                0,
                "Marcel S.",
                "https://firebasestorage.googleapis.com/v0/b/cyclonecarpool.appspot.com/o/placeholderprofilepic.png?alt=media&token=1e53480d-f70e-4be3-8e73-03c5c8fe4b3d",
                "Knowledge is what remains after one has forgotten..."
        ));
        return chats;
    }

    private void reqChats() {
        JsonArrayRequest chatRequest = new JsonArrayRequest(
                Request.Method.GET,
                API.MSG_URL + "conversation/all/" + userId,
                null,
                response -> {
                    Map<Integer, Message> latestMessagesByTrip = new HashMap<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            // Get the JSON object for the current message
                            JSONObject messageJson = response.getJSONObject(i);

                            // Extract the tripId directly from the JSON object
                            Integer tripId = messageJson.getInt("tripId");

                            // Proceed only if tripId is valid
                            if (tripId != null) {
                                // Deserialize the message JSON into a Message object
                                Message msg = gson.fromJson(messageJson.toString(), Message.class);

                                // Get the timestamp of the current message
                                Timestamp msgTimestamp = (Timestamp) msg.getTimestamp();

                                // Retrieve any existing message for this tripId
                                Message existingMessage = latestMessagesByTrip.get(tripId);
                                Timestamp existingTimestamp = existingMessage != null
                                        ? (Timestamp) existingMessage.getTimestamp()
                                        : null;

                                // Update the map if this message is more recent
                                if (existingTimestamp == null || (msgTimestamp != null && msgTimestamp.getTime() > existingTimestamp.getTime())) {
                                    latestMessagesByTrip.put(tripId, msg); // Store the latest message for this tripId
                                }
                            } else {
                                Log.e("[Chats] Null tripId", "Skipping message due to missing trip ID");
                            }
                        } catch (JSONException e) {
                            Log.e("[Chats] JSON Error", e.toString());
                        }
                    }

                    // Populate the chat container with one entry per unique tripId
                    List<ChatItem> chats = new ArrayList<>();
                    for (Map.Entry<Integer, Message> entry : latestMessagesByTrip.entrySet()) {
                        Integer tripId = entry.getKey();
                        Message latestMessage = entry.getValue();
                        String chatTitle = "Trip " + tripId;  // Title for the chat item

                        // Parse the content field to extract the actual message text
                        String content;
                        try {
                            JSONObject contentJson = new JSONObject(latestMessage.getContent());
                            content = contentJson.getString("content");
                        } catch (JSONException e) {
                            content = latestMessage.getContent();
                        }

                        // Add a single chat item for this unique tripId with the latest message content
                        chats.add(new ChatItem(tripId, chatTitle, null, content));
                    }

                    populateChatContainer(chats); // Populate UI with only unique trip entries
                },
                error -> Log.e("[Chats] Volley Error", error.toString())
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(chatRequest);
    }


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

                    runOnUiThread(() -> ImageHelper.loadProfilePic(ChatsPage.this, profilePicUrl, profileBtn));
                } else {
                    Log.e("HTTP Request", "Failed to get profile picture. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("HTTP Request", "Error fetching profile picture", e);
            }
        }).start();
    }

}
