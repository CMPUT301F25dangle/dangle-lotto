package com.example.dangle_lotto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.internal.Checks.notNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EventFirebaseTest {
    private final FirebaseManager firebaseManager;
    private final GeneralUser testOrganizer;
    private final GeneralUser testRegistrant;

    public EventFirebaseTest() {
        firebaseManager = FirebaseManager.getInstance();
        testOrganizer = new GeneralUser("FvKUWJ366kM80HcFeM6gqZxGaIPU", "Mahd", "afzalmahd@gmail.com", "", "", firebaseManager, true);
        testRegistrant = new GeneralUser("RPkgAupyndxOZNRTAlad4wQRE2A6", "Tirth", "qbin57@gmail.com", "", "", firebaseManager, false);
    }

    public Event createTemplateEvent(){
        return firebaseManager.createEvent(testOrganizer.getUid(), "Big ahh midterm", Timestamp.now(), "Da House", "Calculussy", 10, 0, "", new ArrayList<>());
    }

    @BeforeClass
    public static void setupOnce() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        db.useEmulator("10.0.2.2", 8080);
        mAuth.useEmulator("10.0.2.2", 9099);

        System.out.println("✅ Firebase emulator connected once before all tests.");
    }

    @Test
    public void testEventCreationandDeletion() throws ExecutionException, InterruptedException {
        Event newEvent = createTemplateEvent();
        Task<DocumentSnapshot> docTask = firebaseManager.getDb().collection("events")
                .document(newEvent.getEid()).get();

        DocumentSnapshot doc = Tasks.await(docTask);
        assertTrue(doc.exists());
        Task<DocumentSnapshot> organizerTrueTask = firebaseManager.getDb().collection("users")
                .document(testOrganizer.getUid()).collection("Organize").document(newEvent.getEid()).get();

        DocumentSnapshot organizerTrue = Tasks.await(organizerTrueTask);
        assertTrue(organizerTrue.exists());

        Task<Void> deleteTask = firebaseManager.deleteEvent(newEvent.getEid());
        Tasks.await(deleteTask);

        Task<DocumentSnapshot> deleteTaskCheck = firebaseManager.getDb().collection("events")
                .document(newEvent.getEid()).get();

        DocumentSnapshot deleteTaskCheckDoc = Tasks.await(deleteTaskCheck);
        assertFalse(deleteTaskCheckDoc.exists());
    }

    @Test
    public void testGetEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.NOVEMBER, 5, 0, 0, 0);
        Date date = cal.getTime();

        Event expected = new Event(
                "POyVEkNrFErffhZYds1b",
                "FvKUWJ366kM80HcFeM6gqZxGaIPU",
                "Diddy Party",
                new Timestamp(date),
                null,
                "A party for diddy kong",
                null,
                69,
                -1,
                new ArrayList<>(),
                firebaseManager
        );

        firebaseManager.getEvent("POyVEkNrFErffhZYds1b", new FirebaseCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                // Compare important fields
                assertEquals(expected.getEid(), result.getEid());
                assertEquals(expected.getName(), result.getName());
                assertEquals(expected.getDescription(), result.getDescription());
                assertEquals(expected.getEventSize(), result.getEventSize());
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getEvent failed: " + e.getMessage());
                latch.countDown();
            }
        });

        // Wait (up to 5 seconds) for callback to finish
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Callback did not complete in time", completed);
    }

    @Test
    public void testRegisterUser() throws ExecutionException, InterruptedException {
        Event newEvent = createTemplateEvent();
        newEvent.addRegistered(testRegistrant.getUid());
        assertTrue(newEvent.getRegistered().contains(testRegistrant.getUid()));

        Task<DocumentSnapshot> checkUserInEventTask = firebaseManager.getDb().collection("events")
                .document(newEvent.getEid()).collection("Register").document(testRegistrant.getUid()).get();
        Tasks.await(checkUserInEventTask);
        assertTrue(checkUserInEventTask.getResult().exists());

        Task<DocumentSnapshot> checkEventInUserTask = firebaseManager.getDb().collection("users").document(testRegistrant.getUid())
                .collection("Register").document(newEvent.getEid()).get();
        Tasks.await(checkEventInUserTask);
        assertTrue(checkEventInUserTask.getResult().exists());

        firebaseManager.deleteEvent(newEvent.getEid());

    }

    @Test
    public void testChooseUser() throws ExecutionException, InterruptedException {
        Event newEvent = createTemplateEvent();
        Tasks.await(newEvent.addRegistered(testRegistrant.getUid()));
        Tasks.await(newEvent.addChosen(testRegistrant.getUid()));
        assertTrue(newEvent.getChosen().contains(testRegistrant.getUid()));

        Task<DocumentSnapshot> checkUserInEventChosenTask = firebaseManager.getDb().collection("events")
                .document(newEvent.getEid()).collection("Chosen").document(testRegistrant.getUid()).get();
        Task<DocumentSnapshot> checkUserNotInEventRegisteredTask = firebaseManager.getDb().collection("events")
                .document(newEvent.getEid()).collection("Register").document(testRegistrant.getUid()).get();
        Task<DocumentSnapshot> checkEventInUserChosenTask = firebaseManager.getDb().collection("users")
                .document(testRegistrant.getUid()).collection("Chosen").document(newEvent.getEid()).get();
        Task<DocumentSnapshot> checkEventNotInUserRegistered = firebaseManager.getDb().collection("users")
                .document(testRegistrant.getUid()).collection("Register").document(newEvent.getEid()).get();


        // Combine both tasks into one
        Task<List<DocumentSnapshot>> combinedTask = Tasks.whenAllSuccess(
                checkUserInEventChosenTask,
                checkUserNotInEventRegisteredTask,
                checkEventInUserChosenTask,
                checkEventNotInUserRegistered
        );

        // Wait for both to finish
        List<DocumentSnapshot> results = Tasks.await(combinedTask);

        // Retrieve them in order
        DocumentSnapshot userInEventChosen = results.get(0);
        DocumentSnapshot userNotInEventRegistered = results.get(1);
        DocumentSnapshot eventInUserChosen = results.get(2);
        DocumentSnapshot eventNotInUserRegistered = results.get(3);


        // Now assert safely after both completed
        assertTrue(userInEventChosen.exists());
        assertFalse(userNotInEventRegistered.exists());
        assertTrue(eventInUserChosen.exists());
        assertFalse(eventNotInUserRegistered.exists());

        Tasks.await(firebaseManager.deleteEvent(newEvent.getEid()));

    }


}
