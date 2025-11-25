package com.example.dangle_lotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;

import android.util.Log;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.LoginActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerStoriesTests {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<>(LoginActivity.class);

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

    }

    /**
     *
     */
    @Before
    public void setup() throws InterruptedException {
        // Register idling resource so Espresso waits for Firebase calls
        firebaseIdlingResource = firebaseManager.getIdlingResource();
        IdlingRegistry.getInstance().register(firebaseIdlingResource);

        // Creates owner user
        firebaseManager.signUp("owner@gmail.com", "password", "Owner User", "ownerusername","1234123123", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ownerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Create tester AFTER owner is created
        firebaseManager.signUp("tester@gmail.com", "password", "Tester User", "testerusername", "", "",true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                testerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Create an event to test on
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
        onView(withHint("Email")).perform(typeText(email), closeSoftKeyboard());
        onView(withHint("Password")).perform(typeText(password), closeSoftKeyboard());
        onView(withText("LOGIN")).perform(click());
    }

    /**
     * Creates a basic event
     */
    public Event createEvent() {
        return firebaseManager.createEvent(ownerUid, "Good Party", Timestamp.now(), "Da House", "A party for good people", 10, 100, "", "", new ArrayList<String>());
    }

    /**
     * Checks if organizer can make a new event, alongside a qr code that links to the event.
     * <p>
     * US 02.01.01 As an organizer I want to create a new event and generate a unique promotional
     * QR code that links to the event description and event poster in the app.
     */
    @Test
    public void MakeNewEventAndQRCode() throws InterruptedException {
        // Login the user
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on create event button
        onView(withId(R.id.dashboard_fragment_new_event_button)).perform(click());

        // Fill out event details
        onView(withHint("Dangle Lotto Gathering")).perform(typeText("Good Party"), closeSoftKeyboard());
        onView(withHint("We will be gathering…")).perform(typeText("A party for good people"), closeSoftKeyboard());

        // Click on done button
        onView(withText("Done")).perform(click());

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
    public void SetRegistrationPeriod() throws InterruptedException {
        // Login the user
        login("owner@gmail.com", "password");

        // Navigate to dashboard
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Click on create event button
        onView(withId(R.id.dashboard_fragment_new_event_button)).perform(click());

        // Fill out event details
        onView(withHint("Dangle Lotto Gathering")).perform(typeText("Good Party"), closeSoftKeyboard());

        // Click on done button
        onView(withText("Done")).perform(click());
    }

}
