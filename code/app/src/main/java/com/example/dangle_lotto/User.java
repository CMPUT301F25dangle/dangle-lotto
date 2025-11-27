package com.example.dangle_lotto;

import com.google.firebase.firestore.Exclude;

/**
 * Abstract base class representing a generic user within the Dangle Lotto system.
 * <p>
 * This class defines the core attributes and behaviors shared by all user types,
 * such as GeneralUser, Organizer, or Admin. It also provides Firestore
 * synchronization for user updates through {@link FirebaseManager}.
 * </p>
 *
 * <p>
 * Subclasses are responsible for defining user-specific permissions
 * and capabilities, such as event organization or administrative actions.
 * </p>
 *
 * @author Mahd
 * @version 1.0
 * @since 2025-11-01
 */
public abstract class User {

    /** Unique identifier for the user (matches Firebase Auth UID). */
    protected final String uid;

    /** Full name of the user. */
    protected String name;
    /** Username of the user. */
    protected String username;

    /** User's email address. */
    protected String email;

    /** Optional phone number associated with the user. */
    protected String phone;

    /** Optional photo identifier for the user’s profile picture. */
    protected String photo_id;

    /**
     * Reference to the {@link FirebaseManager} instance.
     * <p>
     * Annotated with {@link Exclude} to prevent storing this field in Firestore.
     * </p>
     */
    @Exclude
    protected FirebaseManager firebaseManager;

    /**
     * Constructs a new User object with basic identifying information.
     *
     * @param uid             Unique Firebase UID of the user.
     * @param name            Display name of the user.
     * @param username        Username of the user.
     * @param email           Email address of the user.
     * @param phone           Phone number (nullable).
     * @param photo_id        Profile photo identifier (nullable).
     * @param firebaseManager Reference to the FirebaseManager for database updates.
     */

    public User(String uid, String name, String username, String email, String phone, String photo_id, FirebaseManager firebaseManager) {
        this.uid = uid;
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.photo_id = photo_id;
        this.firebaseManager = firebaseManager;
    }

    // ============================================================
    // Getters
    // ============================================================

    /**
     * @return Unique Firebase user ID.
     */
    public String getUid() {
        return uid;
    }

    /**
     * @return User’s full name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return User’s full name.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return User’s phone number (nullable).
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return User’s email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return User’s profile photo identifier.
     */
    public String getPhotoID() {
        return photo_id;
    }

    // ============================================================
    // Setters (auto-sync with Firestore)
    // ============================================================

    /**
     * Updates the user's name locally and in Firestore.
     *
     * @param name New display name.
     */
    public void setName(String name) {
        this.name = name;
        firebaseManager.updateUser(this);
    }

    /**
     * Updates the user's name locally and in Firestore.
     *
     * @param username New username.
     */
    public void setUsername(String username) {
        this.username = username;
        firebaseManager.updateUser(this);

    }
    /**
     * Updates the user's email locally and in Firestore.
     *
     * @param email New email address.
     */
    public void setEmail(String email) {
        this.email = email;
        firebaseManager.updateUser(this);
    }

    /**
     * Updates the user's phone number locally and in Firestore.
     *
     * @param phone New phone number.
     */
    public void setPhone(String phone) {
        this.phone = phone;
        firebaseManager.updateUser(this);
    }

    /**
     * Updates the user's profile photo ID locally and in Firestore.
     *
     * @param photo_id New photo identifier.
     */
    public void setPhotoID(String photo_id) {
        this.photo_id = photo_id;
        firebaseManager.updateUser(this);
    }

    // ============================================================
    // Abstract Behavior
    // ============================================================

    /**
     * Checks whether the current user has administrative privileges.
     *
     * @return {@code true} if the user is an admin, otherwise {@code false}.
     */
    public abstract boolean isAdmin();
}
