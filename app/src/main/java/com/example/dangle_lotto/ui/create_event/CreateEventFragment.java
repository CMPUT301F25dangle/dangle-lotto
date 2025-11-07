package com.example.dangle_lotto.ui.create_event;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentCreateEventBinding;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * CreateEventFragment - This fragment will be displayed whenever a GeneralUser
 * wants to organize an event.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-05
 */
public class CreateEventFragment extends Fragment {
    private FragmentCreateEventBinding binding;
    private Button cancel;
    private Button done;
    private EditText nameInput;
    private Button uploadBanner;
    private CheckBox enableMaxEntrants;
    private EditText maxEntrantsInput;
    private EditText dateInput;
    private EditText timeInput;
    private EditText descriptionInput;
    private CheckBox enableGeolocation;
    private FirebaseManager firebaseManager = new FirebaseManager();
    private UserViewModel userViewModel;
    private int maxEntrants;
    private String name;
    private String description;
    private String dateTime;
    private String location;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initialize viewmodel and firebase manager
        firebaseManager = new FirebaseManager();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // initialize ui inputs
        cancel = root.findViewById(R.id.create_event_cancel);
        cancel.setOnClickListener(view ->
            goBack()
        );

        done = root.findViewById(R.id.create_event_done);
        done.setOnClickListener(view -> createEvent());


        nameInput = root.findViewById(R.id.create_event_name_input);
        uploadBanner = root.findViewById(R.id.create_event_banner_button);
        enableMaxEntrants = root.findViewById(R.id.cb_max_entrants);
        enableMaxEntrants.setOnClickListener(view -> setEnableMaxEntrants());

        maxEntrantsInput = root.findViewById(R.id.create_event_input_maxEntrants);
        maxEntrantsInput.setText("50");
        maxEntrants = -1;

        // initialize time to default values
        Calendar calendar = Calendar.getInstance();
        Date defaultDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        dateInput = root.findViewById(R.id.create_event_input_date);
        dateInput.setText(dateFormat.format(defaultDate));
        timeInput = root.findViewById(R.id.create_event_input_time);
        timeInput.setText(timeFormat.format(defaultDate));

        descriptionInput = root.findViewById(R.id.create_event_input_description);
        enableGeolocation = root.findViewById(R.id.cb_enable_geolocation);


        return root;
    }

    /**
     * Toggles the status of max entrants input to be in accordance to the checkbox
     */
    private void setEnableMaxEntrants() {
        boolean isEditable = maxEntrantsInput.isFocusable();

        maxEntrantsInput.setFocusable(!isEditable);
        maxEntrantsInput.setFocusableInTouchMode(!isEditable);
        maxEntrantsInput.setClickable(!isEditable);
        maxEntrantsInput.setLongClickable(!isEditable);
        maxEntrantsInput.setCursorVisible(!isEditable);
    }

    /**
     * Creates the event and adds it to the database
     */
    private void createEvent() {
        // Get actual text from EditText
        String name = nameInput.getText().toString();
        String description = descriptionInput.getText().toString();
        if (enableMaxEntrants.isChecked()) {
            maxEntrants = Integer.parseInt(maxEntrantsInput.getText().toString());
        }
        String date = dateInput.getText().toString();
        String time = timeInput.getText().toString();

        // Combine date and time
        String dateTimeString = date + " " + time;

        // Adjust pattern if your time does not include seconds
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("America/Denver"));

        Timestamp dateTimeStamp;
        try {
            Date dateDate = dateTimeFormat.parse(dateTimeString);
            dateTimeStamp = new Timestamp(dateDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return; // Stop if parsing fails
        }

        String location = String.valueOf(enableGeolocation.isChecked());

        firebaseManager.createEvent(
                userViewModel.getUser().getValue().getUid(),
                name,
                dateTimeStamp,
                location,
                description,
                maxEntrants,
                "0"
        );

        goBack();
    }


    public void goBack(){
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
