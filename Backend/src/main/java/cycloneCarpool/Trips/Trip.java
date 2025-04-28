package cycloneCarpool.Trips;

import cycloneCarpool.Users.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eddiegong
 */

@Schema(description = "Entry Record of a Trip.")
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long tripId;

    private long driverId;

    @ManyToMany
    @JoinTable(
            name = "trip_passengers",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> passengers = new ArrayList<>();

    private String startLocation;
    private String endLocation;
    private String pickUp;

    private double distance;

    private LocalDateTime time;

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

    public LocalDateTime getTime() {return time;}
    public void setTime(LocalDateTime time) {this.time = time;}

    public int getSeat() {return seat;}
    public void setSeat(int seat) {this.seat = seat;}
    public long getPrice() {return price;}
    public void setPrice(long price) {this.price = price;}

    public boolean isRoundTrip() {return roundTrip;}
    public void setRoundTrip(boolean roundTrip) {this.roundTrip = roundTrip;}
    public boolean isNoSmoke() {return noSmoke;}
    public void setNoSmoke(boolean noSmoke) {this.noSmoke = noSmoke;}


    public void setAll(Trip trip) {
        this.startLocation = trip.getStartLocation();
        this.endLocation = trip.getEndLocation();
        this.pickUp = trip.getPickUp();
        this.time = trip.getTime();
        this.seat = trip.getSeat();
        this.price = trip.getPrice();
        this.roundTrip = trip.isRoundTrip();
        this.noSmoke = trip.isNoSmoke();
    }

    public List<User> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<User> passengers) {
        this.passengers = passengers;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
