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
    ArrayList<User> signUps = new ArrayList<>();

    public Event(String eid, String name, Timestamp datetime, String location, String description, int eventSize) {
        this.eid = eid;
        this.name = name;
        this.datetime = datetime;
        this.location = location;
        this.description = description;
        this.eventSize = eventSize;
    }

    // getters and setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getDate() {
        return datetime;
    }

    public void setDate(Timestamp datetime) {
        this.datetime = datetime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEventSize () {
        return eventSize;
    }

    public void setEventSize(int eventSize) {
        this.eventSize = eventSize;
    }

    public String getEid () {
        return eid;
    }

    public void addUser(ArrayList<User> signUps, User newUser) {
        signUps.add(newUser);
    }

    // select chosen attendees from signups list
    public ArrayList<User> chooseLottoWinners() {
        ArrayList<User> winners = new ArrayList<>();
        if (signUps.isEmpty()) {
            System.out.println("No sign-ups yet");
            return winners;
        }
        if (signUps.size() <= eventSize) {
            winners = new ArrayList<>(signUps);
            return winners;
        }
        ArrayList<User> shuffled = new ArrayList<>(signUps);
        Collections.shuffle(shuffled);

        for (int i = 0; i < eventSize; i++) {
            winners.add(shuffled.get(i));
        }

        return winners;
    }

}
