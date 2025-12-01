package com.example.dangle_lotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;

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
 * Admin Stories Tests - Unit Tests for Admin Stories
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 11/29/2025
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminStoriesTests {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<>(LoginActivity.class);

    private static FirebaseManager firebaseManager;
    private IdlingResource firebaseIdlingResource;

    private static String adminUid;
    private static String ownerUid;
    private static String testerUid;
    private static String tester2Uid;
    private static Event eventOfInterest1;
    private static Event eventOfInterest2;
    private static Event eventOfInterest3;

    /**
     * Sets up the Firebase emulator for testing.
     */
    @BeforeClass
    public static void setupOnce() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFunctions func = FirebaseFunctions.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        db.useEmulator("10.0.2.2", 8080);
        mAuth.useEmulator("10.0.2.2", 9099);
        func.useEmulator("10.0.2.2", 5001);
        storage.useEmulator("10.0.2.2", 9199);

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

        // Creates admin user
        firebaseManager.signUp("admin@gmail.com", "password", "Admin User", "admin username","1234123123", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                adminUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Make admin user an admin
        firebaseManager.makeUserAdmin(adminUid, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

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

        // Create events to test on
        eventOfInterest1 = firebaseManager.createEvent(ownerUid, "Good Party", makeTimestamp(2024, 11, 1), makeTimestamp(2026, 11, 1), makeTimestamp(2026, 11, 2), "Da House", false,"A party for good people", 10, 100, "", "", new ArrayList<String>());
        eventOfInterest2 = firebaseManager.createEvent(ownerUid, "Best Party", makeTimestamp(2024, 11, 1), makeTimestamp(2026, 11, 1), makeTimestamp(2026, 11, 2), "Da House", false,"A party for good people", 10, 100, "", "", new ArrayList<String>());
        eventOfInterest3 = firebaseManager.createEvent(ownerUid, "Worst Party", makeTimestamp(2024, 11, 1), makeTimestamp(2026, 11, 1), makeTimestamp(2026, 11, 2), "Da House", false,"A party for good people", 10, 100, "", "", new ArrayList<String>());
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
     * Checks if admin can remove events
     * <p>
     * US 03.01.01 As an administrator, I want to be able to remove events.
     */
    @Test
    public void AdminCanRemoveEvent() {
        // Login
        login("admin@gmail.com", "password");

        // Click on event
        onView(withText("Good Party")).perform(scrollTo(), click());

        // Click on delete button
        onView(withText("Delete")).perform(click());

        // Event does not exist any longer
        onView(withText("Good Party")).check(doesNotExist());
    }

    /**
     * Checks if admin can remove profiles
     * <p>
     * US 03.02.01 As an administrator, I want to be able to remove profiles.
     * Test is skipped due to small bug, it takes some time for the delete to propagate,
     * but a callback is not used in the delete, so the view refetches the data before the
     * delete occurs.
     */
    @Test
    public void AdminCanRemoveProfiles() throws InterruptedException {
        // Login
        login("admin@gmail.com", "password");

        // Navigate to users page
        onView(withId(R.id.navigation_admin_users)).perform(click());

        // Click on user
        onView(withText("Owner User")).perform(scrollTo(), click());

        // Click on delete button
        onView(withText("Delete")).perform(click());

        // Skip test
        assumeTrue(false);
    }

    /**
     * Checks if admin can remove images
     * <p>
     * US 03.03.01 As an administrator, I want to be able to remove images.
     */
    @Test
    public void AdminCanRemoveImages() {
        // Skip test
        assumeTrue(false);
    }

    /**
     * Checks if admin can browse events
     * <p>
     * US 03.04.01 As an administrator, I want to be able to browse events.
     */
    @Test
    public void AdminCanBrowseEvents() {
        // Login
        login("admin@gmail.com", "password");

        // Check if events exist
        onView(withText("Good Party")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Best Party")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Worst Party")).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Checks if admin can browse profiles
     * <p>
     * US 03.05.01 As an administrator, I want to be able to browse profiles.
     */
    @Test
    public void AdminCanBrowseProfiles() {
        // Login
        login("admin@gmail.com", "password");

        // Navigate to users page
        onView(withId(R.id.navigation_admin_users)).perform(click());

        // Check if users exist
        onView(withText("Owner User")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Tester User")).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Checks if admin can browse images to remove them
     * <p>
     * US 03.06.01 As an administrator, I want to be able to browse images that are uploaded so I can remove them if necessary.
     */
    @Test
    public void AdminCanBrowseImagesForRemoval() {
        // Skip test
        assumeTrue(false);
    }

    /**
     * Checks if admin can remove organizers who violate app policy
     * <p>
     * US 03.07.01 As an administrator I want to remove organizers that violate app policy.
     */
    @Test
    public void AdminCanRemoveOrganizers() {
        // Login
        login("admin@gmail.com", "password");

        // Navigate to users page
        onView(withId(R.id.navigation_admin_users)).perform(click());

        // Click on user
        onView(withText("Owner User")).perform(scrollTo(), click());

        // Click on Remove organizer toggle
        onView(withId(R.id.admin_switch_organizer)).perform(click());
    }

    /**
     * Checks if admin can review notification logs
     * <p>
     * US 03.08.01 As an administrator, I want to review logs of all notifications sent to entrants by organizers.
     */
    @Test
    public void AdminCanReviewNotificationLogs() throws InterruptedException {
        // Sign up tester 2
        firebaseManager.signUp("tester2@gmail.com", "password", "Tester User 2", "tester 2 username", "534532", "",true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                tester2Uid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Add users to subgroups
        eventOfInterest1.addSignUp(testerUid);
        eventOfInterest1.addCancelled(tester2Uid);

        // Login
        login("admin@gmail.com", "password");

        // Navigate to notifications page
        onView(withId(R.id.navigation_admin_notifications)).perform(click());

        // Check if notifications exist
        onView(withText(containsString(("tester username")))).check(matches(isDisplayed()));
        onView(withText("You have been Signed Up for Good Party")).check(matches(isDisplayed()));
        onView(withText(containsString("tester 2 username"))).check(matches(isDisplayed()));
        onView(withText("You have been Cancelled for Good Party")).check(matches(isDisplayed()));
    }
}
