package com.example.dangle_lotto.ui.create_event;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentCreateEventBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/**
 * CreateEventFragment - This fragment will be displayed whenever a GeneralUser
 * wants to organize an event.
 *
 * Note: Sometimes the Cancel and Done buttons do not click and I don't know why
 * I will fix that later (i hope)
 *
 * @author Annie Ding
 * @version 1.5
 * @since 2025-11-07
 */
public class CreateEventFragment extends Fragment {
    private FragmentCreateEventBinding binding;
    private FirebaseManager firebaseManager = new FirebaseManager();
    private UserViewModel userViewModel;
    private int maxEntrants;
    private Bitmap qr = null;

    private long selectedDateTimeMillis;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Make sure buttons are on top
        binding.btnCancel.bringToFront();
        binding.btnDone.bringToFront();

        selectedDateTimeMillis = System.currentTimeMillis() + 1000*60*60; // an hour ahead is default time

        // initialize viewmodel and firebase manager
        firebaseManager = new FirebaseManager();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // buttons
        binding.btnCancel.setOnClickListener(v -> {
            goBack();
        });

        binding.btnDone.setOnClickListener(v -> {
            createEvent();
        });

        // max entrants toggle
        binding.cbMaxEntrants.setOnClickListener(v -> {
            boolean isEnabled = binding.createEventInputMaxEntrants.isEnabled();
            binding.createEventInputMaxEntrants.setEnabled(!isEnabled);
        });

        // Set default max entrants
        binding.createEventInputMaxEntrants.setText("50");

        binding.createEventInputDate.setOnClickListener(v -> showDateTimePicker());
//        binding.btnDone.setOnClickListener(v -> {
//            if (TextUtils.isEmpty(binding.createEventNameInput.getText())) {
//                Toast.makeText(getActivity(), "Need a name", Toast.LENGTH_SHORT).show();
//            } else {
//                generateQRCode(binding.createEventNameInput.getText().toString());
//            }
//        });

        return root;
    }

    private void showDateTimePicker() {

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select deadline date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {

            // Convert picked date from UTC → local time zone
            Calendar pickedUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            pickedUtc.setTimeInMillis(selection);

            Calendar localCal = Calendar.getInstance();
            localCal.set(Calendar.YEAR, pickedUtc.get(Calendar.YEAR));
            localCal.set(Calendar.MONTH, pickedUtc.get(Calendar.MONTH));
            localCal.set(Calendar.DAY_OF_MONTH, pickedUtc.get(Calendar.DAY_OF_MONTH));

            // ★ Get current local hour/minute for default time picker
            Calendar now = Calendar.getInstance();
            int defaultHour = now.get(Calendar.HOUR_OF_DAY);
            int defaultMinute = now.get(Calendar.MINUTE);

            // TIME PICKER
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view, hour, minute) -> {

                        // Combine chosen date + chosen time (local timezone)
                        localCal.set(Calendar.HOUR_OF_DAY, hour);
                        localCal.set(Calendar.MINUTE, minute);
                        localCal.set(Calendar.SECOND, 0);
                        localCal.set(Calendar.MILLISECOND, 0);

                        if (localCal.getTimeInMillis() < System.currentTimeMillis()){
                            Toast.makeText(getActivity(), "Invalid time. Time must be in the future",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }else {
                            // Convert local → UTC
                            Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            utcCal.setTimeInMillis(localCal.getTimeInMillis());
                            selectedDateTimeMillis = utcCal.getTimeInMillis();

                            // Display back to user in local time
                            SimpleDateFormat fmt = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault());
                            binding.createEventInputDate.setText(fmt.format(localCal.getTime()));
                        }
                    },
                    defaultHour,   // ★ starting hour
                    defaultMinute, // ★ starting minute
                    false
            );

            timePicker.show();

        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }



    /**
     * Toggles the status of max entrants input to be in accordance with the checkbox
     */
    private void toggleMaxEntrants() {
        boolean isEnabled = binding.createEventInputMaxEntrants.isEnabled();
        binding.createEventInputMaxEntrants.setEnabled(!isEnabled);

        // Optional: Change appearance when disabled
        if (!isEnabled) {
            binding.createEventInputMaxEntrants.setAlpha(1.0f);
        } else {
            binding.createEventInputMaxEntrants.setAlpha(0.5f);
        }
    }

    /**
     * Creates the event and adds it to the database
     */
    private void createEvent() {
        try {
            // Validate required fields
            String name = binding.createEventNameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Log.e("CreateEvent", "Event name is required");
                // Show error to user
                binding.createEventNameInput.setError("Event name is required");
                return;
            }

            String description = binding.createEventInputDescription.getText().toString().trim();

            Timestamp dateTimeStamp;
            try {
                dateTimeStamp = new Timestamp(new Date(selectedDateTimeMillis));
            } catch (Exception e) {
                Log.e("CreateEvent", "Error parsing date: " + e.getMessage(), e);
                return;
            }

            String location = String.valueOf(binding.cbEnableGeolocation.isChecked());

            // Process categories
            ArrayList<String> categories = new ArrayList<>();
            String categoriesText = binding.createEventCategoriesInput.getText().toString().trim();
            if (!categoriesText.isEmpty()) {
                for (String item : categoriesText.split(",")) {
                    String c = item.trim();
                    if (!c.isEmpty()) categories.add(c);
                }
            }

            if ((binding.cbMaxEntrants.isChecked())) {
                maxEntrants = Integer.parseInt(binding.createEventInputMaxEntrants.getText().toString());
            }

            // Create event in Firebase
            firebaseManager.createEvent(
                    userViewModel.getUser().getValue().getUid(),
                    name,
                    dateTimeStamp,
                    location,
                    description,
                    Integer.parseInt(binding.createEventSizeInput.getText().toString()),
                    maxEntrants,
                    "0",
                    categories
            );

            goBack();

        } catch (Exception e) {
            Log.e("CreateEvent", "Error creating event: " + e.getMessage(), e);
        }
    }

    /**
     * Goes back to the previous fragment
     */
    private void goBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }

    /**
     * Creates a QR code
     */
    // The following function was made by chaitanyamunje at
    // https://www.geeksforgeeks.org/android/how-to-generate-qr-code-in-android/
    private void generateQRCode(String text) {
        if (qr == null) {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
//            try {
//                qr = barcodeEncoder.encodeBitmap(text + binding.createEventInputTime.getText().toString(), BarcodeFormat.QR_CODE, 600, 600);
//                openQRDialogue();
//            } catch (WriterException e) {
//                e.printStackTrace();
//            }
        }
    }


    /**
     * Opens the QR code dialogue, setting the bitmap to be the generated one
     *
     */

    private void openQRDialogue(){
        QRDialogueFragment dialog = new QRDialogueFragment();
        dialog.setQr(qr);
        dialog.show(getParentFragmentManager(), "QRDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Only clear binding when fragment is permanently destroyed
        if (isRemoving()) {
            binding = null;
        }
    }
}
