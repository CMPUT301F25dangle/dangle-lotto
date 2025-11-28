package com.example.dangle_lotto.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;

/**
 * Class that just contains the selected even for an admin user
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-27
 */
public class AdminViewModel extends ViewModel {
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();


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
