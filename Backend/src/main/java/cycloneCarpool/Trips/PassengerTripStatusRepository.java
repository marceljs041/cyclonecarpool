package cycloneCarpool.Trips;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Anthony Campana
 */

// A Record of Status between Passenger and Trip, Specifically the Request and Payment Status.
public interface PassengerTripStatusRepository extends JpaRepository<PassengerTripStatus, Long> {

    Optional<PassengerTripStatus> findById(Long id);

    Optional<PassengerTripStatus> findByTrip_TripIdAndPassengerId(Long tripId, Long passengerId);

    List<PassengerTripStatus> findByTrip_TripIdAndStatus(Long tripId, String status);

    List<PassengerTripStatus> findByTrip_TripId(Long tripId);


    // Add more custom methods here
}
