package com.example.dangle_lotto;
import java.util.Date;

/**
 * Picture
 *
 * Contains methods for getting picture information
 *
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-01
 */
public class Picture {
    private final String pid;
    private Date dateAdded;
    private String url;

    public Picture(String pid, Date dateAdded, String url) {
        this.pid = pid;
        this.dateAdded = dateAdded;
        this.url = url;
    }

    public String getPid() {
        return pid;
    }

    public Date getDateAdded() {
        return dateAdded;
    }
    public String getUrl() {
        return url;
    }
}
