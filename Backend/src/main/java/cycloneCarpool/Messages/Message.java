package cycloneCarpool.Messages;

import cycloneCarpool.Users.User;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = true)  // Nullable for group messages
    private User receiver;

    @Column(name = "trip_id", nullable = true)  // Ensure this annotation is present
    private Long tripId;  // Nullable for direct messages

    @Column(nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent")
    private Date timestamp = new Date();

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    public Message() {}

    // Constructor for DMs
    public Message(User sender, User receiver, String content, Boolean isRead) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isRead = isRead;
        this.timestamp = new Date();
    }

    // Constructor for Group Messages
    public Message(User sender, Long tripId, String content, Boolean isRead) {
        this.sender = sender;
        this.tripId = tripId;
        this.content = content;
        this.isRead = isRead;
        this.timestamp = new Date();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}

