package com.matt.shopline.objects;

import java.util.Date;

public class ReviewComment {
    int rating;
    String comment;
    Date timestamp;

    public ReviewComment() {
    }

    public ReviewComment(int rating, String comment, Date timestamp) {
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
