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

    private long selectedDateTimeMillis;
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

        selectedDateTimeMillis = System.currentTimeMillis() + 1000*60*60; // an hour ahead is default time

        // initialize viewmodel
        // initalize image picker
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

        // max entrants toggle
        binding.cbMaxEntrants.setOnClickListener(v -> {
            boolean isEnabled = binding.createEventInputMaxEntrants.isEnabled();
            binding.createEventInputMaxEntrants.setEnabled(!isEnabled);
        });

        // Set default max entrants
        binding.createEventInputMaxEntrants.setText("50");

        // open date and time picker
        binding.createEventInputDate.setOnClickListener(v -> showDateTimePicker());

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
    private void createEvent(String photo_url) {
        try {
            // Validate required fields
            String name = binding.createEventNameInput.getText().toString().trim();
            if (name.isEmpty()) {
                binding.createEventNameInput.setError("Event name is required");
                return;
            }

            String description = binding.createEventInputDescription.getText().toString().trim();

            Timestamp dateTimeStamp = new Timestamp(new Date(selectedDateTimeMillis));
            String location = String.valueOf(binding.cbEnableGeolocation.isChecked());

            if (binding.cbMaxEntrants.isChecked()) {
                maxEntrants = Integer.parseInt(binding.createEventInputMaxEntrants.getText().toString());
            }

            // Create event
            Event event = firebaseManager.createEvent(
                    userViewModel.getUser().getValue().getUid(),
                    name,
                    dateTimeStamp,
                    location,
                    description,
                    Integer.parseInt(binding.createEventSizeInput.getText().toString()),
                    maxEntrants,
                    photo_url,
                    null,
                    selectedCategories
            );

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

                    // 5️⃣ Show dialog ONLY NOW
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
