package cycloneCarpool.LocationTracking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveTripRepository extends JpaRepository<ActiveTrip, Long> {
    //Add necessary methods here
}
