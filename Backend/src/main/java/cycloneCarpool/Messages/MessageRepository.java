package cycloneCarpool.Messages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId); //For unread messages
    List<Message> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
    List<Message> findByTripId(@Param("tripId") Long tripId);
}
