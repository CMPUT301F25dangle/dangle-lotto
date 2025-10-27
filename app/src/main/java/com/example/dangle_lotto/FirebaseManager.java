package com.example.dangle_lotto;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
    private FirebaseFirestore db;

    FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }


}
