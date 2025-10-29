package com.example.dangle_lotto;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Time;
import java.util.Map;

public class FirebaseManager {
    private final FirebaseFirestore db;
    private final CollectionReference users;
    private final CollectionReference events;
    FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        users = db.collection("users");
        events = db.collection("events");
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public User createUser(String uid, String first_name, String last_name, String email){
        Map<String, String> data = Map.of(
                "First Name", first_name,
                "Last Name", last_name,
                "Email", email
        );

        users.document(uid).set(data);

        return new User(uid, first_name, last_name, email, this);
    }

    public void updateUser(String uid, String first_name, String last_name, String email) {
        users.document(uid).update(
                "First Name", first_name,
                "Last Name", last_name,
                "Email", email
        );
    }

    public void deleteUser(User user) {
        users.document(user.getUid()).delete();
    }

    public Event createEvent(String name, Timestamp datetime, String location, String description, int eventSize){
        String eid = events.document().getId();
        Map<String, Object> data = Map.of(
                "Name", name,
                "Date", datetime,
                "Location", location,
                "Description", description,
                "Event Size", eventSize
        );

        events.document(eid).set(data);

        return new Event(eid, name, datetime, location, description, eventSize, this);
    }

    public void updateEvent(Event event) {
        events.document(event.getEid()).update(
                "Name", event.getName(),
                "Date", event.getDate(),
                "Location", event.getLocation(),
                "Description", event.getDescription()
        );
    }

    public void deleteEvent(Event event) {
        events.document(event.getEid()).delete();
    }

    public void UserSignUp(User user, Event event){
        // add signup time to user's event document and event's signup document
        Map<String, Object> data = Map.of(
                "SignUpTime", Timestamp.now()
                );
        user.addEvent(event.getEid());
        event.addUser(user.getUid());
        users.document(user.getUid()).collection("Events").document(event.getEid()).set(data);
        events.document(event.getEid()).collection("SignUps").document(user.getUid()).set(data);
    }

    public void UserUnSignUp(User user, Event event) {
        users.document(user.getUid()).collection("Events").document(event.getEid()).delete();
        events.document(event.getEid()).collection("SignUps").document(user.getUid()).delete();
    }

    public void getParticipatedEvents(User user) {
        users.document(user.getUid()).collection("Events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String eid = doc.getId();
                        user.addEvent(eid);
                        Log.d("Firestore", "Event ID: " + eid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting documents", e);
                });

    }
}
