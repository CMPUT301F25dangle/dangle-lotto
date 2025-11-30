package com.example.dangle_lotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import android.Manifest;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * User Stories Tests - Unit Tests for User Stories
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 11/14/2025
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserStoriesTests {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    );

    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<>(LoginActivity.class);

    private static FirebaseManager firebaseManager;
    private IdlingResource firebaseIdlingResource;

    private static String ownerUid;
    private static String testerUid;
    private String tester2Uid;

    private Event eventOfInterest;

    /**
     * Sets up the Firebase emulator for testing.
     */
    @BeforeClass
    public static void setupOnce() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFunctions func = FirebaseFunctions.getInstance();

        db.useEmulator("10.0.2.2", 8080);
        mAuth.useEmulator("10.0.2.2", 9099);
        func.useEmulator("10.0.2.2", 5001);

        firebaseManager = FirebaseManager.getInstance();

        // Clear db
        clearFirestore();
    }

    /**
     * Sets up the emulator before every test for testing.
     */
    @Before
    public void setup() throws InterruptedException {
        // Register idling resource so Espresso waits for Firebase calls
        firebaseIdlingResource = firebaseManager.getIdlingResource();
        IdlingRegistry.getInstance().register(firebaseIdlingResource);

        // Creates owner user
        firebaseManager.signUp("owner@gmail.com", "password", "Owner User", "owner username","1234123123", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ownerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Create tester AFTER owner is created
        firebaseManager.signUp("tester@gmail.com", "password", "Tester User", "tester username", "534532", "",true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                testerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Create an event to test on
        eventOfInterest = firebaseManager.createEvent(ownerUid, "Good Party", makeTimestamp(2024, 11, 1), makeTimestamp(2026, 11, 1), makeTimestamp(2026, 11, 2), "Da House", false,"A party for good people", 10, 100, "", "", new ArrayList<String>());
    }

    @After
    public void tearDown() throws InterruptedException {
        clearFirestore();

        // Unregister idling resource
        IdlingRegistry.getInstance().unregister(firebaseIdlingResource);
    }

    /**
     * Deletes all users and events from the database. This is a helper method
     */
    public static void clearFirestore() throws InterruptedException {
        try {
            // Delete users collection
            QuerySnapshot users = Tasks.await(firebaseManager.getUsersReference().get());
            for (DocumentSnapshot doc : users) {
                System.out.println("Deleting user: " + doc.getId());
                Tasks.await(firebaseManager.deleteUser(doc.getId()));
            }

            // Delete events collection
            QuerySnapshot events = Tasks.await(firebaseManager.getEventsReference().get());
            for (DocumentSnapshot doc : events) {
                Tasks.await(firebaseManager.deleteEvent(doc.getId()));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(1000);
    }

    /**
     * Logs the user in. This is a helper method
     *
     * @param email String representing the user's email.
     * @param password String representing the user's password.
     */
    public void login(String email, String password) {
        // Logs the user in
        onView(withId(R.id.etLoginEmail)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.etLoginPassword)).perform(typeText(password), closeSoftKeyboard());
        onView(withText("LOGIN")).perform(click());
    }

    /**
     * Creates a timestamp object. This is a helper method.
     * @param year The year
     *
     * @param month The month
     * @param day The day
     * @return Timestamp object
     */
    public static Timestamp makeTimestamp(int year, int month, int day) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // IMPORTANT: January = 0
        cal.set(Calendar.DAY_OF_MONTH, day);

        // optional: set time to midnight
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Timestamp(cal.getTime());
    }


    /**
     * Joins the waiting list for an event. We call it registering.
     * <p>
     * US 01.01.01 As an entrant, I want to join the waiting list for a specific event
     * This test works because the buttons appearance only changes if the callback is successful for registering.
     */
    @Test
    public void UserCanJoinWaitingList() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Click on the join button
        onView(withText("Register for Lottery")).perform(click());

        // Check if button says "Withdraw Registration"
        onView(withText("Withdraw Registration")).check(matches(isDisplayed()));
    }

    /**
     * Leaves the waiting list for an event. We call it unregistering.
     * <p>
     * US 01.01.02 As an entrant, I want to leave the waiting list for a specific event
     * This test works because the buttons appearance only changes if the callback is successful for registering.
     */
    @Test
    public void UserCanLeaveWaitingList() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Click on the join button
        onView(withText("Register for Lottery")).perform(click());

        // Click on the join button
        onView(withText("Withdraw Registration")).perform(click());

        // Check if button says "Register for Lottery"
        onView(withText("Register for Lottery")).check(matches(isDisplayed()));
    }

    /**
     * Checks if the home page opens and has events.
     * <p>
     * US 01.01.03 As an entrant, I want to be able to see a list of events that I can join the waiting list for.
     */
    @Test
    public void HomePageOpensAndHasEvents() {
        // Login the user
        login("tester@gmail.com", "password");

        // check if home page opens by scanning for id
        onView(withId(R.id.home_fragment_title)).check(matches(isDisplayed()));

        // Check if an event is displayed on the home page
        onView(withText("Good Party")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can filter events based on their interests.
     * <p>
     * US 01.01.04 As an entrant, I want to filter events based on my interests and availability.
     */
    @Test
    public void UserCanFilterEvents() {
        // Login
        login("tester@gmail.com", "password");

        // FAIL TEST
        fail("Test not implemented");
    }

    /**
     * Checks if user can provide optional personal information.
     * <p>
     * US 01.02.01 As an entrant, I want to provide my personal information such as name, email and optional phone number in the app
     */
    @Test
    public void UserCanProvidePersonalInfo() {
        // User wants to sign up
        onView(withText("Don’t have an account? Sign Up")).perform(click());

        // Sign up using all fields
        onView(withId(R.id.signup_name_input)).perform(typeText("Tester 2"), closeSoftKeyboard());
        onView(withId(R.id.signup_username_input)).perform(typeText("tester2"), closeSoftKeyboard());
        onView(withId(R.id.signup_email_input)).perform(typeText("tester2@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.signup_phone_input)).perform(typeText("1234567890"), closeSoftKeyboard());
        onView(withId(R.id.signup_password_input)).perform(typeText("password"), closeSoftKeyboard());
        onView(withText("Sign Up")).perform(click());

        // Check if if are on login page
        onView(withText("LOGIN")).check(matches(isDisplayed()));

        // Login the user
        login("tester2@gmail.com", "password");

        // Check if user is on home page
        onView(withId(R.id.home_fragment_title)).check(matches(isDisplayed()));

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on settings button
        onView(withId(R.id.dashboard_fragment_setting_button)).perform(click());

        // Check if user personal info is shown
        onView(withText("Tester 2")).check(matches(isDisplayed()));
        onView(withText("tester2")).check(matches(isDisplayed()));
        onView(withText("tester2@gmail.com")).check(matches(isDisplayed()));
        onView(withText("1234567890")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can update their personal information
     * <p>
     * US 01.02.02 As an entrant I want to update information such as name, email and contact information on my profile
     */
    @Test
    public void UserCanUpdatePersonalInfo() {
        // Login the user
        login("tester@gmail.com", "password");

        // User navigates to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // User clicks on settings button
        onView(withId(R.id.dashboard_fragment_setting_button)).perform(click());

        // User updates personal info
        onView(withId(R.id.settings_fragment_name_input)).perform(replaceText("Does This Replace?"), closeSoftKeyboard());
        onView(withId(R.id.settings_fragment_username_input)).perform(replaceText("Does This Replace Too?"), closeSoftKeyboard());
        onView(withId(R.id.settings_fragment_email_input)).perform(replaceText("replaced@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.settings_fragment_phone_input)).perform(typeText("000"), closeSoftKeyboard());
        onView(withId(R.id.settings_fragment_password_input)).perform(typeText("password"), closeSoftKeyboard());

        // User clicks on update button
        onView(withText("Update Profile")).perform(click());
        onView(withText("Confirm Update")).perform(click());

        // Click on settings button
        onView(withId(R.id.dashboard_fragment_setting_button)).perform(click());

        // Update button is unclickable
        onView(withId(R.id.user_settings_update_button)).check(matches(not(isEnabled())));

        // Logout
        onView(withText("LOGOUT")).perform(click());

        // Login the user
        login("replaced@gmail.com", "password");

        // Check if user is on home page
        onView(withId(R.id.home_fragment_title)).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can view their history of events.
     * <p>
     * US 01.02.03 As an entrant, I want to have a history of events I have registered for, whether I was selected or not.
     */
    @Test
    public void UserCanViewHistoryOfEvents() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on attend button
        onView(withText("Register for Lottery")).perform(click());

        // Check if button says "Withdraw Registration"
        onView(withText("Withdraw Registration")).check(matches(isDisplayed()));

        // Click on your event buttons
        onView(withId(R.id.navigation_your_events)).perform(click());

        // Check if event is displayed
        onView(withText("Good Party")).check(matches(isDisplayed()));

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Check if buttons says "Withdraw Registration"
        onView(withText("Withdraw Registration")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can delete their account.
     * <p>
     * US 01.02.04 As an entrant, I want to delete my profile if I no longer wish to use the app.
     */
    @Test
    public void UserCanDeleteAccount() {
        // Login the user
        login("tester@gmail.com", "password");

        // User navigates to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // User clicks on settings button
        onView(withId(R.id.dashboard_fragment_setting_button)).perform(click());

        // User clicks on delete button
        onView(withText("Delete Profile")).perform(click());
        onView(withText("Confirm Delete")).perform(click());

        // Check if back on login page
        onView(withText("LOGIN")).check(matches(isDisplayed()));

        // Check if user can still login
        login("tester@gmail.com", "password");

        // Login fails
        onView(withText("LOGIN")).check(matches(isDisplayed()));

        // Check if user can still sign up using that email
        onView(withText("Don’t have an account? Sign Up")).perform(click());

        // Fill out signup information
        onView(withId(R.id.signup_name_input)).perform(typeText("Tester User"), closeSoftKeyboard());
        onView(withId(R.id.signup_username_input)).perform(typeText("tester username"));
        onView(withId(R.id.signup_email_input)).perform(typeText("tester@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.signup_password_input)).perform(typeText("password"), closeSoftKeyboard());
        onView(withText("Sign Up")).perform(click());

        // Check if user can login now
        login("tester@gmail.com", "password");

        // Login succeeds
        onView(withId(R.id.home_fragment_title)).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can receive notification when they are chosen.
     * <p>
     * US 01.04.01 As an entrant I want to receive notification when I am chosen to participate from the waiting list (when I "win" the lottery)
     */
    @Test
    public void UserCanReceiveNotificationWhenChosen() {
        // Add user to chosen list
        eventOfInterest.addChosen(testerUid);

        // Login the user
        login("tester@gmail.com", "password");

        // Navigate to notifications
        onView(withId(R.id.navigation_notifications)).perform(click());

        // Check if notification is displayed
        onView(withText("Good Party")).check(matches(isDisplayed()));
        onView(withText("You have won the lottery (Chosen)")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can receive notification when they are not chosen.
     * <p>
     * US 01.04.02 As an entrant I want to receive notification of when I am not chosen on the app (when I "lose" the lottery)
     */
    @Test
    public void UserCanReceiveNotificationWhenNotChosen() {
        // Add user to chosen list
        eventOfInterest.addCancelled(testerUid);

        // Login the user
        login("tester@gmail.com", "password");

        // Navigate to notifications
        onView(withId(R.id.navigation_notifications)).perform(click());

        // Check if notification is displayed
        onView(withText("Good Party")).check(matches(isDisplayed()));
        onView(withText("You have lost the lottery")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can opt out of receiving notifications.
     * <p>
     * US 01.04.03 As an entrant I want to opt out of receiving notifications from organizers and admins
     */
    @Test
    public void UserCanOptOutNotifications() {
        // Login the user
        login("tester@gmail.com", "password");

        // Fail test
        fail("Test not implemented");
    }

    /** Check if user can get another chance to sign up.
     * <p>
     * US 01.05.01 As an entrant I want another chance to be chosen from the waiting list if a selected user declines an invitation to sign up.
     * Users are automatically given a chance to be drawn if they have registered for the event.
     */
    @Test
    public void UserCanGetAnotherChanceToSignUp() throws InterruptedException {
        // Create a user to add to the event chosen list
        firebaseManager.signUp("tester2@gmail.com", "password", "Tester User", "tester username2","", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                tester2Uid = result;
            }
            @Override
            public void onFailure(Exception e) { }
        });
        Thread.sleep(1500);
        eventOfInterest.addChosen(tester2Uid);

        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Click on the join waitlist button
        onView(withText("Join Waitlist")).perform(click());

        // Check if button changed to "Leave Waitlist"
        onView(withText("Leave Waitlist")).check(matches(isDisplayed()));

        // Click on the back button
        onView(withId(R.id.btn_back)).perform(click());

        // Make the tester2 user decline
        eventOfInterest.addCancelled(tester2Uid);

        // Make the tester user chosen
        eventOfInterest.addChosen(testerUid);

        // User refreshes the home page
        onView(withId(R.id.refresh_button)).perform(click());

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Check if text says "You’ve Been Chosen!"
        onView(withText("You’ve Been Chosen!")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can accept an event invitation.
     * <p>
     * US 01.05.02 As an entrant I want to be able to accept the invitation to register/sign up when chosen to participate in an event.
     */
    @Test
    public void UserCanAcceptEventInvitation() {
        // Add the user to the event chosen list
        eventOfInterest.addChosen(testerUid);

        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Click on the join button
        onView(withText("You’ve Been Chosen!")).perform(click());

        // Click on the attend button to accept
        onView(withText("Accept")).perform(click());

        // Check if button says "You Have Accepted The Invitation!"
        onView(withText("You Have Accepted The Invitation!")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can decline an event invitation.
     * <p>
     * US 01.05.03 As an entrant I want to be able to decline an invitation when chosen to participate in an event.
     */
    @Test
    public void UserCanDeclineEventInvitation() {
        // Add the user to the event chosen list
        eventOfInterest.addChosen(testerUid);

        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Click on the join button
        onView(withText("You’ve Been Chosen!")).perform(click());

        // Click on the decline button to not accept
        onView(withText("Decline")).perform(click());

        // Check if button says "Cancelled"
        onView(withText("Cancelled")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can see how many entrants are on the waiting list for an event.
     * <p>
     * US 01.05.04 As an entrant, I want to know how many total entrants are on the waiting list for an event.
     */
    @Test
    public void UserCanSeeWaitingListSize() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Check number of registrants
        onView(withText("Spots Remaining: 100/100")).check(matches(isDisplayed()));

        // Click on the join button
        onView(withText("Register for Lottery")).perform(click());

        // Check number of registrants
        onView(withText("Spots Remaining: 99/100")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can see event criteria.
     * <p>
     * US 01.05.05 As an entrant, I want to be informed about the criteria or guidelines for the lottery selection process.
     */
    @Test
    public void UserCanSeeEventCriteria() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Click on info button for criteria
        onView(withId(R.id.event_detail_information_button)).perform(click());

        // Check if criteria is displayed
        onView(withText("Event Criteria")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can scan QR code
     * <p>
     * US 01.06.01 As an entrant I want to view event details within the app by scanning the promotional QR code.
     */
    @Test
    public void UserCanScanQRCode() {
        // Login the user
        login("tester@gmail.com", "password");

        // Skip test
        assumeTrue(false);
    }

    /**
     * Checks if user can sign up for an event
     * <p>
     * US 01.06.02 As an entrant I want to be able to be sign up for an event by from the event details.
     */
    @Test
    public void UserCanSignUpForEventFromEventDetails() {
        // Add user to chosen list
        eventOfInterest.addChosen(testerUid);

        // Login
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(click());

        // Click on the join button
        onView(withText("You’ve Been Chosen!")).perform(click());

        // Click accept button
        onView(withText("Accept")).perform(click());

        // Click on the join button
        onView(withText("You Have Accepted The Invitation!")).perform(click());

        // Click on the join button
        onView(withText("Attend")).perform(click());

        // Check if button says "Attending"
        onView(withText("Attending")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can identify by device
     * <p>
     * US 01.07.01 As an entrant, I want to be identified by my device, so that I don't have to use a username and password.
     * Impossible to test for this in espresso, we just check for the button being there
     */
    @Test
    public void UserCanIdentifyByDevice() {
        // Login the user
        onView(withId(R.id.etLoginEmail)).perform(typeText("tester@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.etLoginPassword)).perform(typeText("password"), closeSoftKeyboard());

        // Fill out checkbox
        onView(withId(R.id.cbRememberMe)).perform(click());

        // Check that textbox is checked
        onView(withId(R.id.cbRememberMe)).check(matches(isChecked()));

        // Click on login
        onView(withText("LOGIN")).perform(click());

        // Check if back on home page
        onView(withId(R.id.home_fragment_title)).check(matches(isDisplayed()));
    }

    /**
     * Checks if the user can logout
     */
    @Test
    public void UserCanLogout() {
        // Login the user
        login("tester@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on settings button
        onView(withId(R.id.dashboard_fragment_setting_button)).perform(click());

        // Click on logout button
        onView(withText("LOGOUT")).perform(click());

        // Check if back on login page
        onView(withText("LOGIN")).check(matches(isDisplayed()));

        // User can login
        login("tester@gmail.com", "password");

        // User is on home page
        onView(withId(R.id.home_fragment_title)).check(matches(isDisplayed()));
    }

    /**
     * Checks that user can not sign up for an event that has not started yet.
     */
    @Test
    public void UserCannotJoinWaitlistForEventThatHasNotStarted() {
        // Create event
        Event event = firebaseManager.createEvent(ownerUid, "Event that has not started yet", makeTimestamp(2026, 11, 1), makeTimestamp(2026, 11, 2), makeTimestamp(2026, 11, 3), "Da House", false,"A party for good people", 10, 100, "", "", new ArrayList<String>());

        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Event that has not started yet")).perform(click());

        // Click on the join button
        onView(withText("Registration Opens Soon")).perform(click());

        // Button should still say "Registration Opens Soon"
        onView(withText("Registration Opens Soon")).check(matches(isDisplayed()));
    }

    /**
     * Checks that user can not see an event where the registration period ended
     */
    @Test
    public void UserCannotJoinWaitlistForEventThatEnded() {
        // Create event
        Event event = firebaseManager.createEvent(ownerUid, "Event that registration ended", makeTimestamp(2025, 11, 1), makeTimestamp(2025, 11, 2), makeTimestamp(2026, 11, 3), "Da House", false,"A party for good people", 10, 100, "", "", new ArrayList<String>());

        // Login the user
        login("tester@gmail.com", "password");

        // See that event isn't here
        onView(withText("Event that registration ended")).check(doesNotExist());
    }
}
