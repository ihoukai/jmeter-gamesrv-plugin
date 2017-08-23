package com.hk.net;

import java.util.HashMap;

public class MessageProtocol {
    public static final int MSG_Route_Limit = 255;
    public static final int MSG_Route_Mask = 0x01;
    public static final int MSG_Type_Mask = 0x07;

    private HashMap<String, Short> dict = new HashMap<String, Short>();
    private HashMap<Short, String> abbrs = new HashMap<Short, String>();
    private HashMap<Integer, String> reqMap;

    public MessageProtocol() {
        this.reqMap = new HashMap<Integer, String>();
    }

    public final byte[] encode(String route, byte[] body) {
        return encode(route, 0, body);
    }

    /**
     * Encode message. Different message types is corresponding to different message header,
     * message types is identified by 2-4 bit of flag field. The relationship between message
     * types and message header is presented as follows:
     *
     * type      flag      other
     * ----      ----      -----
     * request  |----000-|<message id>|<route>
     * notify   |----001-|<route>
     * response |----010-|<message id>
     * push     |----011-|<route>
     * The figure above indicates that the bit does not affect the type of message.
     * @param route
     * @param id
     * @param body
     * @return
     */
    public final byte[] encode(String route, int id, byte[] body) {
        int routeLength = byteLength(route);
        if (routeLength > MSG_Route_Limit) {
            throw new RuntimeException("Route is too long!");
        }

        //Encode head
        //The maximus length of head is 1 byte flag + 4 bytes message id + route string length + 1byte
        byte[] head = new byte[routeLength + 6];
        int offset = 1;
        byte flag = 0;

        if (id > 0) {
            byte[] bytes = Encoder.encodeUInt32(id);

            writeBytes(bytes, offset, head);
            flag |= ((byte) MessageType.MSG_REQUEST.getValue()) << 1;
            offset += bytes.length;
        } else {
            flag |= ((byte) MessageType.MSG_NOTIFY.getValue()) << 1;
        }

        //Compress head
        if (dict.containsKey(route)) {
            short cmpRoute = dict.get(route);
            writeShort(offset, cmpRoute, head);
            flag |= MSG_Route_Mask;
            offset += 2;
        } else {
            head[offset++] = (byte) routeLength;

            //Write route
            writeBytes(route.getBytes(), offset, head);
            offset += routeLength;
        }

        head[0] = flag;

        //Construct the result
        byte[] result = new byte[offset + body.length];
        for (int i = 0; i < offset; i++) {
            result[i] = head[i];
        }

        for (int i = 0; i < body.length; i++) {
            result[offset + i] = body[i];
        }

        //Add id to route map
        if (id > 0) {
            reqMap.put(id, route);
        }

        return result;
    }

    public final Message decode(byte[] buffer) {
        //Decode head
        //Get flag
        byte flag = buffer[0];
        //Set offset to 1, for the 1st byte will always be the flag
        int offset = 1;

        MessageType type = MessageType.forValue((flag >>> 1) & MSG_Type_Mask);
        int id = 0;
        String route;

        if (type == MessageType.MSG_RESPONSE) {
            int length = 0;
            RefObject<Integer> tempRef_length = new RefObject<Integer>(length);
            id = Decoder.decodeUInt32(offset, buffer, tempRef_length);
            length = tempRef_length.argValue;
            if (id <= 0 || !reqMap.containsKey(id)) {
                return null;
            } else {
                route = reqMap.get(id);
                reqMap.remove(id);
            }

            offset += length;
        } else if (type == MessageType.MSG_PUSH) {
            //Get route
            if ((flag & 0x01) == 1) {
                short routeId = readShort(offset, buffer);
                route = abbrs.get(routeId);

                offset += 2;
            } else {
                byte length = buffer[offset];
                offset += 1;

                route = new String(buffer, offset, length);
                offset += length;
            }
        } else {
            return null;
        }

        //Decode body
        byte[] body = new byte[buffer.length - offset];
        for (int i = 0; i < body.length; i++) {
            body[i] = buffer[i + offset];
        }

        //Construct the message
        return new Message(type, id, route, body);
    }

    private void writeInt(int offset, int value, byte[] bytes) {
        bytes[offset] = (byte) (value >>> 24 & 0xff);
        bytes[offset + 1] = (byte) (value >>> 16 & 0xff);
        bytes[offset + 2] = (byte) (value >>> 8 & 0xff);
        bytes[offset + 3] = (byte) (value & 0xff);
    }

    private void writeShort(int offset, short value, byte[] bytes) {
        bytes[offset] = (byte) (value >>> 8 & 0xff);
        bytes[offset + 1] = (byte) (value & 0xff);
    }

    private short readShort(int offset, byte[] bytes) {
        short result = 0;

        result += (short) (bytes[offset] << 8);
        result += (short) (bytes[offset + 1]);
        return result;
    }

    private int byteLength(String msg) {
        return msg.getBytes().length;
    }

    private void writeBytes(byte[] source, int offset, byte[] target) {
        for (int i = 0; i < source.length; i++) {
            target[offset + i] = source[i];
        }
    }
}