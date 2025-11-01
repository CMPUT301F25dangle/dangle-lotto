package com.example.dangle_lotto;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Event — model for an event
 *
 * This class is a representation for each event. It will contain a list of all registered, chosen, signups and cancelled users.
 * Still need to implement methods for chosen, signups, and cancelled and picture stuff
 *
 * @author Mahd and Fogil
 * @version 1.0
 * @since 2025-11-01
 */
public class Event {
    String name;
    Timestamp datetime;
    String location;
    String description;
    int eventSize;
    final String eid;
    ArrayList<String> registered = new ArrayList<>();

    // implement chosen, signups and cancelled later

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

    public void addRegistered(String uid) {
        registered.add(uid);
    }

    public void deleteRegistered(String uid) {
        registered.remove(uid);
    }

    public ArrayList<String> getRegistered() {
        return registered;
    }

    public int getNumRegistered() {
        return registered.size();
    }

    public boolean isMaxRegistered(){
        return registered.size() >= eventSize;
    }

    /**
     * Choose Lottery winners drawn randomly
     *
     * @return arraylist of user id's who have been chosen for the lottery for the event
     */
    public ArrayList<String> chooseLottoWinners() {
        ArrayList<String> winners = new ArrayList<>();
        if (registered.isEmpty()) {
            System.out.println("No sign-ups yet");
            return winners;
        }
        if (registered.size() <= eventSize) {
            winners = new ArrayList<>(registered);
            return winners;
        }
        ArrayList<String> shuffled = new ArrayList<>(registered);
        Collections.shuffle(shuffled);

        for (int i = 0; i < eventSize; i++) {
            winners.add(shuffled.get(i));
        }

        return winners;
    }


    // implement chosen and sign ups and cancelled later
    // implement logic for displaying pictures
}
