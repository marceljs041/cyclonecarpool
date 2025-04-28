package com.example.cyclonecarpool.trips;

public class TripItem {

    private String toLoco;
    private String fromLoco;
    private String dateTime;
    private String pickUpLoco;

    private Integer tripId;

    private Integer creatorUserId;
    private Integer seatsAvailable;
    private Integer price;

    private Boolean roundTrip;
    private Boolean noSmoke;



    public TripItem(Integer tripId, String toLoco, String fromLoco, String dateTime, String pickUpLoco, Integer creatorUserId, Integer seatsAvailable, Integer price, Boolean roundTrip, Boolean noSmoke) {
        this.tripId = tripId;
        this.toLoco = toLoco;
        this.fromLoco = fromLoco;
        this.dateTime = dateTime;
        this.pickUpLoco = pickUpLoco;
        this.creatorUserId = creatorUserId;
        this.seatsAvailable = seatsAvailable;
        this.price = price;
        this.roundTrip = roundTrip;
        this.noSmoke = noSmoke;
    }

    public Integer getTripId() { return tripId; }

    public String getToLoco() {
        return toLoco;
    }

    public String getFromLoco() {
        return fromLoco;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getPickUpLoco() {
        return pickUpLoco;
    }

    public Integer getCreatorUserId() {
        return creatorUserId;
    }

    public Integer getSeatsAvailable() {
        return seatsAvailable;
    }

    public Integer getPrice() {
        return price;
    }

    public Boolean getRoundTrip() {
        return roundTrip;
    }

    public Boolean getNoSmoke() {
        return noSmoke;
    }
}
