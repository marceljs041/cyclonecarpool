package cycloneCarpool.LocationTracking;

import java.util.List;

public class RouteResponseDTO {
    private double duration; // Total duration in minutes
    private double distance; // Total distance in kilometers
    private String summary; // Summary of the route
    private List<double[]> geometry; // List of coordinates for the route path

    // Getters and setters
    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<double[]> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<double[]> geometry) {
        this.geometry = geometry;
    }
}
