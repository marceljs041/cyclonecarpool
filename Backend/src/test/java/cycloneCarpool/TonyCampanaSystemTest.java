package cycloneCarpool;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;


import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;


import io.restassured.RestAssured;
import io.restassured.response.Response;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;


import cycloneCarpool.Payments.PaymentService;
/**
 * System Tests for User Signup and Payments.
 * Tony Campana
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class TonyCampanaSystemTest {


    @LocalServerPort
    int port;


    @Autowired
    PaymentService paymentService;


    /**
     * Setup method to configure RestAssured before each test.
     */
    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }


    /**
     * Test Case 1: Register a new user successfully.
     */
    @Test
    public void registerNewUserTest() {
        String uniqueEmail = "test.user+" + System.currentTimeMillis() + "@example.com"; // Unique email
        // Create JSON payload for signup
        JSONObject signupPayload = new JSONObject();
        try {
            signupPayload.put("firstname", "Jane");
            signupPayload.put("lastname", "Doe");
            signupPayload.put("email", uniqueEmail);
            signupPayload.put("password", "SecurePass123!");
            signupPayload.put("profilePicture", "http://example.com/profile.jpg");
            signupPayload.put("role", "Passenger");
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to create signup payload: " + e.getMessage());
        }


        // Send POST request to /api/users/signup
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(signupPayload.toString())
                .when()
                .post("/api/users/signup");


        // Assert status code 201 Created
        assertEquals("Expected status code 201", 201, response.getStatusCode());


        // Assert response body contains the correct user details
        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertEquals("Firstname should match", "Jane", responseJson.getString("firstname"));
            assertEquals("Lastname should match", "Doe", responseJson.getString("lastname"));
            assertEquals("Email should match", uniqueEmail, responseJson.getString("email"));
            assertEquals("ProfilePicture should match", "http://example.com/profile.jpg", responseJson.getString("profilePicture"));
            assertEquals("Role should match", "Passenger", responseJson.getString("role"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }


    /**
     * Test Case 3: Sign in with correct credentials.
     */
    @Test
    public void signInWithValidCredentialsTest() {
        // Create JSON payload for sign in
        JSONObject signInPayload = new JSONObject();
        try {
            signInPayload.put("email", "alice.smith@example.com");
            signInPayload.put("password", "AlicePass789!");
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to create sign-in payload: " + e.getMessage());
        }


        // Send POST request to /api/users/signin
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(signInPayload.toString())
                .when()
                .post("/api/users/signin");


        // Assert status code 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());


        // Assert response body contains a token and correct email
        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertEquals("Email should match", "alice.smith@example.com", responseJson.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }


    /**
     * Test Case 4: Sign in with incorrect credentials (should fail).
     */
    @Test
    public void signInWithInvalidCredentialsTest() {
        // Create JSON payload for sign in with incorrect password
        JSONObject signInPayload = new JSONObject();
        try {
            signInPayload.put("email", "nonexistent@example.com");
            signInPayload.put("password", "WrongPass!");
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to create sign-in payload: " + e.getMessage());
        }


        // Send POST request to /api/users/signin
        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(signInPayload.toString())
                .when()
                .post("/api/users/signin")
                .then()
                .statusCode(401);
    }


    /**
     * Test Case 5: Initiate a payment request successfully.
     */


    @Test
    public void initiatePaymentTest() {
        // Create JSON payload to initiate payment
        JSONObject paymentPayload = new JSONObject();
        try {
            paymentPayload.put("amount", 50.0);
            paymentPayload.put("tripId", 1);
            paymentPayload.put("passengerId", 9);
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to create payment payload: " + e.getMessage());
        }


        // Send POST request to /payments/initiate
        Response response = RestAssured.given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("amount", 50.0)
                .formParam("tripId", 1)
                .formParam("passengerId", 9)
                .when()
                .post("/payments/initiate");


        // Assert status code 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());


        // Assert response body contains payment details
        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertEquals("Amount should match", 50.0, responseJson.getDouble("amount"), 0.001);
            assertEquals("Trip ID should match", 1, responseJson.getInt("tripId"));
            assertEquals("Passenger ID should match", 9, responseJson.getInt("passengerId"));
            assertEquals("Payment status should be PENDING", "PENDING", responseJson.getString("status"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }


    /**
     * Test Case 6: Process a payment successfully.
     */


    @Test
    public void processPaymentTest() {
        // Send POST request to /payments/process
        Response response = RestAssured.given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("passengerId", 9)
                .formParam("tripId", 1)
                .formParam("amount", 50.0) // Match actual amount
                .formParam("paymentMethod", "pm_card_visa")
                .when()
                .post("/payments/process");


        // Assert status code 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());


        // Assert response body indicates success
        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertEquals("Payment message should match", "Payment processed successfully.", responseJson.getString("message"));


            JSONObject paymentDetails = responseJson.getJSONObject("paymentDetails");
            assertEquals("Payment status should be COMPLETED", "COMPLETED", paymentDetails.getString("status"));
            assertEquals("Amount should match", 50.0, paymentDetails.getDouble("amount"), 0.001);
            assertNotNull("Receipt URL should not be null", paymentDetails.getString("receiptUrl"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }


    /**
     * Test Case 7: Mark a payment as failed.
     */
    @Test
    public void failPaymentTest() {
        // Assume paymentId 2 exists and is in INITIATED status


        // Send POST request to /payments/fail/{paymentId}
        long paymentIdToFail = 2;


        Response response = RestAssured.given()
                .when()
                .post("/payments/fail/" + paymentIdToFail)
                .then()
                .statusCode(200) // Assuming 200 OK
                .body("status", equalTo("FAILED"))
                .extract().response();


        // Optionally, assert additional fields if necessary
        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertEquals("Payment status should be FAILED", "FAILED", responseJson.getString("status"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }


    /**
     * Test Case 8: Complete a payment successfully.
     */
    @Test
    public void completePaymentTest() {
        // Assume paymentId 3 exists and is in INITIATED status


        // Send POST request to /payments/complete/{paymentId}
        long paymentIdToComplete = 3;


        Response response = RestAssured.given()
                .when()
                .post("/payments/complete/" + paymentIdToComplete)
                .then()
                .statusCode(200) // Assuming 200 OK
                .body("status", equalTo("COMPLETED"))
                .extract().response();


        // Optionally, assert additional fields if necessary
        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertEquals("Payment status should be COMPLETED", "COMPLETED", responseJson.getString("status"));
        } catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }


    /**
     * Test Case: Process a payment with an invalid method (should fail).
     */
    @Test
    public void processPaymentWithInvalidMethodTest() {
        Response response = RestAssured.given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("passengerId", 9)
                .formParam("tripId", 1)
                .formParam("amount", 50.0)
                .formParam("paymentMethod", "invalid_method")
                .when()
                .post("/payments/process");


        assertEquals("Expected status code 400", 400, response.getStatusCode());


        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            String message = responseJson.getString("message");
            assertTrue("Error message should contain 'Payment failed: Failed to create payment'",
                    message.startsWith("Payment failed: Failed to create payment"));
            assertTrue("Error message should indicate 'No such PaymentMethod'",
                    message.contains("No such PaymentMethod: 'invalid_method'"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }






    /**
     * Test Case: Initiate a payment with invalid parameters (should fail).
     */
    @Test
    public void initiatePaymentInvalidParametersTest() {
        Response response = RestAssured.given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("amount", -10.0)  // Invalid amount
                .formParam("tripId", 1)
                .formParam("passengerId", 9)
                .when()
                .post("/payments/initiate");


        assertEquals("Expected status code 400", 400, response.getStatusCode());


        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertTrue("Error message should mention invalid parameters",
                    responseJson.getString("error").contains("Invalid payment parameters."));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    @Test
    public void testInitiatePaymentWithNegativeAmount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.initiatePayment(-50.0, 1L, 9L);
        });
        assertEquals("Payment amount must be greater than 0.", exception.getMessage());
    }


    @Test
    public void testProcessPaymentWithZeroAmount() {
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.processPayment(9L, 1L, 0.0, "pm_card_visa")
        );
        assertEquals("Payment amount must be greater than 0.", exception.getMessage());
    }

    /**
     * Test Case: Verify identity successfully.
     */
    @Test
    public void verifyIdentitySuccessTest() {
        Response response = RestAssured.given()
                .when()
                .post("/api/identity/verify?userId=207&firstName=Umar&lastName=Hammami&imageUrl=https://www.idanalyzer.com/assets/testsample_id.jpg");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertTrue("Verified should be true", responseJson.getBoolean("verified"));
            assertEquals("Identity verified successfully.", responseJson.getString("message"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Verify identity with wrong ID picture.
     */
    @Test
    public void verifyIdentityNameMismatchTest() {
        Response response = RestAssured.given()
                .when()
                .post("/api/identity/verify?userId=9&firstName=Anthony&lastName=Campana&imageUrl=https://www.idanalyzer.com/assets/testsample_id.jpg");

        // Expect 200 OK since the request was processed, but verification fails
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        String responseBody = response.getBody().asString();
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            assertFalse("Verified should be false", responseJson.getBoolean("verified"));
            assertEquals("Name mismatch between ID and account.", responseJson.getString("message"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }


    /**
     * Test Case: Fetch a conversation between two users.
     */
    @Test
    public void getConversationBetweenTwoUsersTest() {
        Response response = RestAssured.given()
                .when()
                .get("/messages/conversation/28/14");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        // Validate response body structure
        String responseBody = response.getBody().asString();
        try {
            // Expect an array of messages
            JSONArray messages = new JSONArray(responseBody);
            assertTrue("Should return at least one message in the conversation", messages.length() > 0);

            // Check one message's basic fields
            JSONObject firstMessage = messages.getJSONObject(0);
            assertNotNull("Message ID should not be null", firstMessage.get("id"));
            assertNotNull("Sender should not be null", firstMessage.get("sender"));
            assertNotNull("Receiver should not be null", firstMessage.get("receiver"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Fetch conversation by trip.
     */
    @Test
    public void getConversationByTripTest() {
        Response response = RestAssured.given()
                .when()
                .get("/messages/conversationByTrip/208");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        String responseBody = response.getBody().asString();
        try {
            JSONArray messages = new JSONArray(responseBody);
            assertTrue("Should return at least one message for the trip", messages.length() > 0);

            // Check one message
            JSONObject firstMessage = messages.getJSONObject(0);
            assertEquals("TripId should match", 208, firstMessage.getInt("tripId"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Send a message.
     */
    @Test
    public void sendMessageTest() {
        Response response = RestAssured.given()
                .when()
                .post("/messages/send?senderId=9&receiverId=25&content=Hello+this+is+a+test");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        // Validate the sent message
        String responseBody = response.getBody().asString();
        try {
            JSONObject msg = new JSONObject(responseBody);
            assertEquals("Sender ID should match", 9, msg.getJSONObject("sender").getInt("id"));
            assertEquals("Receiver ID should match", 25, msg.getJSONObject("receiver").getInt("id"));
            assertEquals("Content should match", "Hello+this+is+a+test", msg.getString("content"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Fetch all conversations for a user with no content (expect 204).
     */
    @Test
    public void getAllConversationsEmptyTest() {
        Response response = RestAssured.given()
                .when()
                .get("/messages/conversation/all/207");

        // Expect 204 No Content since no conversations
        assertEquals("Expected status code 204", 204, response.getStatusCode());
    }

    /**
     * Test Case: Fetch all conversations for a user with content (expect 200).
     */
    @Test
    public void getAllConversationsWithContentTest() {
        Response response = RestAssured.given()
                .when()
                .get("/messages/conversation/all/32");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        String responseBody = response.getBody().asString();
        try {
            JSONArray conversations = new JSONArray(responseBody);
            assertTrue("Should return at least one conversation", conversations.length() > 0);
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Get unread messages for a user.
     */
    @Test
    public void getUnreadMessagesTest() {
        Response response = RestAssured.given()
                .when()
                .get("/messages/unread/9");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        String responseBody = response.getBody().asString();
        try {
            JSONArray unreadMessages = new JSONArray(responseBody);
            // Just check that we have some messages and that isRead is false
            if (unreadMessages.length() > 0) {
                JSONObject firstMessage = unreadMessages.getJSONObject(0);
                assertFalse("Message should be unread", firstMessage.getBoolean("isRead"));
            }
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Marking a message as read.
     * Assuming messageId=131 was created recently by the sendMessageTest().
     */
    @Test
    public void markMessageAsReadTest() {
        Response response = RestAssured.given()
                .when()
                .put("/messages/read/131");

        // Expect 200 OK, no body
        assertEquals("Expected status code 200", 200, response.getStatusCode());
    }

    /**
     * Test Case: Edit a message within time limit.
     * Steps:
     * 1. Send a new message with senderId=223 and receiverId=225.
     * 2. Immediately edit the message with a new content.
     */
    @Test
    public void editMessageTest() {
        // Send a new message
        Response sendResponse = RestAssured.given()
                .when()
                .post("/messages/send?senderId=223&receiverId=225&content=Original+Content");
        assertEquals("Expected status code 200 when sending message", 200, sendResponse.getStatusCode());

        int messageId;
        try {
            JSONObject sentMessage = new JSONObject(sendResponse.getBody().asString());
            messageId = sentMessage.getInt("id");
        } catch (JSONException e) {
            fail("Failed to extract message ID: " + e.getMessage());
            return;
        }

        // Edit the message
        Response editResponse = RestAssured.given()
                .when()
                .put("/messages/edit/" + messageId + "?newContent=Edited+Content");
        assertEquals("Expected status code 200 when editing message", 200, editResponse.getStatusCode());

        // Validate edited content
        try {
            JSONObject editedMessage = new JSONObject(editResponse.getBody().asString());
            assertEquals("Edited+Content", editedMessage.getString("content"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Delete a message within time limit.
     * Steps:
     * 1. Send a new message with senderId=223 and receiverId=225.
     * 2. Immediately delete the message.
     */
    @Test
    public void deleteMessageTest() {
        // Send a new message
        Response sendResponse = RestAssured.given()
                .when()
                .post("/messages/send?senderId=223&receiverId=225&content=To+be+deleted");
        assertEquals("Expected status code 200 when sending message", 200, sendResponse.getStatusCode());

        int messageId;
        try {
            JSONObject sentMessage = new JSONObject(sendResponse.getBody().asString());
            messageId = sentMessage.getInt("id");
        } catch (JSONException e) {
            fail("Failed to extract message ID: " + e.getMessage());
            return;
        }

        // Delete the message
        Response deleteResponse = RestAssured.given()
                .when()
                .delete("/messages/delete/" + messageId);
        assertEquals("Expected status code 200 when deleting message", 200, deleteResponse.getStatusCode());

        String deleteResponseBody = deleteResponse.getBody().asString();
        assertTrue("Response should indicate successful deletion",
                deleteResponseBody.contains("Message deleted successfully"));
    }

    /**
     * Test Case: User does not exist scenario
     *
     * Based on the Postman response:
     * http://localhost:8080/api/identity/verify?userId=999999&firstName=Anthony&lastName=Campana&imageUrl=https://www.idanalyzer.com/assets/testsample_id.jpg
     * returns:
     * {
     *     "message": "Name mismatch between ID and account.",
     *     "verified": false
     * }
     *
     * So we now expect a 200 response and "Name mismatch" message.
     */
    @Test
    public void verifyIdentityUserNotFoundScenarioTest() {
        Response response = RestAssured.given()
                .queryParam("imageUrl", "https://www.idanalyzer.com/assets/testsample_id.jpg")
                .queryParam("userId", 999999)
                .queryParam("firstName", "Anthony")
                .queryParam("lastName", "Campana")
                .when()
                .post("/api/identity/verify");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        String responseBody = response.getBody().asString();
        try {
            JSONObject json = new JSONObject(responseBody);
            assertFalse("Verified should be false", json.getBoolean("verified"));
            assertEquals("Name mismatch between ID and account.", json.getString("message"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Test Case: Failed to fetch image scenario
     *
     * Based on the Postman response:
     * http://localhost:8080/api/identity/verify?userId=9&firstName=Anthony&lastName=Campana&imageUrl=http://localhost:9999/nonexistent.jpg
     * returns:
     * {
     *     "message": "Failed to fetch or encode the image.",
     *     "verified": false
     * }
     *
     * Expect a 200 response and "Failed to fetch or encode the image." message.
     */
    @Test
    public void verifyIdentityImageFetchFailTest() {
        Response response = RestAssured.given()
                .queryParam("imageUrl", "http://localhost:9999/nonexistent.jpg")
                .queryParam("userId", 9)
                .queryParam("firstName", "Anthony")
                .queryParam("lastName", "Campana")
                .when()
                .post("/api/identity/verify");

        // Expect 200 OK
        assertEquals("Expected status code 200", 200, response.getStatusCode());

        String responseBody = response.getBody().asString();
        try {
            JSONObject json = new JSONObject(responseBody);
            assertFalse("Verified should be false", json.getBoolean("verified"));
            assertEquals("Failed to fetch or encode the image.", json.getString("message"));
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }




    @Test
    public void isAccountVerifiedNonExistentUserTest() {
        RestAssured.given()
                .get("/api/users/-1/isVerified")
                .then()
                .statusCode(404);
    }
    /**
    @Test
    public void testPassengerLifecycle34() {
        Response res = RestAssured.given()
                .post("/passenger-trip-status/request/55/34")
                .then()
                .statusCode(200) // Expecting success when passenger makes a valid request
                .extract().response();
        String body = res.getBody().asString();
        Assert.assertTrue("Request should be successful", body.contains("Request sent successfully"));

        // Step 3: Verify passenger 34 is in the pending list
        res = RestAssured.given()
                .get("/passenger-trip-status/pending/55")
                .then()
                .statusCode(200) // Ensure the endpoint works as expected
                .extract().response();
        body = res.getBody().asString();
        Assert.assertTrue("Expected passenger 34 in pending list", body.contains("\"id\":34"));

        // Step 4: Accept passenger 34
        res = RestAssured.given()
                .post("/passenger-trip-status/accept?tripId=55&passengerId=34")
                .then()
                .statusCode(200) // Expecting success when passenger is accepted
                .extract().response();
        body = res.getBody().asString();
        Assert.assertTrue("Passenger should be successfully accepted", body.contains("Passenger accepted and notified"));

        // Step 5: Verify passenger payment status (initially false)
        res = RestAssured.given()
                .get("/passenger-trip-status/paymentStatus/55/34")
                .then()
                .statusCode(200) // Ensure the endpoint works as expected
                .extract().response();
        body = res.getBody().asString();
        Assert.assertTrue("Passenger payment status should be false", body.contains("\"paymentStatus\":false"));

        // Step 6: Remove passenger 34 from the trip
        res = RestAssured.given()
                .delete("/passenger-trip-status/remove/55/34")
                .then()
                .statusCode(200) // Expecting success when passenger is removed
                .extract().response();
        body = res.getBody().asString();
        Assert.assertTrue("Passenger should be removed successfully", body.contains("Passenger removed successfully"));

        // Step 7: Verify passenger removal (404 expected on re-removal)
        RestAssured.given()
                .delete("/passenger-trip-status/remove/55/34")
                .then()
                .statusCode(404); // Removing already removed passenger should return 404
    }




    @Test
    public void passengerDeclineAndRemoveTest() {
        // Step 1: Request passenger 82 to join trip 55
        RestAssured.given()
                .post("/passenger-trip-status/request/55/82")
                .then()
                .statusCode(200);

        // Step 2: Check pending passengers should include 82
        Response res = RestAssured.given()
                .get("/passenger-trip-status/pending/55")
                .then()
                .statusCode(200)
                .extract().response();
        String body = res.getBody().asString();
        Assert.assertTrue("Expected passenger 82 in pending list", body.contains("\"id\":82"));

        // Step 3: Decline passenger 82
        RestAssured.given()
                .queryParam("tripId", 55)
                .queryParam("passengerId", 82)
                .post("/passenger-trip-status/decline")
                .then()
                .statusCode(200)
                .body(containsString("Passenger declined and notified"));

        // Step 4: Attempt to remove the declined passenger (optional cleanup)
        res = RestAssured.given()
                .delete("/passenger-trip-status/remove/55/82")
                .then()
                .statusCode(anyOf(is(200), is(400))) // Adjust based on behavior
                .extract().response();
        body = res.getBody().asString();

        if (body.contains("Passenger removed successfully")) {
            Assert.assertTrue("Passenger removal succeeded", true);
        } else if (body.contains("Passenger is not part of the trip")) {
            Assert.assertTrue("Passenger removal failed as expected for declined status", true);
        } else {
            Assert.fail("Unexpected response: " + body);
        }

        // Step 5: Attempt to remove the same passenger again (should return 404)
        RestAssured.given()
                .delete("/passenger-trip-status/remove/55/82")
                .then()
                .statusCode(404);
    }
    */


}

