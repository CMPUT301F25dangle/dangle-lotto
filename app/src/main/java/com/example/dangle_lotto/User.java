package com.example.dangle_lotto;

import java.util.ArrayList;

/**
 * User — abstract class for user data model. is the superclass of GeneralUser and Admin
 *
 * Contains methods for getting and setting some personal user data.
 * Also contains one abstract function definition to check if the user is an admin or not
 *
 *
 * @author Mahd Afzal
 * @version 1.0
 * @since 2025-10-29
 */

public abstract class User {
    protected final String uid;
    protected String first_name;
    protected String last_name;
    protected String email;
    protected FirebaseManager firebaseManager;



    public User(String uid, String first_name, String last_name, String email, FirebaseManager firebaseManager) {
        this.uid = uid;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.firebaseManager = firebaseManager;
    }

    public String getUid() {
        return uid;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getFullNames() {
        return first_name + " " + last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
        firebaseManager.updateUser(this);
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
        firebaseManager.updateUser(this);
    }

    public void setEmail(String email) {
        this.email = email;
        firebaseManager.updateUser(this);
    }

    public abstract boolean isAdmin();
}
