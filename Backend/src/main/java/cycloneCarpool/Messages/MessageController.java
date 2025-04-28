package cycloneCarpool.Messages;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Operation(summary = "Sends a message", description = "Sends a message from the senderId to the receiverId with the given message content. Returns ok response code if successful.")
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestParam Long senderId, @RequestParam Long receiverId, @RequestParam String content) {
        Message message = messageService.sendMessage(senderId, receiverId, content);
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Gets a Conversation between two users.", description = "Returns a conversation (all messages) betweeen the given two user IDs.")
    @GetMapping("/conversation/{senderId}/{receiverId}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable Long senderId, @PathVariable Long receiverId) {
        List<Message> messages = messageService.getMessagesBetweenUsers(senderId, receiverId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Gets conversation by Trip", description = "Gets all messages from the specific tripId group chat. Helper method with the websocket feature of chatting.")
    @GetMapping("/conversationByTrip/{tripId}")
    public ResponseEntity<List<Message>> getConversationByTrip(@PathVariable Long tripId) {
        List<Message> messages = messageService.getMessagesByTrip(tripId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Gets All Converstations for Specific UserId", description = "Gets all conversations a single user is involved in from the database.")
    @GetMapping("/conversation/all/{id}")
    public ResponseEntity<List<Message>> getConversationAll(@PathVariable Long id) {
        List<Message> conversations = messageService.getAllConversationsByUserId(id);

        // Check if the list is empty and return appropriate response
        if (conversations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(conversations);
    }

    @Operation(summary = "Get Unread Messages", description = "Gets all the messages for a user that have not yet been seen by them.")
    @GetMapping("/unread/{receiverId}")
    public ResponseEntity<List<Message>> getUnreadMessages(@PathVariable Long receiverId) {
        List<Message> messages = messageService.getUnreadMessages(receiverId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Marks a message as read.", description = "Updates the status of the message in the database from unread to read.")
    @PutMapping("/read/{messageId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        messageService.markMessageAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    //Delete Message within 5 Minutes
    @Operation(summary = "Deletes a Message", description = "Deletes the message from the database, given a specific messageId. Can only delete a message within a 5 minute time frame.")
    @DeleteMapping("/delete/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId) {
        boolean isDeleted = messageService.deleteMessage(messageId);
        if (isDeleted) {
            return ResponseEntity.ok().body("Message deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot delete message after 5 mintues");
        }
    }

    //Edit Message within 5 Minutes
    @Operation(summary = "Edits a Message", description = "Edits a specific message by its database ID. Can only edit a message within a 5 minute time frame.")
    @PutMapping("/edit/{messageId}")
    public ResponseEntity<?> editMessage(@PathVariable Long messageId, @RequestParam String newContent) {
        try {
            Message updatedMessage = messageService.editMessage(messageId, newContent);
            return ResponseEntity.ok(updatedMessage);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
