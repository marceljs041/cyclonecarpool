package cycloneCarpool.Trips;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author eddiegong
 */

// A Record of Trips in System.
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByDriverId(Long driverId, Sort sort);

    @Query("SELECT t FROM Trip t JOIN t.passengers p WHERE p.id = :passengerId")
    List<Trip> findAllByPassengerId(@Param("passengerId")Long passengerId, Sort sort);

    @Query("SELECT t FROM Trip t WHERE (:startLocation is null or t.startLocation = :startLocation) " +
            "and (:endLocation is null or t.endLocation = :endLocation) " +
            "and (:time is null or t.time >= :time)")
    List<Trip> findByStartLocationAndEndLocationAfter(String startLocation, String endLocation, LocalDateTime time, Sort sort);

    @Query("SELECT t FROM Trip t WHERE (t.time >= :time)")
    List<Trip> findAllAfterTime(LocalDateTime time, Sort sort);

}
