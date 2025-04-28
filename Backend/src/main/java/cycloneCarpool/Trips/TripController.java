package cycloneCarpool.Trips;

import cycloneCarpool.Reviews.Review;
import cycloneCarpool.Reviews.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author eddiegong
 */

@Tag(name = "Trip Controller",
        description = "Operations Related to Trips, Specified Filters, Cost Estimation, Permission Checks, and Trip-Related Reviews.")
@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private ReviewService reviewService;

    // Default showing only future trips
    @Operation(summary = "Fetch All Trips in the Future.",
            description = "Default Behavior, Returns " +
                    "List of All Trips with Vacant Seats in the Future(When this Method is Called), " +
                    "Sorted by Closest in Time to Furthest, Returns " +
                    "[200 + List].")
    @GetMapping("/home")
    public ResponseEntity<List<Trip>> getFutureTrips() {
        List<Trip> trips = tripService.getFutureTrips();
        return ResponseEntity.ok(trips);
    }
    // All trips defaulted sorted by tripId
    @Operation(summary = "Fetch All Trips.",
            description = "Returns List of All Trips in the Repository, Sorteded by ID, Returns " +
                    "[200 + List].")
    @GetMapping("/home/all")
    public ResponseEntity<List<Trip>> getAllTrips() {
        List<Trip> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    // Filter by Start/End Locations, Null in parameter is set to no filter
    @Operation(summary = "Given Filtered Conditions, Fetch All Trips in the Future with Filtered Specifications.",
            description = "Search Repository for List of Entries with Given Start and End Location(if any), " +
                    "Vacant Seats in the Future(When this Method is Called), and Sorted by Closest in Time to Furthest, Returns" +
                    "[200 + List].")
    @PostMapping("/home/filter")
    public ResponseEntity<List<Trip>> getFilteredTrips(@RequestBody(required = false) TripDTO tripFilter) {
        String startLocation = tripFilter.getStartLocation();
        String endLocation = tripFilter.getEndLocation();

        List<Trip> trips = tripService.getFilteredTrips(startLocation, endLocation);
        return ResponseEntity.ok(trips);
    }

    // Add trip
    @Operation(summary = "Given Trip Information, Create a New Trip Entry.",
            description = "Create New Trip Entry, Calculate Distance Estimation, and Save to Repository, Returns " +
                    "[200 + Created Entry].")
    @PostMapping("/home/create")
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        Trip newTrip = tripService.createTrip(trip);
        return ResponseEntity.ok(newTrip);
    }

    @Operation(summary = "Given Trip Information, Create a Fuel Estimate.",
            description = "*You Must Have a Vehicle Tied to the Trip Driver and Valid Locations for Estimate to be Successful." +
                    "Fetch Trip Driver's Vehicle's MPG, Calculate Distance Estimation, Calculate Average Gas Price in US, and Returns " +
                    "[200 + Estimation(Distance, Fuel Consumption, Fuel Cost)] if Successful, " +
                    "[500] if Unable to Estimate.")
    @PostMapping("/home/create/estimate")
    public ResponseEntity<?> estimateCost(@RequestBody TripDTO tripDTO) {
        try {
            return ResponseEntity.ok(tripService.estimateCost(tripDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Find by id, return trip or not found
    @Operation(summary = "Given Trip ID, Fetch Trip.",
            description = "Search Repository for an Entry of Given Trip ID, Returns " +
                    "[200 + Entry] if Successful," +
                    "[404] if No Such Trip Found in Repository.")
    @GetMapping("/{tripId}")
    public ResponseEntity<Trip> getTrip(@PathVariable Long tripId) {
        try {
            Trip trip = tripService.getSingleTrip(tripId);
            return ResponseEntity.ok(trip);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Check if a User has Permission to edit(Update, Delete) a Trip
    @Operation(summary = "Given Trip ID and User ID, Check if User has Permission to Edit the Trip.",
            description = "Check if the User has Edit Permission of the Trip, " +
                    "Client has Edit Permission if They are Admins or the Driver of the Trip, Returns " +
                    "[200] if Client has Edit Permission of the Trip," +
                    "[403] if Not.")
    @PostMapping("/{tripId}/edit")
    public ResponseEntity<Void> checkTripEditPermission(@PathVariable Long tripId, @RequestBody TripDTO userId) {
        boolean permission = tripService.checkTripEditPermission(tripId, userId.getUserId());

        if (permission) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Passenger join a trip
    @Operation(summary = "Given Trip ID and User ID, Add User to the Trip as Passenger.",
            description = "Search Trip Repository for a Trip and User Repository for a User, Add the User to the Trip as a Passenger, Returns " +
                    "[200 + Entry(After Change)] if Successful, " +
                    "[404] if No Such Trip or User Found.")
    @PostMapping("/{tripId}/join")
    public ResponseEntity<Trip> joinTrip(@PathVariable Long tripId, @RequestBody TripDTO userId) {
        try {
            Trip joinedTrip = tripService.addPassenger(tripId, userId.getUserId());
            return ResponseEntity.ok(joinedTrip);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Passenger leave a trip
    @Operation(summary = "Given Trip ID and User ID, Remove User to the Trip as Passenger.",
            description = "Search Trip Repository for a Trip and User Repository for a User, Remove the User to the Trip from Passenger List, Returns " +
                    "[200 + Entry(After Change)] if Successful, " +
                    "[404] if No Such Trip or User Found." +
                    "*If Target User is Not in Passenger List of the Trip, this Method does Nothing.")
    @PostMapping("/{tripId}/leave")
    public ResponseEntity<Trip> leaveTrip(@PathVariable Long tripId, @RequestBody TripDTO userId) {
        try {
            Trip joinedTrip = tripService.removePassenger(tripId, userId.getUserId());
            return ResponseEntity.ok(joinedTrip);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // List all trips associated with this driver
    @Operation(summary = "Given User ID, Fetch All Trips that User is Driver.",
            description = "Search Repository for List of Entries with Given User as Driver, Returns " +
                    "[200 + List].")
    @PostMapping("/mytrip")
    public ResponseEntity<List<Trip>> getDriverTrips(@RequestBody Trip trip) {
        List<Trip> trips = tripService.getMyTrips(trip.getDriverId());
        return ResponseEntity.ok(trips);
    }

    // Modify trip with the specific tripId
    @Operation(summary = "Given Trip ID and New Trip Content, Update Trip Entry in Repository.",
            description = "Update Repository an Entry of Given Trip ID, Returns " +
                    "[200 + Entry(After Change)] if Successful, " +
                    "[404] if No Such Trip Found.")
    @PutMapping("/mytrip/{tripId}")
    public ResponseEntity<?> editTrip(@PathVariable Long tripId, @RequestBody Trip trip) {
        try {
            Trip updatedTrip = tripService.editTrip(tripId, trip);
            return ResponseEntity.ok(updatedTrip);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Remove trip with the specific tripId
    @Operation(summary = "Given Trip ID, Remove Trip Entry from Repository.",
            description = "Remove Repository an Entry of Given Trip ID, Returns " +
                    "[204] if Successful, " +
                    "[404] if No Such Trip Found.")
    @DeleteMapping("/mytrip/{tripId}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long tripId) {

        boolean deleted = tripService.deleteTripById(tripId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // List all trips associated with User as a passenger
    @Operation(summary = "Given User ID, Fetch All Trips that User is Passenger.",
            description = "Search Repository for List of Trip Entries with Given User in the Trips' Passenger List, Returns " +
                    "[200 + List].")
    @PostMapping("/myride")
    public ResponseEntity<List<Trip>> getPassengerTrips(@RequestBody TripDTO userId) {
        List<Trip> trips = tripService.getMyRides(userId.getUserId());
        return ResponseEntity.ok(trips);
    }

    @Operation(summary = "Given Trip ID and User ID, Check if the User is in Passenger List of the Trip.",
            description = "Fetch Trip with Trip ID, then the Trip's Passenger List with User ID, Returns " +
                    "[200] if User is in Passenger List of the Trip, " +
                    "[404] if Not.")
    @PostMapping("/myride/{tripId}")
    public ResponseEntity<List<Trip>> hasPassenger(@PathVariable Long tripId, @RequestBody TripDTO userId) {
        boolean hasPassenger = tripService.checkHasPassenger(tripId, userId.getUserId());
        if (hasPassenger) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Following Section of Code are Additional Methods for Reviews(Reviewer Side)

    /**
     * Reviewer : Anthony Campana
     * Verifies the reviewer-receiver relationship before creating a review.
     * @param review
     * @return Response code to frontend, OK if successful, notFOUND if not successful
     */
    @Operation(summary = "Given Review Information, Create a New Review Entry in Review Repository.",
            description = "Create New Review Entry and Save to Review Repository, Returns " +
                    "[200 + Entry] if Successful, " +
                    "[403] if No Association Found Between the Trip, Reviewer, and Receiver, " +
                    "[404] if No Such Trip Found.")
    @PostMapping("/review")
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        try {
            Trip reviewTrip = tripService.getSingleTrip(review.getTrip().getTripId());
            // Only if Trip, Reviewer, Receiver has Relationship
            if (((review.getReviewer().getId() == reviewTrip.getDriverId()) &&
                    (reviewTrip.getPassengers().stream().anyMatch(passenger -> Objects.equals(passenger.getId(), review.getReceiver().getId())))) ||
                    ((review.getReceiver().getId() == reviewTrip.getDriverId())  &&
                            (reviewTrip.getPassengers().stream().anyMatch(passenger -> Objects.equals(passenger.getId(), review.getReviewer().getId()))))) {
                reviewService.createReview(review);
                return ResponseEntity.ok(review);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reviewer : Anthony Campana
     * Updates an existing review, checking for matching reviewer, receiver, and trip details.
     * @param reviewId
     * @param review
     * @return Updated Review JSON object if successful, badRequest code if not successful
     */
    @Operation(summary = "Given Review ID and New Review Content, Update Review Entry in Review Repository.",
            description = "Update Review Repository an Entry of Given Review ID, Returns " +
                    "[200 + Entry(After Change)] if Successful, " +
                    "[404] if No Such Review Found, " +
                    "[400] if Trip, Reviewer, and Receiver Differs From Original.")
    @PutMapping("/review/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @RequestBody Review review) {
        try {
            Review updatedReview = reviewService.updateReview(reviewId, review);
            return ResponseEntity.ok(updatedReview);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reviewer : Anthony Campana
     * Deletes a review by its ID, returning a response based on success or failure.
     * @param reviewId
     * @return Response code for no content if delete is successful, notFound code if delete is not successful
     */
    @Operation(summary = "Given Review ID, Remove Trip Entry in Repository.",
            description = "Remove Repository an Entry of Given Review ID, Returns " +
                    "[204] if Successful, " +
                    "[404] if No Such Review Found.")
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        boolean deleted = reviewService.deleteReview(reviewId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
