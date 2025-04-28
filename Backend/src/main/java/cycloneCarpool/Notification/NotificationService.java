package cycloneCarpool.Notification;

import cycloneCarpool.Trips.PassengerTripStatus;
import cycloneCarpool.Users.User;
import cycloneCarpool.Users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author eddiegong
 */

/**
 * Code Review: Anthony Campana
 * This class includes the essential functions that we want for notifications.
 * Includes:
 * notifications to driver for passenger requesting to join trip
 * notifications to passenger if driver accepted or declined the passenger for the trip
 */

// According to PassengerTripStatus Entry, Builds Notifications Accordingly.
@Service("notificationServiceOne")
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    public Notification notifyDriverOfRequest(PassengerTripStatus passengerTripStatus) {
        User passenger = passengerTripStatus.getPassenger();
        String passengerName = passenger.getFirstname() + " " + passenger.getLastname();
        User driver = userService.getUserById(passengerTripStatus.getTrip().getDriverId());

        String content = "Passenger " + passengerName + " has requested to join your trip.";
        Notification notification = new Notification(driver, content, false);
        notificationRepository.save(notification);

        return notification;
    }

    public Notification notifyPassengerOfApproval(PassengerTripStatus passengerTripStatus) {
        User passenger = passengerTripStatus.getPassenger();
        User driver = userService.getUserById(passengerTripStatus.getTrip().getDriverId());
        String driverName = driver.getFirstname() + " " + driver.getLastname();

        String content = "Driver " + driverName + " has approved your request to join a trip.";
        Notification notification = new Notification(passenger, content, false);
        notificationRepository.save(notification);

        return notification;
    }

    public Notification notifyPassengerOfDecline(PassengerTripStatus passengerTripStatus) {
        User passenger = passengerTripStatus.getPassenger();
        User driver = userService.getUserById(passengerTripStatus.getTrip().getDriverId());
        String driverName = driver.getFirstname() + " " + driver.getLastname();

        String content = "Driver " + driverName + " has declined your request to join a trip.";
        Notification notification = new Notification(passenger, content, false);
        notificationRepository.save(notification);

        return notification;
    }

    public List<Notification> getUnsentNotifications(Long receiverId){
        return notificationRepository.findByReceiverIdAndSentFalse(receiverId);
    }

    public void notified(Notification notification) {
        notification.setSent(true);
        notificationRepository.save(notification);
    }
}
