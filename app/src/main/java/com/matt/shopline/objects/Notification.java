package com.matt.shopline.objects;

import java.util.Date;

/**
 * @STATE 1=LIKED
 * 2=COMMENTED
 * 3=FOLLOWED
 * 4=ORDERED
 * 5=REPOSTED
 * 6=REFERRED
 * 7=RATED
 */
public class Notification {
    String userID;
    long state;
    Date timestamp;
    String postID;

    public Notification() {
    }

    public String getUserID() {
        return userID;
    }

    public long getState() {
        return state;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getPostID() {
        return postID;
    }
}
