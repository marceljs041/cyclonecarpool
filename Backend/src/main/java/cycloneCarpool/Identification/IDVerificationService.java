package cycloneCarpool.Identification;


import cycloneCarpool.Users.User;
import cycloneCarpool.Users.UserRepository;
import cycloneCarpool.Users.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Service
public class IDVerificationService {


    @Value("${idanalyzer.apiKey}")
    private String apiKey;


    private final String apiUrl = "https://api2.idanalyzer.com/scan";


    @Autowired
    private UserRepository userRepository;


    @Transactional
    public Map<String, Object> verifyDriverLicense(String imageUrl, Long userId, String userFirstName, String userLastName) {
        RestTemplate restTemplate = new RestTemplate();


        String base64Image = fetchAndEncodeImage(imageUrl);
        if (base64Image == null) {
            return Map.of("verified", false, "message", "Failed to fetch or encode the image.");
        }


        Map<String, Object> payload = new HashMap<>();
        payload.put("document", base64Image);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", apiKey);


        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);


        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);


            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");


                    if (data != null && data.containsKey("firstName") && data.containsKey("lastName")) {
                        String documentFirstName = extractFieldValue(data, "firstName");
                        String documentLastName = extractFieldValue(data, "lastName");


                        if (userFirstName.equalsIgnoreCase(documentFirstName) && userLastName.equalsIgnoreCase(documentLastName)) {
                            // Update the user's verification status
                            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                            user.setVerified(true);
                            userRepository.save(user);


                            return Map.of("verified", true, "message", "Identity verified successfully.");
                        } else {
                            return Map.of("verified", false, "message", "Name mismatch between ID and account.");
                        }
                    }
                }
                return Map.of("verified", false, "message", "API verification failed or insufficient data.");
            }
        } catch (Exception e) {
            return Map.of("verified", false, "message", "Error occurred during verification: " + e.getMessage());
        }


        return Map.of("verified", false, "message", "Unable to connect to ID Analyzer.");
    }


    private String fetchAndEncodeImage(String imageUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            if (imageBytes != null) {
                return Base64.getEncoder().encodeToString(imageBytes);
            }
        } catch (Exception e) {
            // Log the error if necessary
        }
        return null;
    }


    private String extractFieldValue(Map<String, Object> data, String fieldName) {
        Object fieldData = data.get(fieldName);
        if (fieldData instanceof java.util.List) {
            Map<String, Object> firstItem = ((java.util.List<Map<String, Object>>) fieldData).get(0);
            return (String) firstItem.get("value");
        }
        return null;
    }
}
