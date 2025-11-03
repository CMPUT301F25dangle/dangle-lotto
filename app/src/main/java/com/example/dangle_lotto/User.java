package com.example.dangle_lotto;

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
    protected String name;
    protected String email;
    protected String phone;
    protected FirebaseManager firebaseManager;



    public User(String uid, String name, String email, FirebaseManager firebaseManager) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.firebaseManager = firebaseManager;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhone(){
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
        firebaseManager.updateUser(this);
    }

    public void setEmail(String email) {
        this.email = email;
        firebaseManager.updateUser(this);
    }

    public void setPhone(String phone){
        this.phone = phone;
        firebaseManager.updateUser(this);
    }

    public abstract boolean isAdmin();
}
