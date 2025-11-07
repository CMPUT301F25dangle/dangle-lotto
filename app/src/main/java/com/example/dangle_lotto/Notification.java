package com.example.dangle_lotto;

/**
 * Notification
 *
 * Contains methods to get notification information.
 *
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-01
 */
public class Notification {
    private String name;
    private String status;

    public Notification(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getStatus() { return status; }
}
