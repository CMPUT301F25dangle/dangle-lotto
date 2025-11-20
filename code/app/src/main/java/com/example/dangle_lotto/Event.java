package com.example.dangle_lotto;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents an Event within the Dangle Lotto system.
 * <p>
 * Each event tracks participants across four distinct states:
 * Registered, Chosen, SignUps, and Cancelled. The class
 * handles all participant transitions between these states
 * and maintains synchronization with Firebase through
 * {@link FirebaseManager}.
 * </p>
 *
 * <p>State Flow:</p>
 * <pre>
 * Registered → Chosen → (SignUps OR Cancelled)
 * </pre>
 *
 * @author Mahd
 * @version 1.0
 * @since 2025-11-01
 */
public class Event {

    /**
     * Unique identifier for the event.
     */
    final String eid;

    /**
     * Description of the event.
     */
    String description;

    /**
     * Maximum number of people who can attend the event.
     */
    int eventSize;

    /**
     * Optional maximum entrants cap (unused).
     */
    int maxEntrants;

    /**
     * Display name of the event.
     */
    String name;

    /**
     * Timestamp of the event’s deadline or draw date.
     */
    Timestamp deadline;

    /**
     * Location where the event takes place.
     */
    String location;

    /**
     * Unique ID of the event organizer.
     */
    String organizer_id;

    /**
     * ID of the associated event photo (if any).
     */
    String photo_id;

    /**
     * List of category tags the event belongs to.
     */
    ArrayList<String> categories = new ArrayList<>();

    /**
     * List of users currently registered (pre-lottery).
     */
    ArrayList<String> registered = new ArrayList<>();

    /**
     * List of users chosen after the lottery draw.
     */
    ArrayList<String> chosen = new ArrayList<>();

    /**
     * List of users who confirmed attendance after being chosen.
     */
    ArrayList<String> signUps = new ArrayList<>();

    /**
     * List of users who declined after being chosen.
     */
    ArrayList<String> cancelled = new ArrayList<>();

    /**
     * Reference to FirebaseManager for database operations.
     */
    FirebaseManager firebaseManager;

    /**
     * Constructs an Event object and initializes participant lists
     * by asynchronously fetching data from Firebase.
     *
     * @param eid             Unique event identifier.
     * @param organizer_id    Organizer’s user ID.
     * @param name            Event name.
     * @param deadline        Event deadline or draw date.
     * @param location        Location where event occurs.
     * @param description     Description of the event.
     * @param photo_id        Associated photo identifier.
     * @param eventSize       Maximum number of attendees.
     * @param maxEntrants     Optional maximum entrants cap.
     * @param categories      List of category tags.
     * @param firebaseManager Reference to FirebaseManager instance.
     */
    public Event(String eid, String organizer_id, String name, Timestamp deadline, String location,
                 String description, String photo_id, int eventSize, Integer maxEntrants,
                 ArrayList<String> categories, FirebaseManager firebaseManager) {

        this.eid = eid;
        this.organizer_id = organizer_id;
        this.name = name;
        this.deadline = deadline;
        this.location = location;
        this.description = description;
        this.photo_id = photo_id;
        this.eventSize = eventSize;
        this.maxEntrants = maxEntrants;
        this.categories = categories;
        this.firebaseManager = firebaseManager;

        populateList("Register", registered);
        populateList("Chosen", chosen);
        populateList("SignUps", signUps);
        populateList("Cancelled", cancelled);
    }


    /**
     * Populates a participant list from a specific Firebase subcollection.
     *
     * @param subcollection Name of the Firebase subcollection (e.g., "Register", "Chosen").
     * @param list          List to populate with user IDs.
     */
    private void populateList(String subcollection, ArrayList<String> list) {
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

    // ================================================================
    // Getters and Setters
    // ================================================================

    /**
     * @return The unique event ID.
     */
    public String getEid() {
        return eid;
    }

    /**
     * @return The organizer’s user ID.
     */
    public String getOrganizerID() {
        return organizer_id;
    }

    /**
     * @return The event description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the event description and updates Firebase.
     *
     * @param description New description text.
     */
    public void setDescription(String description) {
        this.description = description;
        firebaseManager.updateEvent(this);
    }

    /**
     * @return The event location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the event location and updates Firebase.
     *
     * @param location New location name.
     */
    public void setLocation(String location) {
        this.location = location;
        firebaseManager.updateEvent(this);
    }

    /**
     * @return The event deadline as a Firebase {@link Timestamp}.
     */
    public Timestamp getDate() {
        return deadline;
    }

    /**
     * Sets the event deadline and updates Firebase.
     *
     * @param datetime New deadline timestamp.
     */
    public void setDate(Timestamp datetime) {
        this.deadline = datetime;
        firebaseManager.updateEvent(this);
    }

    /**
     * @return The event’s display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the event’s name and updates Firebase.
     *
     * @param name New event name.
     */
    public void setName(String name) {
        this.name = name;
        firebaseManager.updateEvent(this);
    }

    /**
     * @return The event’s maximum attendee limit.
     */
    public int getEventSize() {
        return eventSize;
    }

    /**
     * Sets the event’s capacity and updates Firebase.
     *
     * @param eventSize New capacity value.
     */
    public void setEventSize(int eventSize) {
        this.eventSize = eventSize;
        firebaseManager.updateEvent(this);
    }

    /**
     * @return The optional maximum entrants cap.
     */

    public int getMaxEntrants() {
        return maxEntrants;
    }

    /**
     * Sets the optional maximum entrants cap and updates Firebase.
     *
     * @param maxEntrants New cap value.
     */
    public void setMaxEntrants(int maxEntrants) {
        this.maxEntrants = maxEntrants;
        firebaseManager.updateEvent(this);
    }

    /**
     * @return A list of category tags for this event.
     */
    public ArrayList<String> getCategories() {
        return categories;
    }

    /**
     * @return List of user IDs currently registered.
     */
    public ArrayList<String> getRegistered() {
        return registered;
    }

    /**
     * @return Total number of registered users.
     */
    public int getNumRegistered() {
        return registered.size();
    }

    /**
     * @return List of user IDs that have been chosen after lottery draw.
     */
    public ArrayList<String> getChosen() {
        return chosen;
    }

    /**
     * @return List of users who confirmed attendance.
     */
    public ArrayList<String> getSignUps() {
        return signUps;
    }

    /**
     * @return List of users who cancelled attendance.
     */
    public ArrayList<String> getCancelled() {
        return cancelled;
    }

    /**
     * @return The associated photo identifier.
     */
    public String getPhotoID() {
        return photo_id;
    }

    /**
     * Sets the event’s photo ID.
     *
     * @param photo_id ID of the uploaded event photo.
     */
    public void setPhotoID(String photo_id) {
        this.photo_id = photo_id;
    }

    // ================================================================
    // Category Management
    // ================================================================

    /**
     * Adds a new category tag and updates Firebase.
     *
     * @param category The category name to add.
     */
    public void addCategory(String category) {
        categories.add(category);
        firebaseManager.updateEvent(this);
    }

    /**
     * Removes a category tag and updates Firebase.
     *
     * @param category The category name to remove.
     */
    public void removeCategory(String category) {
        categories.remove(category);
        firebaseManager.updateEvent(this);
    }

    // ================================================================
    // Participant Management
    // ================================================================

    /**
     * Adds a user to the registered list and removes them from other lists.
     *
     * @param uid The user ID to register.
     * @return A Firebase {@link Task} representing the asynchronous operation.
     * @throws IllegalArgumentException if the user is already registered.
     */
    public Task<Void> addRegistered(String uid) {
        if (registered.contains(uid)) {
            throw new IllegalArgumentException("User is already registered");
        }
        registered.add(uid);
        this.deleteSignUp(uid);
        this.deleteChosen(uid);
        this.deleteCancelled(uid);
        return firebaseManager.userAddStatus(uid, eid, "Register");
    }

    /**
     * Removes a user from the registered list.
     *
     * @param uid The user ID to remove.
     * @return A Firebase {@link Task} representing the operation.
     */
    public Task<Void> deleteRegistered(String uid) {
        registered.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "Register");
    }

    /**
     * Adds a user to the chosen list and removes them from other lists.
     *
     * @param uid The user ID to add.
     * @return A Firebase {@link Task} representing the operation.
     * @throws IllegalArgumentException if the user is already chosen.
     */
    public Task<Void> addChosen(String uid) {
        if (chosen.contains(uid)) {
            throw new IllegalArgumentException("User is already chosen");
        }
        chosen.add(uid);
        this.deleteRegistered(uid);
        this.deleteSignUp(uid);
        this.deleteCancelled(uid);
        return firebaseManager.userAddStatus(uid, eid, "Chosen");
    }

    /**
     * Removes a user from the chosen list.
     *
     * @param uid The user ID to remove.
     * @return A Firebase {@link Task} representing the operation.
     */
    public Task<Void> deleteChosen(String uid) {
        chosen.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "Chosen");
    }

    /**
     * Adds a user to the sign-up list (attendance confirmed).
     *
     * @param uid The user ID to add.
     * @return A Firebase {@link Task} representing the operation.
     * @throws IllegalArgumentException if the user already signed up.
     */
    public Task<Void> addSignUp(String uid) {
        if (signUps.contains(uid)) {
            throw new IllegalArgumentException("User is already signed up");
        }
        signUps.add(uid);
        this.deleteRegistered(uid);
        this.deleteChosen(uid);
        this.deleteCancelled(uid);
        return firebaseManager.userAddStatus(uid, eid, "SignUps");
    }

    /**
     * Removes a user from the sign-up list.
     *
     * @param uid The user ID to remove.
     * @return A Firebase {@link Task} representing the operation.
     */
    public Task<Void> deleteSignUp(String uid) {
        signUps.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "SignUps");
    }

    /**
     * Adds a user to the cancelled list and removes them from other lists.
     *
     * @param uid The user ID to add.
     * @return A Firebase {@link Task} representing the operation.
     * @throws IllegalArgumentException if the user already cancelled.
     */
    public Task<Void> addCancelled(String uid) {
        if (cancelled.contains(uid)) {
            throw new IllegalArgumentException("User is already cancelled");
        }
        cancelled.add(uid);
        this.deleteRegistered(uid);
        this.deleteChosen(uid);
        this.deleteSignUp(uid);
        return firebaseManager.userAddStatus(uid, eid, "Cancelled");
    }

    /**
     * Removes a user from the cancelled list.
     *
     * @param uid The user ID to remove.
     * @return A Firebase {@link Task} representing the operation.
     */
    public Task<Void> deleteCancelled(String uid) {
        cancelled.remove(uid);
        return firebaseManager.userRemoveStatus(uid, eid, "Cancelled");
    }

    /**
     * Removes a user from all event-related lists.
     *
     * @param uid The user ID to remove.
     * @return A Firebase {@link Task} representing the operation.
     */
    public Task<Void> deleteFromEvent(String uid) {
        Task<Void> t1 = deleteRegistered(uid);
        Task<Void> t2 = deleteSignUp(uid);
        Task<Void> t3 = deleteChosen(uid);
        Task<Void> t4 = deleteCancelled(uid);
        return Tasks.whenAll(t1, t2, t3, t4);
    }

    // ================================================================
    // Lottery Logic
    // ================================================================

    /**
     * Randomly selects event participants from the registered list.
     * <p>
     * Users are moved from Registered → Chosen. If fewer users are registered
     * than the event’s capacity, all are chosen. Otherwise, a random subset
     * is selected up to the event size.
     * </p>
     *
     * @throws IllegalArgumentException if there are no registered users.
     */
    public void chooseLottoWinners() {
        if (registered.isEmpty()) {
            throw new IllegalArgumentException("Event has no registered users");
        }
//        if (!chosen.isEmpty()) return; causes problems

        if (registered.size() <= eventSize) {
            ArrayList<String> temp = new ArrayList<>(registered);
            for (String user : temp) {
                addChosen(user);
            }
        } else if (chosen.size() + signUps.size() < eventSize) {
            ArrayList<String> shuffled = new ArrayList<>(registered);
            Collections.shuffle(shuffled);
            for (int i = 0; i < eventSize - chosen.size() - signUps.size(); i++) {
                addChosen(shuffled.get(i));
            }
        } else {
            ArrayList<String> shuffled = new ArrayList<>(registered);
            Collections.shuffle(shuffled);
            for (int i = 0; i < eventSize; i++) {
                addChosen(shuffled.get(i));
            }
        }
    }

    public void cancelAllChosen() {
        ArrayList<String> temp = new ArrayList<>(chosen);
        for (String user : temp) {
            addCancelled(user);
        }
    }
}
