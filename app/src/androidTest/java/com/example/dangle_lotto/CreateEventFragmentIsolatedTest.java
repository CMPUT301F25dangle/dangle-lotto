package com.example.dangle_lotto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.dangle_lotto.ui.create_event.CreateEventFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CreateEventFragmentIsolatedTest {

    @BeforeClass
    public static void setupFirebase() {
        // Optional: if your fragment touches Firestore, route to emulator or use mock
        FirebaseApp.initializeApp(
                androidx.test.core.app.ApplicationProvider.getApplicationContext()
        );
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        System.out.println("✅ Firebase emulator ready for isolated fragment tests.");
    }

    @Test
    public void testCreateEventFragmentUI() {
        // Launch the fragment *alone*, no activity or login
        FragmentScenario<CreateEventFragment> scenario =
                FragmentScenario.launchInContainer(CreateEventFragment.class);

        // Interact with views as if it's on screen
        onView(withId(R.id.create_event_name_input))
                .perform(scrollTo(), typeText("Isolated Espresso Test Event"), closeSoftKeyboard());

        onView(withId(R.id.create_event_done))
                .perform(scrollTo(), click());

        // Verify a UI reaction (e.g., a Toast or TextView)
        onView(withText("Event created successfully"))
                .check(matches(isDisplayed()));
    }
}
