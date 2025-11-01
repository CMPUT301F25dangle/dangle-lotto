package com.example.dangle_lotto;

import java.util.Date;

public class Notification {
    private String nid;
    private String message;
    private String sender;
    private String recipient;
    private Date date;

    public Notification(String nid, String message, String sender, String recipient, Date date) {
        this.nid = nid;
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
        this.date = date;
    }

    public String getNid() {
        return nid;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public Date getDate() {
        return date;
    }
}
