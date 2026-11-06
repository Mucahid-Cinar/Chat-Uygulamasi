package com.mucahid.mm_chat.model;

import com.google.firebase.database.IgnoreExtraProperties;

/** Firebase model for Messages/{senderUid}/{receiverUid}/{pushId}. */
@IgnoreExtraProperties
public class PrivateMessage {

    private String from;
    private String message;
    private String type;
    private String time;

    public PrivateMessage() {
        // Required by Firebase
    }

    public PrivateMessage(String from, String message, String type, String time) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
