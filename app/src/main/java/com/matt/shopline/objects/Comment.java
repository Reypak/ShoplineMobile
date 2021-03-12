package com.matt.shopline.objects;

public class Comment {
    String userID;
    String comment;
    long timestamp;

    public Comment() {
    }

    public Comment(String userID, String comment, long timestamp) {
        this.userID = userID;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
