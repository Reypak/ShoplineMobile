package com.matt.shopline.objects;

public class Order {
    String userID;
    String quantity;
    String location;
    String postID;

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
}
