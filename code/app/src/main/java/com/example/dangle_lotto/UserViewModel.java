package com.example.dangle_lotto;

import android.util.Log;

import androidx.lifecycle.LiveData;
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
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private final MutableLiveData<GeneralUser> user = new MutableLiveData<GeneralUser>();
    private final MutableLiveData<ArrayList<Event>> homeEvents = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Event>> yourEvents = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Event>> organizedEvents = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedOrganizedEvent = new MutableLiveData<>();

    /**
     * Sets the user object
     *
     * @param user user object
     */
    public void setUser(GeneralUser user) {
        this.user.postValue(user);
    }

    /**
     * Gets the user object that is currently in the View Model
     *
     * @return user object
     */
    public LiveData<GeneralUser> getUser() {
        return user;
    }

    public void loadUser(String uid) {
        // Only fetch if the user hasn't been loaded yet to avoid unnecessary calls
        if (user.getValue() != null) {
            return;
        }

        firebaseManager.getUser(uid, new FirebaseCallback<GeneralUser>() {
            @Override
            public void onSuccess(GeneralUser result) {
                Log.d("UserViewModel", "User loaded: " + result.getUsername());
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
     * Sets the events that are currently in the Home Fragment
     *
     * @param events Arraylist of events that you want to save in the View Model
     */
    public void setHomeEvents(ArrayList<Event> events) {
        if (events != null) {
            homeEvents.setValue(events);
        } else {
            homeEvents.setValue(new ArrayList<>());
        }
    }

    /**
     * Get the events that are saved in the View Model
     *
     * @return Arraylist of events that are currently in the Home Fragment
     */
    public LiveData<ArrayList<Event>> getHomeEvents() {
        return homeEvents;
    }

    /**
     * Sets the index of the event that is currently selected in the Home Fragment
     *
     * @param event Index of selected event
     */
    public void setSelectedEvent(Event event) {
        selectedEvent.setValue(event);
    }

    /**
     * Gets the index of the event that is currently selected in the Home Fragment
     *
     * @return Index
     */
    public LiveData<Event> getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * Sets the events that are currently in the Your Events Fragment
     *
     * @param events Arraylist of events that you want to save in the View Model
     */
    public void setYourEvents(ArrayList<Event> events) {
        if (events != null) {
            yourEvents.setValue(events);
        } else {
            yourEvents.setValue(new ArrayList<>());
        }
    }

    /**
     * Gets yourEvents that are currently in the View Model
     *
     * @return Arraylist of events that the user has signed up for in some way
     */
    public LiveData<ArrayList<Event>> getYourEvents() {
        return yourEvents;
    }

    /**
     * Sets the events that are currently in the Organized Fragment
     *
     * @param events Arraylist of events that you want to save in the View
     */
    public void setOrganizedEvents(ArrayList<Event> events) {
        if (events != null) {
            organizedEvents.setValue(events);
        } else {
            organizedEvents.setValue(new ArrayList<>());
        }
    }

    /**
     * Gets organizedEvents that are currently in the View Model
     *
     * @return Arraylist of events that the user has organized
     */
    public LiveData<ArrayList<Event>> getOrganizedEvents() {
        return organizedEvents;
    }

    /**
     * Sets event that is currently selected in the Dashboard Fragment
     *
     * @param event Event that is currently selected
     */
    public void setSelectedOrganizedEvent(Event event) {
        selectedOrganizedEvent.setValue(event);
    }

    /**
     * Gets event that is currently selected in the Dashboard Fragment
     *
     * @return Event that is currently selected
     */
    public LiveData<Event> getSelectedOrganizedEvent() {
        return selectedOrganizedEvent;
    }
}
