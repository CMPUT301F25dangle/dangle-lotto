package com.example.dangle_lotto;

import androidx.test.services.events.TimeStamp;

import com.google.firebase.Timestamp;

/**
 * Notification
 * <p>
 * Contains methods to get notification information.
 *
 *
 * @author Annie Ding, Prem Elango, Mahd Afzal
 * @version 2.0
 * @since 2025-11-28
 */
public class Notification {
    private String senderId;
    private String receiverId;
    private String message;
    private boolean isFromAdmin;
    private String nid;
    private Timestamp receiptTime;


    // An empty constructor for easy firestore loading
    public Notification() {}

    public Notification(String senderId, String receiverId, String nid, Timestamp receiptTime, String message, Boolean isFromAdmin) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.isFromAdmin = isFromAdmin;
        this.nid = nid;
        this.message = message;
        this.receiptTime = receiptTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }


    public String getNid() {
        return nid;
    }

    public Timestamp getReceiptTime() {
        return receiptTime;
    }

    public String getMessage() {return message; }

    public Boolean getIsFromAdmin() { return isFromAdmin; }



}
