package com.hk.net;


/**
 * Protocol refs: https://github.com/NetEase/pomelo/wiki/Communication-Protocol
 * -<type>-|--------<length>--------|-<data>-
 * --------|------------------------|--------
 * 1 byte packet type, 3 bytes packet data length(big end), and data segment
 */
public class PackageProtocol {
    public static final int HEADER_LENGTH = 4;

    public static byte[] encode(PackageType type) {
        return new byte[]{(byte) type.getValue(), 0, 0, 0};
    }

    public static byte[] encode(PackageType type, byte[] body) {
        int length = HEADER_LENGTH;
        if (body != null) {
            length += body.length;
        }

        byte[] buf = new byte[length];
        int index = 0;
        buf[index++] = (byte) type.getValue();
        buf[index++] = (byte) (body.length >> 16 & 0xFF);
        buf[index++] = (byte) (body.length >> 8 & 0xFF);
        buf[index++] = (byte) (body.length & 0xFF);

        while (index < length) {
            buf[index] = body[index - HEADER_LENGTH];
            index++;
        }
        return buf;
    }

    public static Package decode(byte[] buf) {
        PackageType type = PackageType.forValue(buf[0]);
        byte[] body = new byte[buf.length - HEADER_LENGTH];

        for (int i = 0; i < body.length; i++) {
            body[i] = buf[i + HEADER_LENGTH];
        }
        return new Package(type, body);
    }
}