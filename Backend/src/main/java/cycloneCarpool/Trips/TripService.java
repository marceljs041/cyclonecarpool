package cycloneCarpool.Trips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cycloneCarpool.LocationTracking.RouteService;
import cycloneCarpool.Users.User;
import cycloneCarpool.Users.UserRepository;
import cycloneCarpool.Vehicles.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author eddiegong
 */

// CRUD Methods of Trip, has Methods for Filtering Trips, Permission Check,
// and Distance/Cost Estimation with Mapbox API and Gas Prices API.
@Service
public class TripService {

    @Autowired
    TripRepository tripRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RouteService routeService;

    @Autowired
    VehicleService vehicleService;

    @Value("${collectapi.api.key}")
    private String apiKey;

    // Sort by time
    Sort timeSort = Sort.by(Sort.Order.asc("time"));

    public Trip createTrip(Trip trip) {
        trip.setDistance(estimateDistance(trip.getStartLocation(), trip.getEndLocation()));
        return tripRepository.save(trip);}

    public List<Trip> getAllTrips() {return tripRepository.findAll();}

    public List<Trip> getFilteredTrips(String startLocation, String endLocation) {
        // Fetch trips using existing repository method
        List<Trip> trips = tripRepository.findByStartLocationAndEndLocationAfter(
                startLocation, endLocation, LocalDateTime.now(), timeSort);

        return trips.stream()
                .filter(trip -> trip.getPassengers().size() < trip.getSeat()) // Only include trips with available seats
                .toList();
    }

    // All trips after the current time
    public List<Trip> getFutureTrips() {
        List<Trip> trips = tripRepository.findAllAfterTime(LocalDateTime.now(), timeSort);

    // Filter out trips that are full
    return trips.stream()
            .filter(trip -> trip.getPassengers().size() < trip.getSeat())
            .toList();
    }

    public List<Trip> getMyTrips(Long driverId) {
        return tripRepository.findAllByDriverId(driverId, timeSort);
    }

    public List<Trip> getMyRides(Long passengerId) {
        return tripRepository.findAllByPassengerId(passengerId, timeSort);
    }

    public Trip editTrip(Long tripId, Trip trip) {
        Optional<Trip> editingTrip = tripRepository.findById(tripId);
        if (editingTrip.isPresent()) {
            Trip editTrip = editingTrip.get();
            editTrip.setAll(trip);
            editTrip.setDistance(estimateDistance(editTrip.getStartLocation(), editTrip.getEndLocation()));

            return tripRepository.save(editTrip);

        } else {
            throw new EntityNotFoundException("Trip with ID " + tripId + " not found.");
        }
    }

    public Trip getSingleTrip(Long tripId) {
        return tripRepository.findById(tripId).orElseThrow(EntityNotFoundException::new);
    }

    public boolean checkTripEditPermission(Long tripId, Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
            Trip trip = tripRepository.findById(tripId).orElseThrow(EntityNotFoundException::new);

            if (Objects.equals(user.getRole(), "Admin")) { return true; } // Admin can edit all trips
            else if (Objects.equals(user.getRole(), "Driver")) { // Driver can edit their trips
                return user.getId() == trip.getDriverId();
            }
            return false;

        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    public boolean deleteTripById(Long tripId) {

        if (tripRepository.existsById(tripId)) {
            tripRepository.deleteById(tripId);
            return true;
        } else {
            return false;
        }
    }

    public Trip addPassenger(Long tripId, Long passengerId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!trip.getPassengers().contains(passenger)) {
            trip.getPassengers().add(passenger);
            tripRepository.save(trip);
        }

        return trip;

    }

    public Trip removePassenger(Long tripId, Long passengerId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        trip.getPassengers().remove(passenger);
        tripRepository.save(trip);
        return trip;
    }

    public boolean checkHasPassenger(Long tripId, Long userId) {
        try {
            Trip trip = tripRepository.findById(tripId).orElseThrow(EntityNotFoundException::new);
            return trip.getPassengers().stream()
                    .anyMatch(passenger -> passenger.getId().equals(userId));

        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    public double estimateDistance(String startLocation, String endLocation) {
        try {
            double[] coordinates = routeService.getCoordinatesFromPlaceName(startLocation);
            String startCoordinates = String.format("%f, %f", coordinates[0], coordinates[1]);
            coordinates = routeService.getCoordinatesFromPlaceName(endLocation);
            String endCoordinates = String.format("%f, %f", coordinates[0], coordinates[1]);

            return Math.round((routeService.getOptimalRoute(startCoordinates, endCoordinates)).getDistance()* 100.0) / 100.0;
        } catch (Exception e) {
            return 0; // No estimates
        }
    }

    public FuelEstimates estimateCost(TripDTO tripDTO) throws IOException, InterruptedException {
        long userId = tripDTO.getUserId();
        long mpg = vehicleService.getVehicleByOwnerId(userId).getMpg();

        String startLocation = tripDTO.getStartLocation();
        String endLocation = tripDTO.getEndLocation();

        String url = "https://api.collectapi.com/gasPrice/allUsaPrice";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("authorization", apiKey)
                .header("content-type", "application/json")
                .GET() // Send GET request
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode resultArray = rootNode.path("result");

        double totalRegular = 0.0;
        double totalPremium = 0.0;
        double totalDiesel = 0.0;
        int count = resultArray.size();

        for (int i = 0; i < count; i++) {
            JsonNode state = resultArray.get(i);

            totalRegular += Double.parseDouble((state.path("gasoline").asText()).replace("$", ""));
            totalPremium += Double.parseDouble((state.path("premium").asText()).replace("$", ""));
            totalDiesel += Double.parseDouble((state.path("diesel").asText()).replace("$", ""));
        }
        double avgRegular = totalRegular / count;
        double avgPremium = totalPremium / count;
        double avgDiesel = totalDiesel / count;


        FuelEstimates estimates = new FuelEstimates();
        estimates.setDistanceEstimate(estimateDistance(startLocation, endLocation));
        estimates.setFuelEstimate(Math.round(estimates.getDistanceEstimate()/mpg * 100.0) / 100.0);

        estimates.setRegular(Math.round(avgRegular * estimates.getFuelEstimate() * 100.0) / 100.0);
        estimates.setPremium(Math.round(avgPremium * estimates.getFuelEstimate() * 100.0) / 100.0);
        estimates.setDiesel(Math.round(avgDiesel * estimates.getFuelEstimate() * 100.0) / 100.0);

        return estimates;

    }

    @Transactional
    public List<User> getPassengersByTripId(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        trip.getPassengers().size();
        return trip.getPassengers();
    }

    @Transactional
    public Trip updateTrip(Trip trip) {
        return tripRepository.save(trip);
    }

}
