package cycloneCarpool.LocationTracking;

import cycloneCarpool.LocationTracking.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LocationController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private DriverLocationService driverLocationService;

    @Operation(summary = "Get Optimal Route", description = "Uses MapBox api to find the most optimal route given a start and end location.")
    @GetMapping("/getRoute")
    public ResponseEntity<?> getRoute(@RequestParam String startCoordinates, @RequestParam String endCoordinates) {
        try {
            RouteResponseDTO route = routeService.getOptimalRoute(startCoordinates, endCoordinates);
            return ResponseEntity.ok(route);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    //Add a get current location for implementing locationTracking
    @Operation(summary = "Get Current Location", description = "Helper method with location tracking, to keep track of most recent saved location of the driver in the database.")
    @GetMapping("/getCurrentLocation")
    public ResponseEntity<?> getCurrentLocation(@RequestParam Long tripId) {
        DriverLocation currentLocation = driverLocationService.getLatestLocation(tripId);
        if (currentLocation != null) {
            return ResponseEntity.ok(currentLocation);
        } else {
            return ResponseEntity.status(404).body("Location not found for trip ID: " + tripId);
        }
    }

    @Operation(summary = "Get Coordinates from a name of a location.", description = "This method takes in a location name (i.e. Jack Trice Stadium) and returns the coordinates of location. " +
            "This is a helper for the getRoute function because it needs to take in coordinates and not a name of a location.")
    @GetMapping("/getCoordinates")
    public ResponseEntity<?> getCoordinates(@RequestParam String placeName) {
        double[] coordinates = routeService.getCoordinatesFromPlaceName(placeName);
        if (coordinates != null) {
            return ResponseEntity.ok(coordinates);
        } else {
            return ResponseEntity.status(404).body("Coordinates not found for the given place name.");
        }
    }

}
