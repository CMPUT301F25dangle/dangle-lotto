package com.example.dangle_lotto;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * GeneralUser — model for a general application user (entrant or organizer).
 * <p>
 * This class represents a regular user in the system who may or may not have
 * permissions to organize events. It extends the abstract {@link User} class
 * and provides functionality to retrieve user-specific event information such as
 * registered, chosen, signed-up, or cancelled events.
 * </p>
 *
 * <p>
 * Admin-specific users should extend the {@code Admin} subclass instead, which
 * includes higher-level management privileges.
 * </p>
 *
 * @author Mahd
 * @version 1.0
 * @since 2025-11-01
 */
public class GeneralUser extends User {

    /** Indicates whether this user has permissions to organize events. */
    private boolean canOrganize;

    /**
     * Constructs a new GeneralUser object with the given attributes.
     *
     * @param uid             Unique Firebase user ID.
     * @param name            User’s display name.
     * @param username            User's username.
     * @param email           User’s email address.
     * @param phone           User’s phone number (nullable).
     * @param pid             Profile photo identifier (nullable).
     * @param firebaseManager Reference to the {@link FirebaseManager} for database operations.
     * @param canOrganize     Whether the user has organizer privileges.
     */
    public GeneralUser(String uid, String name, String username, String email, String phone,
                       String pid, FirebaseManager firebaseManager, boolean canOrganize) {
        super(uid, name, username, email, phone, pid, firebaseManager);
        this.canOrganize = canOrganize;
    }

    // ============================================================
    // User Management
    // ============================================================

    /**
     * Deletes this user and all associated data from Firebase.
     * <p>
     * This method calls {@link FirebaseManager#deleteUser(String)} and
     * removes all references to this user, including event registrations
     * and organized events.
     * </p>
     */
    public void delete() {
        firebaseManager.deleteUser(uid);
    }

    // ============================================================
    // Event Retrieval Methods
    // ============================================================

    /**
     * Retrieves a list of event IDs for events where this user is registered (waiting list).
     *
     * @param callback Callback to handle the resulting list of event IDs or an error.
     */
    public void registeredEvents(FirebaseCallback<ArrayList<String>> callback) {
        firebaseManager.getUserSubcollection(this.uid, "Registered", new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Retrieves a list of event IDs for events where this user has been chosen through the lottery.
     *
     * @param callback Callback to handle the resulting list of event IDs or an error.
     */
    public void chosenEvents(FirebaseCallback<ArrayList<String>> callback) {
        firebaseManager.getUserSubcollection(this.uid, "Chosen", new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Retrieves a list of event IDs for events this user has signed up to attend
     * after being chosen from the lottery.
     *
     * @param callback Callback to handle the resulting list of event IDs or an error.
     */
    public void signedUpEvents(FirebaseCallback<ArrayList<String>> callback) {
        firebaseManager.getUserSubcollection(this.uid, "SignUps", new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Retrieves a list of event IDs for events this user has cancelled or declined
     * after being chosen from the lottery.
     *
     * @param callback Callback to handle the resulting list of event IDs or an error.
     */
    public void cancelledEvents(FirebaseCallback<ArrayList<String>> callback) {
        firebaseManager.getUserSubcollection(this.uid, "Cancelled", new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // ============================================================
    // Privilege Methods
    // ============================================================

    /**
     * Checks whether this user has the privilege to organize events.
     *
     * @return {@code true} if the user can organize events, otherwise {@code false}.
     */
    public boolean canOrganize() {
        return canOrganize;
    }

    /**
     * Checks if this user is an admin.
     * <p>
     * For GeneralUser, this always returns {@code false}.
     * Admin users should override this in a subclass.
     * </p>
     *
     * @return {@code false}, since GeneralUser is not an admin.
     */
    @Override
    public boolean isAdmin() {
        return false;
    }
}
