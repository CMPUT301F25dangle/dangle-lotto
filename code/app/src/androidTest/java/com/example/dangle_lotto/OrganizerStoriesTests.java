package com.example.dangle_lotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import android.Manifest;
import android.widget.EditText;

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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerStoriesTests {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    private static FirebaseManager firebaseManager;
    private IdlingResource firebaseIdlingResource;

    private static String ownerUid;
    private static String testerUid;
    private String tester2Uid;

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

        System.out.println("✅ Firebase emulator connected once before all tests.");

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
        firebaseManager.signUp("owner@gmail.com", "password", "Owner User", "Owner User", "1234123123", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ownerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Create tester AFTER owner is created
        firebaseManager.signUp("tester@gmail.com", "password", "Tester User","Tester User", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                testerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);
    }

    @After
    public void tearDown() throws InterruptedException {
        clearFirestore();

        // Unregister idling resource
        IdlingRegistry.getInstance().unregister(firebaseIdlingResource);
    }

    /**
     * Deletes all users and events from the database.
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
     * Logs the user in.
     *
     * @param email String representing the user's email
     * @param password String representing the user's password
     */
    public void login(String email, String password) {
        // Logs the user in
        onView(withId(R.id.etLoginEmail)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.etLoginPassword)).perform(typeText(password), closeSoftKeyboard());
        onView(withText("LOGIN")).perform(click());
    }

    /**
     * Creates a basic event
     */
    public Event createEvent() {
        return firebaseManager.createEvent(ownerUid, "Good Party", Timestamp.now(), Timestamp.now(), Timestamp.now(), "Da House", false, "A party for good people", 10, 100, "", "", new ArrayList<String>());
    }

    /**
     * Manages filling in the date picker for the event.
     *
     * @param date String representing the date in the format mmddyyyy
     */
    public void fillDatePicker(String date) {
        // Switches to manual date mode
        onView(withContentDescription("Switch to text input mode")).perform(click());

        // Enter date (mm/dd/yyyy)
        onView(isAssignableFrom(EditText.class)).perform(replaceText(""));
        onView(isAssignableFrom(EditText.class)).perform(typeText(date));
        onView(withText("OK")).perform(click());

        // Pick date (press ok for auto date)
        onView(withText("OK")).perform(click());
    }

    /**
     * Checks if organizer can make a new event, alongside a qr code that links to the event.
     * <p>
     * US 02.01.01 As an organizer I want to create a new event and generate a unique promotional
     * QR code that links to the event description and event poster in the app.
     */
    @Test
    public void OrganizerCanMakeNewEventAndQRCode() throws InterruptedException {
        // Login the user
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on create event button
        onView(withId(R.id.dashboard_fragment_new_event_button)).perform(click());

        // Event name
        onView(withHint("Dangle Lotto Gathering")).perform(typeText("Good Party"), closeSoftKeyboard());

        // Event size
        onView(withId(R.id.create_event_size_input)).perform(typeText("100"), closeSoftKeyboard());

        // Event registration start date
        onView(withId(R.id.create_event_registration_start_input)).perform(scrollTo(), click());
        fillDatePicker("12122026");

        // Event registration end date
        onView(withId(R.id.create_event_registration_end_input)).perform(scrollTo(), click());
        fillDatePicker("12132026");

        // Event date
        onView(withId(R.id.create_event_date_input)).perform(scrollTo(), click());
        fillDatePicker("12142026");

        // Event description
        onView(withId(R.id.create_event_description_input)).perform(scrollTo(), typeText("A party for good people"), closeSoftKeyboard());

        // Click on done button
        onView(withText("Done")).perform(scrollTo(), click());

        // Let QR code dialogue to appear
        Thread.sleep(3000);

        // Check if qr code is displayed
        onView(withId(R.id.create_event_banner_QR_display)).check(matches(isDisplayed()));

        // Click on done button
        onView(withText("Done")).perform(click());

        // Check if event is displayed on home page
        onView(withText("Good Party")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can set a registration period for an event.
     * <p>
     * US 02.01.04 As an organizer, I want to set a registration period.
     */
    @Test
    public void OrganizerCanSetRegistrationPeriod() throws InterruptedException {
        // Login the user
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on create event button
        onView(withId(R.id.dashboard_fragment_new_event_button)).perform(click());

        // Event name
        onView(withHint("Dangle Lotto Gathering")).perform(typeText("Good Party"), closeSoftKeyboard());

        // Event size
        onView(withId(R.id.create_event_size_input)).perform(typeText("100"), closeSoftKeyboard());

        // Event registration start date
        onView(withId(R.id.create_event_registration_start_input)).perform(scrollTo(), click());
        fillDatePicker("12122026");

        // Event registration end date
        onView(withId(R.id.create_event_registration_end_input)).perform(scrollTo(), click());
        fillDatePicker("12132026");

        // Event date
        onView(withId(R.id.create_event_date_input)).perform(scrollTo(), click());
        fillDatePicker("12142026");

        // Event description
        onView(withId(R.id.create_event_description_input)).perform(scrollTo(), typeText("A party for good people"), closeSoftKeyboard());

        // Click on done button
        onView(withText("Done")).perform(scrollTo(), click());

        // Let QR code dialogue to appear
        Thread.sleep(3000);

        // Check if qr code is displayed
        onView(withId(R.id.create_event_banner_QR_display)).check(matches(isDisplayed()));

        // Click on done button
        onView(withText("Done")).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on EVENT button
        onView(withText("EVENT")).perform(click());

        // Check if registration period is displayed
        onView(withText("Opens: Dec 12, 2026 00:00")).check(matches(isDisplayed()));
        onView(withText("Closes: Dec 13, 2026 00:00")).check(matches(isDisplayed()));
    }

    /**
     * Checks if organizer can view the list of entrants who joined event waiting list
     * <p>
     * US 02.02.01 As an organizer I want to view the list of entrants who joined my event waiting list
     */
    @Test
    public void OrganizerCanViewWaitingList() {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Add the test user to the event register list
        firebaseManager.userAddStatus(testerUid, eventOfInterest.getEid(), "Register");

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on waiting list button
        onView(withText("Entrants")).perform(click());

        // Click on registrants button
        onView(withText("Registrants")).perform(click());

        // Check if user is displayed
        onView(withText("Tester User (tester@gmail.com)")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can view the map of entrants who joined event waiting list
     * <p>
     * US 02.02.02 As an organizer I want to see on a map where entrants joined my event waiting list from.
     * This can't be tested for, we just check if the map option is available
     */
    @Test
    public void OrganizerCanViewMap() {
        // Create event
        createEvent();

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on MAP button
        onView(withText("MAP")).perform(click());
    }

    /**
     * Check if organizer can enable or disable the geolocation requirement.
     * <p>
     * US 02.02.03 As an organizer I want to enable or disable the geolocation requirement for my event.
     */
    @Test
    public void OrganizerCanEnableDisableGeolocation() {
        // Login
        login("owner@gmail.com", "password");

        // Fails test instantly
        fail("Fail immediately");
    }

    /**
     * Check if organizer can limit the number of entrants who can join waiting list.
     * <p>
     * US 02.03.01 As an organizer I want to OPTIONALLY limit the number of entrants who can join my waiting list.
     */
    @Test
    public void OrganizerCanLimitWaitingList() throws InterruptedException {
        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on create event button
        onView(withId(R.id.dashboard_fragment_new_event_button)).perform(click());

        // Fill out event details
        onView(withHint("Dangle Lotto Gathering")).perform(typeText("Good Party"), closeSoftKeyboard());

        // Fill in number of entrants
        onView(withHint("50")).perform(typeText("100"), closeSoftKeyboard());

        // Click on done button
        onView(withText("Done")).perform(click());

        Thread.sleep(3000);

        // Click on done button for dialogue fragment
        onView(withText("Done")).perform(click());

        // Check if event is displayed on home page
        onView(withText("Good Party")).perform(click());

        // Click on EVENT button
        onView(withText("EVENT")).perform(click());

        // Check if number of entrants is displayed
        onView(withText("100")).check(matches(isDisplayed()));
    }

    /**
     * Checks if organizer can upload event poster
     * <p>
     * US 02.04.01 As an organizer I want to upload an event poster to the event details page to provide visual information to entrants.
     */
    @Test
    public void OrganizerCanUploadEventPoster() {
        // Login
        login("owner@gmail.com", "password");

        // Skip test
        assumeTrue(false);
    }

    /**
     * Check if organizer can update event poster
     * <p>
     * US 02.04.02 As an organizer I want to update an event poster to provide visual information to entrants.
     */
    @Test
    public void OrganizerCanUpdateEventPoster() {
        // Login
        login("owner@gmail.com", "password");

        // Skip test
        assumeTrue(false);
    }

    /**
     * Check if organizer can send notifications to chosen entrants
     * <p>
     * US 02.05.01 As an organizer I want to send a notification to chosen entrants to sign up for events.
     */
    @Test
    public void OrganizerSendsNotificationsToChosenEntrants() {
        // Login
        login("owner@gmail.com", "password");

        // Fails test instantly
        fail("Fail immediately");
    }

    /**
     * Check if organizer can randomly choose entrants
     * <p>
     * US 02.05.02 As an organizer I want to set the system to sample a specified number of attendees to register for the event.
     */
    @Test
    public void OrganizerCanRandomlyChooseEntrants() {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Add the test user to the event register list
        firebaseManager.userAddStatus(testerUid, eventOfInterest.getEid(), "Register");

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on ENTRANTS button
        onView(withText("Entrants")).perform(click());

        // Click on CHOOSE button
        onView(withText("CHOOSE")).perform(click());

        // Click on Chosen button
        onView(withText("Chosen")).perform(click());

        // Check if user is displayed
        onView(withText("Tester User (tester@gmail.com)")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can draw replacement user
     * <p>
     * US 02.05.03 As an organizer I want to be able to draw a replacement applicant from the pooling system when a previously selected applicant cancels or rejects the invitation.
     */
    @Test
    public void OrganizerCanDrawReplacementUser() throws InterruptedException {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Create another test user
        firebaseManager.signUp("tester2@gmail.com", "password", "Tester User 2", "Tester User 2", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                tester2Uid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });
        Thread.sleep(1500);

        // Add the test2 user to the event register list
        eventOfInterest.addRegistered(tester2Uid);

        // Add the test user to the event register list
        eventOfInterest.addChosen(testerUid);

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on ENTRANTS button
        onView(withText("Entrants")).perform(click());

        // Click on Chosen button
        onView(withText("Chosen")).perform(click());

        // Click back button
        onView(withId(R.id.organizer_event_details_back_button)).perform(click());

        // Test user declines
        eventOfInterest.deleteChosen(testerUid);

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on ENTRANTS button
        onView(withText("Entrants")).perform(click());

        // Click on CHOOSE button
        onView(withText("CHOOSE")).perform(click());

        // Click on Chosen button
        onView(withText("Chosen")).perform(click());

        // Check if user is displayed
        onView(withText("Tester User 2 (tester2@gmail.com)")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can see all chosen entrants
     * <p>
     * US 02.06.01 As an organizer I want to view a list of all chosen entrants who are invited to apply.
     */
    @Test
    public void OrganizerCanViewAllChosenEntrants() throws InterruptedException {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Create another test user
        firebaseManager.signUp("tester2@gmail.com", "password", "Tester User 2", "Tester User 2", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                tester2Uid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });
        Thread.sleep(1500);

        // Add the test2 user to the event register list
        eventOfInterest.addChosen(tester2Uid);

        // Add the test user to the event register list
        eventOfInterest.addChosen(testerUid);

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on ENTRANTS button
        onView(withText("Entrants")).perform(click());

        // Click on Chosen button
        onView(withText("Chosen")).perform(click());

        // Check if user is displayed
        onView(withText("Tester User 2 (tester2@gmail.com)")).check(matches(isDisplayed()));
        onView(withText("Tester User (tester@gmail.com)")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can see all cancelled entrants
     * <p>
     * US 02.06.02 As an organizer I want to see a list of all the cancelled entrants.
     */
    @Test
    public void OrganizerCanViewAllCancelledEntrants() throws InterruptedException {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Create another test user
        firebaseManager.signUp("tester2@gmail.com", "password", "Tester User 2", "Tester User 2", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                tester2Uid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });
        Thread.sleep(1500);

        // Add the test2 user to the event register list
        eventOfInterest.addCancelled(tester2Uid);

        // Add the test user to the event register list
        eventOfInterest.addCancelled(testerUid);

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on ENTRANTS button
        onView(withText("Entrants")).perform(click());

        // Click on Cancelled button
        onView(withText("Cancelled")).perform(click());

        // Check if user is displayed
        onView(withText("Tester User 2 (tester2@gmail.com)")).check(matches(isDisplayed()));
        onView(withText("Tester User (tester@gmail.com)")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can see all final entrants
     * <p>
     * US 02.06.03 As an organizer I want to see a final list of entrants who enrolled for the event.
     */
    @Test
    public void OrganizerCanViewAllFinalEntrants() throws InterruptedException {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Create another test user
        firebaseManager.signUp("tester2@gmail.com", "password", "Tester User 2", "Tester User 2", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                tester2Uid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });
        Thread.sleep(1500);

        // Add the test2 user to the event register list
        eventOfInterest.addSignUp(tester2Uid);

        // Add the test user to the event register list
        eventOfInterest.addSignUp(testerUid);

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on ENTRANTS button
        onView(withText("Entrants")).perform(click());

        // Click on Signups button
        onView(withText("Signups")).perform(click());

        // Check if user is displayed
        onView(withText("Tester User 2 (tester2@gmail.com)")).check(matches(isDisplayed()));
        onView(withText("Tester User (tester@gmail.com)")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can cancel entrants
     * <p>
     * US 02.06.04 As an organizer I want to cancel entrants that did not sign up for the event
     */
    @Test
    public void OrganizerCanCancelEntrants() throws InterruptedException {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Choose the user
        eventOfInterest.addChosen(testerUid);

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // Click on ENTRANTS button
        onView(withText("Entrants")).perform(click());

        // Click on Chosen button
        onView(withText("Chosen")).perform(click());

        // Click on Remove button
        onView(withText("Remove")).perform(click());

        // Check if user not is displayed
        onView(withText("Tester User (tester@gmail.com)")).check(doesNotExist());

        // Click on cancelled button
        onView(withText("Cancelled")).perform(click());

        // Check if user is displayed
        onView(withText("Tester User (tester@gmail.com)")).check(matches(isDisplayed()));
    }

    /**
     * Check if organizer can export final list of entrants
     * <p>
     * US 02.06.05 As an organizer I want to export a final list of entrants who enrolled for the event in CSV format.
     */
    @Test
    public void OrganizerCanExportFinalList() throws InterruptedException {
        // Create an event to test on
        Event eventOfInterest = createEvent();

        // Create another test user
        firebaseManager.signUp("tester2@gmail.com", "password", "Tester User 2", "Tester User 2", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                tester2Uid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });
        Thread.sleep(1500);

        // Add the test2 user to the event register list
        eventOfInterest.addSignUp(tester2Uid);

        // Add the test user to the event register list
        eventOfInterest.addSignUp(testerUid);

        // Login
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on event
        onView(withText("Good Party")).perform(click());

        // FAIL TEST
        fail("Fail immediately");
    }

    /**
     * Check if organizer can send notifications to all registrants
     * <p>
     * US 02.07.01 As an organizer I want to send notifications to all entrants on the waiting list
     */
    @Test
    public void OrganizerSendsNotificationsToAllRegistrants() {
        // Login
        login("owner@gmail.com", "password");

        // Fails test instantly
        fail("Test not implemented");
    }

    /**
     * Check if organizer can send notifications to all selected entrants
     * <p>
     * US 02.07.02 As an organizer I want to send notifications to all selected entrants
     */
    @Test
    public void OrganizerSendsNotificationsToAllSelectedEntrants() {
        // Login
        login("owner@gmail.com", "password");

        // Fails test instantly
        fail("Test not implemented");
    }

    /**
     * Check if organizer can send notifications to all cancelled entrants
     * <p>
     * US 02.07.03 As an organizer I want to send a notification to all cancelled entrants
     */
    @Test
    public void OrganizerSendsNotificationsToAllCancelledEntrants() {
        // Fails test instantly
        fail("Test not implemented");
    }
}
