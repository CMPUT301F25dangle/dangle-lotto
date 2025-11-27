package com.example.dangle_lotto;

import java.util.ArrayList;
/**
 * Admin class to generate an admin user that manages events, notifications, pictures, and general
 * users.
 * <p>
 * This class overrides the isAdmin() boolean to be true.
 * </p>
 *
 * @author Mahd Afzal, Annie Ding
 * @version 1.0
 * @since 2025-11-20
 */
public class AdminUser extends User{
    /**
     * Constructs a new User object with basic identifying information.
     *
     * @param uid             Unique Firebase UID of the user.
     * @param name            Display name of the user.
     * @param email           Email address of the user.
     * @param phone           Phone number (nullable).
     * @param photo_id        Profile photo identifier (nullable).
     * @param firebaseManager Reference to the FirebaseManager for database updates.
     */
    public AdminUser(String uid, String name, String username, String email, String phone, String photo_id, FirebaseManager firebaseManager) {
        super(uid, name, username, email, phone, photo_id, firebaseManager);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }



}
