package com.example.dangle_lotto;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * Class will contain all user information that needs to exist between fragments
 * <p>
 * Contains methods to get user information and update it.
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-11-05
 */
public class UserViewModel extends ViewModel {
    private final FirebaseManager firebaseManager = new FirebaseManager();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Event>> yourEvents = new MutableLiveData<>();
    private boolean yourEventsLoaded = false;
    private final MutableLiveData<ArrayList<Event>> organizedEvents = new MutableLiveData<>();;
    private boolean organizedEventsLoaded = false;

    /**
     * Sets the user object
     *
     * @param user user object
     */
    public void setUser(User user) {
        this.user.postValue(user);
    }

    /**
     * Gets the user object that is currently in the View Model
     *
     * @return user object
     */
    public MutableLiveData<User> getUser() {
        return user;
    }

    public void loadUser(String uid) {
        // Only fetch if the user hasn't been loaded yet to avoid unnecessary calls
        if (user.getValue() != null) {
            return;
        }

        firebaseManager.getUser(uid, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User result) {
                Log.d("UserViewModel", "User loaded: " + result.getName());
                user.setValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("UserViewModel", "User failed to load");
                user.setValue(null);
            }
        });
    }

    /**
     * Gets yourEvents that are currently in the View Model
     *
     * @return Arraylist of events that the user has signed up for in some way
     */
    public MutableLiveData<ArrayList<Event>> getYourEvents() {
        return yourEvents;
    }

    /**
     * Fetches users yourEvents data from firebase if it hasn't already been fetched.
     * <p>
     * Sets boolean to false, so that this can only occur once in an instance of the app running.
     */
    public void loadYourEvents() {
        if (!yourEventsLoaded) {
            // fetch yourEvents from firebase and if successful then use
            // yourEvents.postValue(some data) to update yourEvents in the View Model
            // and set yourEventsLoaded to true
        }
    }

    public void addYourEvent(Event event) {
        ArrayList<Event> currentList = getYourEvents().getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(event);
        getYourEvents().setValue(currentList);
    }

    /**
     * Gets organizedEvents that are currently in the View Model
     *
     * @return Arraylist of events that the user has organized
     */
    public MutableLiveData<ArrayList<Event>> getOrganizedEvents() {
        return organizedEvents;
    }

    /**
     * Fetches users organizedEvents data from firebase if it hasn't already been fetched.
     * <p>
     * Sets boolean to false, so that this can only occur once in an instance of the app running.
     */
    public void loadOrganizedEvents() {
        if (!organizedEventsLoaded) {
            // fetch organizedEvents from firebase and if successful then use
            // organizedEvents.postValue(some data) to update organizedEvents in the View Model
            // and set organizedEventsLoaded to true
        }
    }

}
