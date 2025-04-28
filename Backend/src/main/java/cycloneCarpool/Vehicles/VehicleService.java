package cycloneCarpool.Vehicles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cycloneCarpool.LicensePlateInfo.*;
import cycloneCarpool.Users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * @author eddiegong
 */

// CRUD Methods of Vehicle, Initialize from License Plate Info Repository, MPG from Cars API.
@Service
public class VehicleService {

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    LicensePlateInfoRepository licensePlateInfoRepository;

    @Autowired
    UserRepository userRepository;


    @Value("${ninjas.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    public Vehicle createVehicle(Vehicle vehicle) {
        long ownerId = vehicle.getOwnerId();

        if (userRepository.existsById(ownerId)) {
            Optional<Vehicle> vehicleOptional = vehicleRepository.findVehicleByOwnerId(ownerId);

            if (vehicleOptional.isEmpty()) {
                fetchByLicensePlate(vehicle);
                fetchMpg(vehicle);

                return vehicleRepository.save(vehicle);
            }
        } else {
            throw new EntityNotFoundException("User with id " + ownerId + " does not exist");
        }
        throw new IllegalStateException("Reached max amount of vehicles tied to this account");
    }

    public List<Vehicle> getAllVehicles() {return vehicleRepository.findAll();}

    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId).orElseThrow(EntityNotFoundException::new);
    }

    public Vehicle getVehicleByOwnerId(Long ownerId) {
        return vehicleRepository.findVehicleByOwnerId(ownerId).orElseThrow(EntityNotFoundException::new);
    }

    public boolean checkVehicleEditPermission(Long vehicleId, Long ownerId) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);

        return vehicle.map(v -> v.getOwnerId() == ownerId)
                .orElse(false);
    }

    public Vehicle editVehicle(Long vehicleId, Vehicle vehicle) {
        Optional<Vehicle> editingVehicle = vehicleRepository.findById(vehicleId);

        if (editingVehicle.isPresent()) {
            Vehicle v = editingVehicle.get();
            v.setLicensePlate(vehicle.getLicensePlate());
            v.setState(vehicle.getState());

            v = fetchByLicensePlate(v);
            v = fetchMpg(v);

            return vehicleRepository.save(v);
        } else {
            throw new EntityNotFoundException("Vehicle with ID " + vehicleId + " not found.");
        }
    }


    public boolean deleteVehicleById(Long vehicleId) {

        if (vehicleRepository.existsById(vehicleId)) {
            vehicleRepository.deleteById(vehicleId);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteVehicleByOwnerId(Long ownerId) {
        Optional<Vehicle> vehicle = vehicleRepository.findVehicleByOwnerId(ownerId);

        if (vehicle.isPresent()) {
            vehicleRepository.delete(vehicle.get());
            return true;
        } else {
            return false;
        }

    }

    private Vehicle fetchByLicensePlate(Vehicle vehicle) {
        String licensePlate = vehicle.getLicensePlate();
        String state = vehicle.getState();

        Optional<LicensePlateInfo> oLp = licensePlateInfoRepository.findByLicensePlateAndState(licensePlate, state);

        if (oLp.isPresent()) {
            LicensePlateInfo lp = oLp.get();

            vehicle.setMake(lp.getMake());
            vehicle.setModel(lp.getModel());
            vehicle.setYear(lp.getYear());
            vehicle.setColor(lp.getColor());

            return vehicle;
        }

        throw new EntityNotFoundException("License plate info [" + licensePlate + ", " + state + "] not found");
    }

    public Vehicle fetchMpg(Vehicle vehicle) {
        String make = vehicle.getMake();
        String model = vehicle.getModel();
        String year = vehicle.getYear();

        String url = String.format("https://api.api-ninjas.com/v1/cars?make=%s&model=%s&year=%s&limit=1", make, model, year);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                JsonNode rootNode = objectMapper.readTree(body);

                if (rootNode != null && !rootNode.isEmpty()) {
                    JsonNode carNode = rootNode.get(0);  // Get the first car model result

                    // Extract the mpg values
                    long combinationMpg = carNode.path("combination_mpg").asLong();
                    // Update the vehicle with these values

                    vehicle.setMpg(combinationMpg);
                    return vehicle;
                }
            }

            throw new RuntimeException("Failed to fetch vehicle details. Status: " + response.getStatusCode());

        } catch (Exception e) {
            throw new RuntimeException("Error occurred while fetching vehicle details", e);
        }
    }

}
