package com.example.dangle_lotto.ui.create_event;

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
    private int maxEntrants = -1;
    private Bitmap qr = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Make sure buttons are on top
        binding.createEventCancel.bringToFront();
        binding.createEventDone.bringToFront();

        // initialize viewmodel and firebase manager
        firebaseManager = new FirebaseManager();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // buttons
        binding.createEventCancel.setOnClickListener(v -> {
            goBack();
        });

        binding.createEventDone.setOnClickListener(v -> {
            createEvent();
        });

        // max entrants toggle
        binding.cbMaxEntrants.setOnClickListener(v -> {
            boolean isEnabled = binding.createEventInputMaxEntrants.isEnabled();
            binding.createEventInputMaxEntrants.setEnabled(!isEnabled);
        });

        // Set default max entrants
        binding.createEventInputMaxEntrants.setText("50");

        // Set default date/time
        Calendar calendar = Calendar.getInstance();
        Date defaultDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        binding.createEventInputDate.setText(dateFormat.format(defaultDate));
        binding.createEventInputTime.setText(timeFormat.format(defaultDate));

        binding.createEventQrGenerate.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.createEventNameInput.getText())) {
                Toast.makeText(getActivity(), "Need a name", Toast.LENGTH_SHORT).show();
            } else {
                generateQRCode(binding.createEventNameInput.getText().toString());
            }
        });

        return root;
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
            // Parse date/time safely
            String date = binding.createEventInputDate.getText().toString().trim();
            String time = binding.createEventInputTime.getText().toString().trim();

            String dateTimeString = date + " " + time;
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.getDefault());
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("America/Denver"));

            Timestamp dateTimeStamp;
            try {
                Date parsedDate = dateTimeFormat.parse(dateTimeString);
                if (parsedDate == null) {
                    Log.e("CreateEvent", "Failed to parse date");
                    return;
                }
                dateTimeStamp = new Timestamp(parsedDate);
            } catch (ParseException e) {
                Log.e("CreateEvent", "Date parsing error: " + e.getMessage());
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
            try {
                Bitmap qr = barcodeEncoder.encodeBitmap(text + binding.createEventInputTime.getText().toString(), BarcodeFormat.QR_CODE, 400, 400);
                printQRCodeToLogcat(qr);
//                openQRDialogue();
            } catch (WriterException e) {
                e.printStackTrace();
            }
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


    // The following is from DeepSeek, "Android studio print a QR code bitmap to logCat"
    private void printQRCodeToLogcat(Bitmap bitmap) {
        // Scale down for logcat readability
        int logcatWidth = 40; // Smaller for logcat
        int scaledWidth = logcatWidth;
        int scaledHeight = (int) (bitmap.getHeight() * ((float) scaledWidth / bitmap.getWidth()));

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== QR CODE ===\n");

        for (int y = 0; y < scaledBitmap.getHeight(); y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < scaledBitmap.getWidth(); x++) {
                int pixel = scaledBitmap.getPixel(x, y);
                boolean isDark = isPixelDark(pixel);
                line.append(isDark ? "##" : "  "); // Use simple characters
            }
            // Log each line separately to avoid truncation
            Log.d("QR_CODE", line.toString());
        }
        Log.d("QR_CODE", "=== END QR CODE ===");
    }

    private boolean isPixelDark(int pixel) {
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance < 128;
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
