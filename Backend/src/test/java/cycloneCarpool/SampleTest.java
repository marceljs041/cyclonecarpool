package cycloneCarpool;

import static org.hamcrest.Matchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;	// SBv3


// A sample test case to see that it is communicating with database, prints tripId=1 in console
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SampleTest {

    @LocalServerPort
    int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void tempTest() {
        long firstTripId = 1;

        // Make a GET request to fetch the trip by ID
        Response response = RestAssured.given()
                .when()
                .get("/api/trips/" + firstTripId) // Adjust URL path as per your controller
                .then()
                .statusCode(200) // Expect HTTP 200 OK
                .body("tripId", equalTo((int) firstTripId)) // Check if the tripId matches the expected value
                .extract().response();

        // Optionally print out the response body to verify
        System.out.println(response.getBody().asString());
    }


}
