package cycloneCarpool.Trips;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author eddiegong
 */

// For storing filter variables instead of the entire trip variable
@Schema(description = "Entry Record of a Trip's Specifications.")
public class TripDTO {

    private Long userId;
    private String startLocation;
    private String endLocation;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStartLocation() {return startLocation;}
    public void setStartLocation(String startLocation) {this.startLocation = startLocation;}
    public String getEndLocation() {return endLocation;}
    public void setEndLocation(String endLocation) {this.endLocation = endLocation;}

}
