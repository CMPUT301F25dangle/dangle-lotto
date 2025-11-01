package com.example.dangle_lotto;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;

public class Event {
    String name;
    Timestamp datetime;
    String location;
    String description;
    int eventSize;
    final String eid;
    ArrayList<String> signUps = new ArrayList<>();

    FirebaseManager firebaseManager;

    public Event(String eid, String name, Timestamp datetime, String location, String description, int eventSize, FirebaseManager firebaseManager) {
        this.eid = eid;
        this.name = name;
        this.datetime = datetime;
        this.location = location;
        this.description = description;
        this.eventSize = eventSize;
        this.firebaseManager = firebaseManager;
    }

    // getters and setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        firebaseManager.updateEvent(this);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        firebaseManager.updateEvent(this);
    }

    public Timestamp getDate() {
        return datetime;
    }

    public void setDate(Timestamp datetime) {
        this.datetime = datetime;
        firebaseManager.updateEvent(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        firebaseManager.updateEvent(this);
    }

    public int getEventSize () {
        return eventSize;
    }

    public void setEventSize(int eventSize) {
        this.eventSize = eventSize;
        firebaseManager.updateEvent(this);
    }

    public String getEid () {
        return eid;
    }

    public void addUser(String uid) {
        signUps.add(uid);
    }

    public void deleteUser(String uid) {
        signUps.remove(uid);
    }

    // select chosen attendees from signups list
    public ArrayList<String> chooseLottoWinners() {
        ArrayList<String> winners = new ArrayList<>();
        if (signUps.isEmpty()) {
            System.out.println("No sign-ups yet");
            return winners;
        }
        if (signUps.size() <= eventSize) {
            winners = new ArrayList<>(signUps);
            return winners;
        }
        ArrayList<String> shuffled = new ArrayList<>(signUps);
        Collections.shuffle(shuffled);

        for (int i = 0; i < eventSize; i++) {
            winners.add(shuffled.get(i));
        }

        return winners;
    }

}
