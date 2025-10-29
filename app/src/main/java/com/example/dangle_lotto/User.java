package com.example.dangle_lotto;

import java.util.ArrayList;

/**
 * User — data model for the user
 *
 * <p>Usage example:
 * <pre>
 *     ExampleClass example = new ExampleClass(context);
 *     example.doSomething();
 * </pre>
 *
 * @author Mahd Afzal
 * @version 1.0
 * @since 2025-10-29
 */

public class User {
    private final String uid;
    private String first_name;
    private String last_name;
    private String email;
    private ArrayList<String> signedUpEvents = new ArrayList<>();
    private FirebaseManager firebaseManager;



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
        firebaseManager.updateUser(uid, first_name, last_name, email);
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
        firebaseManager.updateUser(uid, first_name, last_name, email);
    }

    public void setEmail(String email) {
        this.email = email;
        firebaseManager.updateUser(uid, first_name, last_name, email);
    }

    public void addEvent(String eid) {
        signedUpEvents.add(eid);
    }



}
