package com.example.dangle_lotto;

public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);

    // optional
    default void onComplete(){
        // default is do nothing
    }
}
