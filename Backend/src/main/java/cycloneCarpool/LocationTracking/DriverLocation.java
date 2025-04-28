package cycloneCarpool.LocationTracking;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tripId;
    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;

    public DriverLocation() {}

    public DriverLocation(Long tripId, double latitude, double longitude, LocalDateTime now) {
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = now;
    }

    public Long getId() {return id;}
    public Long getTripId() {return tripId;}
    public void setTripId(Long tripId) {this.tripId = tripId;}
    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}
    public double getLongitude() {return longitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}
    public LocalDateTime getTimestamp() {return timestamp;}
    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}
}
