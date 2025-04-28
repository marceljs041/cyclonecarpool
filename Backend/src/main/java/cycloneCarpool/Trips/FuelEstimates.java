package cycloneCarpool.Trips;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author eddiegong
 */

@Schema(description = "Estimation of Trip's Distance, Fuel Consumption, and Fuel Cost.")
public class FuelEstimates {

    private double distanceEstimate;
    private double fuelEstimate;

    private double regular;
    private double premium;
    private double diesel;


    public double getDistanceEstimate() {
        return distanceEstimate;
    }

    public void setDistanceEstimate(double distanceEstimate) {
        this.distanceEstimate = distanceEstimate;
    }

    public double getFuelEstimate() {
        return fuelEstimate;
    }

    public void setFuelEstimate(double fuelEstimate) {
        this.fuelEstimate = fuelEstimate;
    }

    public double getRegular() {
        return regular;
    }

    public void setRegular(double regular) {
        this.regular = regular;
    }

    public double getPremium() {
        return premium;
    }

    public void setPremium(double premium) {
        this.premium = premium;
    }

    public double getDiesel() {
        return diesel;
    }

    public void setDiesel(double diesel) {
        this.diesel = diesel;
    }
}