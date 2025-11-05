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
    final String eid;
    String description;
    int eventSize;
    String name;
    Timestamp deadline;
    String location;
    String organizer_id;
    String photo_id;
    ArrayList<String> categories = new ArrayList<>();
    ArrayList<String> registered = new ArrayList<>();
    ArrayList<String> chosen = new ArrayList<>();
    ArrayList<String> signUps = new ArrayList<>();
    ArrayList<String> cancelled = new ArrayList<>();


    // implement chosen, signups and cancelled later

    FirebaseManager firebaseManager;

    public Event(String eid, String organizer_id, String name, Timestamp deadline, String location, String description, String photo_id, int eventSize, FirebaseManager firebaseManager) {
        this.eid = eid;
        this.organizer_id = organizer_id;
        this.name = name;
        this.deadline = deadline;
        this.location = location;
        this.description = description;
        this.photo_id = photo_id;
        this.eventSize = eventSize;
        this.firebaseManager = firebaseManager;
        this.populateSignUps();
        this.populateRegistrants();
    }

    private void populateSignUps(){
        firebaseManager.getEventSubcollection(eid, "SignUps", new FirestoreCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                signUps = result;
            }

            @Override
            public void onFailure(Exception e) {
                signUps = null;
            }
        });
    }
    private void populateRegistrants(){
        firebaseManager.getEventSubcollection(eid, "Registrants", new FirestoreCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                registered = result;
            }

            @Override
            public void onFailure(Exception e) {
                signUps = null;
            }

        });
    }
    public String getEid () {
        return eid;
    }

    public String getOrganizerID() {
        return organizer_id;
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
        return deadline;
    }

    public void setDate(Timestamp datetime) {
        this.deadline = datetime;
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

    public void addRegistered(String uid) {
        if (registered.contains(uid)){
            throw new IllegalArgumentException("User is already registered");
        }else{
            registered.add(uid);

        }
    }

    public void deleteRegistered(String uid) {
        if (!registered.contains(uid)){
            throw new IllegalArgumentException("User is not registered");
        } else {
            registered.remove(uid);
        }
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

    // FINALIZE IMPLEMENTATION LATER
//    public ArrayList<String> getCategories() {
//        return categories;
//    }
//
//    public void addCategory(String category) {
//        categories.add(category);
//    }
//
//    public void removeCategory(String category) {
//        categories.remove(category);
//    }

    public String getPhotoID(){
        return photo_id;
    }

    public void setPhotoID(String photo_id) {
        this.photo_id = photo_id;
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
    public ArrayList<String> getChosen() {
        return chosen;
    }

    /**
     * Adds a user ID to the list of chosen participants.
     *
     * @param uid the user ID to add to the chosen list
     */
    public void addChosen(String uid) {
        if (!chosen.contains(uid)) {
            chosen.add(uid);
            firebaseManager.updateEvent(this);
        }
    }

    public ArrayList<String> getSignUps() {
        return signUps;
    }

    /**
     * Adds a user ID to the list of sign-ups.
     *
     * @param uid the user ID to add to the sign-ups list
     */
    public void addSignUp(String uid) {
        if (!signUps.contains(uid)) {
            signUps.add(uid);
        }else{
            throw new IllegalArgumentException("User is already signed up");
        }
    }

    public void cancelSignUp(String uid) {
        if (!signUps.contains(uid)) {
            throw new IllegalArgumentException("User is not signed up");
        } else {
            signUps.remove(uid);
        }
    }

    public ArrayList<String> getCancelled() {
        return cancelled;
    }

    /**
     * Adds a user ID to the cancelled list and removes them
     * from registered, chosen, or sign-up lists if present.
     *
     * @param uid the user ID to add to the cancelled list
     */
    public void addCancelled(String uid) {
        if (!cancelled.contains(uid)) {
            cancelled.add(uid);
            registered.remove(uid);
            chosen.remove(uid);
            signUps.remove(uid);
            firebaseManager.updateEvent(this);
        }
    }

    // implement logic for displaying pictures
}
