package cycloneCarpool.Identification;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/identity")
public class IdentityVerificationController {


    @Autowired
    private IDVerificationService idVerificationService;


    @PostMapping("/verify")
    public ResponseEntity<?> verifyDriverLicense(@RequestParam("imageUrl") String imageUrl,
                                                 @RequestParam("userId") Long userId,
                                                 @RequestParam("firstName") String firstName,
                                                 @RequestParam("lastName") String lastName) {
        try {
            // Call the verification service
            Map<String, Object> result = idVerificationService.verifyDriverLicense(imageUrl, userId, firstName, lastName);


            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Verification failed", "details", e.getMessage()));
        }
    }
}
