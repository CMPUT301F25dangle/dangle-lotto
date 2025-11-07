package com.example.dangle_lotto;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

    public Event(String eid, String organizer_id, String name, Timestamp deadline, String location, String description, String photo_id, int eventSize, ArrayList<String> categories, FirebaseManager firebaseManager) {
        this.eid = eid;
        this.organizer_id = organizer_id;
        this.name = name;
        this.deadline = deadline;
        this.location = location;
        this.description = description;
        this.photo_id = photo_id;
        this.eventSize = eventSize;
        this.categories = categories;
        this.firebaseManager = firebaseManager;
        this.populateList("Register", registered);
        this.populateList("Chosen", chosen);
        this.populateList("SignUps", signUps);
        this.populateList("Cancelled", cancelled);
    }

    private void populateList(String subcollection, ArrayList<String> list){
        firebaseManager.getEventSubcollection(eid, subcollection, new FirebaseCallback<ArrayList<String>>() {
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

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void addCategory(String category) {
        categories.add(category);
        firebaseManager.updateEvent(this);
    }

    public void removeCategory(String category) {
        categories.remove(category);
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


    public Task<Void> addRegistered(String uid) {
        if (registered.contains(uid)){
            throw new IllegalArgumentException("User is already registered");
        }else{
            registered.add(uid);

            // remove from other lists (just to make sure)
            this.deleteSignUp(uid);
            this.deleteChosen(uid);
            this.deleteCancelled(uid);

            return firebaseManager.userAddStatus(uid, eid, "Register");
        }
    }

    public Task<Void> deleteRegistered(String uid) {
        registered.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "Register");
    }

    // FIX WHEN ADDING A REGISTRATION CAP
//    public boolean isMaxRegistered(){
//        return registered.size() >= eventSize;
//    }

    public Task<Void> addChosen(String uid) {
        if (!chosen.contains(uid)) {
            chosen.add(uid);

            // remove from other lists
            this.deleteSignUp(uid);
            this.deleteRegistered(uid);
            this.deleteCancelled(uid);

            return firebaseManager.userAddStatus(uid, eid, "Chosen");
        }else{
            throw new IllegalArgumentException("User is already chosen");
        }

    }

    public Task<Void> deleteChosen(String uid) {
        chosen.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "Register");
    }

    /**
     * Adds a user ID to the list of sign-ups.
     *
     * @param uid the user to add to the sign-ups list
     */
    public Task<Void> addSignUp(String uid) {
        if (!signUps.contains(uid)) {
            signUps.add(uid);

            // remove from other lists
            this.deleteRegistered(uid);
            this.deleteChosen(uid);
            this.deleteCancelled(uid);

            return firebaseManager.userAddStatus(uid, eid, "SignUps");
        }else{
            throw new IllegalArgumentException("User is already signed up");
        }
    }

    public Task<Void> deleteSignUp(String uid) {
        signUps.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "SignUps");
    }

    public Task<Void> addCancelled(String uid) {
        if (!cancelled.contains(uid)) {
            cancelled.add(uid);

            // remove from other lists
            this.deleteRegistered(uid);
            this.deleteChosen(uid);
            this.deleteSignUp(uid);

            return firebaseManager.userAddStatus(uid, eid, "Cancelled");
        } else{
            throw new IllegalArgumentException("User is already cancelled");
        }
    }

    public Task<Void> deleteCancelled(String uid) {
        cancelled.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "Cancelled");
    }

    public Task<Void> deleteFromEvent(String uid) {
        Task<Void> t1 = deleteRegistered(uid);
        Task<Void> t2 = deleteSignUp(uid);
        Task<Void> t3 = deleteChosen(uid);
        Task<Void> t4 = deleteCancelled(uid);
        return Tasks.whenAll(t1, t2, t3, t4);

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
    public void chooseLottoWinners() {
        if (registered.isEmpty()) {
            throw new IllegalArgumentException("Event has no registered users");
        }
        // if all ppl who registered can go (there is less than the max on the event)
        if (registered.size() <= eventSize) {
            for (String user : registered) {
                addChosen(user);
            }
        }else{
            ArrayList<String> shuffled = new ArrayList<>(registered);
            Collections.shuffle(shuffled);
            for (int i = 0; i < eventSize; i++) {
                addChosen(shuffled.get(i));
            }
        }
    }

    // implement logic for displaying pictures
}
