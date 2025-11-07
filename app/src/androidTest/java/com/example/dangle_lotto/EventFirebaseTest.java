package com.example.dangle_lotto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.Checks.notNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class EventFirebaseTest {
    private final FirebaseManager firebaseManager;
    private GeneralUser testUser;

    public EventFirebaseTest() {
        firebaseManager = new FirebaseManager(true);
        testUser = new GeneralUser("FvKUWJ366kM80HcFeM6gqZxGaIPU", "Mahd", "afzalmahd@gmail.com", "", "", firebaseManager, false);
    }

    @Test
    public void testEventCreationandDeletion() throws ExecutionException, InterruptedException {
        Event newEvent = firebaseManager.createEvent(testUser.getUid(), "Big ahh midterm", Timestamp.now(), "Da House", "Calculussy", 10, "");
        Task<DocumentSnapshot> docTask = firebaseManager.getDb().collection("events")
                .document(newEvent.getEid()).get();

        DocumentSnapshot doc = Tasks.await(docTask);
        assertTrue(doc.exists());
        Task<DocumentSnapshot> organizerTrueTask = firebaseManager.getDb().collection("users")
                .document(testUser.getUid()).collection("Organize").document(newEvent.getEid()).get();

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
    public void testGetEvent() throws ExecutionException, InterruptedException {
        Event checkEvent = new Event("POyVEkNrFErffhZYds1b", "FvKUWJ366kM80HcFeM6gqZxGaIPU", "Diddy Party", Timestamp.now(), null, "A party for diddy kong", 69, firebaseManager);
;
        firebaseManager.getEvent("POyVEkNrFErffhZYds1b", new FirebaseCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                assertEquals(result, checkEvent);
            }
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });

    }

}
