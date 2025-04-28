package cycloneCarpool.Notification;

import cycloneCarpool.Trips.PassengerTripStatus;
import cycloneCarpool.Trips.PassengerTripStatusService;
import cycloneCarpool.Users.UserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.*;

/**
 * @author eddiegong
 */

/**
 * Code Review: Anthony Campana
 * Using websockets for handling real time notifications
 * Uses a hashtable to map session to user id
 * handles onOpen, onNotification, onClose, and onError well using supporting methods if needed.
 */

// WebSocket for Notifications.
@Controller
@ServerEndpoint(value = "/notification/{userId}")
public class NotificationSocket {

    private static UserRepository userRepo;

    @Autowired
    public void setUserRepository(UserRepository repo) {
        userRepo = repo;
    }

    private static NotificationService notificationService; // Use the service for persistence
    @Autowired
    public void setNotificationService(NotificationService service) {
        notificationService = service;
    }

    private static PassengerTripStatusService passengerTripStatusService;
    @Autowired
    public void setPassengerTripStatusService(PassengerTripStatusService service) {
        passengerTripStatusService = service;
    }

    private static final Logger logger = LoggerFactory.getLogger(NotificationSocket.class);

    private static Map<Session, Long> sessionUserIdMap = new Hashtable<>();
    private static Map<Long, Session> userIdSessionMap = new Hashtable<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) throws IOException {
        userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("Connection opened for userId: {}", userId);

        sessionUserIdMap.put(session, userId);
        userIdSessionMap.put(userId, session);

        List<Notification> unsentNotifications = notificationService.getUnsentNotifications(userId);

        // Send each unsent notification to the user
        for (Notification notification : unsentNotifications) {
            sendNotification(notification);
        }
    }

    @OnMessage
    public void onNotification(Session session, String ptsId) {
        Long passengerTripStatusId = Long.parseLong(ptsId);

        // Lookup PassengerTripStatus by ID
        PassengerTripStatus passengerTripStatus = passengerTripStatusService.getPassengerTripStatusById(passengerTripStatusId);

        String status = passengerTripStatus.getStatus();

        logger.info("Handling notification of {} request", status);

        // Framework implemented for notifying other passengers in the same trip.
        List<Notification> notifications = new ArrayList<>();

        if (Objects.equals(status, "PENDING")) {
            notifications.add(notificationService.notifyDriverOfRequest(passengerTripStatus));
        } else if (Objects.equals(status, "ACCEPTED")) {
            notifications.add(notificationService.notifyPassengerOfApproval(passengerTripStatus));
        } else if (Objects.equals(status, "REJECTED")) {
            notifications.add(notificationService.notifyPassengerOfDecline(passengerTripStatus));
        }

        for (Notification notification : notifications) {
            sendNotification(notification);
        }
    }

    private void sendNotification(Notification notification) {
        Long receiverId = notification.getReceiver().getId();
        Session session = userIdSessionMap.get(receiverId);

        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(notification.getContent());
                notificationService.notified(notification);

            } catch (IOException e) {
                logger.error("Failed to send notification to userId: {}", receiverId, e);

            }
        } else {
            logger.info("No active session found for userId: {}.", receiverId);
        }

    }

    @OnClose
    public void onClose(Session session, @PathParam("tripId") String tripId) throws IOException {
        Long userId = sessionUserIdMap.get(session);
        userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        sessionUserIdMap.remove(session);
        userIdSessionMap.remove(userId);
        logger.info("Connection closed for userId: {}", userId);

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error in WebSocket session {}", session.getId(), throwable);
    }
}


