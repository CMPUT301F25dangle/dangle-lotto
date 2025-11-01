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


    public User createNewUser(String uid, String first_name, String last_name, String email, boolean canOrganize){
        Map<String, Object> data = Map.of(
                "First Name", first_name,
                "Last Name", last_name,
                "Email", email,
                "CanOrganize", canOrganize
        );

        users.document(uid).set(data);

        return new GeneralUser(uid, first_name, last_name, email, this, canOrganize);
    }


    public void updateUser(User user) {
        users.document(user.getUid()).update(
                "First Name", user.getFirst_name(),
                "Last Name", user.getLast_name(),
                "Email", user.getEmail()
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
            Boolean canOrganize = (Boolean) data.get("CanOrganize");
            return new GeneralUser(uid, first_name, last_name, email, this, Boolean.TRUE.equals(canOrganize));
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

    public void userRegister(User user, Event event){
        // add register time to user's event document and event's signup document
        Map<String, Object> data = Map.of(
                "SignUpTime", Timestamp.now()
                );
        event.addRegistered(user.getUid());
        users.document(user.getUid()).collection("Registered").document(event.getEid()).set(data);
        events.document(event.getEid()).collection("Registrants").document(user.getUid()).set(data);
    }

    public void userUnregister(User user, Event event) {
        event.deleteRegistered(user.getUid());
        users.document(user.getUid()).collection("Registered").document(event.getEid()).delete();
        events.document(event.getEid()).collection("Registrants").document(user.getUid()).delete();
    }

    public ArrayList<String> getRegisteredEvents(String uid) {
        QuerySnapshot docs = users.document(uid).collection("Registered").get().getResult();
        ArrayList<String> participatedEvents = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            participatedEvents.add(doc.getId());
        }
        return participatedEvents;
    }

    public ArrayList<String> getEventRegistrants(String eid) {
        QuerySnapshot docs = events.document(eid).collection("SignUps").get().getResult();
        ArrayList<String> eventParticipants = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            eventParticipants.add(doc.getId());
        }
        return eventParticipants;
    }

    // implement for chosen and cancelled and stuff
}
