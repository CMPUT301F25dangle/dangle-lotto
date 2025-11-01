package com.example.dangle_lotto;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Map;

public class FirebaseManager {
    private final FirebaseFirestore db;
    private final CollectionReference users;
    private final CollectionReference events;
    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        users = db.collection("users");
        events = db.collection("events");
    }


    public User createNewUser(String uid, String first_name, String last_name, String email){
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

    public void deleteUser(String uid) {
        users.document(uid).delete();
    }

    public User getUser(String uid){
        DocumentSnapshot doc = users.document(uid).get().getResult();
        if (doc.exists()) {
            Map<String, Object> data = doc.getData();
            String first_name = (String) data.get("First Name");
            String last_name = (String) data.get("Last Name");
            String email = (String) data.get("Email");
            return new User(uid, first_name, last_name, email, this);
        }
        return null;

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
                "Description", event.getDescription(),
                "Event Size", event.getEventSize()
        );
    }

    public void deleteEvent(String eid) {
        events.document(eid).delete();
    }

    public Event getEvent(String eid){
        DocumentSnapshot doc = events.document(eid).get().getResult();
        if (doc.exists()) {
            Map<String, Object> data = doc.getData();
            String name = (String) data.get("Name");
            Timestamp datetime = (Timestamp) data.get("Date");
            String location = (String) data.get("Location");
            String description = (String) data.get("Description");
            int eventSize = (int) data.get("Event Size");
            return new Event(eid, name, datetime, location, description, eventSize, this);
        }
        return null;
    }

    public void userSignUp(User user, Event event){
        // add signup time to user's event document and event's signup document
        Map<String, Object> data = Map.of(
                "SignUpTime", Timestamp.now()
                );
        user.addEvent(event.getEid());
        event.addUser(user.getUid());
        users.document(user.getUid()).collection("Events").document(event.getEid()).set(data);
        events.document(event.getEid()).collection("SignUps").document(user.getUid()).set(data);
    }

    public void userUnSignUp(User user, Event event) {
        user.deleteEvent(event.getEid());
        event.deleteUser(user.getUid());
        users.document(user.getUid()).collection("Events").document(event.getEid()).delete();
        events.document(event.getEid()).collection("SignUps").document(user.getUid()).delete();
    }

    public ArrayList<String> getParticipatedEvents(String uid) {
        QuerySnapshot docs = users.document(uid).collection("Events").get().getResult();
        ArrayList<String> participatedEvents = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            participatedEvents.add(doc.getId());
        }
        return participatedEvents;
    }

    public ArrayList<String> getEventParticipants(String eid) {
        QuerySnapshot docs = events.document(eid).collection("SignUps").get().getResult();
        ArrayList<String> eventParticipants = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            eventParticipants.add(doc.getId());
        }
        return eventParticipants;
    }
}
