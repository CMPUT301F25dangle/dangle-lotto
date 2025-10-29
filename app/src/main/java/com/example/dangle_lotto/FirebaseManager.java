package com.example.dangle_lotto;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        return new User(uid, first_name, last_name, email);
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

        return new Event(eid, name, datetime, location, description, eventSize);
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

    }

}
