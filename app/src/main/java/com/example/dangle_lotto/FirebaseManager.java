package com.example.dangle_lotto;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class FirebaseManager {
    private FirebaseFirestore db;
    private CollectionReference users;
    private CollectionReference events;
    FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        users = db.collection("users");
        events = db.collection("events");
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void addUser(String uid, String first_name, String last_name, String email){
        Map<String, String> data = Map.of(
                "First Name", first_name,
                "Last Name", last_name,
                "Email", email
        );

        users.document(uid).set(data);;


    }

}
