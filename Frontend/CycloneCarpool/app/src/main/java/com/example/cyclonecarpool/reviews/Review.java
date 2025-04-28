package com.example.cyclonecarpool.reviews;

import com.example.cyclonecarpool.trips.Trip;
import com.example.cyclonecarpool.user.User;

public class Review {
    private Long id;
    private User reviewer;
    private User receiver;
    private Trip trip;

    private String content;
    private int rating;

    public Review() {}

    public Review(Long id, User reviewer, User receiver, Trip trip, String content, int rating) {
        this.id = id;
        this.reviewer = reviewer;
        this.receiver = receiver;
        this.trip = trip;
        this.content = content;
        this.rating = rating;
    }

    public Review(User reviewer, User receiver, Trip trip, String content, int rating) {
        this.id = null;
        this.reviewer = reviewer;
        this.receiver = receiver;
        this.trip = trip;
        this.content = content;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public Integer getIntId() { return id.intValue(); }

    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }
    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
