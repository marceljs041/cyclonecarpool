package cycloneCarpool.Reviews;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author eddiegong
 */

// A Record of Reviews in System.
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Reviewer : Anthony Campana
    // Retrieves a specific review by its unique reviewId.
    // Method is not used currently
    Optional<Review> findByReviewId(Long reviewId);

    // Finds all reviews created by a specific reviewer, sorted by most recent.
    List<Review> findReviewsByReviewerId(Long reviewerId);

    // Finds all reviews received by a specific user (receiver).
    List<Review> findReviewsByReceiverId(Long receiverId);

    // Finds all reviews received during a specific trip (tripId).
    // Method is not used currently
    List<Review> findReviewsByTrip_tripId(Long tripId);

}
