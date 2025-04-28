package coms309.cars;

/**
 * Cars, Schrödinger's Transformers
 */
public class Car {
    private String owner;
    private String licensePlate;
    private int seats;

    public Car(){

    }

    public Car(String owner, String licensePlate, int seats) {
        this.owner = owner;
        this.licensePlate = licensePlate;
        this.seats = seats;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLicensePlate() {
        return this.licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getSeats() {
        return this.seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    @Override
    public String toString() {
        return owner + " "
                + licensePlate + " "
                + seats;
    }
}
