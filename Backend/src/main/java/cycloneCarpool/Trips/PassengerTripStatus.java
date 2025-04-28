package cycloneCarpool.Trips;

import cycloneCarpool.Users.User;
import jakarta.persistence.*;


/**
 * @author Anthony Campana
 */

@Entity
@Table(name = "passenger_trip_status")
public class PassengerTripStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User passenger;

    private String status; // e.g., "PENDING", "ACCEPTED", "REJECTED"
    private boolean paid; // Payment status for the passenger

    // Constructors, getters, and setters
    public PassengerTripStatus() {}

    public PassengerTripStatus(Trip trip, User passenger, String status, boolean paid) {
        this.trip = trip;
        this.passenger = passenger;
        this.status = status;
        this.paid = paid;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
