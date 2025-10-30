package com.example.dangle_lotto.ui;

import com.example.dangle_lotto.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Event {
    private String title;
    private String description;
    private int limit;
    private Date deadline;

    private ArrayList<User> entrants;
    private ArrayList<User> chosen;
}
