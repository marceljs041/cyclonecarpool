package cycloneCarpool.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author eddiegong
 */

// A Record of Notifications.
public interface NotificationRepository extends JpaRepository<Notification, Long>{

    List<Notification> findByReceiverIdAndSentFalse(Long receiverId);
}
