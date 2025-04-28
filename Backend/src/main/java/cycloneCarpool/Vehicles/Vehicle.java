package cycloneCarpool.Vehicles;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

/**
 * @author eddiegong
 */

@Schema(description = "Entry of a Vehicle.")
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long vehicleId;

    private long ownerId;

    private String licensePlate;
    private String state;

    private String make;
    private String model;
    private String year;
    private String color;

    private long mpg;


    public long getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public long getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getMpg() { return mpg; }
    public void setMpg(long mpg) { this.mpg = mpg; }
}
