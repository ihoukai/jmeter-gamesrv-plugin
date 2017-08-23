package com.hk.net;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class SocketIO {
    private Socket socket;
    private String host;
    private int port;
    private ReceiveThread receiveThread;
    private IOCallback ioCallback;

    public SocketIO(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setIOCallback(IOCallback IOCallback) {
        this.ioCallback = IOCallback;
    }

    public void connect() throws IOException {
        socket = new Socket(this.host, this.port);
        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    public void disConnect() throws IOException {
        if (receiveThread != null) {
            receiveThread.close();
            receiveThread = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public void send(byte[] data) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        bos.write(data);
        bos.flush();
    }

    enum TransportState {
        readHead, // On read head
        readBody, // On read body
        closed;// connection closed, will ignore all the message and wait for clean up
    }

    class ReceiveThread extends Thread {
        public static final int HeadLength = 4;
        private byte[] headBuffer = new byte[4];
        private byte[] buffer;
        private int bufferOffset = 0;
        private int pkgLength = 0;
        private TransportState transportState;

        public ReceiveThread() {
            transportState = TransportState.readHead;
        }

        public void close() {
            transportState = TransportState.closed;
            this.interrupt();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            while (transportState != TransportState.closed) {
                try {
                    InputStream reader = socket.getInputStream();
                    int count = reader.read(buffer);
                    if (count > 0) {
                        processBytes(buffer, 0, count);
                    } else {
                        ioCallback.onError(new Exception("read error!"));
                    }
                } catch (Exception e) {
                    ioCallback.onError(e);
                }
            }
        }

        public final void processBytes(byte[] bytes, int offset, int limit) {
            if (transportState == TransportState.readHead) {
                readHead(bytes, offset, limit);
            } else if (transportState == TransportState.readBody) {
                readBody(bytes, offset, limit);
            }
        }

        private boolean readHead(byte[] bytes, int offset, int limit) {
            int length = limit - offset;
            int headNum = HeadLength - bufferOffset;

            try {
                if (length >= headNum) {
                    //Write head buffer
                    writeBytes(bytes, offset, headNum, bufferOffset, headBuffer);
                    //Get package length
                    pkgLength = (headBuffer[1] << 16) + (headBuffer[2] << 8) + (headBuffer[3] & 0xFF);

                    //Init message buffer
                    int len = HeadLength + pkgLength;

                    buffer = new byte[len];
                    writeBytes(headBuffer, 0, HeadLength, buffer);
                    offset += headNum;
                    bufferOffset = HeadLength;
                    transportState = TransportState.readBody;

                    if (offset <= limit) {
                        processBytes(bytes, offset, limit);
                    }
                    return true;
                } else {
                    writeBytes(bytes, offset, length, bufferOffset, headBuffer);
                    bufferOffset += length;
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void readBody(byte[] bytes, int offset, int limit) {
            int length = pkgLength + HeadLength - bufferOffset;
            if ((offset + length) <= limit) {
                writeBytes(bytes, offset, length, bufferOffset, buffer);
                offset += length;

                //Invoke the protocol api to handle the message
                ioCallback.onMessage(buffer);
                bufferOffset = 0;
                pkgLength = 0;

                if (transportState != TransportState.closed) {
                    transportState = TransportState.readHead;
                }
                if (offset < limit) {
                    processBytes(bytes, offset, limit);
                }
            } else {
                writeBytes(bytes, offset, limit - offset, bufferOffset, buffer);
                bufferOffset += limit - offset;
                transportState = TransportState.readBody;
            }
        }

        private void writeBytes(byte[] source, int start, int length, byte[] target) {
            writeBytes(source, start, length, 0, target);
        }

        private void writeBytes(byte[] source, int start, int length, int offset, byte[] target) {
            for (int i = 0; i < length; i++) {
                target[offset + i] = source[start + i];
            }
        }
    }
}
