package com.example.dangle_lotto;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;

public class UserFactory {
    public static User getUser(DocumentSnapshot doc, FirebaseManager firebaseManager){
        Boolean isAdmin = doc.getBoolean("isAdmin");

        String username = doc.getString("Username");
        String name = doc.getString("Name");
        String email = doc.getString("Email");
        String phone = doc.getString("Phone");
        String pid = doc.getString("Picture");
        Log.d("UserFactory", "User loaded: " + username);


        if (isAdmin != null && isAdmin){
            return new AdminUser(doc.getId(), name, username, email, phone, pid, firebaseManager);
        }else{
            Boolean canOrganize = doc.getBoolean("CanOrganize");
            return new GeneralUser(doc.getId(), name, username, email, phone, pid, firebaseManager, Boolean.TRUE.equals(canOrganize));
        }

    }
}
