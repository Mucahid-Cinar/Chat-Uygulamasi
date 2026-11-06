package com.mucahid.mm_chat.model;

import com.google.firebase.database.IgnoreExtraProperties;

/** Firebase model for Users/{uid}. */
@IgnoreExtraProperties
public class User {

    private String uid;
    private String username;
    private String firstName;
    private String lastName;
    private String about;
    private String image;

    public User() {
        // Required by Firebase
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
