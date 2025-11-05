package com.example.dangle_lotto;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
/**
 * GeneralUser - model for general user (organizer or entrant)
 *
 * This class is a representation for each general user. It contains a boolean to determine if they have organizer permissions or not
 * The admin privileges will go under the Admin subclass
 *
 * @author Mahd
 * @version 1.0
 * @since 2025-11-01
 */
public class GeneralUser extends User{

    private boolean canOrganize;
    public GeneralUser(String uid, String name, String email, String phone, String pid, FirebaseManager firebaseManager, boolean canOrganize) {
        super(uid, name, email, phone, pid, firebaseManager);
        this.canOrganize = canOrganize;
    }



    public boolean canOrganize() {
        return canOrganize;
    }

    public boolean isAdmin(){
        return false;
    }
    // implement chosen and signed up later

}
