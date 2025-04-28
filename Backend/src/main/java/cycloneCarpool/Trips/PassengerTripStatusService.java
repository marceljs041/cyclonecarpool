package cycloneCarpool.Trips;

import cycloneCarpool.Messages.ChatSocket;
import cycloneCarpool.Messages.Message;
import cycloneCarpool.Messages.MessageRepository;
import cycloneCarpool.Messages.MessageService;
import cycloneCarpool.Payments.Payment;
import cycloneCarpool.Payments.PaymentService;
import cycloneCarpool.Users.User;
import cycloneCarpool.Users.UserRepository;
import cycloneCarpool.Users.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Anthony Campana
 */

// CRUD of PassengerTripStatus, has Methods to Initialize PassengerTripStatus Specifically and to Validate Status.
@Service
public class PassengerTripStatusService {

    @Autowired
    private PassengerTripStatusRepository passengerTripStatusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TripService tripService;
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private MessageService messageService; // To save system messages for chat
    @Autowired
    private MessageRepository messageRepository;

    public PassengerTripStatus getPassengerTripStatus(Long tripId, Long passengerId) {
        try {
            Optional<PassengerTripStatus> statusRecord = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId);
            return statusRecord.orElseThrow(() -> new IllegalArgumentException("Passenger status not found for the specified trip and passenger."));
        } catch (IllegalArgumentException e) {
            // Log error if needed
            throw e; // Re-throw to let the controller handle it
        }
    }

    @Transactional
    public PassengerTripStatus requestToJoinTrip(Long tripId, Long passengerId) {
        // Check if the trip and passenger exist
        Trip trip = tripService.getSingleTrip(tripId);
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check if a PTS already exists
        PassengerTripStatus existingPTS = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId).orElse(null);
        if (existingPTS != null) {
            throw new IllegalStateException("A request from this passenger for this trip already exists.");
        }

        // Create new PTS with PENDING status
        PassengerTripStatus newPTS = new PassengerTripStatus();
        newPTS.setTrip(trip);
        newPTS.setPassenger(passenger);
        newPTS.setStatus("PENDING");
        newPTS.setPaid(false);

        return passengerTripStatusRepository.save(newPTS);
    }

    public void updateStatus(Long tripId, Long passengerId, String status) {
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        Optional<PassengerTripStatus> statusRecord = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId);
        if (statusRecord.isPresent()) {
            PassengerTripStatus passengerTripStatus = statusRecord.get();
            passengerTripStatus.setStatus(status);
            passengerTripStatusRepository.save(passengerTripStatus);
        } else {
            throw new IllegalArgumentException("Passenger status not found for the specified trip and passenger.");
        }
    }

    public void updatePaymentStatus(Long tripId, Long passengerId, boolean paid) {
        Optional<PassengerTripStatus> statusRecord = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId);
        if (statusRecord.isPresent()) {
            PassengerTripStatus passengerTripStatus = statusRecord.get();
            passengerTripStatus.setPaid(paid);
            passengerTripStatusRepository.save(passengerTripStatus);
        } else {
            throw new IllegalArgumentException("Passenger payment status not found for the specified trip and passenger.");
        }
    }

    public boolean getPaymentStatus(Long tripId, Long passengerId) {
        Optional<PassengerTripStatus> statusRecord = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId);
        if (statusRecord.isPresent()) {
            return statusRecord.get().isPaid();
        } else {
            throw new RuntimeException("Passenger status not found for the specified trip and passenger.");
        }
    }

    private boolean isValidStatus(String status) {
        return status.equals("PENDING") || status.equals("ACCEPTED") || status.equals("REJECTED");
    }

    public PassengerTripStatus createPassengerStatus(Long tripId, Long passengerId) throws IllegalStateException, EntityNotFoundException {
        Optional<PassengerTripStatus> passengerTripStatus = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId);
        if (passengerTripStatus.isPresent()) { throw new IllegalStateException("Request already Exists."); }
        Trip trip = tripService.getSingleTrip(tripId);
        User passenger = userService.getUserById(passengerId);
        PassengerTripStatus createdPassengerTripStatus = new PassengerTripStatus(trip, passenger, "PENDING", false);
        passengerTripStatusRepository.save(createdPassengerTripStatus);

        return createdPassengerTripStatus;

    }

    public PassengerTripStatus getPassengerTripStatusById(Long id) {
        return passengerTripStatusRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public Long getPTSid(Long tripId, Long passengerId) {
        Optional<PassengerTripStatus> opts = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId);
        if (opts.isPresent()) {
            PassengerTripStatus pts = opts.get();
            return pts.getId();
        } else {
            throw new RuntimeException("Passenger status not found for the specified trip and passenger.");
        }
    }

    public boolean deletePTSById(Long ptsId) {
        if (passengerTripStatusRepository.existsById(ptsId)) {
            passengerTripStatusRepository.deleteById(ptsId);
            return true;
        }
        return false;

    }


    public void acceptPassengerForTrip(Long tripId, Long passengerId) {
        // Fetch passenger-trip status
        PassengerTripStatus pts = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger trip status not found"));

        // Check if already accepted
        if ("ACCEPTED".equals(pts.getStatus())) {
            throw new IllegalStateException("Passenger already accepted for the trip.");
        }

        // Update status
        pts.setStatus("ACCEPTED");
        passengerTripStatusRepository.save(pts);

        // Add passenger to trip
        tripService.addPassenger(tripId, passengerId);

        // Send a system message about the new passenger to the trip chat
        sendSystemMessageToTripChat(tripId, passengerId);

        // Initiate payment request for this passenger
        Trip trip = tripService.getSingleTrip(tripId);
        Double tripCost = (double) trip.getPrice(); // Implement this logic or set a fixed cost
        Payment payment = paymentService.initiatePayment(tripCost, tripId, passengerId);

        // Send a message requesting payment to the passenger
        sendPaymentRequestMessage(tripId, passengerId, tripCost, payment.getId());
    }

    private void sendPaymentRequestMessage(Long tripId, Long passengerId, Double amount, Long paymentId) {
        User passenger = userService.getUserById(passengerId);
        User systemUser = userService.getUserById((long) 196); // System user

        String content = "Payment of $" + amount + " is requested for trip " + tripId
                + ".";
        Message paymentMessage = new Message();
        paymentMessage.setSender(systemUser); // from system or driver
        paymentMessage.setReceiver(passenger);
        paymentMessage.setTripId(tripId);
        paymentMessage.setContent(content);
        paymentMessage.setIsRead(false);
        paymentMessage.setTimestamp(new java.util.Date());

        messageRepository.save(paymentMessage);

        // Broadcast payment request message to WebSocket if passenger is online
        ChatSocket.broadcastToTrip(tripId, "Payment requested for " + passenger.getFirstname() + " for trip " + tripId + " of amount " + amount);
    }

    private void sendSystemMessageToTripChat(Long tripId, Long passengerId) {
        User passenger = userService.getUserById(passengerId);
        String content = "User " + passenger.getFirstname() + ' ' + passenger.getLastname() + " has been added to the trip.";
        User systemUser = userService.getUserById((long) 196);

        Message systemMessage = new Message();
        systemMessage.setSender(systemUser); // Indicate system sender
        systemMessage.setTripId(tripId);
        systemMessage.setContent(content);
        systemMessage.setIsRead(true);
        systemMessage.setTimestamp(new java.util.Date());

        messageRepository.save(systemMessage);
        ChatSocket.broadcastToTrip(tripId, content);
    }

    public void declinePassengerForTrip(Long tripId, Long passengerId) {
        PassengerTripStatus pts = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger trip status not found"));

        if ("DECLINED".equals(pts.getStatus())) {
            throw new IllegalStateException("Passenger already declined.");
        }

        pts.setStatus("DECLINED");
        passengerTripStatusRepository.save(pts);

        notifyPassengerOfDecline(tripId, passengerId);
    }

    private void notifyPassengerOfDecline(Long tripId, Long passengerId) {
        User passenger = userService.getUserById(passengerId);
        User systemUser = userService.getUserById((long) 196);
        String content = "Your request to join trip " + tripId + " has been declined.";
        Message systemMessage = new Message();
        systemMessage.setSender(systemUser); // System message
        systemMessage.setReceiver(passenger);
        systemMessage.setContent(content);
        systemMessage.setIsRead(false);
        systemMessage.setTimestamp(new java.util.Date());
        messageRepository.save(systemMessage);
    }

    public void notifyDriverOfPayment(Long tripId, Long passengerId) {
        Trip trip = tripService.getSingleTrip(tripId);
        Long driverId = trip.getDriverId();
        User driver = userService.getUserById(driverId);
        User systemUser = userService.getUserById((long) 196); // System user placeholder
        User passenger = userService.getUserById(passengerId);

        String content = "Passenger " + passenger.getFirstname() + " has completed payment for trip " + tripId + ".";

        Message paymentNotification = new Message();
        paymentNotification.setSender(systemUser);
        paymentNotification.setReceiver(driver);
        paymentNotification.setTripId(tripId);
        paymentNotification.setContent(content);
        paymentNotification.setIsRead(false);
        paymentNotification.setTimestamp(new java.util.Date());

        messageRepository.save(paymentNotification);

        // Send directly to the driver if online
        ChatSocket.sendMessageToUser(driverId, "System: " + content);
    }

    /**
     * Get a list of all pending passengers for a given trip ID.
     *
     * @param tripId the ID of the trip
     * @return a list of users (passengers) with a pending status
     */
    public List<User> getPendingPassengersForTrip(Long tripId) {
        // Ensure the trip exists
        Trip trip = tripService.getSingleTrip(tripId);
        if (trip == null) {
            throw new EntityNotFoundException("Trip with ID " + tripId + " not found.");
        }

        // Fetch all PassengerTripStatus entries for the trip with status "PENDING"
        List<PassengerTripStatus> pendingStatuses = passengerTripStatusRepository.findByTrip_TripIdAndStatus(tripId, "PENDING");

        // Extract the list of passengers from the pending statuses
        return pendingStatuses.stream()
                .map(PassengerTripStatus::getPassenger)
                .collect(Collectors.toList());
    }


    public List<Map<String, Object>> getAllPassengersPaymentStatusForTrip(Long tripId) {
        // Ensure the trip exists
        Trip trip = tripService.getSingleTrip(tripId);
        if (trip == null) {
            throw new EntityNotFoundException("Trip with ID " + tripId + " not found.");
        }

        // Fetch all PassengerTripStatus entries for the trip
        List<PassengerTripStatus> tripStatuses = passengerTripStatusRepository.findByTrip_TripId(tripId);

        // Map results to a list of JSON-like objects
        return tripStatuses.stream()
                .map(pts -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("passengerId", pts.getPassenger().getId());
                    result.put("passengerName", pts.getPassenger().getFirstname() + " " + pts.getPassenger().getLastname());
                    result.put("paymentStatus", pts.isPaid());
                    return result;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPaymentStatusForPassengerInTrip(Long tripId, Long passengerId) {
        // Fetch the PassengerTripStatus entry
        PassengerTripStatus pts = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger status not found for trip ID " + tripId + " and passenger ID " + passengerId));

        // Return the payment status as a JSON-like object
        return Map.of(
                "passengerId", pts.getPassenger().getId(),
                "passengerName", pts.getPassenger().getFirstname() + " " + pts.getPassenger().getLastname(),
                "paymentStatus", pts.isPaid()
        );
    }

    @Transactional
    public void removePassengerFromTrip(Long tripId, Long passengerId) {
        // Remove PTS entry
        PassengerTripStatus pts = passengerTripStatusRepository.findByTrip_TripIdAndPassengerId(tripId, passengerId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger or trip association not found."));
        passengerTripStatusRepository.delete(pts);

        // Remove the passenger from the Trip's passengers list
        Trip trip = tripService.getSingleTrip(tripId);
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger not found."));

        if (trip.getPassengers().contains(passenger)) {
            trip.getPassengers().remove(passenger);
            tripService.updateTrip(trip); // Save updated trip
        } else {
            throw new IllegalStateException("Passenger is not part of the trip.");
        }
    }


}
