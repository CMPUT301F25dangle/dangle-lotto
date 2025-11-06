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
        this.populateList("Register", registered);
        this.populateList("Chosen", chosen);
        this.populateList("SignUps", signUps);
        this.populateList("Cancelled", cancelled);
    }

    private void populateList(String subcollection, ArrayList<String> list){
        firebaseManager.getEventSubcollection(eid, subcollection, new FirestoreCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                list.clear();
                list.addAll(result);
            }

            @Override
            public void onFailure(Exception e) {
                list.clear();
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

    public ArrayList<String> getRegistered() {
        return registered;
    }

    public int getNumRegistered() {
        return registered.size();
    }

    public ArrayList<String> getChosen() {
        return chosen;
    }

    public ArrayList<String> getSignUps() {
        return signUps;
    }

    public ArrayList<String> getCancelled() {
        return cancelled;
    }


    public void addRegistered(User user) {
        if (registered.contains(user.getUid())){
            throw new IllegalArgumentException("User is already registered");
        }else{
            registered.add(user.getUid());

            // remove from other lists (just to make sure)
            this.deleteSignUp(user);
            this.deleteChosen(user);
            this.deleteCancelled(user);

            firebaseManager.userAddStatus(user, this, "Register");
        }
    }

    public void deleteRegistered(User user) {
        if (!registered.contains(user.getUid())){
            throw new IllegalArgumentException("User is not registered");
        } else {
            registered.remove(user.getUid());
            firebaseManager.userRemoveStatus(user, this, "Register");
        }
    }

    // FIX WHEN ADDING A REGISTRATION CAP
//    public boolean isMaxRegistered(){
//        return registered.size() >= eventSize;
//    }

    public void addChosen(User user) {
        if (!chosen.contains(user.getUid())) {
            chosen.add(user.getUid());

            // remove from other lists
            this.deleteSignUp(user);
            this.deleteRegistered(user);
            this.deleteCancelled(user);

            firebaseManager.userAddStatus(user, this, "Chosen");
        }else{
            throw new IllegalArgumentException("User is already chosen");
        }

    }

    public void deleteChosen(User user) {
        if (!registered.contains(user.getUid())){
            throw new IllegalArgumentException("User is not registered");
        } else {
            registered.remove(user.getUid());
            firebaseManager.userRemoveStatus(user, this, "Register");
        }
    }

    /**
     * Adds a user ID to the list of sign-ups.
     *
     * @param user the user to add to the sign-ups list
     */
    public void addSignUp(User user) {
        if (!signUps.contains(user.getUid())) {
            signUps.add(user.getUid());

            // remove from other lists
            this.deleteRegistered(user);
            this.deleteChosen(user);
            this.deleteCancelled(user);

            firebaseManager.userAddStatus(user, this, "SignUps");
        }else{
            throw new IllegalArgumentException("User is already signed up");
        }
    }

    public void deleteSignUp(User user) {
        if (!signUps.contains(user.getUid())) {
            throw new IllegalArgumentException("User is not signed up");
        } else {
            signUps.remove(user.getUid());
            firebaseManager.userRemoveStatus(user, this, "SignUps");
        }
    }

    public void addCancelled(User user) {
        if (!cancelled.contains(user.getUid())) {
            cancelled.add(user.getUid());

            // remove from other lists
            this.deleteRegistered(user);
            this.deleteChosen(user);
            this.deleteSignUp(user);

            firebaseManager.userAddStatus(user, this, "Cancelled");
        } else{
            throw new IllegalArgumentException("User is already cancelled");
        }
    }

    public void deleteCancelled(User user) {
        if (!cancelled.contains(user.getUid())) {
            throw new IllegalArgumentException("User is not cancelled");
        } else {
            cancelled.remove(user.getUid());
            firebaseManager.userRemoveStatus(user, this, "Cancelled");
        }
    }

    // FINALIZE IMPLEMENTATION LATER - need to save to firebase?
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

    // implement logic for displaying pictures
}
