package com.example.dangle_lotto;

public class User {
    private final String uid;
    private String first_name;
    private String last_name;
    private String email;

    public User(String uid, String first_name, String last_name, String email) {
        this.uid = uid;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
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
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
