package com.example.dangle_lotto;

public class Event {
    private final String title;
    private final int imageResId;

    public Event(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}

