package com.cjacquet.ft.hangouts.messages;

public enum MessageType {
    SENT(0), RECEIVED(1);

    private final int value;

    MessageType(final int value) {
        this.value = value;
    }
}
