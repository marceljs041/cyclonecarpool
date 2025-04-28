package cycloneCarpool.Reviews;

import cycloneCarpool.Users.User;
import cycloneCarpool.Users.UserRepository;
import cycloneCarpool.Users.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author eddiegong
 */

/**
 * Code Review:
 * Reviewer : Anthony Campana
 * I added some in line comments
 * Overall, the service class is well-organized with clear methods for creating,
 * fetching, updating, and deleting reviews.
 */

// CRUD Methods of Review, has Methods for User-Filtered Results(Review Lists by User, Total Rating) and Permission Checks.
// Review Method Usage are in Controller of Trips for Reviewer(CRUD) & Users for Receiver(R).
@Service
public class ReviewService {

    @Autowired
    ReviewRepository reviewRepository;

    UserService userService;

    //Creates a new review to the database and returns the saved review entity.
    public Review createReview(Review review) {return reviewRepository.save(review);}

    /**
     * Finds specific review from database given the reviewId
     * @param reviewId
     * @return Review object by specific reviewId
     */
    public Review getReviewByReviewId(long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
    }
    // sorted by most recent

    /**
     * Finds all reviews in the database that is mapped to the reviewerId
     * @param reviewerId
     * @return All reviews that were sent by reviewerId
     */
    public List<Review> getReviewByReviewerId(long reviewerId) {
        return reviewRepository.findReviewsByReviewerId(reviewerId);
    }

    /**
     * Finds all reviews in the database for a specific user by their id.
     * @param receiverId
     * @return All reviews that were sent to the same user (same receiverId)
     */
    public List<Review> getReviewByReceiverId(long receiverId) {
        return reviewRepository.findReviewsByReceiverId(receiverId);
    }


    /**
     * Calculates the average rating for a receiver by summing ratings and dividing by the count
     * @param receiverId
     * @return Rating of the user with id=receiverId
     */
    // Suggestion: add validation for rating range (e.g., 1-5).
    public double getReceiverRating(long receiverId) {
        List<Review> reviews = getReviewByReceiverId(receiverId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double totalRating = reviews.stream()
                .mapToInt(Review::getRating)
                .sum();

        return Math.round((totalRating/reviews.size()) * 100.0) / 100.0;
    }

    /**
     * Updates an existing review only if reviewer, receiver, and trip match the original review.
     * @param reviewId
     * @param review
     * @return updated Review object
     */
    public Review updateReview(long reviewId, Review review) {
        Optional<Review> updatingReview = reviewRepository.findById(reviewId);
        if (updatingReview.isPresent()) {
            Review updateReview = updatingReview.get();
            // Only if Trip, Reviewer, Receiver matches Original
            if ((Objects.equals(updateReview.getReviewer().getId(), review.getReviewer().getId())) &&
                    (Objects.equals(updateReview.getReceiver().getId(), review.getReceiver().getId())) &&
                    (updateReview.getTrip().getTripId() == review.getTrip().getTripId())) {
                updateReview.updateReview(review);
                return reviewRepository.save(updateReview);
            }

            throw new IllegalArgumentException("Review with ID" + reviewId + " does not match with given input.");


        } else {
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
    }

    /**
     * Allows Admins to delete any review; Reviewers can delete their own reviews.
     * @param reviewId
     * @param userId
     * @return True if user can delete the review
     * @return False if user can't delete the review
     */
    public boolean checkReviewDeletePermission(Long reviewId, Long userId) {
        try {
            User user = userService.getUserById(userId);
            Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);

            // Reviewer can delete their Reviews
            if (Objects.equals(user.getRole(), "Admin")) { return true; } // Admin can delete Reviews
            else return Objects.equals(user.getId(), review.getReviewer().getId()); // Reviewers can delete their Reviews.

        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /**
     * Deletes a review
     * @param reviewId
     * @return True if delete was successful and false if review was not deleted.
     */
    public boolean deleteReview(Long reviewId) {

        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
            return true;
        } else {
            return false;
        }
    }



}
