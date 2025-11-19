package com.example.dangle_lotto;

/**
 * Notification
 * <p>
 * Contains methods to get notification information.
 *
 *
 * @author Annie Ding, Prem Elango
 * @version 1.2
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
