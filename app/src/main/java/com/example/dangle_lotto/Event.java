package com.example.dangle_lotto;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Event {
    String name;
    Timestamp date;
    Timestamp time;
    String location;
    String description;
    int eventSize;
    String eid;
    ArrayList<User> signUps = new ArrayList<>();

    public Event(String name, Timestamp date, Timestamp time, String location, String description, int eventSize) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.eventSize = eventSize;
    }

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

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
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

    public void setEid(String eid) {
        this.eid = eid;
    }

    public void addUser(ArrayList<User> signUps, User newUser) {
        signUps.add(newUser);
    }

    public ArrayList<User> chooseLottoWinners(ArrayList<User> signUps) {
        if (signUps.isEmpty()) {

        }
        if (signUps.size() <= eventSize) {
            return signUps;
        }
        return signUps;
    }

}
