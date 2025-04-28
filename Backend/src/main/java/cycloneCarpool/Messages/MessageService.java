package cycloneCarpool.Messages;

import cycloneCarpool.Users.User;
import cycloneCarpool.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public Message sendMessage(Long senderId, Long receiverId, String content){
        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));

        return messageRepository.save(message);
    }

    public List<Message> getMessagesBetweenUsers(Long senderId, Long receiverId){
        return messageRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    }

    public List<Message> getUnreadMessages(Long receiverId){
        return messageRepository.findByReceiverIdAndIsReadFalse(receiverId);
    }

    public List<Message> getMessagesByTrip(Long tripId){
        return messageRepository.findByTripId(tripId);
    }

    public List<Message> getAllConversationsByUserId(Long userId) {
        // Fetch messages where the user is either the sender or the receiver
        return messageRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    public void markMessageAsRead(Long messageId){
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    //Delete Message Within 5 Minutes
    public boolean deleteMessage(Long messageId){
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));

        long timeDifference = System.currentTimeMillis() - message.getTimestamp().getTime();
        long fiveMinutesInMillis = 5 * 60 * 1000;

        if (timeDifference <= fiveMinutesInMillis){
            messageRepository.delete(message);
            return true;
        } else {
            return false; //Can't delete after the 5 minutes
        }
    }

    //Edit Message Within 5 Minutes
    public Message editMessage(Long messageId, String newContent){
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));

        long timeDifference = System.currentTimeMillis() - message.getTimestamp().getTime();
        long fiveMinutesInMillis = 5 * 60 * 1000;

        if (timeDifference <= fiveMinutesInMillis){
            message.setContent(newContent);
            return messageRepository.save(message);
        } else {
            throw new RuntimeException("Cannot edit message after 5 minutes");
        }
    }

    //Saving messages to table from websockets
    public void sendMessageToRepository(Message message) {
        messageRepository.save(message);
    }

}
