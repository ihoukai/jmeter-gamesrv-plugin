package com.hk.net;

import java.util.EventObject;

/**
 * Data event of broadcast message.
 */
public class DataEvent extends EventObject {

    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public DataEvent(Object source, byte[] data) {
        super(source);
        this.data = data;
    }

}
