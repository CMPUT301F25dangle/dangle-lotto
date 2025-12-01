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

    /**
     * Returns the timestamp indicating when this notification was received.
     *
     * @return A {@link Timestamp} representing the receipt time.
     */
    public Timestamp getReceiptTime() {
        return receiptTime;
    }
    /**
     * Returns the text content of this notification message.
     *
     * @return The message body as a string.
     */
    public String getMessage() {return message; }

    /**
     * Indicates whether this notification was sent by an administrator account.
     *
     * @return {@code true} if sent by an admin, {@code false} otherwise.
     */
    public Boolean getIsFromAdmin() { return isFromAdmin; }



}
