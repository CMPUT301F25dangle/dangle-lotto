package com.example.dangle_lotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
public class UserStoriesTests {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<>(LoginActivity.class);

    private static FirebaseManager firebaseManager;
    private IdlingResource firebaseIdlingResource;

    private static String ownerUid;
    private static String testerUid;
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
        firebaseManager.signUp("owner@gmail.com", "password", "Owner User", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ownerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);


        // Create tester AFTER owner is created
        firebaseManager.signUp("tester@gmail.com", "password", "Tester User", "", "", true, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                testerUid = result;
            }

            @Override
            public void onFailure(Exception e) { }
        });

        Thread.sleep(1500);

        // Create an event to test on
        eventOfInterest = firebaseManager.createEvent(ownerUid, "Good Party", Timestamp.now(), "Da House", "A party for good people", 10, 100, "", new ArrayList<String>());

        Thread.sleep(500);

    }

    @After
    public void tearDown() throws InterruptedException {
        clearFirestore();

        // Unregister idling resource
        IdlingRegistry.getInstance().unregister(firebaseIdlingResource);
    }

    /**
     * Deletes all users and events from the database.
     *
     * @return A Firebase {@link Task} representing the operation.
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

    public void login(String email, String password) {
        // Logs the user in
        onView(withHint("Email")).perform(typeText(email), closeSoftKeyboard());
        onView(withHint("Password")).perform(typeText(password), closeSoftKeyboard());
        onView(withText("LOGIN")).perform(ViewActions.click());
    }

    /**
     * Joins the waiting list for an event. We call it registering.
     * <p>
     * US 01.01.01 As an entrant, I want to join the waiting list for a specific event
     * This test works because the buttons appearance only changes if the callback is successful for registering.
     */
    @Test
    public void JoinWaitingList() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(ViewActions.click());

        // Click on the join button
        onView(withText("Register for Lottery")).perform(ViewActions.click());

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
    public void LeaveWaitingList() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(ViewActions.click());

        // Click on the join button
        onView(withText("Register for Lottery")).perform(ViewActions.click());

        // Click on the join button
        onView(withText("Withdraw Registration")).perform(ViewActions.click());

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
     * Checks if user can accept an event invitation.
     * <p>
     * US 01.05.02 As an entrant I want to be able to accept the invitation to register/sign up when chosen to participate in an event.
     */
    @Test
    public void AcceptEventInvitation() {
        // Add the user to the event chosen list
        firebaseManager.userAddStatus(testerUid, eventOfInterest.getEid(), "Chosen");

        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(ViewActions.click());

        // Click on the join button
        onView(withText("You’ve Been Chosen!")).perform(ViewActions.click());

        // Click on the attend button to accept
        onView(withText("Attend")).perform(ViewActions.click());

        // Check if button says "Attending"
        onView(withText("Attending")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can decline an event invitation.
     * <p>
     * US 01.05.03 As an entrant I want to be able to decline an invitation when chosen to participate in an event.
     */
    @Test
    public void DeclineEventInvitation() {
        // Add the user to the event chosen list
        firebaseManager.userAddStatus(testerUid, eventOfInterest.getEid(), "Chosen");

        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(ViewActions.click());

        // Click on the join button
        onView(withText("You’ve Been Chosen!")).perform(ViewActions.click());

        // Click on the decline button to not accept
        onView(withText("Decline")).perform(ViewActions.click());

        // Check if button says "Cancelled"
        onView(withText("Cancelled")).check(matches(isDisplayed()));
    }

    /**
     * Checks if user can see how many entrants are on the waiting list for an event.
     * <p>
     * US 01.05.04 As an entrant, I want to know how many total entrants are on the waiting list for an event.
     */
    @Test
    public void WaitingListSize() {
        // Login the user
        login("tester@gmail.com", "password");

        // Click on the event
        onView(withText("Good Party")).perform(ViewActions.click());

        // Check number of registrants
        onView(withText("Spots Remaining: 100/100")).check(matches(isDisplayed()));

        // Click on the join button
        onView(withText("Register for Lottery")).perform(ViewActions.click());

        // Check number of registrants
        onView(withText("Spots Remaining: 99/100")).check(matches(isDisplayed()));
    }
}
