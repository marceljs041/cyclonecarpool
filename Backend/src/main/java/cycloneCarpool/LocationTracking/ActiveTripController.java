package cycloneCarpool.LocationTracking;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/activeTrips")
public class ActiveTripController {

    @Autowired
    private ActiveTripService activeTripService;

    @Operation(summary = "Starting the Trip", description = "Activates the trip and adds the trip information to the active trip database.")
    @PostMapping("/start")
    public ResponseEntity<ActiveTrip> startTrip(@RequestParam String driverId,
                                                @RequestParam String startLocation,
                                                @RequestParam String endLocation,
                                                @RequestParam List<String> passengerIds) {
        ActiveTrip trip = activeTripService.startTrip(driverId, startLocation, endLocation, passengerIds);
        return ResponseEntity.ok(trip);
    }

    @Operation(summary = "Ends an Active Trip", description = "Once a trip is finished this method ends the trip and deletes it from the active trip database.")
    @DeleteMapping("/end/{tripId}")
    public ResponseEntity<Void> endTrip(@PathVariable Long tripId) {
        activeTripService.endTrip(tripId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gets Trip information for a specific Trip", description = "By path variable tripId, method returns trip information for that specific tripId.")
    @GetMapping("/{tripId}")
    public ResponseEntity<ActiveTrip> getTrip(@PathVariable Long tripId) {
        ActiveTrip trip = activeTripService.getTrip(tripId);
        if (trip != null) {
            return ResponseEntity.ok(trip);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get All Active Trips", description = "This method returns all current active trips from the database.")
    @GetMapping("/all")
    public ResponseEntity<List<ActiveTrip>> getAllTrips() {
        List<ActiveTrip> trips = activeTripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * This method should not be used for the most part since endLocation should not change during the trip.
     * @param tripId
     * @param newEndLocation
     * @return
     */
    @Operation(summary = "Update Trip", description = "Updates the end location of the active trip if needed. (Method should not be used normally).")
    @PutMapping("/update/{tripId}")
    public ResponseEntity<ActiveTrip> updateTrip(@PathVariable Long tripId, @RequestParam String newEndLocation) {
        ActiveTrip updatedTrip = activeTripService.updateTrip(tripId, newEndLocation);
        return ResponseEntity.ok(updatedTrip);
    }
    
}
