package com.hk.net;

import java.util.*;

public class Encoder {

    //Encode the UInt32.
    public static byte[] encodeUInt32(String n) {
        return encodeUInt32(Integer.parseInt(n));
    }

    public static byte[] encodeUInt32(int n) {
        ArrayList<Byte> byteList = new ArrayList<Byte>();
        do {
            int tmp = n % 128;
            int next = n >>> 7;
            if (next != 0) {
                tmp = tmp + 128;
            }
            byteList.add((byte) tmp);
            n = next;
        } while (n != 0);

        int len = byteList.size();
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = byteList.get(i);
        }
        return bytes;
    }

    public static byte[] encodeSInt32(String n) {
        return encodeSInt32(Integer.parseInt(n));
    }

    public static byte[] encodeSInt32(int n) {
        int num = (int) (n < 0 ? (Math.abs(n) * 2 - 1) : n * 2);
        return encodeUInt32(num);
    }

    public static int byteLength(String msg) {
        return msg.getBytes().length;
    }
}