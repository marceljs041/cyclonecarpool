package cycloneCarpool.Vehicles;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author eddiegong
 */

// A Record of Vehicles in System.
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findVehicleByOwnerId(Long id);
}
