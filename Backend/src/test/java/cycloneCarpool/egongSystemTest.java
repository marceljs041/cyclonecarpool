package cycloneCarpool;

import cycloneCarpool.Reviews.*;
import cycloneCarpool.Trips.*;
import cycloneCarpool.Users.*;
import cycloneCarpool.Vehicles.*;
import cycloneCarpool.LocationTracking.*;

import static org.junit.Assert.*;

import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;	// SBv3

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


// A sample test case to see that it is communicating with database, prints tripId=1 in console
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class egongSystemTest {

    @LocalServerPort
    int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void test1_tripFilters() {

        // tes 1.1 getFutureTrips(), as expected
        Response r = RestAssured.given()
                .get("/api/trips/home")
                .then()
                .statusCode(200)
                .extract().response();
        List<Trip> tList1 = Arrays.asList(r.getBody().as(Trip[].class));
        assertFalse(tList1.stream().anyMatch(t -> t.getTripId() == 1));

        // test 1.2 getFilteredTrips(specs), as expected
        TripDTO tDTO = new TripDTO();
        tDTO.setStartLocation("Ames");
        tDTO.setEndLocation("Cedar Rapids");
        r = RestAssured.given()
                .contentType("application/json")
                .body(tDTO)
                .post("/api/trips/home/filter")
                .then()
                .statusCode(200)
                .extract().response();
        Trip[] tList2 = r.getBody().as(Trip[].class);
        for (Trip t : tList2) {
            assertEquals("Ames", t.getStartLocation());
            assertEquals("Cedar Rapids", t.getEndLocation());
        }
        List<Long> tIdList = tList1.stream()
                .map(Trip::getTripId)
                .toList();
        for (Trip t : tList2) {
            assertTrue("Filtered trip is not present in unfiltered list",
                    tIdList.contains(t.getTripId()));
        }
    }
    @Test
    public void test2_tripCrud() {

        // test 2.1 createTrip(trip), as expected
        Trip t1 = new Trip();
        t1.setDriverId(1L);
        t1.setStartLocation("New York");
        t1.setEndLocation("Boston");
        t1.setPickUp("Downtown");
        t1.setTime(LocalDateTime.now().plusDays(1)); // A future trip
        t1.setSeat(4);
        t1.setPrice(50L);
        t1.setRoundTrip(true);
        t1.setNoSmoke(false);
        Response r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(t1)
                .post("/api/trips/home/create")
                .then()
                .statusCode(200)
                .extract().response();
        t1 = r.getBody().as(Trip.class);
        Long tId = t1.getTripId();
        assertNotNull(tId);
        assertEquals(1L, t1.getDriverId());
        assertEquals("New York", t1.getStartLocation());
        assertEquals("Boston", t1.getEndLocation());
        assertEquals("Downtown", t1.getPickUp());
        assertEquals(4, t1.getSeat());
        assertEquals(50L, t1.getPrice());
        assertTrue(t1.isRoundTrip());
        assertFalse(t1.isNoSmoke());

        // test 2.2 getTrip(id), as expected
        r = RestAssured.given()
                .get("/api/trips/" + tId)
                .then()
                .statusCode(200)
                .extract().response();
        Trip t2 = r.getBody().as(Trip.class);
        assertEquals((long)tId, t2.getTripId());
        assertEquals(t1.getStartLocation(), t2.getStartLocation());
        assertEquals(t1.getEndLocation(), t2.getEndLocation());
        assertEquals(t1.getPickUp(), t2.getPickUp());
        assertEquals(t1.getTime().truncatedTo(ChronoUnit.SECONDS), t2.getTime().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(t1.getSeat(), t2.getSeat());
        assertEquals(t1.getPrice(), t2.getPrice());
        assertEquals(t1.isRoundTrip(), t2.isRoundTrip());
        assertEquals(t1.isNoSmoke(), t2.isNoSmoke());

        // test 2.3 getTrip(id), not found
        RestAssured.given()
                .get("/api/trips/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 2.4 editTrip(id, content), as expected
        t1.setStartLocation("Los Angeles");
        t1.setSeat(2);
        r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(t1)
                .put("/api/trips/mytrip/" + tId)
                .then()
                .statusCode(200)
                .extract().response();
        t1 = r.getBody().as(Trip.class);
        assertEquals("Los Angeles", t1.getStartLocation());
        assertEquals(2, t1.getSeat());
        r = RestAssured.given()
                .get("/api/trips/" + tId)
                .then()
                .statusCode(200)
                .extract().response();
        t2 = r.getBody().as(Trip.class);
        assertEquals("Los Angeles", t2.getStartLocation());
        assertEquals(2, t2.getSeat());

        // test 2.4 editTrip(id, content), not found
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(t2)
                .put("/api/trips/mytrip/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 2.5 deleteTrip(id), as expected
        RestAssured.given()
                .delete("/api/trips/mytrip/" + tId)
                .then()
                .statusCode(204);

        // test 2.6 deleteTrip(id), not found
        RestAssured.given()
                .get("/api/trips/" + tId)
                .then()
                .statusCode(404);

    }
    @Test
    public void test3_tripCost() {

        // test 3.1 estimateCost(specs), as expected
        TripDTO tDTO = new TripDTO();
        tDTO.setUserId(33L);
        tDTO.setStartLocation("Austin");
        tDTO.setEndLocation("Eugene");
        Response r1 = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/home/create/estimate")
                .then()
                .statusCode(200)
                .extract().response();
        FuelEstimates e = r1.getBody().as(FuelEstimates.class);
        assertTrue(e.getDistanceEstimate() > 0);
        assertTrue(e.getFuelEstimate() > 0);
        assertTrue(e.getRegular() > 0);
        assertTrue(e.getPremium() > 0);
        assertTrue(e.getDiesel() > 0);

        // test 3.2 estimateCost(specs), no vehicle
        tDTO.setUserId(14L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/home/create/estimate")
                .then()
                .statusCode(500)
                .extract().response();

        // api literally responds to anywhere, it might not get the locations as expected but it won't 500
    }

    @Test
    public void test4_users() {

        // test 4.1 signup(specs), as expected
        User u = new User();
        u.setFirstname("Justin");
        u.setLastname("Case");
        u.setEmail("typical_test@case.com");
        u.setPassword("passwerd");
        u.setProfilePicture("http://example.com/profile.jpg");
        u.setRole("Passenger");
        u.setVerified(false);
        Response r = RestAssured.given()
                .contentType("application/json")
                .body(u)
                .post("/api/users/signup")
                .then()
                .statusCode(201)
                .extract().response();
        u = r.getBody().as(User.class);
        long uId = u.getId();
        assertEquals("Justin", u.getFirstname());
        assertEquals("Case", u.getLastname());
        assertEquals("typical_test@case.com", u.getEmail());
        assertEquals("passwerd", u.getPassword());
        assertEquals("Passenger", u.getRole());

        // test 4.2 signup(specs), duplicate email
        RestAssured.given()
                .contentType("application/json")
                .body(u)
                .post("/api/users/signup")
                .then()
                .statusCode(500)
                .extract().response();

        // test 4.3 signin(specs), as expected
        SignInRequest s = new SignInRequest();
        s.setEmail("typical_test@case.com");
        s.setPassword("passwerd");
        r = RestAssured.given()
                .contentType("application/json")
                .body(s)
                .post("/api/users/signin")
                .then()
                .statusCode(200)
                .extract().response();
        u = r.getBody().as(User.class);
        assertEquals("Justin", u.getFirstname());
        assertEquals("Case", u.getLastname());
        assertEquals("typical_test@case.com", u.getEmail());
        assertEquals("passwerd", u.getPassword());
        assertEquals("Passenger", u.getRole());

        // test 4.3 signin(specs), wrong password
        s.setPassword("password");
        r = RestAssured.given()
                .contentType("application/json")
                .body(s)
                .post("/api/users/signin")
                .then()
                .statusCode(401)
                .extract().response();

        // test 4.4 getUserById(id), as expected
        r = RestAssured.given()
                .get("/api/users/" + uId)
                .then()
                .statusCode(200)
                .extract().response();
        u = r.getBody().as(User.class);
        assertEquals("Justin", u.getFirstname());
        assertEquals("Case", u.getLastname());
        assertEquals("typical_test@case.com", u.getEmail());
        assertEquals("passwerd", u.getPassword());
        assertEquals("Passenger", u.getRole());

        // test 4.5 updateUserToDriver(id), as expected
        r = RestAssured.given()
                .get("/api/users/"+uId+"/update")
                .then()
                .statusCode(200)
                .extract().response();
        u = r.getBody().as(User.class);
        assertEquals("Justin", u.getFirstname());
        assertEquals("Case", u.getLastname());
        assertEquals("typical_test@case.com", u.getEmail());
        assertEquals("passwerd", u.getPassword());
        assertEquals("Driver", u.getRole());

        // test 4.6 updateUserToDriver(id), bad role
        RestAssured.given()
                .get("/api/users/"+uId+"/update")
                .then()
                .statusCode(400)
                .extract().response();

        // test 4.7 editProfile(id, specs), as expected
        u.setProfilePicture("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        r = RestAssured.given()
                .contentType("application/json")
                .body(u)
                .put("/api/users/editProfile/" + uId)
                .then()
                .statusCode(200)
                .extract().response();
        u = r.getBody().as(User.class);
        assertEquals("Justin", u.getFirstname());
        assertEquals("Case", u.getLastname());
        assertEquals("typical_test@case.com", u.getEmail());
        assertEquals("passwerd", u.getPassword());
        assertEquals("Driver", u.getRole());
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", u.getProfilePicture());

        // test 4.8 editProfile(id, specs), no user
        RestAssured.given()
                .contentType("application/json")
                .body(u)
                .put("/api/users/editProfile/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 4.9 getProfilePicById(id), as expected
        r = RestAssured.given()
                .accept("text/plain")
                .get("/api/users/profilePic/"+uId)
                .then()
                .statusCode(200)
                .extract().response();
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", r.getBody().asString());

        // test 4.10 getProfilePicById(id), no user
        RestAssured.given()
                .accept("text/plain")
                .get("/api/users/profilePic/-1")
                .then()
                .statusCode(500)
                .extract().response();

        // test 4.11 deleteUserById(id), as expected
        RestAssured.given()
                .delete("/api/users/" + uId)
                .then()
                .statusCode(204);

        // test 4.12 deleteUserById(id), no user
        RestAssured.given()
                .delete("/api/users/" + uId)
                .then()
                .statusCode(404);
    }
    @Test
    public void test5_roles() {

        // test 5.1 checkTripEditPermission(tId, uId), as expected(Driver)
        Trip t = new Trip();
        t.setDriverId(14L);
        t.setStartLocation("Brisbane");
        t.setEndLocation("Cairns");
        t.setPickUp("Greenslopes Private Hospital");
        t.setTime(LocalDateTime.now().plusDays(69)); // A future trip
        t.setSeat(4);
        t.setPrice(50L);
        t.setRoundTrip(false);
        t.setNoSmoke(true);
        Response r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(t)
                .post("/api/trips/home/create")
                .then()
                .statusCode(200)
                .extract().response();
        t = r.getBody().as(Trip.class);
        Long tId = t.getTripId();
        TripDTO tDTO = new TripDTO();
        tDTO.setUserId(14L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/edit")
                .then()
                .statusCode(200)
                .extract().response();

        // test 5.2 checkTripEditPermission(tId, uId), as expected(Admin)
        tDTO.setUserId(33L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/edit")
                .then()
                .statusCode(200)
                .extract().response();

        // test 5.3 checkTripEditPermission(tId, uId), no trip
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/-1/edit")
                .then()
                .statusCode(403)
                .extract().response();

        // test 5.4 checkTripEditPermission(tId, uId), no user
        tDTO.setUserId(-1L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/edit")
                .then()
                .statusCode(403)
                .extract().response();

        // test 5.5 checkTripEditPermission(tId, uId), bad role(Driver)
        tDTO.setUserId(8L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/edit")
                .then()
                .statusCode(403)
                .extract().response();

        // test 5.6 checkTripEditPermission(tId, uId), bad role(Passenger)
        tDTO.setUserId(9L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/edit")
                .then()
                .statusCode(403)
                .extract().response();

        // test 5.7 joinTrip(tId, uId), as expected
        r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/join")
                .then()
                .statusCode(200)
                .extract().response();
        t = r.getBody().as(Trip.class);
        assertEquals((long)tId, t.getTripId());
        assertTrue(t.getPassengers().stream().anyMatch(user -> user.getId() == 9L));

        // test 5.8 joinTrip(tId, uId), no trip
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/-1/join")
                .then()
                .statusCode(404)
                .extract().response();

        // test 5.9 joinTrip(tId, uId), no user
        tDTO.setUserId(-1L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/join")
                .then()
                .statusCode(404)
                .extract().response();

        // test 5.10 hasPassenger(tId, uId), as expected
        tDTO.setUserId(9L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/myride/" + tId)
                .then()
                .statusCode(200)
                .extract().response();

        // test 5.11 hasPassenger(tId, uId), not passenger
        tDTO.setUserId(14L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/myride/" + tId)
                .then()
                .statusCode(404)
                .extract().response();

        // test 5.12 hasPassenger(tId, uId), no trip
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/myride/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 5.13 leaveTrip(tId, uId), as expected
        tDTO.setUserId(9L);
        r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/leave")
                .then()
                .statusCode(200)
                .extract().response();
        t = r.getBody().as(Trip.class);
        assertEquals((long)tId, t.getTripId());
        assertFalse(t.getPassengers().stream().anyMatch(user -> user.getId() == 9L));

        // test 5.14 leaveTrip(tId, uId), no user
        tDTO.setUserId(-1L);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/" + tId + "/leave")
                .then()
                .statusCode(404)
                .extract().response();

        // test 5.15 leaveTrip(tId, uId), no trip
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tDTO)
                .post("/api/trips/-1/leave")
                .then()
                .statusCode(404)
                .extract().response();
        RestAssured.given()
                .delete("/api/trips/mytrip/" + tId)
                .then()
                .statusCode(204);
    }

    @Test
    public void test6_review() {

        // test 6.1 createReview(specs), as expected
        RestAssured.defaultParser = Parser.JSON;
        long uId1 = 9L;
        long uId2 = 14L;
        long uId3 = 33L;
        long uId4 = 37L;
        long tId = 1L;
        Response r = RestAssured.given()
                .get("/api/users/" + uId1)
                .then()
                .statusCode(200)
                .extract().response();
        User u1 = r.getBody().as(User.class);
        r = RestAssured.given()
                .get("/api/users/" + uId2)
                .then()
                .statusCode(200)
                .extract().response();
        User u2 = r.getBody().as(User.class);
        r = RestAssured.given()
                .get("/api/users/" + uId3)
                .then()
                .statusCode(200)
                .extract().response();
        User u3 = r.getBody().as(User.class);
        r = RestAssured.given()
                .get("/api/users/" + uId4)
                .then()
                .statusCode(200)
                .extract().response();
        User u4 = r.getBody().as(User.class);
        r = RestAssured.given()
                .get("/api/trips/" + tId)
                .then()
                .statusCode(200)
                .extract().response();
        Trip t1 = r.getBody().as(Trip.class);
        Review r1 = new Review();
        r1.setReviewer(u1);
        r1.setReceiver(u2);
        r1.setTrip(t1);
        r1.setContent("Supercalifragilisticexpialidocious!");
        r1.setRating(5);
        r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(r1)
                .post("/api/trips/review")
                .then()
                .statusCode(200)
                .extract().response();
        r1 = r.getBody().as(Review.class);
        long rId1 = r1.getReviewId();
        Review r2 = new Review();
        r2.setReviewer(u3);
        r2.setReceiver(u2);
        r2.setTrip(t1);
        r2.setContent("Erm, what the sigma?");
        r2.setRating(1);
        r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(r2)
                .post("/api/trips/review")
                .then()
                .statusCode(200)
                .extract().response();
        r2 = r.getBody().as(Review.class);
        long rId2 = r2.getReviewId();

        // test 6.2 createReview(specs), no relation
        Review r3 = new Review();
        r3.setReviewer(u4);
        r3.setReceiver(u2);
        r3.setTrip(t1);
        r3.setContent("I only remember that I did Karate with this guy 13 years ago.");
        r3.setRating(2);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(r3)
                .post("/api/trips/review")
                .then()
                .statusCode(403)
                .extract().response();

        // test 6.3 createReview(specs), no trip
        Review review4 = new Review();
        review4.setReviewer(u3);
        review4.setReceiver(u2);
        review4.setTrip(null);
        review4.setContent("Erm, what the sigma?");
        review4.setRating(1);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(review4)
                .post("/api/trips/review")
                .then()
                .statusCode(500)
                .extract().response();

        // test 6.4 getReviewReceivedById(id), as expected
        r = RestAssured.given()
                .get("/api/users/"+ uId2 +"/review/content")
                .then()
                .statusCode(200)
                .extract().response();
        List<Review> reviews = Arrays.asList(r.getBody().as(Review[].class));
        String actualReviewContent1 = reviews.get(0).getContent();
        assertEquals("Supercalifragilisticexpialidocious!", actualReviewContent1);
        String actualReviewContent2 = reviews.get(1).getContent();
        assertEquals("Erm, what the sigma?", actualReviewContent2);

        // test 6.5 updateReview(id, specs), as expected
        r2.setRating(5);
        r = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(r2)
                .put("/api/trips/review/"+ rId2)
                .then()
                .statusCode(200)
                .extract().response();
        r2 = r.getBody().as(Review.class);
        assertEquals(5, r2.getRating());

        // test 6.6 updateReview(id, specs), bad content
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(r3)
                .put("/api/trips/review/"+ rId2)
                .then()
                .statusCode(400)
                .extract().response();

        // test 6.7 updateReview(id, specs), no review
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(r2)
                .put("/api/trips/review/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 6.8 getRatingById(id), as expected
        r = RestAssured.given()
                .get("/api/users/14/review")
                .then()
                .statusCode(200)
                .extract().response();
        Double s = r.getBody().as(Double.class);
        assertEquals(5.0, s, 0.0001);

        // test 6.9 deleteReview(id), as expected
        RestAssured.given()
                .delete("/api/trips/review/" + rId1)
                .then()
                .statusCode(204);
        RestAssured.given()
                .delete("/api/trips/review/" + rId2)
                .then()
                .statusCode(204);

        // test 6.10 deleteReview(id), no review
        RestAssured.given()
                .delete("/api/trips/review/" + rId2)
                .then()
                .statusCode(404);
    }

    @Test
    public void test7_vehicles() {
        // For this Test to Pass, Make Sure User 14, 23, 24 has No Vehicle Associations.

        // test 7.1 createVehicle(specs), as expected
        Vehicle v = new Vehicle();
        v.setOwnerId(23);
        v.setLicensePlate("LMN4321");
        v.setState("NY");
        Response r = RestAssured.given()
                .contentType("application/json")
                .body(v)
                .post("/api/vehicles/create")
                .then()
                .statusCode(201)
                .extract().response();
        v = r.getBody().as(Vehicle.class);
        long vId = v.getVehicleId();
        assertEquals("LMN4321", v.getLicensePlate());
        assertEquals("NY", v.getState());
        assertEquals(23, v.getOwnerId());
        assertEquals("Honda", v.getMake());
        assertEquals("Civic", v.getModel());
        assertEquals("2012", v.getYear());
        assertEquals("White", v.getColor());
        assertTrue(v.getMpg() > 0);

        // test 7.2 createVehicle(specs), multiple cars
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .post("/api/vehicles/create")
                .then()
                .statusCode(403)
                .extract().response();

        // test 7.3 createVehicle(specs), no user
        v.setOwnerId(-1);
        v.setLicensePlate("RST6543");
        v.setState("NV");
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .post("/api/vehicles/create")
                .then()
                .statusCode(404)
                .extract().response();

        // test 7.4 createVehicle(specs), no license plate info
        v.setOwnerId(24);
        v.setLicensePlate("RST6543");
        v.setState("HI");
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .post("/api/vehicles/create")
                .then()
                .statusCode(404)
                .extract().response();

        // test 7.5 createVehicle(specs), phantom model
        v.setOwnerId(14);
        v.setLicensePlate("MOONCAR");
        v.setState("IA");
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .post("/api/vehicles/create")
                .then()
                .statusCode(500)
                .extract().response();

        // test 7.6 getAllVehicles(), as expected
        v = new Vehicle();
        v.setOwnerId(14);
        v.setLicensePlate("MNO2345");
        v.setState("OR");
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .post("/api/vehicles/create")
                .then()
                .statusCode(201)
                .extract().response();
        r = RestAssured.given()
                .get("/api/vehicles/all")
                .then()
                .statusCode(200)
                .extract().response();
        List<Vehicle> vList = Arrays.asList(r.getBody().as(Vehicle[].class));
        assertTrue(vList.stream().anyMatch(vehicle ->
                "LMN4321".equals(vehicle.getLicensePlate()) &&
                        "NY".equals(vehicle.getState()) &&
                        "Honda".equals(vehicle.getMake()) &&
                        "Civic".equals(vehicle.getModel()) &&
                        "2012".equals(vehicle.getYear()) &&
                        "White".equals(vehicle.getColor()) &&
                        vehicle.getOwnerId() == 23));
        assertTrue(vList.stream().anyMatch(vehicle ->
                "MNO2345".equals(vehicle.getLicensePlate()) &&
                        "OR".equals(vehicle.getState()) &&
                        "Subaru".equals(vehicle.getMake()) &&
                        "Outback".equals(vehicle.getModel()) &&
                        "2014".equals(vehicle.getYear()) &&
                        "Brown".equals(vehicle.getColor()) &&
                        vehicle.getOwnerId() == 14));

        // test 7.7 getVehicle(id), as expected
        r = RestAssured.given()
                .get("/api/vehicles/"+vId)
                .then()
                .statusCode(200)
                .extract().response();
        v = r.getBody().as(Vehicle.class);
        assertEquals("LMN4321", v.getLicensePlate());
        assertEquals("NY", v.getState());
        assertEquals(23, v.getOwnerId());
        assertEquals("Honda", v.getMake());
        assertEquals("Civic", v.getModel());
        assertEquals("2012", v.getYear());
        assertEquals("White", v.getColor());
        assertTrue(v.getMpg() > 0);

        // test 7.8 getVehicle(id), no vehicle
        RestAssured.given()
                .get("/api/vehicles/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 7.9 getVehiclesByOwner(id), as expected
        r = RestAssured.given()
                .get("/api/vehicles/owner/14")
                .then()
                .statusCode(200)
                .extract().response();
        v = r.getBody().as(Vehicle.class);
        assertEquals("MNO2345", v.getLicensePlate());
        assertEquals("OR", v.getState());
        assertEquals("Subaru", v.getMake());
        assertEquals("Outback", v.getModel());
        assertEquals("2014", v.getYear());
        assertEquals("Brown", v.getColor());
        assertTrue(v.getMpg() > 0);

        // test 7.10 getVehiclesByOwner(id), no user
        RestAssured.given()
                .get("/api/vehicles/owner/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 7.11 checkVehicleEditPermission(vId, uId), as expected
        Vehicle u = new Vehicle();
        u.setOwnerId(23);
        RestAssured.given()
                .contentType("application/json")
                .body(u)
                .post("/api/vehicles/"+vId+"/edit")
                .then()
                .statusCode(200)
                .extract().response();

        // test 7.11 checkVehicleEditPermission(vId, uId), not owner
        u.setOwnerId(14);
        Response responseCheckBad = RestAssured.given()
                .contentType("application/json")
                .body(u)
                .post("/api/vehicles/"+vId+"/edit")
                .then()
                .statusCode(403)
                .extract().response();

        // test 7.12 editVehicle(vId, specs), as expected
        v.setLicensePlate("YZA7890");
        v.setState("OH");
        r = RestAssured.given()
                .contentType("application/json")
                .body(v)
                .put("/api/vehicles/"+vId+"/edit")
                .then()
                .statusCode(200)
                .extract().response();
        v = r.getBody().as(Vehicle.class);
        assertEquals("YZA7890", v.getLicensePlate());
        assertEquals("OH", v.getState());
        assertEquals(23, v.getOwnerId());
        assertEquals("Chrysler", v.getMake());
        assertEquals("Pacifica", v.getModel());
        assertEquals("2018", v.getYear());
        assertEquals("Red", v.getColor());
        assertTrue(v.getMpg() > 0);

        // test 7.12 editVehicle(vId, specs), no vehicle
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .put("/api/vehicles/-1/edit")
                .then()
                .statusCode(404)
                .extract().response();

        // test 7.13 editVehicle(vId, specs), no license plate info
        v.setLicensePlate("RST6544");
        v.setState("NV");
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .put("/api/vehicles/"+vId+"/edit")
                .then()
                .statusCode(404)
                .extract().response();

        // test 7.14 editVehicle(vId, specs), phantom model
        v.setLicensePlate("MOONCAR");
        v.setState("IA");
        RestAssured.given()
                .contentType("application/json")
                .body(v)
                .put("/api/vehicles/"+vId+"/edit")
                .then()
                .statusCode(500)
                .extract().response();

        // test 7.15 deleteVehicle(id), as expected
        RestAssured.given()
                .delete("/api/vehicles/" + vId)
                .then()
                .statusCode(204);

        // test 7.16 deleteVehicle(id), no vehicle
        RestAssured.given()
                .delete("/api/vehicles/" + vId)
                .then()
                .statusCode(404);

        // test 7.17 deleteVehicleByOwner(id), as expected
        RestAssured.given()
                .delete("/api/vehicles/owner/14")
                .then()
                .statusCode(204);

        // test 7.17 deleteVehicleByOwner(id), no vehicle
        RestAssured.given()
                .delete("/api/vehicles/owner/14")
                .then()
                .statusCode(404);
    }

    @Test
    public void test8_pts() {

        // test 8.1 createPassengerTripStatus(tId, uId), as expected
        RestAssured.given()
                .post("/passenger-trip-status/status/57/34")
                .then()
                .statusCode(200)
                .extract().response();

        // test 8.2 createPassengerTripStatus(tId, uId), duplicate
        RestAssured.given()
                .post("/passenger-trip-status/status/57/34")
                .then()
                .statusCode(400)
                .extract().response();

        // test 8.3 createPassengerTripStatus(tId, uId), no trip
        RestAssured.given()
                .post("/passenger-trip-status/status/-1/9")
                .then()
                .statusCode(404)
                .extract().response();

        // test 8.4 getPassengerTripStatus(tId, uId), as expected
        Response r = RestAssured.given()
                .get("/passenger-trip-status/status/57/34")
                .then()
                .statusCode(200)
                .extract().response();
        PassengerTripStatus pts = r.getBody().as(PassengerTripStatus.class);
        assertEquals(57, pts.getTrip().getTripId());
        assertEquals(Long.valueOf(34), pts.getPassenger().getId());
        assertEquals("PENDING", pts.getStatus());
        assertFalse(pts.isPaid());

        // test 8.5 getPassengerTripStatus(tId, uId), no trip
        RestAssured.given()
                .get("/passenger-trip-status/status/-1/9")
                .then()
                .statusCode(404)
                .extract().response();

        // test 8.6 getPTSId(tId, uId), as expected
        r = RestAssured.given()
                .get("/passenger-trip-status/status/id/57/34")
                .then()
                .statusCode(200)
                .extract().response();
        long ptsId = Long.parseLong(r.getBody().asString());

        // test 8.7 updatePassengerStatus(tId, uId, s), as expected
        RestAssured.given()
                .queryParam("tripId", 57)
                .queryParam("passengerId", 34)
                .queryParam("status", "REJECTED")
                .post("/passenger-trip-status/update-status")
                .then()
                .statusCode(200)
                .extract().response();

        // test 8.8 updatePassengerStatus(tId, uId, s), bad state
        RestAssured.given()
                .queryParam("tripId", 57)
                .queryParam("passengerId", 34)
                .queryParam("status", "CONSTANT STATE OF CHAOS")
                .post("/passenger-trip-status/update-status")
                .then()
                .statusCode(400)
                .extract().response();

        // test 8.8 updatePassengerStatus(tId, uId, s), no trip
        RestAssured.given()
                .queryParam("tripId", -1)
                .queryParam("passengerId", 34)
                .queryParam("status", "APPROVED")
                .post("/passenger-trip-status/update-status")
                .then()
                .statusCode(400)
                .extract().response();

        // test 8.9  updatePaymentStatus(tId, uId, p), as expected
        RestAssured.given()
                .queryParam("tripId", 57)
                .queryParam("passengerId", 34)
                .queryParam("paid", "TRUE")
                .post("/passenger-trip-status/update-payment")
                .then()
                .statusCode(200)
                .extract().response();
        r = RestAssured.given()
                .get("/passenger-trip-status/status/57/34")
                .then()
                .statusCode(200)
                .extract().response();
        pts = r.getBody().as(PassengerTripStatus.class);
        assertEquals(57, pts.getTrip().getTripId());
        assertEquals(Long.valueOf(34), pts.getPassenger().getId());
        assertEquals("REJECTED", pts.getStatus());
        assertTrue(pts.isPaid());

        // test 8.10 getPaymentStatus(tId, uId), as expected
        RestAssured.given()
                .get("/passenger-trip-status/paymentStatus/57/34")
                .then()
                .statusCode(200)
                .extract().response();

        // test 8.11 getPaymentStatus(tId, uId), no user
        RestAssured.given()
                .get("/passenger-trip-status/paymentStatus/57/-1")
                .then()
                .statusCode(400)
                .extract().response();

        // test 8.12 deletePassengerStatus(ptsId), as expected
        RestAssured.given()
                .delete("/passenger-trip-status/status/" + ptsId)
                .then()
                .statusCode(204);

        // test 8.13 deletePassengerStatus(ptsId), no pts
        RestAssured.given()
                .delete("/passenger-trip-status/status/" + ptsId)
                .then()
                .statusCode(404);
    }

    @Test
    public void test9_activeTrip() {

        // test 9.1 startTrip(specs), as expected
        ActiveTrip a = new ActiveTrip();
        a.setDriverId("14");
        a.setPassengerIds(List.of("162", "164", "166"));
        a.setStartLocation("Perth");
        a.setEndLocation("Sydney");

        Response r = RestAssured.given()
                .contentType("application/json")
                .queryParam("driverId", a.getDriverId())
                .queryParam("passengerIds", a.getPassengerIds())
                .queryParam("startLocation", a.getStartLocation())
                .queryParam("endLocation", a.getEndLocation())
                .post("/activeTrips/start")
                .then()
                .statusCode(200)
                .extract().response();
        a = r.getBody().as(ActiveTrip.class);
        long aId = a.getId();
        assertEquals("14", a.getDriverId());
        assertEquals("162", a.getPassengerIds().get(0));
        assertEquals("164", a.getPassengerIds().get(1));
        assertEquals("166", a.getPassengerIds().get(2));
        assertEquals("Perth", a.getStartLocation());
        assertEquals("Sydney", a.getEndLocation());

        // test 9.2 getTrip(id), as expected
        r = RestAssured.given()
                .get("/activeTrips/" + aId)
                .then()
                .statusCode(200)
                .extract().response();
        a = r.getBody().as(ActiveTrip.class);
        assertEquals("14", a.getDriverId());
        assertEquals("162", a.getPassengerIds().get(0));
        assertEquals("164", a.getPassengerIds().get(1));
        assertEquals("166", a.getPassengerIds().get(2));
        assertEquals("Perth", a.getStartLocation());
        assertEquals("Sydney", a.getEndLocation());

        // test 9.3 getTrip(id), bad id
        RestAssured.given()
                .get("/activeTrips/-1")
                .then()
                .statusCode(404)
                .extract().response();

        // test 9.4 updateTrip(id, spec), as expected
        a.setEndLocation("Melbourne");
        r = RestAssured.given()
                .contentType("application/json")
                .queryParam("newEndLocation", a.getEndLocation())
                .put("/activeTrips/update/"  + aId)
                .then()
                .statusCode(200)
                .extract().response();
        a = r.getBody().as(ActiveTrip.class);
        assertEquals("Melbourne", a.getEndLocation());

        // test 9.5 updateTrip(id, spec), bad id
        RestAssured.given()
                .contentType("application/json")
                .queryParam("newEndLocation", a.getEndLocation())
                .put("/activeTrips/update/-1")
                .then()
                .statusCode(500)
                .extract().response();

        // test 9.6 getAllTrips(), as expected
        r = RestAssured.given()
                .get("/activeTrips/all")
                .then()
                .statusCode(200)
                .extract().response();
        List<ActiveTrip> aList = Arrays.asList(r.getBody().as(ActiveTrip[].class));
        assertFalse(aList.isEmpty());
        assertTrue(aList.stream().anyMatch(activeTrip ->
                "14".equals(activeTrip.getDriverId()) &&
                        List.of("162", "164", "166").equals(activeTrip.getPassengerIds()) &&
                        "Perth".equals(activeTrip.getStartLocation()) &&
                        "Melbourne".equals(activeTrip.getEndLocation())));

        // test 9.7 endTrip(id), as expected
        RestAssured.given()
                .delete("/activeTrips/end/" + aId)
                .then()
                .statusCode(204);
    }

    @Test
    public void test10_location() {

        // test 10.1 getRoute(specs), as expected
        Response r = RestAssured.given()
                .queryParam("startCoordinates", "37.7749,-122.4194")
                .queryParam("endCoordinates", "34.0522,-118.2437")
                .get("getRoute")
                .then()
                .statusCode(200)
                .extract().response();

        RouteResponseDTO rDTO = r.getBody().as(RouteResponseDTO.class);
        assertTrue(rDTO.getDuration() > 0);
        assertTrue(rDTO.getDistance() > 0);

        // test 10.2 getRoute(specs), bad param
        RestAssured.given()
                .queryParam("never gonna give", "you up")
                .queryParam("never gonna let", "you down")
                .get("getRoute")
                .then()
                .statusCode(400)
                .extract().response();

        // test 10.3 getCurrentLocation(id), as expected
        r = RestAssured.given()
                .queryParam("tripId", "32")
                .get("getCurrentLocation")
                .then()
                .statusCode(200)
                .extract().response();

        DriverLocation d = r.getBody().as(DriverLocation.class);
        assertTrue(d.getLatitude() >= -90.0 && d.getLatitude() <= 90.0);
        assertTrue(d.getLongitude() >= -180.0 && d.getLongitude() <= 180.0);

        // test 10.4 getCurrentLocation(id), no trip
        RestAssured.given()
                .queryParam("tripId", "-1")
                .get("getCurrentLocation")
                .then()
                .statusCode(404)
                .extract().response();

        // test 10.5 getCoordinates(name), as expected
        r = RestAssured.given()
                .queryParam("placeName", "Boulder Tap House")
                .get("getCoordinates")
                .then()
                .statusCode(200)
                .extract().response();

        double[] c = r.getBody().as(double[].class);
        assertEquals(2, c.length);
        assertTrue(c[0]>= -90.0 && c[0] <= 90.0);
        assertTrue(c[1]>= -180.0 && c[1] <= 180.0);

    }

}
