package com.cjacquet.ft.hangouts.messages;

import java.util.Date;

public class Message{
    private String id;
    private String address;
    private String text;
    private boolean read;
    private Long time;
    private MessageType type;

    public Message(){}

    public Message(String text, String address) {
        this.address = address;
        this.text = text;
        this.read = true;
        this.time = new Date().getTime();
        this.type = MessageType.SENT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}