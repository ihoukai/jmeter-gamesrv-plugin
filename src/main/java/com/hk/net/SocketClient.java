package com.hk.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class SocketClient {
    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);

    private int reqId;
    private SocketIO socket;
    private Map<Integer, DataCallBack> cbs;
    private Map<String, List<DataListener>> listeners;
    private MessageProtocol msgProtocol;

    public SocketClient(String host, int port) {
        cbs = new HashMap<Integer, DataCallBack>();
        listeners = new HashMap<String, List<DataListener>>();
        msgProtocol = new MessageProtocol();
        socket = new SocketIO(host, port);
        socket.setIOCallback(new IOCallback() {
            public void onMessage(byte[] data) {
                processMessage(data);
            }

            public void onError(Exception e) {
                log.error("SocketIO ReceiveThread Error!", e);
                try {
                    socket.disConnect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * connect server
     *
     * @throws IOException
     */
    public void connect() throws IOException {
        socket.connect();
    }

    /**
     * disConnect server
     *
     * @throws IOException
     */
    public void disConnect() throws IOException {
        socket.disConnect();
    }

    /**
     * Send message to the server side.
     *
     * @param reqId request id
     * @param route request route
     * @param msg   reqest message
     */
    private void sendMessage(int reqId, String route, byte[] msg) throws IOException {
        byte[] msgBytes = msgProtocol.encode(route, reqId, msg);
        byte[] packetBytes = PackageProtocol.encode(PackageType.PKG_DATA, msgBytes);
        socket.send(packetBytes);
    }

    /**
     * Notify the server without response
     *
     * @param route
     * @param data
     */
    public void inform(String route, byte[] data) throws IOException {
        request(route, data, null);
    }

    /**
     * Client send request to the server and get response data.
     *
     * @param route
     * @param data
     * @param callBack
     * @throws IOException
     */
    public void request(String route, byte[] data, DataCallBack callBack) throws IOException {
        reqId++;
        if (callBack != null) {
            cbs.put(reqId, callBack);
        }
        sendMessage(reqId, route, data);
    }

    /**
     * Process the message from the server.
     *
     * @param data
     */
    private void processMessage(byte[] data) {
        Package pkg = PackageProtocol.decode(data);
        if (pkg.type == PackageType.PKG_DATA) {
            Message msg = msgProtocol.decode(pkg.body);
            switch (msg.type) {
                case MSG_RESPONSE:
                    DataCallBack callback = cbs.get(msg.id);
                    callback.responseData(msg.data);
                    cbs.remove(msg.id);
                    break;
                case MSG_PUSH:
                    emit(msg.route, msg.data);
                    break;
            }
        }
    }

    /**
     * Add event listener and wait for broadcast message.
     *
     * @param route
     * @param listener
     */
    public void on(String route, DataListener listener) {
        List<DataListener> list = listeners.get(route);
        if (list == null)
            list = new ArrayList<DataListener>();
        list.add(listener);
        listeners.put(route, list);
    }

    /**
     * Touch off the event and call listeners corresponding route.
     *
     * @param route
     * @param data
     * @return true if call success, false if there is no listeners for this
     * route.
     */
    private void emit(String route, byte[] data) {
        List<DataListener> list = listeners.get(route);
        if (list == null) {
            log.debug("there is no listeners.");
            return;
        }
        for (DataListener listener : list) {
            DataEvent event = new DataEvent(this, data);
            listener.receiveData(event);
        }
    }
}
