package cycloneCarpool.LocationTracking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DriverLocationService {
    @Autowired
    private DriverLocationRepository driverLocationRepository;

    public DriverLocation saveLocation(Long tripId, double latitude, double longitude) {
        DriverLocation location = new DriverLocation(tripId, latitude, longitude, LocalDateTime.now());
        return driverLocationRepository.save(location);
    }

    public List<DriverLocation> getLocationsForTrip(Long tripId) {
        return driverLocationRepository.findByTripId(tripId);
    }

    public DriverLocation getLatestLocation(Long tripId) {
        return driverLocationRepository.findTopByTripIdOrderByTimestampDesc(tripId).orElse(null);
    }
}
