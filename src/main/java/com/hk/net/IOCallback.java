package com.hk.net;

public interface IOCallback {
    void onMessage(byte[] data);

    void onError(Exception e);
}