package cycloneCarpool.Trips;

import cycloneCarpool.Notification.NotificationService;
import cycloneCarpool.Users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Campana
 */

@Tag(name = "PassengerTripStatus(PTS) Controller",
        description = "Operations Related to Passenger's Status of a Trip, Records Request and Payment Status.")
@RestController
@RequestMapping("/passenger-trip-status")
public class PassengerTripStatusController {

    @Autowired
    private PassengerTripStatusService passengerTripStatusService;

    @Operation(summary = "Given Trip and Passenger ID, fetch PassengerTripStatus.",
            description = "Search Repository for an Entry of Given Trip and Passenger ID, Returns " +
                    "[200 + the Entry] if Successful," +
                    "[404] if No Such PassengerTripStatus Found in Repository.")
    @GetMapping("/status/{tripId}/{passengerId}")
    public ResponseEntity<PassengerTripStatus> getPassengerTripStatus(@PathVariable Long tripId, @PathVariable Long passengerId) {
        try {
            PassengerTripStatus pts = passengerTripStatusService.getPassengerTripStatus(tripId, passengerId);
            return ResponseEntity.ok(pts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Given Trip and Passenger ID, Create a New Status Entry.",
            description = "Create New PassengerTripStatus Entry and Save to Repository, Returns " +
                    "[200 + Entry's ID] if Successful," +
                    "[404] if No Such Trip Found," +
                    "[400] if No Such User Found or Entry for Given Trip and Passenger ID Already Exists.")
    @PostMapping("/status/{tripId}/{passengerId}")
    public ResponseEntity<String> createPassengerStatus(@PathVariable Long tripId, @PathVariable Long passengerId) {
        try {
            PassengerTripStatus ptsCreated = passengerTripStatusService.createPassengerStatus(tripId, passengerId);
            return ResponseEntity.ok("pts created: " +  ptsCreated.getId());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @Operation(summary = "Given PassengerTripStatusID, Remove Status Entry from Repository.",
            description = "Remove from Repository an Entry of Given PassengerTripStatusID, Returns " +
            "[204] if Successful, " +
            "[404] if No Such PassengerTripStatus Found in Repository.")
    @DeleteMapping("/status/{ptsId}")
    public ResponseEntity<String> deletePassengerTripStatus(@PathVariable Long ptsId) {
        boolean deleted = passengerTripStatusService.deletePTSById(ptsId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Given Trip and Passenger ID, Return Status Entry's PassengerTripStatusID from Repository.",
            description = "Search Repository for an Entry of Given Trip and Passenger ID, Returns" +
                    "[200 + Entry's ID]," +
                    "[404] if No Such PassengerTripStatus Found in Repository.")
    @GetMapping("/status/id/{tripId}/{passengerId}")
    public ResponseEntity<String> getPTSId(@PathVariable Long tripId, @PathVariable Long passengerId) {
        try {
            Long ptsId = passengerTripStatusService.getPTSid(tripId, passengerId);
            return ResponseEntity.ok(String.valueOf(ptsId));
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Given Trip ID, Passenger ID, and a New Status, Update Status Entry in Repository.",
            description = "Update Repository a PassengerTripStatus Entry of Given Trip and Passenger ID, Returns" +
                    "[200] if Updated Successfully, " +
                    "[400] if Request Status is Illegal or No Such PassengerTripStatus Found in Repository.")
    @PostMapping("/update-status")
    public ResponseEntity<String> updatePassengerStatus(@RequestParam Long tripId, @RequestParam Long passengerId, @RequestParam String status) {
        try {
            passengerTripStatusService.updateStatus(tripId, passengerId, status);
            return ResponseEntity.ok("Passenger status updated successfully.");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Update PTS payment status by passenger id and trip id", description = "Update PTS payment status in the database by the given trip's id and passenger's id with the provided payment status.")
    @PostMapping("/update-payment")
    public ResponseEntity<String> updatePaymentStatus(@RequestParam Long tripId, @RequestParam Long passengerId, @RequestParam boolean paid) {
        try {
            passengerTripStatusService.updatePaymentStatus(tripId, passengerId, paid);
            return ResponseEntity.ok("Payment status updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get single PTS payment status by passenger id and trip id", description = "Get PTS payment status from database for a single PTS by the given trip's id and passenger's id.")
    @GetMapping("/paymentStatus/{tripId}/{passengerId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long tripId, @PathVariable Long passengerId) {
        try {
            boolean isPaid = passengerTripStatusService.getPaymentStatus(tripId, passengerId);
            String statusMessage = isPaid ? "Payment completed" : "Payment pending";
            return ResponseEntity.ok("Payment status for trip ID " + tripId + " and passenger ID " + passengerId + ": " + statusMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Accept a passenger for a trip", description = "Marks a passenger's request as accepted, adds the trip to their account, and notifies the trip chat.")
    @PostMapping("/accept")
    public ResponseEntity<String> acceptPassenger(
            @RequestParam Long tripId,
            @RequestParam Long passengerId) {
        try {
            passengerTripStatusService.acceptPassengerForTrip(tripId, passengerId);
            return ResponseEntity.ok("Passenger accepted and notified.");
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Decline a passenger for a trip", description = "Marks a passenger's request as declined and notifies them.")
    @PostMapping("/decline")
    public ResponseEntity<String> declinePassenger(
            @RequestParam Long tripId,
            @RequestParam Long passengerId) {
        try {
            passengerTripStatusService.declinePassengerForTrip(tripId, passengerId);
            return ResponseEntity.ok("Passenger declined and notified.");
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Request to join a trip", description = "A passenger requests to join a given trip. Creates a PTS in PENDING status.")
    @PostMapping("/request/{tripId}/{passengerId}")
    public ResponseEntity<String> requestToJoinTrip(@PathVariable Long tripId, @PathVariable Long passengerId) {
        try {
            PassengerTripStatus pts = passengerTripStatusService.requestToJoinTrip(tripId, passengerId);
            return ResponseEntity.ok("Request sent successfully. PTS ID: " + pts.getId());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/pending/{tripId}")
    public ResponseEntity<?> getPendingPassengers(@PathVariable Long tripId) {
        try {
            // Fetch pending passengers for the given trip ID
            List<User> pendingPassengers = passengerTripStatusService.getPendingPassengersForTrip(tripId);

            if (pendingPassengers.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Return the list of pending passengers
            return ResponseEntity.ok(pendingPassengers);

        } catch (EntityNotFoundException e) {
            // Handle case where the trip is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trip with ID " + tripId + " not found.");
        } catch (Exception e) {
            // Handle any unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Get payment status of all passengers in a trip", description = "Retrieve the payment status for all passengers in a trip.")
    @GetMapping("/paymentStatus/all/{tripId}")
    public ResponseEntity<?> getAllPassengersPaymentStatus(@PathVariable Long tripId) {
        try {
            List<Map<String, Object>> paymentStatuses = passengerTripStatusService.getAllPassengersPaymentStatusForTrip(tripId);
            return ResponseEntity.ok(paymentStatuses);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }


    @Operation(summary = "Get payment status for a passenger in a trip", description = "Fetches the payment status for a specific passenger in a specific trip by trip ID and passenger ID.")
    @GetMapping("/payment-status/{tripId}/{passengerId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatusForPassengerInTrip(
            @PathVariable Long tripId,
            @PathVariable Long passengerId) {
        try {
            // Call the service method to get payment status
            Map<String, Object> paymentStatus = passengerTripStatusService.getPaymentStatusForPassengerInTrip(tripId, passengerId);
            return ResponseEntity.ok(paymentStatus);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }

    @Operation(summary = "Remove a passenger from a trip", description = "Removes a passenger from a specified trip by trip ID and passenger ID.")
    @DeleteMapping("/remove/{tripId}/{passengerId}")
    public ResponseEntity<String> removePassengerFromTrip(@PathVariable Long tripId, @PathVariable Long passengerId) {
        try {
            passengerTripStatusService.removePassengerFromTrip(tripId, passengerId);
            return ResponseEntity.ok("Passenger removed successfully from the trip.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

}
