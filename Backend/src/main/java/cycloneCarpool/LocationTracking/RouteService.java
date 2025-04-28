package cycloneCarpool.LocationTracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    @Value("${mapbox.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RouteResponseDTO getOptimalRoute(String startCoordinates, String endCoordinates) {
        try {
            // Validate and parse coordinates as latitude,longitude
            double[] start = parseCoordinates(startCoordinates);
            double[] end = parseCoordinates(endCoordinates);

            // Mapbox expects longitude,latitude, so swap them
            String formattedStart = start[1] + "," + start[0];
            String formattedEnd = end[1] + "," + end[0];

            String profile = "mapbox/driving";
            String url = String.format("https://api.mapbox.com/directions/v5/%s/%s;%s?geometries=geojson&access_token=%s",
                    profile, formattedStart, formattedEnd, apiKey);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode routeNode = rootNode.path("routes").get(0);

            if (routeNode.isNull()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No route found for the given coordinates.");
            }

            // Extract details for the response DTO
            RouteResponseDTO routeResponse = new RouteResponseDTO();
            routeResponse.setDuration(routeNode.path("duration").asDouble() / 60);
            routeResponse.setDistance(routeNode.path("distance").asDouble() / 1609.34);
            routeResponse.setSummary(routeNode.path("legs").get(0).path("summary").asText());

            // Extract geometry coordinates
            List<double[]> coordinates = new ArrayList<>();
            for (JsonNode coord : routeNode.path("geometry").path("coordinates")) {
                double[] point = {coord.get(1).asDouble(), coord.get(0).asDouble()};
                coordinates.add(point);
            }
            routeResponse.setGeometry(coordinates);

            return routeResponse;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the route.", e);
        }
    }

    private double[] parseCoordinates(String coordinates) {
        try {
            String[] parts = coordinates.split(",");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid coordinates format");

            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);

            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Coordinates out of range");
            }

            return new double[]{latitude, longitude};
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinates provided. Coordinates must be in the format (latitude,longitude) and within valid ranges.");
        }
    }

    public double[] getCoordinatesFromPlaceName(String placeName) {
        try {
            String url = String.format("https://api.mapbox.com/geocoding/v5/mapbox.places/%s.json?access_token=%s&limit=1",
                    placeName, apiKey);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode featuresNode = rootNode.path("features");

            if (featuresNode.isArray() && featuresNode.size() > 0) {
                JsonNode coordinates = featuresNode.get(0).path("geometry").path("coordinates");
                double longitude = coordinates.get(0).asDouble();
                double latitude = coordinates.get(1).asDouble();
                return new double[]{latitude, longitude};
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No coordinates found for the given place name.");
            }

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching coordinates.", e);
        }
    }
}
