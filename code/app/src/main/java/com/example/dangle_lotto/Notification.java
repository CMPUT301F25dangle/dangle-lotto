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
    private String status;

    private String eid;

    private String nid;
    private Timestamp receipt_time;

    // An empty constructor for easy firestore loading
    public Notification() {}
    public Notification(String nid, String eid, String status, Timestamp receipt_time) {
        this.eid = eid;
        this.status = status;
        this.nid = nid;
        this.receipt_time = receipt_time;
    }

    public String getEid() {
        return eid;
    }

    public String getNid() {
        return nid;
    }

    public Timestamp getReceiptTime() {
        return receipt_time;
    }

    public String getStatus() { return status; }
}
