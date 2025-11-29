package com.example.dangle_lotto.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;

import java.util.ArrayList;

/**
 * Class that just contains the selected even for an admin user
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-27
 */
public class AdminViewModel extends ViewModel {
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private final MutableLiveData<ArrayList<Event>> events = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    /**
     * Sets the events that are currently in the Admin Events Fragment
     *
     * @param events Arraylist of events that you want to save in the View Model
     */
    public void setEvents(ArrayList<Event> events) {
        if (events != null) {
            this.events.setValue(events);
        } else {
            this.events.setValue(new ArrayList<>());
        }
    }

    /**
     * Get the events that are saved in the View Model
     *
     * @return Arraylist of events that are currently in the Admin Events Fragment
     */
    public LiveData<ArrayList<Event>> getEvents() {
        return this.events;
    }

    /**
     * sets index of event selected
     * @param event selected event
     */
    public void setSelectedEvent(Event event){selectedEvent.setValue(event);}

    /** Gets event selected
     * @return event
     */
    public LiveData<Event> getSelectedEvent(){return selectedEvent;}
}