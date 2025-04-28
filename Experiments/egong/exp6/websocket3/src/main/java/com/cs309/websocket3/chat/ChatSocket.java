package com.cs309.websocket3.chat;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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

@Controller
@ServerEndpoint(value = "/chat/{username}")
public class ChatSocket {

	private static MessageRepository msgRepo;

	@Autowired
	public void setMessageRepository(MessageRepository repo) {
		msgRepo = repo;
	}

	// Maps for session handling
	private static Map<Session, Role> sessionRoleMap = new Hashtable<>();
	private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
	private static Map<String, Session> usernameSessionMap = new Hashtable<>();

	private final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username)
			throws IOException {

		logger.info("Entered into Open");

		// Check if the user exists in the UserRepository
		if (!UserRepository.userExists(username)) {
			// Add new user to the UserRepository and assign as VIEWER
			User newUser = new User(username, Role.VIEWER); // Assuming User class has this constructor
			UserRepository.addUser(newUser); // Add user to repository

			String message = "Username " + username + " not found. You have been assigned as a VIEWER.";
			session.getBasicRemote().sendText(message);
		} else {
			String message = "Welcome back, " + username + ".";
			session.getBasicRemote().sendText(message);
		}

		// Store connecting user information and assign their role
		Role userRole = UserRepository.findRoleByUsername(username);
		sessionUsernameMap.put(session, username);
		usernameSessionMap.put(username, session);
		sessionRoleMap.put(session, userRole); // Assign the role

		// Send chat history to the newly connected user
		sendMessageToParticularUser(username, getChatHistory());

		// Broadcast that new user joined
		String message = username + "(" + userRole + ") has Joined the Chat";
		broadcast(message);
	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException {
		logger.info("Entered into Message: Got Message: " + message);
		String username = sessionUsernameMap.get(session);
		Role userRole = sessionRoleMap.get(session); // Get user's role

		// Check user role before allowing message sending
		if (userRole == Role.VIEWER) {
			// If the user is a VIEWER, do not allow sending messages
			sendMessageToParticularUser(username, "You are a viewer and cannot send messages.");
			return; // Exit early if the user cannot send messages
		}

		// Format the message with username and role
		String formattedMessage = username + "(" + userRole + "): " + message;

		// Direct message to a user using the format "@username <message>"
		if (message.startsWith("@")) {
			String destUsername = message.split(" ")[0].substring(1);
			sendMessageToParticularUser(destUsername, "[DM] " + formattedMessage);
			sendMessageToParticularUser(username, "[DM] " + formattedMessage);
		} else {
			// Broadcast the formatted message
			broadcast(formattedMessage);
		}
	}

	@OnClose
	public void onClose(Session session) throws IOException {
		logger.info("Entered into Close");

		// Remove the user connection information
		String username = sessionUsernameMap.get(session);
		sessionUsernameMap.remove(session);
		usernameSessionMap.remove(username);
		sessionRoleMap.remove(session); // Remove user role information

		// Broadcast that the user disconnected
		String message = username + " disconnected";
		broadcast(message);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		logger.info("Entered into Error");
		throwable.printStackTrace();
	}

	private void sendMessageToParticularUser(String username, String message) {
		try {
			usernameSessionMap.get(username).getBasicRemote().sendText(message);
		} catch (IOException e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void broadcast(String message) {
		sessionUsernameMap.forEach((session, username) -> {
			try {
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				logger.info("Exception: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	private String getChatHistory() {
		List<Message> messages = msgRepo.findAll();
		StringBuilder sb = new StringBuilder();
		if (messages != null && !messages.isEmpty()) {
			for (Message message : messages) {
				sb.append(message.getUserName()).append(": ").append(message.getContent()).append("\n");
			}
		}
		return sb.toString();
	}
}
