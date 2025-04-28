package com.example.cyclonecarpool.trips;

import com.example.cyclonecarpool.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Trip {
    private long tripId;
    private long driverId;

    private List<User> passengers = new ArrayList<>();

    private String startLocation;
    private String endLocation;
    private String pickUp;

    private String time;

    private int seat;
    private long price;

    private boolean roundTrip;
    private boolean noSmoke;

    public long getTripId() {return tripId;}
    public void setTripId(long tripId) {this.tripId = tripId;}

    public long getDriverId() {return driverId;}
    public void setDriverId(long driverId) {this.driverId = driverId;}

    public String getStartLocation() {return startLocation;}
    public void setStartLocation(String startLocation) {this.startLocation = startLocation;}
    public String getEndLocation() {return endLocation;}
    public void setEndLocation(String endLocation) {this.endLocation = endLocation;}
    public String getPickUp() {return pickUp;}
    public void setPickUp(String pickUp) {this.pickUp = pickUp;}

    public String getTime() {return time;}
    public void setTime(String time) {this.time = time;}

    public int getSeat() {return seat;}
    public void setSeat(int seat) {this.seat = seat;}
    public long getPrice() {return price;}
    public void setPrice(long price) {this.price = price;}

    public boolean isRoundTrip() {return roundTrip;}
    public void setRoundTrip(boolean roundTrip) {this.roundTrip = roundTrip;}
    public boolean isNoSmoke() {return noSmoke;}
    public void setNoSmoke(boolean noSmoke) {this.noSmoke = noSmoke;}

    public List<User> getPassengers() { return passengers; }
    public void setPassengers(List<User> passengers) { this.passengers = passengers; }

    public TripItem toTripItem() {
        return new TripItem(
                (int) tripId,
                startLocation,
                endLocation,
                time.toString(),
                pickUp,
                (int) driverId,
                seat,
                (int) price,
                roundTrip,
                noSmoke
        );
    }
}
