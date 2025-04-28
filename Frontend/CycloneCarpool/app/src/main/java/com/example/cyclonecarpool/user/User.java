package com.example.cyclonecarpool.user;

public class User {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String profilePicture;
    private String role;

    private double rating = 0.0;

    public User(Long id, String firstname, String lastname, String email, String password, String role, String profilePicture) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.profilePicture = profilePicture;
        this.role = role;
    }

    public User() {}

    public Long getId() { return id; }
    public Integer getIntId() { return id.intValue(); }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public String getFullName() { return firstname + " " + lastname; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getProfilePicture() { return profilePicture; }
    public String getRole() { return role; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

}
