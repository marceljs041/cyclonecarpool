package cycloneCarpool.Notification;

import cycloneCarpool.Users.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.Date;

/**
 * @author eddiegong
 */

/**
 * Code Review: Anthony Campana
 * This class is essential for the service and socket class to have an object to use.
 * Also I like the setup of the table for hadnling storage of the notifications
 */

@Schema(description = "Entry Record of a Notification Message.")
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(nullable = false)
    private String content;

    @Column(name = "sent", nullable = false)
    private Boolean sent = false;

    public Notification() {}

    // Constructor for initializing a notification
    public Notification(User receiver, String content, boolean sent) {
        this.receiver = receiver;
        this.content = content;
        this.sent = sent;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }
}
