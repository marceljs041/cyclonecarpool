package cycloneCarpool.LocationTracking;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActiveTripService {

    @Autowired
    private ActiveTripRepository activeTripRepository;

    public ActiveTrip startTrip(String driverId, String startLocation, String endLocation, List<String> passengerIds) {
        ActiveTrip trip = new ActiveTrip();
        trip.setDriverId(driverId);
        trip.setStartLocation(startLocation);
        trip.setEndLocation(endLocation);
        trip.setPassengerIds(passengerIds);
        return activeTripRepository.save(trip);
    }

    public ActiveTrip updateTrip(Long tripId, String newEndLocation) {
        ActiveTrip trip = activeTripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        trip.setEndLocation(newEndLocation);
        return activeTripRepository.save(trip);
    }


    public void endTrip(Long tripId) {
        activeTripRepository.deleteById(tripId);
    }

    public ActiveTrip getTrip(Long tripId) {
        return activeTripRepository.findById(tripId).orElse(null);
    }

    public List<ActiveTrip> getAllTrips(){
        return activeTripRepository.findAll();
    }
}
