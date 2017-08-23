package com.hk.net;


public class Message {
    public MessageType type;
    public String route;
    public int id;
    public byte[] data;

    public Message(MessageType type, int id, String route, byte[] data) {
        this.type = type;
        this.id = id;
        this.route = route;
        this.data = data;
    }
}