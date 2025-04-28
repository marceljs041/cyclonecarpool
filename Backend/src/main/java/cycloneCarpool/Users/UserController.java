package cycloneCarpool.Users;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cycloneCarpool.Reviews.Review;
import cycloneCarpool.Reviews.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.naming.AuthenticationException;


import java.util.List;
import java.util.Map;
import java.util.Objects;


@Tag(name = "User Controller",
        description = "Operations Related to Users, Authentication, User-Related Reviews")
@RestController
@RequestMapping("/api/users")
public class UserController {


    @Autowired
    private UserService userService;


    @Autowired
    private ReviewService reviewService;




    // Endpoint for user signup
    @Operation(summary = "Given User Information, Create a New USer Entry.",
            description = "Create User Entry that has an Unregistered Email, Returns " +
                    "[200 + Entry] if Successful, " +
                    "[500] if Provided Email is Already Registered.")
    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody User user) {
        User newUser = userService.registerUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }


    // Signin returns also user information if authentication is successful
    @Operation(summary = "Given Sign In Information, Authenticate the Sign In Attempt.",
            description = "Search Repository for Entry that Match the Sign In Request's Email and Password, Returns " +
                    "[200 + Entry] if Successful, " +
                    "[401] if Not.")
    @PostMapping("/signin")
    public ResponseEntity<User> signin(@RequestBody SignInRequest signInRequest) {
        try {
            String email = signInRequest.getEmail();
            String password = signInRequest.getPassword();
            // Validate user with the extracted data
            User existingUser = userService.validateUser(email, password);


            return ResponseEntity.ok(existingUser);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 401 Unauthorized
        }
    }


    // GET method to fetch all users
    @Operation(summary = "Fetch All Users.",
            description = "Returns List of All Users in the Repository, Sorted by ID, Returns " +
                    "[200 + List].")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    // GET method to fetch all emails
    @Operation(summary = "Fetch All Users' Emails.",
            description = "Returns List of All Users' Emails in the Repository, Returns " +
                    "[200 + List].")
    @GetMapping("/emails")
    public ResponseEntity<List<String>> getAllEmails() {
        List<String> emails = userService.getAllEmails();
        return ResponseEntity.ok(emails);
    }


    @Operation(summary = "Given User ID, Fetch User.",
            description = "Search Repository for an Entry of Given User ID, Returns " +
                    "[200 + Entry or null]")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    // DELETE method to delete a user by ID
    @Operation(summary = "Given User ID, Remove User Entry from Repository.",
            description = "Remove Repository an Entry of Given User ID, Returns " +
                    "[204] if Successful, " +
                    "[404] if No Such User Found.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        boolean isDeleted = userService.deleteUserById(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();  // 204 No Content if successful
        } else {
            return ResponseEntity.notFound().build();  // 404 Not Found if user not found
        }
    }


    @Operation(summary = "Given User ID, Update User Entry's Role to Driver in Repository.",
            description = "Update Repository an Entry of Given User ID, Returns " +
                    "[200 + Entry(After Change)] if Successful, " +
                    "[400] if User does not have Passenger Role.")
    @GetMapping("/{id}/update")
    public ResponseEntity<User> updateUserToDriver(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (Objects.equals(user.getRole(), "Passenger")) {
            user.setRole("Driver");
            User updatedUser = userService.editProfile(id, user);
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.badRequest().build();
    }


    @Operation(summary = "Given User ID, Fetch User's Profile Picture.",
            description = "Search Repository for an Entry's Profile Picture URL of Given User ID, Returns " +
                    "[200 + URL] if Successful," +
                    "[500] if No Such User Found in Repository.")
    @GetMapping("/profilePic/{id}")
    public ResponseEntity<String> getProfilePicById(@PathVariable Long id) {
        String ProfilePicture = userService.getPictureById(id);
        return ResponseEntity.ok(ProfilePicture);
    }


    @Operation(summary = "Given User ID and New User Content, Update User Entry in Repository.",
            description = "Update Repository an Entry of Given User ID, Returns " +
                    "[200 + Entry(After Change)] if Successful, " +
                    "[404] if No Such User Found.")
    @PutMapping("/editProfile/{id}")
    public ResponseEntity<User> editProfile(@PathVariable Long id, @RequestBody User userDetails) {
        // Delegate the logic to UserService
        User updatedUser = userService.editProfile(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();  // Return 404 if the user is not found
        }
    }


    // Following Section of Code are Additional Methods for Reviews(Receiver Side)


    /**
     * Reviewer : Anthony Campana
     * Retrieves the average rating for a user based on reviews they received.
     * @param id
     * @return Rating for specific user (id)
     */
    @Operation(summary = "Given User ID, Fetch User's Rating Among All Reviews Received.",
            description = "Search Review Repository for List of Entries of Given User ID, " +
                    "Calculate Ratings, Returns " +
                    "[200 + Rating]." +
                    "*Rating is Set to 0.0 if No Reviews found for the Receiver.")
    @GetMapping("/{id}/review")
    public ResponseEntity<Double> getRatingById(@PathVariable Long id) {
        double rating = reviewService.getReceiverRating(id);
        return ResponseEntity.ok(rating);
    }


    /**
     * Reviewer : Anthony Campana
     * Retrieves the reviews that were all sent to the specific user ID.
     * @param id
     * @return List of reviews sent to specific user (id)
     */
    @Operation(summary = "Given User ID, List of All Reviews Received by the USer.",
            description = "Search Review Repository for List of Entries of Given User ID, Returns " +
                    "[200 + List].")
    @GetMapping("/{id}/review/content")
    public ResponseEntity<List<Review>> getReviewReceivedById(@PathVariable Long id) {
        List<Review> reviews = reviewService.getReviewByReceiverId(id);
        return ResponseEntity.ok(reviews);
    }


    @Operation(summary = "Check if a User's Account is Verified.",
            description = "Given a User ID, check if the account is verified. Returns " +
                    "[200 + JSON with userId and verified] if Successful," +
                    "[404] if No Such User Found.")
    @GetMapping("/{id}/isVerified")
    public ResponseEntity<Map<String, Object>> isAccountVerified(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build(); // Return 404 if the user is not found
        }


        // Return a JSON response like:
        // {
        //   "verified": "true"
        //   "userId": 207,
        // }
        Map<String, Object> response = Map.of(
                "userId", id,
                "verified", String.valueOf(user.isVerified())
        );


        return ResponseEntity.ok(response);
    }

}
