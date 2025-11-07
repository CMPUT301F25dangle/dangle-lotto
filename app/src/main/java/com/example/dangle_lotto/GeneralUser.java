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

    public void delete() {
        firebaseManager.deleteUser(uid);
    }

    public void registeredEvents(FirebaseCallback<ArrayList<String>> callback){
        firebaseManager.getUserSubcollection(this.uid, "Register", new FirebaseCallback<ArrayList<String>>(){
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void chosenEvents(FirebaseCallback<ArrayList<String>> callback){
        firebaseManager.getUserSubcollection(this.uid, "Chosen", new FirebaseCallback<ArrayList<String>>(){
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
   }

    public void notChosenEvents(FirebaseCallback<ArrayList<String>> callback){
        firebaseManager.getUserSubcollection(this.uid, "Not Chosen", new FirebaseCallback<ArrayList<String>>(){
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void signedUpEvents(FirebaseCallback<ArrayList<String>> callback){
        firebaseManager.getUserSubcollection(this.uid, "SignUps", new FirebaseCallback<ArrayList<String>>(){
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void cancelledEvents(FirebaseCallback<ArrayList<String>> callback){
        firebaseManager.getUserSubcollection(this.uid, "Cancelled", new FirebaseCallback<ArrayList<String>>(){
            @Override
            public void onSuccess(ArrayList<String> result) {
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public boolean canOrganize() {
        return canOrganize;
    }

    public boolean isAdmin(){
        return false;
    }
    // implement chosen and signed up later

}
