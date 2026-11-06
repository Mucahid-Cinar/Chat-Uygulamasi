package com.mucahid.mm_chat.model;

import com.google.firebase.database.IgnoreExtraProperties;

/** Firebase model for Groups/{groupName}/{pushId}. */
@IgnoreExtraProperties
public class GroupMessage {

    private String username;
    private String userId;
    private String message;
    private String date;
    private String time;

    public GroupMessage() {
        // Required by Firebase
    }

    public GroupMessage(String username, String userId, String message, String date, String time) {
        this.username = username;
        this.userId = userId;
        this.message = message;
        this.date = date;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
