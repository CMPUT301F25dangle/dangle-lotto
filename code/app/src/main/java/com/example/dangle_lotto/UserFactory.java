package com.example.dangle_lotto;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

/**
 * UserFactory — generates a User object from a Firestore document.
 * <p>
 * This class serves the factory design pattern. It determines if the user is
 * an admin or not and returns the corresponding object
 * </p>
 *
 * @author Mahd
 * @version 1.0
 * @since 2025-11-26
 */
public class UserFactory {
    /**
     * Generates a User object from a Firestore document.
     *
     * @param doc             Firestore document containing user data.
     * @param firebaseManager Reference to the FirebaseManager for database updates..
     */
    public static User getUser(DocumentSnapshot doc, FirebaseManager firebaseManager){
        Boolean isAdmin = doc.getBoolean("isAdmin");

        String username = doc.getString("Username");
        String name = doc.getString("Name");
        String email = doc.getString("Email");
        String phone = doc.getString("Phone");
        String pid = doc.getString("Picture");
        GeoPoint location = doc.getGeoPoint("Location");
        Log.d("UserFactory", "User loaded: " + username);


        if (isAdmin != null && isAdmin){
            return new AdminUser(doc.getId(), name, username, email, phone, pid, firebaseManager);
        }else{
            Boolean canOrganize = doc.getBoolean("CanOrganize");
            return new GeneralUser(doc.getId(), name, username, email, phone, location, pid,
                    firebaseManager, Boolean.TRUE.equals(canOrganize));
        }

    }
}
