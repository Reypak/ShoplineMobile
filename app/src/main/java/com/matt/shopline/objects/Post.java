package com.matt.shopline.objects;

public class Post {
    String product;
    String price;
    String description;
    String imageUrl;
    String userID;
    long timestamp;
    String offers;
    String size;
   /* long comments;
    long likes;
    long reposts;*/

    public Post() {
    }

    public String getProduct() {
        return product;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUserID() {
        return userID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOffers() {
        return offers;
    }

    public String getSize() {
        return size;
    }
}
