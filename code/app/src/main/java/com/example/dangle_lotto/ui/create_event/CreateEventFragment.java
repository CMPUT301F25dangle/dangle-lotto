package com.example.dangle_lotto.ui.create_event;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentCreateEventBinding;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;

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
 *
 * @author Annie Ding, Mahd Afzal
 * @version 2.0
 * @since 2025-11-23
 */
public class CreateEventFragment extends Fragment {
    private FragmentCreateEventBinding binding;
    private FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private UserViewModel userViewModel;
    private int maxEntrants;

    private Long registrationStartDate = null;
    private Long registrationEndDate = null;
    private Long eventDate = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private Uri selectedUri;

    String[] categoryItems = {
            "Sports", "Music", "Volunteering", "Education",
            "Gaming", "Food", "Tech", "Career", "Community"
    };

    boolean[] selectedItems = new boolean[categoryItems.length];
    ArrayList<String> selectedCategories = new ArrayList<>();

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

//        registrationStartDate = System.currentTimeMillis() + 1000*60*60; // an hour ahead is default time

        // initialize image picker
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    binding.createEventBanner.setImageURI(uri);
                    selectedUri = uri;
                }
            }
        );

        // initialize viewmodel and firebase manager
        firebaseManager = new FirebaseManager();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // buttons
        binding.createEventBannerButton.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        });

        binding.btnCancel.setOnClickListener(v -> {
            goBack();
        });

        // done button will upload banner to
        binding.btnDone.setOnClickListener(v -> {
            if (selectedUri != null) {
                firebaseManager.uploadBannerPic(selectedUri, new FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                                createEvent(result);
                            }

                    @Override
                    public void onFailure(Exception e) {
                                createEvent(null);
                            }
                });
            } else createEvent(null);
        });

        // date picker for registration start date
        binding.createEventRegistrationStartInput.setOnClickListener(v -> {
            showDateTimePicker(utcMillis -> {
                registrationStartDate = utcMillis;

                // check if the dates selected are valid
                if (!validateEventDates()) {
                    registrationStartDate = null;
                    return;
                }

                binding.createEventRegistrationStartInput.setError(null);
                SimpleDateFormat fmt = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault());
                binding.createEventRegistrationStartInput.setText(fmt.format(new Date(utcMillis)));
            }, false);
        });

        // date picker for registration end date
        binding.createEventRegistrationEndInput.setOnClickListener(v -> {
            showDateTimePicker(utcMillis -> {
                registrationEndDate = utcMillis;

                // check if the dates selected are valid
                if (!validateEventDates()) {
                    registrationEndDate = null;
                    return;
                }

                binding.createEventRegistrationEndInput.setError(null);
                SimpleDateFormat fmt = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault());
                binding.createEventRegistrationEndInput.setText(fmt.format(new Date(utcMillis)));
            }, true);
        });

        // date picker for event date
        binding.createEventDateInput.setOnClickListener(v -> {
            showDateTimePicker(utcMillis -> {
                eventDate = utcMillis;

                // check if the dates selected are valid
                if (!validateEventDates()) {
                    eventDate = null;
                    return;
                }

                binding.createEventDateInput.setError(null);
                SimpleDateFormat fmt = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault());
                binding.createEventDateInput.setText(fmt.format(new Date(utcMillis)));
            }, true);
        });

        // open category multiple choice dialogue
        binding.createEventCategoriesInput.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            builder.setTitle("Select Categories");

            builder.setMultiChoiceItems(categoryItems,
                    selectedItems, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            selectedCategories.add(categoryItems[which]);
                        } else {
                            selectedCategories.remove(categoryItems[which]);
                        }
                    });
            builder.setPositiveButton("OK", (dialog, which) -> {
                String categories;
                if (selectedCategories.isEmpty()) categories = "Select Categories";
                else categories = String.join(", ", selectedCategories);
                binding.createEventCategoriesInput.setText(categories);
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        return root;
    }

    /**
     * Shows a date picker dialog.
     *
     * @param onResultUtc Callback to execute when a date is selected.
     * @param enforceFutureDates Whether to enforce future dates.
     */
    private void showDateTimePicker(Consumer<Long> onResultUtc, boolean enforceFutureDates) {
        CalendarConstraints.Builder constraintsBuilder =
                new CalendarConstraints.Builder();

        if (enforceFutureDates) {
            constraintsBuilder.setStart(MaterialDatePicker.todayInUtcMilliseconds());
            constraintsBuilder.setValidator(DateValidatorPointForward.now());
        }

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {

            // Convert picked date from UTC → local
            Calendar pickedUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            pickedUtc.setTimeInMillis(selection);

            Calendar localCal = Calendar.getInstance();
            localCal.set(Calendar.YEAR, pickedUtc.get(Calendar.YEAR));
            localCal.set(Calendar.MONTH, pickedUtc.get(Calendar.MONTH));
            localCal.set(Calendar.DAY_OF_MONTH, pickedUtc.get(Calendar.DAY_OF_MONTH));

            // Default time: current local time
            Calendar now = Calendar.getInstance();
            int defaultHour   = now.get(Calendar.HOUR_OF_DAY);
            int defaultMinute = now.get(Calendar.MINUTE);

            // TIME PICKER
            TimePickerDialog timePicker = new TimePickerDialog(
                    requireContext(),
                    (view, hour, minute) -> {

                        // combine date + picked time
                        localCal.set(Calendar.HOUR_OF_DAY, hour);
                        localCal.set(Calendar.MINUTE, minute);
                        localCal.set(Calendar.SECOND, 0);
                        localCal.set(Calendar.MILLISECOND, 0);

                        long pickedLocalMillis = localCal.getTimeInMillis();

                        if (enforceFutureDates) {
                            if (localCal.getTimeInMillis() < System.currentTimeMillis()) {
                                Toast.makeText(getActivity(), "Invalid time. Time must be in the future",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // Convert local → UTC
                        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        utcCal.setTimeInMillis(pickedLocalMillis);

                        long utcResult = utcCal.getTimeInMillis();

                        // Call user callback
                        onResultUtc.accept(utcResult);
                    },
                    defaultHour,
                    defaultMinute,
                    false
            );

            timePicker.show();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    /**
     * Validates registrationStartDate, registrationEndDate, and eventDate ordering.
     * Returns true if valid; otherwise false and shows appropriate messages.
     */
    private boolean validateEventDates() {
        // check if registration start is before registration end
        if (registrationStartDate != null && registrationEndDate != null) {
            if (registrationStartDate >= registrationEndDate) {
                Toast.makeText(getContext(),
                        "Registration end must be after registration start",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // check if event date is after registration end
        if (registrationEndDate != null && eventDate != null) {
            if (registrationEndDate >= eventDate) {
                Toast.makeText(getContext(),
                        "Event date must be after registration end",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // check if event date is after registration start
        if (registrationStartDate != null && eventDate != null) {
            if (registrationStartDate >= eventDate) {
                Toast.makeText(getContext(),
                        "Event date must be after registration start",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    /**
     * Creates the event and adds it to the database
     *
     * @param photo_url - the url of the banner image
     */
    private void createEvent(String photo_url) {
        try {
            boolean hasError = false;

            // Validate event name
            String eventName = binding.createEventNameInput.getText().toString().trim();
            if (eventName.isEmpty()) {
                binding.createEventNameInput.setError("Event name is required");
                hasError = true;
            }

            // Validate description
            String description = binding.createEventDescriptionInput.getText().toString().trim();
            if (description.isEmpty()) {
                binding.createEventDescriptionInput.setError("Description is required");
                hasError = true;
            }

            // Validate event limit
            String eventLimit = binding.createEventSizeInput.getText().toString().trim();
            if (eventLimit.isEmpty() || Integer.parseInt(eventLimit) <= 0) {
                binding.createEventSizeInput.setError("Event limit is required");
                hasError = true;
            }

            // Validate dates
            if (registrationStartDate == null) {
                binding.createEventRegistrationStartInput.setError("Invalid date");
                hasError = true;
            }

            if (registrationEndDate == null) {
                binding.createEventRegistrationEndInput.setError("Invalid date");
                hasError = true;
            }

            if (eventDate == null) {
                binding.createEventDateInput.setError("Invalid date");
                hasError = true;
            }

            // returns if there is an error
            if (hasError) {
                return;
            }

            // Creating timestamps for the event
            Timestamp registrationStartDateStamp = new Timestamp(new Date(registrationStartDate));
            Timestamp registrationEndDateStamp = new Timestamp(new Date(registrationEndDate));
            Timestamp eventDateTimeStamp = new Timestamp(new Date(eventDate));

            // Checking if geo location is enabled
            String location = "template";
            Boolean locationRequired = binding.cbEnableGeolocation.isChecked();

            // Checking if max entrants is enabled (has text in it)
            String maxEntrantsInput = binding.createEventInputMaxEntrants.getText().toString();
            if (maxEntrantsInput.isEmpty()) {
                maxEntrants = -1;
            } else {
                maxEntrants = Integer.parseInt(maxEntrantsInput);
            }

            // Create event
            Event event = firebaseManager.createEvent(
                    userViewModel.getUser().getValue().getUid(),
                    eventName,
                    registrationStartDateStamp,
                    registrationEndDateStamp,
                    eventDateTimeStamp,
                    location,
                    locationRequired,
                    description,
                    Integer.parseInt(binding.createEventSizeInput.getText().toString()),
                    maxEntrants,
                    photo_url,
                    null,
                    selectedCategories
            );

            // get event id
            String eid = event.getEid();

            // Generate the QR image (LOCALLY, not as a global field)
            Bitmap qrBitmap = generateQRCodeBitmap(eid);
            if (qrBitmap == null) {
                Log.e("CreateEvent", "QR generation failed");
                return;
            }

            // Upload QR
            firebaseManager.uploadQR(qrBitmap, new FirebaseCallback<String>() {
                @Override
                public void onSuccess(String downloadUrl) {
                    // Save QR URL on event
                    event.setQR(downloadUrl);

                    // update list (forces reload)
                    userViewModel.setOrganizedEvents(null);

                    // Show dialog ONLY NOW
                    openQRDialogue(qrBitmap);

                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("CreateEvent", "Error uploading QR: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e("CreateEvent", "Error creating event: " + e.getMessage(), e);
        }
    }

    /**
     * Goes back to the previous fragment
     */
    public void goBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }

    /**
     * Creates a QR code
     *
     * @param text - the text to be encoded
     * @return the bitmap of the QR code
     */
    // The following function was made by chaitanyamunje at
    // https://www.geeksforgeeks.org/android/how-to-generate-qr-code-in-android/
    private Bitmap generateQRCodeBitmap(String text) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 600, 600);

            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Opens the QR code dialogue, setting the bitmap to be the generated one
     *
     * @param qrBitmap - the bitmap of the QR code
     */
    private void openQRDialogue(Bitmap qrBitmap){
        QRDialogueFragment dialog = new QRDialogueFragment();
        dialog.setQr(qrBitmap);
        dialog.show(getChildFragmentManager(), "QRDialog");
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
