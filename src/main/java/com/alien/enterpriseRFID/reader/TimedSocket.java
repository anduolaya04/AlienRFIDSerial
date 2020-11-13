package com.alien.enterpriseRFID.reader;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;

public class TimedSocket {
    private static final int POLL_DELAY = 100;

    public TimedSocket() {
    }

    public static Socket getSocket(InetAddress addr, int port, int delay) throws InterruptedIOException, IOException {
        TimedSocket.SocketThread st = new TimedSocket.SocketThread(addr, port);
        st.start();
        int timer = 0;
        Socket sock = null;

        do {
            if (st.isConnected()) {
                sock = st.getSocket();
                return sock;
            }

            if (st.isError()) {
                throw st.getException();
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException var7) {
            }

            timer += 100;
        } while(timer <= delay);

        throw new InterruptedIOException("Connection timed out - could not connect for " + delay + " milliseconds");
    }

    public static Socket getSocket(String host, int port, int delay) throws InterruptedIOException, IOException {
        InetAddress inetAddr = InetAddress.getByName(host);
        return getSocket(inetAddr, port, delay);
    }

    static class SocketThread extends Thread {
        private volatile Socket m_connection = null;
        private String m_host = null;
        private InetAddress m_inet = null;
        private int m_port = 0;
        private IOException m_exception = null;

        public SocketThread(String host, int port) {
            this.m_host = host;
            this.m_port = port;
        }

        public SocketThread(InetAddress inetAddr, int port) {
            this.m_inet = inetAddr;
            this.m_port = port;
        }

        public void run() {
            Socket sock = null;

            try {
                if (this.m_host != null) {
                    sock = new Socket(this.m_host, this.m_port);
                } else {
                    sock = new Socket(this.m_inet, this.m_port);
                }
            } catch (IOException var3) {
                this.m_exception = var3;
                return;
            }

            this.m_connection = sock;
        }

        public boolean isConnected() {
            return this.m_connection != null;
        }

        public boolean isError() {
            return this.m_exception != null;
        }

        public Socket getSocket() {
            return this.m_connection;
        }

        public IOException getException() {
            return this.m_exception;
        }
    }
}
