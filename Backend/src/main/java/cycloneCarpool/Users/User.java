package cycloneCarpool.Users;

import cycloneCarpool.Trips.Trip;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Anthony Campana
 */

@Schema(description = "Entry of a User")
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String profilePicture;
    private String role;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false; // Default to false

    @ManyToMany(mappedBy = "passengers")
    private List<Trip> trips = new ArrayList<>();

    public User(String firstname, String lastname, String email, String password, String role, String profilePicture) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePicture = profilePicture;
    }

    public User() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {return role; }

    public void setRole(String role) {this.role = role; }

    public String getProfilePicture() { return profilePicture;}

    public void setProfilePicture(String profilePicture) {this.profilePicture = profilePicture; }

    public Boolean isVerified() { return isVerified; }

    public void setVerified(Boolean isVerified) { this.isVerified = isVerified; }

}
