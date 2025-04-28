package com.example.cyclonecarpool.chat;

import com.example.cyclonecarpool.user.User;

import java.sql.Timestamp;

public class Message {

    public Long id;
    public User sender;
    public User receiver;
    public String content;
    public Timestamp timestamp;
    public Boolean isRead = false;

    public Message(Long id, User sender, User receiver, String content, Timestamp timestamp, Boolean isRead) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public <U> U getTimestamp() {
        return (U) timestamp;
    }

    public String getContent() {
        return content;
    }

    public Integer getTripId() {
        return Math.toIntExact(id);
    }
}
