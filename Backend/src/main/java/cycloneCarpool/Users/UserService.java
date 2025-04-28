package cycloneCarpool.Users;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


// CRUD Methods of User, and Sign-In Authentication.
@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public List<String> getAllEmails(){
        return userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toList());
    }


    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null); // Return null if user not found
    }


    // Method to delete a user by ID
    public boolean deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        } else {
            return false;  // User not found
        }
    }


    public String getPictureById(Long id){
        Optional<User> user = userRepository.findById(id);
        if(user.isPresent()){
            return user.get().getProfilePicture();
        } else {
            throw new IllegalStateException("User not found");
        }
    }


    public User registerUser(User user) {
        // Check if a user with the same email already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            // Handle the case where the user already exists
            throw new IllegalStateException("Email already registered");
        }


        // If no user exists, save the new user
        return userRepository.save(user);
    }


    public User editProfile(long id, User userDetails) {
        // Find the user by ID
        Optional<User> optionalUser = userRepository.findById(id);


        if (optionalUser.isPresent()) {
            // Retrieve the existing user
            User existingUser = optionalUser.get();


            // Update the user's details
            existingUser.setFirstname(userDetails.getFirstname());
            existingUser.setLastname(userDetails.getLastname());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setProfilePicture(userDetails.getProfilePicture());
            existingUser.setPassword(userDetails.getPassword());
            existingUser.setRole(userDetails.getRole());


            // Save the updated user back to the database
            return userRepository.save(existingUser);
        } else {
            // Return null if the user is not found
            return null;
        }
    }


    public User validateUser(String email, String password) throws AuthenticationException {
        Optional<User> validatingUser = userRepository.findByEmail(email);
        if (validatingUser.isPresent()) {
            User existingUser = validatingUser.get();
            if (password.equals(existingUser.getPassword())) {
                return existingUser;
            }
        }
        throw new AuthenticationException("Invalid email or password");
    }




}
