package com.matt.shopline.objects;

public class Order {
    String userID;
    String quantity;
    String location;
    String postID;
    long status;

    public Order() {
    }

    public String getUserID() {
        return userID;
    }

    public String getPostID() {
        return postID;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getLocation() {
        return location;
    }

    public long getStatus() {
        return status;
    }
}
