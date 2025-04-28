package cycloneCarpool.LocationTracking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    List<DriverLocation> findByTripId(Long tripId);
    Optional<DriverLocation> findTopByTripIdOrderByTimestampDesc(Long tripId);
}
