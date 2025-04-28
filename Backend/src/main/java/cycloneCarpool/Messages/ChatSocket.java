package cycloneCarpool.Messages;

import cycloneCarpool.Trips.TripService;
import cycloneCarpool.Users.User;
import cycloneCarpool.Users.UserRepository;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Controller
@ServerEndpoint(value = "/chat/{tripId}/{userId}")
public class ChatSocket {

    private static final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

    private static Map<Session, Long> sessionUserIdMap = new Hashtable<>();
    private static Map<Long, Session> userIdSessionMap = new Hashtable<>();

    private TripService tripService;
    private UserRepository userRepo;
    private MessageRepository msgRepo;

    @OnOpen
    public void onOpen(Session session, @PathParam("tripId") Long tripId, @PathParam("userId") Long userId) throws IOException {
        if (tripService == null || userRepo == null || msgRepo == null) {
            this.tripService = ChatSocketConfig.getBean(TripService.class);
            this.userRepo = ChatSocketConfig.getBean(UserRepository.class);
            this.msgRepo = ChatSocketConfig.getBean(MessageRepository.class);
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("Connection opened for tripId: " + tripId + ", userId: " + userId);

        // Store session and user info
        sessionUserIdMap.put(session, userId);
        userIdSessionMap.put(userId, session);

        // Send chat history to the connected user
        sendChatHistory(session, tripId);

        // Broadcast that a new user has joined
        broadcastToTrip(tripId, "User " + user.getFirstname() + " has joined the chat for trip " + tripId);
    }

    @OnMessage
    public void onMessage(Session session, @PathParam("tripId") Long tripId, String messageContent) throws IOException {
        Long senderId = sessionUserIdMap.get(session);
        User sender = userRepo.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));

        if (messageContent.startsWith("@")) {
            // Handle Direct Message (DM)
            Long receiverId = Long.parseLong(messageContent.split(" ")[0].substring(1)); // Extract receiverId
            User receiver = userRepo.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));

            // Save the DM in the database
            Message directMessage = new Message(sender, receiver, messageContent, false);
            directMessage.setTimestamp(new Date());
            msgRepo.save(directMessage);

            // Send the message only to the sender and receiver
            sendMessageToUser(senderId, "You (to " + receiver.getFirstname() + "): " + messageContent);
            sendMessageToUser(receiverId, sender.getFirstname() + " (DM): " + messageContent);

        } else {
            // Handle Group Message
            Message groupMessage = new Message(sender, tripId, messageContent, false);
            groupMessage.setTimestamp(new Date());
            msgRepo.save(groupMessage);

            // Broadcast to all users in the trip
            broadcastToTrip(tripId, sender.getFirstname() + ": " + messageContent);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("tripId") Long tripId) throws IOException {
        Long userId = sessionUserIdMap.get(session);
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        sessionUserIdMap.remove(session);
        userIdSessionMap.remove(userId);
        logger.info("Connection closed for userId: " + userId);

        broadcastToTrip(tripId, "User " + user.getFirstname() + " has left the chat for trip " + tripId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error in WebSocket session " + session.getId(), throwable);
    }

    public static void sendMessageToUser(Long userId, String message) {
        try {
            if (userIdSessionMap.containsKey(userId)) {
                userIdSessionMap.get(userId).getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            logger.error("Error sending message to user with ID: " + userId, e);
        }
    }

    /**private void broadcastToTrip(Long tripId, String message) {
        List<User> passengers = tripService.getPassengersByTripId(tripId);
        for (User passenger : passengers) {
            Long userId = passenger.getId();
            if (userIdSessionMap.containsKey(userId)) {
                try {
                    userIdSessionMap.get(userId).getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.error("Error broadcasting message to trip " + tripId, e);
                }
            }
        }
    }*/

    public static void broadcastToTrip(Long tripId, String message) {
        // Obtain beans from the application context for each broadcast call
        TripService tripService = ChatSocketConfig.getBean(TripService.class);
        UserRepository userRepo = ChatSocketConfig.getBean(UserRepository.class);

        // Fetch passengers for the trip
        List<User> passengers = tripService.getPassengersByTripId(tripId);

        for (User passenger : passengers) {
            Long userId = passenger.getId();
            if (userIdSessionMap.containsKey(userId)) {
                try {
                    userIdSessionMap.get(userId).getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.error("Error broadcasting message to trip " + tripId, e);
                }
            }
        }
    }

    private void sendChatHistory(Session session, Long tripId) throws IOException {
        // Retrieve previous messages for the trip
        List<Message> messages = msgRepo.findByTripId(tripId);

        for (Message message : messages) {
            String formattedMessage = message.getSender().getFirstname() + ": " + message.getContent();
            session.getBasicRemote().sendText(formattedMessage);
        }
    }
}
