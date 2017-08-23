package com.hk.net;

import org.json.JSONObject;

public class Protocol {
    private final static int HEADER = 5;

    public static byte[] encode(int id, String route, JSONObject msg) {
        String str = msg.toString();
        if (route.length() > 255) {
            throw new RuntimeException("route max length is overflow.");
        }
        byte[] arr = new byte[HEADER + route.length()];
        int index = 0;
        arr[index++] = (byte) ((id >> 24) & 0xFF);
        arr[index++] = (byte) ((id >> 16) & 0xFF);
        arr[index++] = (byte) ((id >> 8) & 0xFF);
        arr[index++] = (byte) (id & 0xFF);
        arr[index++] = (byte) (route.length() & 0xFF);

        for (int i = 0; i < route.length(); i++) {
            arr[index++] = (byte) route.codePointAt(i);
        }
        return arr;
    }
}
