package com.alien.enterpriseRFID.notify;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;

public class MessageListenerService implements Runnable, MessageListener{

    private Thread motor;
    private Vector engineList;
    private int listenerPort;
    private InetAddress listenerInterface;
    private MessageListener messageListener;
    private ServerSocketChannel serverSocket;
    private boolean isDebug;
    private boolean isRunning;
    private boolean isCustomTaglist;

    public MessageListenerService() {
        this(3600);
    }

    public MessageListenerService(int listenerPort) {
        this(listenerPort, (InetAddress)null);
    }

    public MessageListenerService(int listenerPort, InetAddress listenerInterface) {
        this.isRunning = false;
        this.isCustomTaglist = false;
        this.setListenerPort(listenerPort);
        this.setListenerInterface(listenerInterface);
        this.setDebug(false);
        this.serverSocket = null;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public int getListenerPort() {
        return this.listenerPort;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }

    public InetAddress getListenerInterface() {
        return this.listenerInterface;
    }

    public void setListenerInterface(InetAddress listenerInterface) {
        this.listenerInterface = listenerInterface;
    }

    public MessageListener getMessageListener() {
        return this.messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public boolean isCustomTagList() {
        return this.isCustomTaglist;
    }

    public void setIsCustomTagList(boolean isCustom) {
        this.isCustomTaglist = isCustom;
    }

    public void startService() throws IOException {
        if (this.motor != null) {
            this.stopService();
        }

        try {
            this.serverSocket = ServerSocketChannel.open();
            this.serverSocket.socket().bind(new InetSocketAddress(this.getListenerInterface(), this.getListenerPort()));
            this.serverSocket.configureBlocking(false);
            if (this.isDebug) {
                System.out.println("Message Listener: Listening on port " + this.getListenerPort());
            }

            this.motor = new Thread(this);
            this.motor.setDaemon(true);
            this.motor.start();
        } catch (IOException var4) {
            try {
                if (this.serverSocket != null) {
                    this.serverSocket.close();
                    this.serverSocket = null;
                }
            } catch (IOException var3) {
            }

            if (this.getListenerInterface() != null) {
                throw new IOException("Could not listen on " + this.getListenerInterface().getHostAddress() + ":" + this.getListenerPort() + ". Is that port in use or the interface unavailable?");
            }

            throw new IOException("Could not listen on port " + this.getListenerPort() + ". Is that port in use?");
        }

        if (this.isDebug) {
            System.out.println("Message Listener: Service Started on Port " + this.getListenerPort());
        }

        this.isRunning = true;
    }

    public void stopService() {
        if (this.isDebug) {
            System.out.println("Stopping motor.");
        }

        this.motor = null;
        if (this.isDebug) {
            System.out.println("Closing server socket.");
        }

        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (IOException var3) {
            }

            this.serverSocket = null;
        }

        while(this.isRunning) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException var2) {
            }
        }

        if (this.isDebug) {
            System.out.println("Message Listener: Service Stopped on Port " + this.getListenerPort());
        }

    }


    @Override
    public void run() {
        int currentEngineID = 0;
        this.engineList = new Vector();

        MessageListenerEngine anEngine;
        try {
            do {
                SocketChannel socketChannel = this.serverSocket.accept();
                if (socketChannel != null) {
                    if (this.isDebug) {
                        System.out.println("MLS: Accepted connection.");
                    }

                    anEngine = new MessageListenerEngine(currentEngineID++, socketChannel.socket(), this, this.isDebug);
                    if (this.isDebug) {
                        System.out.println("MLS: Starting new MessageListenerEngine.");
                    }

                    anEngine.start();
                    this.engineList.add(anEngine);
                } else {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException var16) {
                    }
                }

                Iterator iterator = this.engineList.iterator();

                while(iterator.hasNext()) {
                    anEngine = (MessageListenerEngine)iterator.next();
                    if (!anEngine.isAlive()) {
                        iterator.remove();
                    }
                }
            } while(this.motor != null);
        } catch (IOException var17) {
            System.err.println("Failed to accept a connection on port: " + this.getListenerPort());
        } finally {
            try {
                if (this.serverSocket != null) {
                    this.serverSocket.close();
                    this.serverSocket = null;
                }
            } catch (IOException var14) {
            }

        }

        if (this.isDebug) {
            System.out.println("Stopping remaining MessageListenerEngines.");
        }

        Iterator iterator = this.engineList.iterator();

        while(true) {
            do {
                if (!iterator.hasNext()) {
                    this.isRunning = false;
                    return;
                }

                anEngine = (MessageListenerEngine)iterator.next();
            } while(!anEngine.isAlive());

            if (this.isDebug) {
                System.out.println("Halting MessageListenerEngine #" + anEngine.engineID);
            }

            anEngine.halt();

            while(anEngine.isAlive()) {
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException var15) {
                }
            }

            if (this.isDebug) {
                System.out.println("MessageListenerEngine #" + anEngine.engineID + " halted.");
            }
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void messageReceived(Message var1) {
        if (this.messageListener != null) {
            this.messageListener.messageReceived(var1);
        }
    }
}
