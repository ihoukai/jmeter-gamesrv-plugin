package com.hk.net;

public class Decoder {
    public static int decodeUInt32(int offset, byte[] bytes, RefObject<Integer> length) {
        int n = 0;
        length.argValue = 0;

        for (int i = offset; i < bytes.length; i++) {
            length.argValue++;
            int m = (int) bytes[i];
            n = n + (int) ((m & 0x7f) * Math.pow(2, (7 * (i - offset))));
            if (m < 128) {
                break;
            }
        }
        return n;
    }

    public static int decodeUInt32(byte[] bytes) {
        int length = 0;
        RefObject<Integer> tempRef_length = new RefObject<Integer>(length);
        int tempVar = decodeUInt32(0, bytes, tempRef_length);
        length = tempRef_length.argValue;
        return tempVar;
    }

    public static int decodeSInt32(byte[] bytes) {
        int n = decodeUInt32(bytes);
        int flag = ((n % 2) == 1) ? -1 : 1;

        int result = (int) (((n % 2 + n) / 2) * flag);
        return result;
    }
}