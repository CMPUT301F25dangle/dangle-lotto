package com.example.dangle_lotto;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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


    public User createNewUser(String uid, String name, String phone, String pid, String email, boolean canOrganize){
        Map<String, Object> data = Map.of(
                "Name", name,
                "Email", email,
                "Phone", phone,
                "Picture", pid,
                "CanOrganize", canOrganize
        );

        users.document(uid).set(data);
        return new GeneralUser(uid, name, email, phone, pid,this, canOrganize);
    }


    public void updateUser(User user) {
        users.document(user.getUid()).update(
                "Name", user.getName(),
                "Email", user.getEmail(),
                "Phone", user.getPhone()
        );
    }

    public void deleteUser(String uid) {
        users.document(uid).delete();
    }

    public User getUser(String uid){
        DocumentSnapshot doc = users.document(uid).get().getResult();
        if (doc.exists()) {
            Map<String, Object> data = doc.getData();
            String name = (String) data.get("Name");
            String email = (String) data.get("Email");
            Boolean canOrganize = (Boolean) data.get("CanOrganize");
            String phone = (String) data.get("Phone");
            String pid = (String) data.get("Picture");
            return new GeneralUser(uid, name, email, phone, pid, this, Boolean.TRUE.equals(canOrganize));
        }
        return null;

    }

    public Event createEvent(String oid, String name, Timestamp datetime, String location, String description, int eventSize, String pid){
        String eid = events.document().getId();
        Map<String, Object> data = Map.of(
                "Organizer", oid,
                "Name", name,
                "Date", datetime,
                "Location", location,
                "Description", description,
                "Event Size", eventSize,
                "Picture", pid
        );

        events.document(eid).set(data);
        return new Event(eid, oid, name, datetime, location, description, pid, eventSize, this);
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
            assert data != null;
            String oid = (String) data.get("Organizer");
            String name = (String) data.get("Name");
            Timestamp datetime = (Timestamp) data.get("Date");
            String location = (String) data.get("Location");
            String description = (String) data.get("Description");
            int eventSize = (int) data.get("Event Size");
            String pid = (String) data.get("Picture");
            return new Event(eid, oid, name, datetime, location, description, pid, eventSize, this);
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
