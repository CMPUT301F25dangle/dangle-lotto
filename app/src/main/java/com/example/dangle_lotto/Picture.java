package com.example.dangle_lotto;
import java.util.Date;

public class Picture {
    private String pid;
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
