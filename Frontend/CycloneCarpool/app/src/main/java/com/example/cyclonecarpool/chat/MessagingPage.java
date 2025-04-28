package com.example.cyclonecarpool.chat;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cyclonecarpool.utils.API;
import com.example.cyclonecarpool.utils.CircleBorderTransform;
import com.example.cyclonecarpool.trips.CreateTripPage;
import com.example.cyclonecarpool.HomePage;
import com.example.cyclonecarpool.trips.MyTripPage;
import com.example.cyclonecarpool.user.ProfilePage;
import com.example.cyclonecarpool.R;
import com.example.cyclonecarpool.utils.ImageHelper;
import com.example.cyclonecarpool.utils.VolleySingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MessagingPage extends AppCompatActivity implements WebSocketHelper.WebSocketListener, View.OnClickListener {
	private WebSocketHelper webSocketHelper;
	private String webSocketUrl;

	public String role = null;
	public Integer userId = null;
	public Integer otherUserId;

	private Button homeNavBtn, tripsNavBtn, messagesTripBtn;
	private ImageButton paymentDashboardBtn;
	private LinearLayout messageContainer;
	private ScrollView messageScrollView;
	private ImageView profileBtn;

	private EditText msgInput;
	private Button msgSendBtn, msgEditConfirmBtn;

	private Long messageId;
	private final Gson gson = new Gson();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getSupportActionBar().hide();

		Intent intent = getIntent();
		if (intent != null && intent.hasExtra("userId") && intent.hasExtra("role")) {
			userId = intent.getIntExtra("userId", -1);
			role = intent.getStringExtra("role");
			otherUserId = intent.getIntExtra("otherId", -1);
			System.out.println(otherUserId);
		}

		setContentView(R.layout.message_screen);

		messageContainer = findViewById(R.id.message_Container);
		messageScrollView = findViewById(R.id.messageScrollView);

		homeNavBtn = findViewById(R.id.house_navBtn);
		tripsNavBtn = findViewById(R.id.trips_navBtn);
		messagesTripBtn = findViewById(R.id.messages_navBtn);
		profileBtn = findViewById(R.id.profileBtn);

		msgInput = findViewById(R.id.message_input);
		msgSendBtn = findViewById(R.id.message_send);
		msgEditConfirmBtn = findViewById(R.id.message_edit_confirm);

		homeNavBtn.setOnClickListener(this);
		tripsNavBtn.setOnClickListener(this);
		messagesTripBtn.setOnClickListener(this);
		profileBtn.setOnClickListener(this);

		msgSendBtn.setOnClickListener(view -> {
			String msgText = msgInput.getText().toString();
			if (!msgText.isEmpty()) {
				sendMessageWebSocket(msgText);
				msgInput.setText("");
			}
		});

		msgEditConfirmBtn.setOnClickListener(view -> {
			String newContent = msgInput.getText().toString();
			editMessageReq(messageId, newContent);
		});

		paymentDashboardBtn = findViewById(R.id.payment_dashboard_btn);
		paymentDashboardBtn.setOnClickListener(view -> openPaymentDashboard());

		// Fetch message history before establishing WebSocket connection
		fetchMessageHistory();

		// WebSocket URL setup and connection
		webSocketUrl = "ws://coms-3090-029.class.las.iastate.edu:8080/chat/" + otherUserId + "/" + userId;
		webSocketHelper = new WebSocketHelper(webSocketUrl, this);
		webSocketHelper.connect();

		makeProfilePicReq();
	}

	private void openPaymentDashboard() {
		Dialog paymentDialog = new Dialog(this);
		paymentDialog.setContentView(R.layout.popup_payment_dashboard);
		paymentDialog.setCancelable(true);

		TextView title = paymentDialog.findViewById(R.id.payment_status_title);
		RecyclerView paymentStatusList = paymentDialog.findViewById(R.id.payment_status_list);
		paymentStatusList.setLayoutManager(new LinearLayoutManager(this));

		// Fetch payment statuses
		fetchPaymentStatuses(paymentStatusList, paymentDialog, title);
		paymentDialog.show();
	}

	private void fetchPaymentStatuses(RecyclerView paymentStatusList, Dialog paymentDialog, TextView title) {
		String role = getIntent().getStringExtra("role");
		Long tripId = (long) otherUserId;

		if (tripId == -1) {
			Toast.makeText(this, "Invalid trip ID", Toast.LENGTH_SHORT).show();
			paymentDialog.dismiss();
			return;
		}

		String url = API.PASS_URL + "/paymentStatus/all/" + tripId;

		JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
				response -> {
					List<PaymentStatusItem> paymentStatuses = new ArrayList<>();
					try {
						for (int i = 0; i < response.length(); i++) {
							JSONObject obj = response.getJSONObject(i);
							String name = obj.optString("passengerName", "Unknown");
							String status = obj.optString("paymentStatus", "Unknown");
							paymentStatuses.add(new PaymentStatusItem(name, status));
						}
					} catch (JSONException e) {
						Log.e("PaymentPopup", "Error parsing payment status", e);
					}

					// Update RecyclerView
					PaymentStatusAdapter adapter = new PaymentStatusAdapter(paymentStatuses);
					paymentStatusList.setAdapter(adapter);

					// Update title for role
					if ("Driver".equalsIgnoreCase(role)) {
						title.setText("Passenger's Payment Status");
					} else {
						title.setText("Your Payment Status");
					}
				},
				error -> {
					Log.e("PaymentPopup", "Error fetching payment statuses", error);
					Toast.makeText(this, "Failed to load payment statuses.", Toast.LENGTH_SHORT).show();
					paymentDialog.dismiss();
				});

		VolleySingleton.getInstance(this).addToRequestQueue(request);
	}


	private void fetchMessageHistory() {
		String historyUrl = API.MSG_URL + "conversation/" + userId + "/" + otherUserId;
		JsonArrayRequest historyRequest = new JsonArrayRequest(
				Request.Method.GET,
				historyUrl,
				null,
				response -> {
					try {
						for (int i = 0; i < response.length(); i++) {
							MessageItem msgItem = gson.fromJson(response.get(i).toString(), MessageItem.class);

							// Check if the logged-in user is the sender
							boolean isSender = msgItem.getSenderId().equals(userId);

							// Extract message content, handling nested JSON if necessary
							String content;
							try {
								JSONObject contentObj = new JSONObject(msgItem.getMessage());
								content = contentObj.getString("content");
							} catch (JSONException e) {
								content = msgItem.getMessage();
							}

							// Display the message aligned based on sender status
							addMessageToUI(content, isSender);
						}
					} catch (JSONException e) {
						Log.e("MessagingPage", "Error parsing history", e);
					}
				},
				error -> Log.e("MessagingPage", "Error fetching message history", error)
		);
		VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(historyRequest);
	}



	private void sendMessageWebSocket(String messageContent) {
		JSONObject messageObj = new JSONObject();
		try {
			messageObj.put("senderId", userId);
			messageObj.put("receiverId", otherUserId);
			messageObj.put("content", messageContent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		webSocketHelper.sendMessage(messageObj.toString());
	}

	@Override
	public void onMessageReceived(String message) {
		runOnUiThread(() -> {
			try {
				// Separate the prefix from the JSON content using regex to find the JSON part
				int jsonStartIndex = message.indexOf("{");
				if (jsonStartIndex == -1) {
					// No JSON found, display message as plain text
					addMessageToUI(message, false);
					return;
				}

				// Extract the sender name (prefix) and JSON content
				String prefix = message.substring(0, jsonStartIndex).trim();
				String jsonContent = message.substring(jsonStartIndex);

				// Parse the JSON part
				JSONObject msgObj = new JSONObject(jsonContent);
				String content = msgObj.getString("content");
				boolean isSender = msgObj.getInt("senderId") == userId;

				// Display the message in the format "Prefix: Message Content"
				addMessageToUI(prefix + ": " + content, isSender);

			} catch (JSONException e) {
				Log.e("MessagingPage", "Error parsing incoming WebSocket message", e);
				// Display the raw message as fallback if parsing fails
				addMessageToUI(message, false);
			}
		});
	}




	private void addMessageToUI(String messageText, boolean isSender) {
		// Inflate the appropriate layout based on sender status
		View msgItemView = LayoutInflater.from(this)
				.inflate(isSender ? R.layout.message_sent_item : R.layout.message_received_item, messageContainer, false);

		TextView messageTextView = msgItemView.findViewById(R.id.message);
		messageTextView.setText(messageText);

		messageContainer.addView(msgItemView);
		messageScrollView.post(() -> messageScrollView.fullScroll(View.FOCUS_DOWN)); // Auto-scroll to latest message
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		webSocketHelper.close();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		Intent intent;
		if (id == R.id.house_navBtn) {
			intent = new Intent(this, HomePage.class);
		} else if (id == R.id.trips_navBtn) {
			intent = new Intent(this, MyTripPage.class);
		} else if (id == R.id.messages_navBtn) {
			intent = new Intent(this, ChatsPage.class);
		} else if (id == R.id.profileBtn) {
			intent = new Intent(this, ProfilePage.class);
		} else {
			return;
		}
		intent.putExtra("userId", userId);
		intent.putExtra("role", role);
		startActivity(intent);
	}

	private void editMessageReq(Long msgId, String newMsg) {
		StringRequest editMsgRequest = new StringRequest(
				Request.Method.PUT,
				String.format(API.MSG_URL + "edit/%d?newContent=%s", msgId, newMsg),
				response -> Log.d("[Messages] Volley Response", response),
				error -> Log.e("[Messages] Volley Error", error.toString())
		);
		VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(editMsgRequest);
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
					runOnUiThread(() -> ImageHelper.loadProfilePic(MessagingPage.this, profilePicUrl, profileBtn));
				} else {
					Log.e("HTTP Request", "Failed to get profile picture. Response Code: " + responseCode);
				}
			} catch (Exception e) {
				Log.e("HTTP Request", "Error fetching profile picture", e);
			}
		}).start();
	}
}
