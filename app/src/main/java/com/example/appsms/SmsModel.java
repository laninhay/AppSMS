package com.example.appsms;

public class SmsModel {
    private String sender;
    private String message;

    public SmsModel(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}