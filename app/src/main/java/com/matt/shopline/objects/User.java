package com.matt.shopline.objects;

public class User {
    String userID;
    String username;
    String occupation;
    String profileUrl;

    public User() {
    }

    public User(String userID, String username, String occupation, String profileUrl) {
        this.userID = userID;
        this.username = username;
        this.occupation = occupation;
        this.profileUrl = profileUrl;
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getProfileUrl() {
        return profileUrl;
    }
}
