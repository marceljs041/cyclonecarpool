package com.example.cyclonecarpool;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.cyclonecarpool.user.ProfilePage;
import com.example.cyclonecarpool.user.SignUp;
import com.example.cyclonecarpool.trips.CreateTripPage;
import com.example.cyclonecarpool.trips.MyTripPage;
import com.example.cyclonecarpool.trips.EditTripPage;
import com.example.cyclonecarpool.trips.TripInfoPage;
import com.example.cyclonecarpool.chat.ChatsPage;
import com.example.cyclonecarpool.chat.MessagingPage;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Combined test file for multiple activities:
 * - SignUp Activity (original)
 * - HomePage Activity
 * - MyTripPage Activity
 * - CreateTripPage Activity
 * - EditTripPage Activity
 */
@RunWith(AndroidJUnit4.class)
public class MarceljsSystemTest {

    // -----------------------------------------------------------
    // Tests for SignUp Activity
    // -----------------------------------------------------------

    @Rule
    public ActivityTestRule<SignUp> signUpActivityRule =
            new ActivityTestRule<>(SignUp.class);

    @Test
    public void testUIElementsDisplayed() {
        onView(withId(R.id.firstNameInput)).check(matches(isDisplayed()));
        onView(withId(R.id.lastNameInput)).check(matches(isDisplayed()));
        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));
        onView(withId(R.id.password1Input)).check(matches(isDisplayed()));
        onView(withId(R.id.password2Input)).check(matches(isDisplayed()));
        onView(withId(R.id.roleSpinner)).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirmSignUp)).check(matches(isDisplayed()));
    }

    @Test
    public void testSpinnerSelection() {
        onView(withId(R.id.roleSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Driver"))).perform(click());
        onView(withId(R.id.roleSpinner)).check(matches(withSpinnerText(containsString("Driver"))));
    }

    @Test
    public void testErrorOnPasswordMismatch() {
        onView(withId(R.id.password1Input)).perform(typeText("password23"), closeSoftKeyboard());
        onView(withId(R.id.password2Input)).perform(typeText("password321"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmSignUp)).perform(click());
        onView(withText("The passwords do not match!")).check(matches(isDisplayed()));
    }

    @Test
    public void testValidInputSubmission() {
        onView(withId(R.id.firstNameInput)).perform(typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.lastNameInput)).perform(typeText("Doe"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password1Input)).perform(typeText("password23"), closeSoftKeyboard());
        onView(withId(R.id.password2Input)).perform(typeText("password23"), closeSoftKeyboard());
        onView(withId(R.id.roleSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Driver"))).perform(click());
        onView(withId(R.id.btnConfirmSignUp)).perform(click());
    }

    // -----------------------------------------------------------
    // Tests for HomePage Activity
    // -----------------------------------------------------------

    @Rule
    public ActivityTestRule<HomePage> homeActivityRule =
            new ActivityTestRule<>(HomePage.class, false, false);

    @Before
    public void setUpIntents() {
        Intents.init();
    }

    @After
    public void releaseIntents() {
        Intents.release();
    }

    private Intent createHomePageIntent(String role, int userId) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomePage.class);
        intent.putExtra("role", role);
        intent.putExtra("userId", userId);
        return intent;
    }

    @Test
    public void testUIElementsDisplayedForDriver_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 23));
        onView(withId(R.id.house_navBtn_h)).check(matches(isDisplayed()));
        onView(withId(R.id.trips_navBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.messages_navBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCreate)).check(matches(isDisplayed())); // Driver sees create button
    }

    @Test
    public void testUIElementsDisplayedForPassenger_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Passenger", 23));
        onView(withId(R.id.btnCreate)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testUIElementsDisplayedForAdmin_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Admin", 23));
        onView(withId(R.id.btnCreate)).check(matches(isDisplayed())); // Admin sees create button
    }

    @Test
    public void testToggleSearchBar_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 23));
        onView(withId(R.id.searchBarClosed)).check(matches(isDisplayed()));
        onView(withId(R.id.searchBarToggle)).perform(click());
        onView(withId(R.id.searchBarExpanded)).check(matches(isDisplayed()));
        onView(withId(R.id.searchBarToggle)).perform(click());
        onView(withId(R.id.searchBarClosed)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileNavigation_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 23));
        onView(withId(R.id.profileBtn)).perform(click());
        intended(hasComponent(ProfilePage.class.getName()));
    }

    @Test
    public void testTripsNavigation_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 23));
        onView(withId(R.id.trips_navBtn)).perform(click());
        intended(hasComponent(MyTripPage.class.getName()));
    }

    @Test
    public void testMessagesNavigation_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 23));
        onView(withId(R.id.messages_navBtn)).perform(click());
        intended(hasComponent(ChatsPage.class.getName()));
    }

    @Test
    public void testCreateTripNavigationAsDriver_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 23));
        onView(withId(R.id.btnCreate)).perform(click());
        intended(hasComponent(CreateTripPage.class.getName()));
    }

    @Test
    public void testSearchFunctionality_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 23));
        onView(withId(R.id.searchBarToggle)).perform(click());
        onView(withId(R.id.fromSearchInput)).perform(typeText("Ames"), closeSoftKeyboard());
        onView(withId(R.id.toSearchInput)).perform(typeText("Des Moines"), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.trip_Container)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserNotVerifiedShowsPopup_HomePage() {
        homeActivityRule.launchActivity(createHomePageIntent("Driver", 900));
        onView(withId(R.id.trips_navBtn)).perform(click());
        onView(withId(R.id.btn_close_popup)).perform(click());
    }

    // -----------------------------------------------------------
    // Tests for MyTripPage Activity
    // -----------------------------------------------------------

    @Rule
    public ActivityTestRule<MyTripPage> myTripActivityRule =
            new ActivityTestRule<>(MyTripPage.class, false, false);

    private Intent createMyTripPageIntent(String role, int userId) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MyTripPage.class);
        intent.putExtra("role", role);
        intent.putExtra("userId", userId);
        return intent;
    }

    @Test
    public void testUIElementsDisplayedForDriver_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Driver", 23));
        onView(withId(R.id.house_navBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.trips_navBtn_h)).check(matches(isDisplayed()));
        onView(withId(R.id.messages_navBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.profileBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCreate)).check(matches(isDisplayed())); // Driver can create
    }

    @Test
    public void testUIElementsDisplayedForPassenger_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Passenger", 23));
        onView(withId(R.id.btnCreate)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testUIElementsDisplayedForAdmin_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Admin", 23));
        onView(withId(R.id.btnCreate)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToHomePage_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Driver", 23));
        onView(withId(R.id.house_navBtn)).perform(click());
        intended(hasComponent(HomePage.class.getName()));
    }

    @Test
    public void testNavigationToProfile_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Driver", 23));
        onView(withId(R.id.profileBtn)).perform(click());
        intended(hasComponent(ProfilePage.class.getName()));
    }

    @Test
    public void testNavigationToMessages_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Driver", 23));
        onView(withId(R.id.messages_navBtn)).perform(click());
        intended(hasComponent(ChatsPage.class.getName()));
    }

    @Test
    public void testCreateTripNavigationAsDriver_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Driver", 23));
        onView(withId(R.id.btnCreate)).perform(click());
        intended(hasComponent(CreateTripPage.class.getName()));
    }

    @Test
    public void testMyTripsDisplay_MyTripPage() {
        myTripActivityRule.launchActivity(createMyTripPageIntent("Driver", 23));
        onView(withId(R.id.trip_Container)).check(matches(isDisplayed()));
    }

    // -----------------------------------------------------------
    // Tests for CreateTripPage Activity
    // -----------------------------------------------------------

    @Rule
    public ActivityTestRule<CreateTripPage> createTripActivityRule =
            new ActivityTestRule<>(CreateTripPage.class, false, false);

    private Intent createTripPageIntent(int userId, String role) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CreateTripPage.class);
        intent.putExtra("userId", userId);
        intent.putExtra("role", role);
        return intent;
    }

    @Test
    public void testUIElementsDisplayed_CreateTripPage() {
        createTripActivityRule.launchActivity(createTripPageIntent(23, "Driver"));
        onView(withId(R.id.fromCreateInput)).check(matches(isDisplayed()));
        onView(withId(R.id.toCreateInput)).check(matches(isDisplayed()));
        onView(withId(R.id.pickupCreateInput)).check(matches(isDisplayed()));
        onView(withId(R.id.dateCreateInput)).check(matches(isDisplayed()));
        onView(withId(R.id.timeCreateInput)).check(matches(isDisplayed()));
        onView(withId(R.id.seatsCreateInput)).check(matches(isDisplayed()));
        onView(withId(R.id.priceCreateInput)).check(matches(isDisplayed()));
        onView(withId(R.id.checkBoxRoundTrip)).check(matches(isDisplayed()));
        onView(withId(R.id.checkBoxNoSmoke)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCreateTrip)).check(matches(isDisplayed()));
    }

//    @Test
//    public void testInvalidDateFormat_CreateTripPage() {
//        createTripActivityRule.launchActivity(createTripPageIntent(23, "Driver"));
//        onView(withId(R.id.fromCreateInput)).perform(typeText("Ames"), closeSoftKeyboard());
//        onView(withId(R.id.toCreateInput)).perform(typeText("Des Moines"), closeSoftKeyboard());
//        onView(withId(R.id.pickupCreateInput)).perform(typeText("Central Campus"), closeSoftKeyboard());
//        onView(withId(R.id.dateCreateInput)).perform(typeText("22-22-2222"), closeSoftKeyboard());
//        onView(withId(R.id.timeCreateInput)).perform(typeText("25:61"), closeSoftKeyboard());
//        onView(withId(R.id.seatsCreateInput)).perform(typeText("4"), closeSoftKeyboard());
//        onView(withId(R.id.priceCreateInput)).perform(typeText("20"), closeSoftKeyboard());
//        onView(withId(R.id.btnCreateTrip)).perform(click());
//        onView(withText("Invalid date/time format. Please use 'yyyy-MM-dd' for date and 'HH:mm' for time."))
//                .check(matches(isDisplayed()));
//    }

//    @Test
//    public void testNonNumericSeatsPrice_CreateTripPage() {
//        createTripActivityRule.launchActivity(createTripPageIntent(23, "Driver"));
//        onView(withId(R.id.fromCreateInput)).perform(typeText("Ames"), closeSoftKeyboard());
//        onView(withId(R.id.toCreateInput)).perform(typeText("Des Moines"), closeSoftKeyboard());
//        onView(withId(R.id.pickupCreateInput)).perform(typeText("Central Campus"), closeSoftKeyboard());
//        onView(withId(R.id.dateCreateInput)).perform(typeText("2030-12-31"), closeSoftKeyboard());
//        onView(withId(R.id.timeCreateInput)).perform(typeText("10:30"), closeSoftKeyboard());
//        onView(withId(R.id.seatsCreateInput)).perform(typeText("abc"), closeSoftKeyboard());
//        onView(withId(R.id.priceCreateInput)).perform(typeText("xyz"), closeSoftKeyboard());
//        onView(withId(R.id.btnCreateTrip)).perform(click());
//        onView(withText("Seats and Price must be valid numbers."))
//                .check(matches(isDisplayed()));
//    }

//    @Test
//    public void testPastDate_CreateTripPage() {
//        createTripActivityRule.launchActivity(createTripPageIntent(23, "Driver"));
//        onView(withId(R.id.fromCreateInput)).perform(typeText("Ames"), closeSoftKeyboard());
//        onView(withId(R.id.toCreateInput)).perform(typeText("Des Moines"), closeSoftKeyboard());
//        onView(withId(R.id.pickupCreateInput)).perform(typeText("Central Campus"), closeSoftKeyboard());
//        onView(withId(R.id.dateCreateInput)).perform(typeText("2000-01-01"), closeSoftKeyboard());
//        onView(withId(R.id.timeCreateInput)).perform(typeText("10:30"), closeSoftKeyboard());
//        onView(withId(R.id.seatsCreateInput)).perform(typeText("4"), closeSoftKeyboard());
//        onView(withId(R.id.priceCreateInput)).perform(typeText("20"), closeSoftKeyboard());
//        onView(withId(R.id.btnCreateTrip)).perform(click());
//        onView(withText("Please select a future date and time."))
//                .check(matches(isDisplayed()));
//    }

    @Test
    public void testValidTripCreation_CreateTripPage() {
        createTripActivityRule.launchActivity(createTripPageIntent(23, "Driver"));
        onView(withId(R.id.fromCreateInput)).perform(typeText("Ames"), closeSoftKeyboard());
        onView(withId(R.id.toCreateInput)).perform(typeText("Des Moines"), closeSoftKeyboard());
        onView(withId(R.id.pickupCreateInput)).perform(typeText("Central Campus"), closeSoftKeyboard());
        onView(withId(R.id.dateCreateInput)).perform(typeText("2030-12-31"), closeSoftKeyboard());
        onView(withId(R.id.timeCreateInput)).perform(typeText("10:30"), closeSoftKeyboard());
        onView(withId(R.id.seatsCreateInput)).perform(typeText("4"), closeSoftKeyboard());
        onView(withId(R.id.priceCreateInput)).perform(typeText("20"), closeSoftKeyboard());
        onView(withId(R.id.checkBoxRoundTrip)).perform(click());
        onView(withId(R.id.checkBoxNoSmoke)).perform(click());
        onView(withId(R.id.btnCreateTrip)).perform(click());
        // Can't guarantee server response, just ensuring no crash and UI handled.
    }

    // -----------------------------------------------------------
    // Tests for EditTripPage Activity
    // -----------------------------------------------------------

    @Rule
    public ActivityTestRule<EditTripPage> editTripActivityRule =
            new ActivityTestRule<>(EditTripPage.class, false, false);

    private Intent createEditTripIntent(int tripId, int userId, String role) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditTripPage.class);
        intent.putExtra("tripId", tripId);
        intent.putExtra("userId", userId);
        intent.putExtra("role", role);
        return intent;
    }

    @Test
    public void testUIElementsDisplayed_EditTripPage() {
        editTripActivityRule.launchActivity(createEditTripIntent(1, 23, "Driver"));
        onView(withId(R.id.fromEditInput)).check(matches(isDisplayed()));
        onView(withId(R.id.toEditInput)).check(matches(isDisplayed()));
        onView(withId(R.id.pickupEditInput)).check(matches(isDisplayed()));
        onView(withId(R.id.dateEditInput)).check(matches(isDisplayed()));
        onView(withId(R.id.timeEditInput)).check(matches(isDisplayed()));
        onView(withId(R.id.seatsEditInput)).check(matches(isDisplayed()));
        onView(withId(R.id.priceEditInput)).check(matches(isDisplayed()));
        onView(withId(R.id.checkBoxRoundTrip)).check(matches(isDisplayed()));
        onView(withId(R.id.checkBoxNoSmoke)).check(matches(isDisplayed()));
        onView(withId(R.id.btnEditTrip)).check(matches(isDisplayed()));
    }

//    @Test
//    public void testInvalidDateFormat_EditTripPage() {
//        editTripActivityRule.launchActivity(createEditTripIntent(1, 23, "Driver"));
//        // Invalid date/time
//        onView(withId(R.id.dateEditInput)).perform(clearText());
//        onView(withId(R.id.dateEditInput)).perform(typeText("12-12-2020"), closeSoftKeyboard());
//        onView(withId(R.id.timeEditInput)).perform(clearText());
//        onView(withId(R.id.timeEditInput)).perform(typeText("10:00"), closeSoftKeyboard());
//        onView(withId(R.id.btnEditTrip)).perform(click());
//        onView(withText("Invalid date/time format. Please use 'yyyy-MM-dd' for date and 'HH:mm' for time.")).check(matches(isDisplayed())); // Since we show a generic error toast
//    }

    @Test
    public void testValidEditTrip_EditTripPage() {
        editTripActivityRule.launchActivity(createEditTripIntent(1, 23, "Driver"));
        onView(withId(R.id.fromEditInput)).perform(typeText("Ames"), closeSoftKeyboard());
        onView(withId(R.id.toEditInput)).perform(typeText("Des Moines"), closeSoftKeyboard());
        onView(withId(R.id.pickupEditInput)).perform(typeText("Central Campus"), closeSoftKeyboard());
        onView(withId(R.id.dateEditInput)).perform(typeText("2030-12-31"), closeSoftKeyboard());
        onView(withId(R.id.timeEditInput)).perform(typeText("10:30"), closeSoftKeyboard());
        onView(withId(R.id.seatsEditInput)).perform(typeText("4"), closeSoftKeyboard());
        onView(withId(R.id.priceEditInput)).perform(typeText("20"), closeSoftKeyboard());
        onView(withId(R.id.checkBoxRoundTrip)).perform(click());
        onView(withId(R.id.checkBoxNoSmoke)).perform(click());
        onView(withId(R.id.btnEditTrip)).perform(click());
        // Without mocking backend, just ensure no crash occurs.
        // On success, toast "Trip updated successfully!" would appear.
    }

}
