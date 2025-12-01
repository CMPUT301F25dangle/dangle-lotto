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
    /** @return ID of the user who sent the notification. */
    public String getSenderId() {
        return senderId;
    }

    /** @return ID of the user who receives the notification. */
    public String getReceiverId() {
        return receiverId;
    }

    /** @return Notification ID. */
    public String getNid() {
        return nid;
    }

    /** @return Time the notification was received. */
    public Timestamp getReceiptTime() {
        return receiptTime;
    }

    /** @return Notification message text. */
    public String getMessage() {
        return message;
    }

    /** @return true if sent by an admin, false otherwise. */
    public Boolean getIsFromAdmin() {
        return isFromAdmin;
    }



}
