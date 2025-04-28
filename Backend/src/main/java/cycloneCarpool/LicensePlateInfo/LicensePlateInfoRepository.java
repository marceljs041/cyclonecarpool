package cycloneCarpool.LicensePlateInfo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author eddiegong
 */

// License Plate Lookup Database, contains 16 Entries of Sample Data.
// No Methods should Modify the Data to Imitate using a Foreign API.
public interface LicensePlateInfoRepository extends JpaRepository<LicensePlateInfo, Long> {

    Optional<LicensePlateInfo> findByLicensePlateAndState(String licensePlate, String state);
}
