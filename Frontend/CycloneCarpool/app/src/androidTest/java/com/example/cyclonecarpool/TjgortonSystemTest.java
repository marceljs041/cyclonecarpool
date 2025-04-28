package com.example.cyclonecarpool;

import com.example.cyclonecarpool.user.SignIn;
import com.example.cyclonecarpool.user.ProfilePage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;

import static org.hamcrest.CoreMatchers.allOf;

import android.content.Context;
import android.content.Intent;

@RunWith(Enclosed.class)
public class TjgortonSystemTest {

    private static final int SIMULATED_DELAY_MS = 1500;

    @RunWith(AndroidJUnit4ClassRunner.class)
    public static class ProfileTest {

        @Rule
        public ActivityScenarioRule<ProfilePage> profileScenarioRule = new ActivityScenarioRule<>(getProfileIntent());

        private Intent getProfileIntent() {
            Context context = ApplicationProvider.getApplicationContext();
            Intent intent = new Intent(context, ProfilePage.class);
            intent.putExtra("userId", 28);
            intent.putExtra("role", "Driver");
            return intent;
        }

        @Test
        public void loadProfile() {
            String userName = "Tyler Gorton";
            String userEmail = "tjgorton@iastate.edu";

            try {
                Thread.sleep(SIMULATED_DELAY_MS);
            } catch (InterruptedException ignored) {}

            onView(withId(R.id.profile_name)).check(matches(withText(userName)));
            onView(withId(R.id.profile_email)).check(matches(withText(userEmail)));
        }

        @Test
        public void editProfile() {
            String newFirstName = "TJ";
            String newLastName = "G";
            String newUserName = String.format("%s %s", newFirstName, newLastName);

            try {
                Thread.sleep(SIMULATED_DELAY_MS);
            } catch (Exception ignored) {}

            onView(withId(R.id.profile_edit_btn)).perform(click());
            onView(withId(R.id.profile_edit_fname)).perform(typeText(newFirstName), closeSoftKeyboard());
            onView(withId(R.id.profile_edit_lname)).perform(typeText(newLastName), closeSoftKeyboard());

            onView(withId(R.id.profile_confirm_btn)).perform(click());

            try {
                Thread.sleep(SIMULATED_DELAY_MS);
            } catch (Exception ignored) {}

            onView(withId(R.id.profile_name)).check(matches(withText(newUserName)));
        }

        @Test
        public void undoEditProfile() {
            String newFirstName = "Tyler";
            String newLastName = "Gorton";
            String newUserName = String.format("%s %s", newFirstName, newLastName);

            try {
                Thread.sleep(SIMULATED_DELAY_MS);
            } catch (Exception ignored) {}

            onView(withId(R.id.profile_edit_btn)).perform(click());
            onView(withId(R.id.profile_edit_fname)).perform(typeText(newFirstName), closeSoftKeyboard());
            onView(withId(R.id.profile_edit_lname)).perform(typeText(newLastName), closeSoftKeyboard());

            onView(withId(R.id.profile_confirm_btn)).perform(click());

            try {
                Thread.sleep(SIMULATED_DELAY_MS);
            } catch (Exception ignored) {}

            onView(withId(R.id.profile_name)).check(matches(withText(newUserName)));
        }

    }

    @RunWith(AndroidJUnit4ClassRunner.class)
    public static class SignInTest {

        @Rule
        public ActivityScenarioRule<SignIn> signInScenarioRule = new ActivityScenarioRule<>(SignIn.class);

        @Test
        public void signIn() {
            Intents.init();

            String email = "tjgorton@iastate.edu";
            String password = "pass";

            onView(withId(R.id.signin_emailInput)).perform(typeText(email), closeSoftKeyboard());
            onView(withId(R.id.signin_passwordInput)).perform(typeText(password), closeSoftKeyboard());
            onView(withId(R.id.btnConfirmSignIn)).perform(click());

            try {
                Thread.sleep(SIMULATED_DELAY_MS);
            } catch (Exception ignored) {}

            intended(allOf(
                    hasComponent(HomePage.class.getName()),
                    hasExtra("userId", 28),
                    hasExtra("role", "Driver")
            ));

            Intents.release();
        }

        @Test
        public void failedSignIn() {
            String email = "tjgorton23@iastate.edu"; // No account for this email
            String password = "password";

            onView(withId(R.id.signin_emailInput)).perform(typeText(email), closeSoftKeyboard());
            onView(withId(R.id.signin_passwordInput)).perform(typeText(password), closeSoftKeyboard());
            onView(withId(R.id.btnConfirmSignIn)).perform(click());

            try {
                Thread.sleep(SIMULATED_DELAY_MS);
            } catch (Exception ignored) {}

            onView(withText("The password or email doesn't match")).check(matches(isDisplayed()));
        }
    }

}
