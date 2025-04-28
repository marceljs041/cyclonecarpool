package cycloneCarpool.LocationTracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocationWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private DriverLocationService driverLocationService;

    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<WebSocketSession, Long> sessionTripMap = new ConcurrentHashMap<>();

    private static final long MIN_UPDATE_INTERVAL_MS = 60000; // 1 minute
    private static final double MIN_DISTANCE_CHANGE_METERS = 50; // Minimum distance change to update, in meters
    private final ConcurrentHashMap<Long, DriverLocation> lastLocationMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastUpdateTimestampMap = new ConcurrentHashMap<>();


    public void addSessionToTrip(WebSocketSession session, Long tripId) {
        sessionTripMap.put(session, tripId);
    }

    private Long getTripIdForSession(WebSocketSession session) {
        return sessionTripMap.get(session);
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract tripId from URL query parameters
        String tripIdParam = session.getUri().getQuery().split("tripId=")[1];
        Long tripId = Long.parseLong(tripIdParam);
        sessionTripMap.put(session, tripId); // Associate session with tripId

        System.out.println("Connection established for trip ID: " + tripId);
        session.sendMessage(new TextMessage("Connection established. Ready for location updates."));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Received message: " + message.getPayload());

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode locationData = mapper.readTree(message.getPayload());
            double latitude = locationData.get("latitude").asDouble();
            double longitude = locationData.get("longitude").asDouble();

            // Retrieve tripId from session map
            Long tripId = sessionTripMap.get(session);
            if (tripId == null) {
                session.close(CloseStatus.SERVER_ERROR);
                System.err.println("Error: No trip ID associated with this session.");
                return;
            }

            // Conditionally save and broadcast based on location change
            if (shouldSaveOrBroadcast(tripId, latitude, longitude)) {
                // Save location to the database
                driverLocationService.saveLocation(tripId, latitude, longitude);
                System.out.println("Location saved for trip ID: " + tripId);

                // Broadcast location update to all sessions for the same trip
                for (Map.Entry<WebSocketSession, Long> entry : sessionTripMap.entrySet()) {
                    WebSocketSession s = entry.getKey();
                    Long sessionTripId = entry.getValue();
                    if (s.isOpen() && sessionTripId.equals(tripId)) {
                        s.sendMessage(new TextMessage(message.getPayload()));
                    }
                }
                System.out.println("Location broadcasted to trip ID: " + tripId);
            } else {
                System.out.println("Skipped update for trip ID: " + tripId + " due to minimal change.");
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private boolean shouldSaveOrBroadcast(Long tripId, double latitude, double longitude) {
        long currentTime = System.currentTimeMillis();
        Long lastUpdateTime = lastUpdateTimestampMap.get(tripId);
        DriverLocation lastLocation = lastLocationMap.get(tripId);

        // Check if enough time has passed
        if (lastUpdateTime != null && (currentTime - lastUpdateTime) < MIN_UPDATE_INTERVAL_MS) {
            return false; // Not enough time has passed
        }

        // Check if location has changed significantly
        if (lastLocation != null) {
            double distance = calculateDistance(lastLocation.getLatitude(), lastLocation.getLongitude(), latitude, longitude);
            if (distance < MIN_DISTANCE_CHANGE_METERS) {
                return false; // Location change is too small
            }
        }

        // Update the last saved location and time
        lastLocationMap.put(tripId, new DriverLocation(tripId, latitude, longitude, LocalDateTime.now()));
        lastUpdateTimestampMap.put(tripId, currentTime);

        return true;
    }

    // Helper method to calculate the distance between two coordinates in meters
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; // Radius of Earth in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Error in session " + session.getId() + ": " + exception.getMessage());
        // Optionally, close the session if necessary
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("Connection closed: " + session.getId() + " with status " + status);
    }
}
