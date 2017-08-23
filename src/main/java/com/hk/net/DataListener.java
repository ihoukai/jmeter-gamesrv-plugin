package com.hk.net;

import java.util.EventListener;

/**
 * Listener of broadcast message.
 */
public interface DataListener extends EventListener {
    void receiveData(DataEvent event);
}
