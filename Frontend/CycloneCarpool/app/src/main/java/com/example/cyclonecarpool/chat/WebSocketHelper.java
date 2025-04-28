// WebSocketHelper.java
package com.example.cyclonecarpool.chat;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketHelper {
    private WebSocketClient webSocketClient;
    private String serverUri;
    private WebSocketListener listener;

    public WebSocketHelper(String serverUri, WebSocketListener listener) {
        this.serverUri = serverUri;
        this.listener = listener;
    }

    public void connect() {
        URI uri = URI.create(serverUri);

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.d("WebSocket", "Connected to server");
            }

            @Override
            public void onMessage(String message) {
                listener.onMessageReceived(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d("WebSocket", "Connection closed");
            }

            @Override
            public void onError(Exception ex) {
                Log.e("WebSocket", "Error: " + ex.getMessage());
            }
        };
        webSocketClient.connect();
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        }
    }

    public void close() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public interface WebSocketListener {
        void onMessageReceived(String message);
    }
}
