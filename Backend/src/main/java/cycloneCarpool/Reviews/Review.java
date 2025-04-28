package cycloneCarpool.Reviews;

import cycloneCarpool.Trips.Trip;
import cycloneCarpool.Users.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

/**
 * @author eddiemgong
 */

/**
 * Code Review:
 * Reviewer : Anthony Campana
 * I added some in line comments to describe the type of functions being used in the code.
 * This class overall clean, including well-defined associations for reviewer, receiver, and trip.
 * The @Entity and @Table annotations indicate mapping to the database.
 * The use of @ManyToOne is perfect in this case as many reviews are mapped to one user.
 */

@Schema(description = "Entry Record of a User Review.")
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reviewId;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    private String content;
    private int rating;

    //Empty constructor
    public Review() { }

    //Constructor for full review objects
    public Review(User reviewer, User receiver, Trip trip, String content, int rating) {
        this.reviewer = reviewer;
        this.receiver = receiver;
        this.trip = trip;
        this.content = content;
        this.rating = rating;
    }

    //Getter and Setter methods to get/set attributes of review
    public User getReviewer() {
        return reviewer;
    }
    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public User getReceiver() {
        return receiver;
    }
    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Trip getTrip() {
        return trip;
    }
    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public long getReviewId() {
        return reviewId;
    }
    public void setReviewId(long reviewId) {
        this.reviewId = reviewId;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Updates review with the new review's content and rating
      * @param review
     */
    public void updateReview(Review review) {
        this.content = review.getContent();
        this.rating = review.getRating();
    }
}