package com.alien.enterpriseRFID.notify;

import com.alien.enterpriseRFID.util.SerialManager;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public class MessageListenerServiceSerial implements Runnable, MessageListener{

    private static final int TIMEOUT = 1000;
    private Thread motor;
    private Vector engineList;
    private SerialManager serialManager;
    private MessageListener messageListener;
    private String PORT;
    private boolean isDebug;
    private boolean isRunning;
    private boolean isCustomTagList;

    public MessageListenerServiceSerial(){
    }

    public MessageListenerServiceSerial(String PORT) {
        this.PORT = PORT;
    }

    public void setDebug(boolean debug) { isDebug = debug; }

    public void setPORT(String PORT) { this.PORT = PORT; }

    public MessageListener getMessageListener() { return messageListener; }

    public void setMessageListener(MessageListener messageListener) { this.messageListener = messageListener; }

    public boolean isCustomTagList() { return isCustomTagList; }

    public void setCustomTagList(boolean customTagList) { isCustomTagList = customTagList; }


    public void startService() throws IOException {

        serialManager = new SerialManager();
        serialManager.setSerialPortName( this.PORT );
        serialManager.setSerialPortTimeout( TIMEOUT );

        if (motor != null) {
            stopService();
        }
        try {

            serialManager.openSerialConnection();

            if (isDebug) {
                System.out.println("Message Listener: Listening on port" + serialManager.getSerialPortName());
            }

            motor = new Thread( this );
            motor.setDaemon( true );
            motor.start();

        } catch (Exception e) {
            if (serialManager != null) {
                serialManager.close();
                serialManager = null;
            }

            if (PORT != null) {
                throw new IOException( "Could nor listen on port: " + PORT );
            }

            throw new IOException("Could not listen on port. Is that port in use?");
        }

        if (isDebug) {
            System.out.println("Message Listener: Service Started on Port " + PORT);
        }

        isRunning = true;

    }

    public void stopService() {

        if (isDebug) {
            System.out.println("Stopping motor.");
        }

        motor = null;

        if (isDebug) {
            System.out.println("Closing serial connection.");
        }

        if (this.serialManager != null) {
            serialManager.close();
            serialManager = null;
        }

        while (isRunning) {

            try {
                Thread.sleep( 100L );
            } catch (InterruptedException e) {
            }
        }

        if (isDebug) {
            System.out.println("Message Listener: Service Stopped on Port:" + serialManager.getSerialPortName() );
        }
    }

    @Override
    public void run() {

        int currentEngineID = 0;
        engineList = new Vector();

        MessageListenerEngineSerial seEngine;
        try {
            do {
                if (serialManager != null) {
                    if (isDebug) {
                        System.out.println( "Serial conn accepted" );
                    }
                    seEngine = new MessageListenerEngineSerial( serialManager, this, isDebug, currentEngineID );

                    if (isDebug) {
                        System.out.println( "Message Listener: Start new MessageListener" );
                    }
                    seEngine.start();
                    engineList.add( seEngine );
                } else {
                    try {
                        Thread.sleep( 500L );
                    } catch (InterruptedException e) {
                    }
                }
                Iterator iterator = engineList.iterator();

                while (iterator.hasNext()) {

                    seEngine = (MessageListenerEngineSerial) iterator.next();
                    if (!seEngine.isAlive()) {
                        iterator.remove();
                    }
                }
            } while (motor != null);
        } catch (Exception e) {
            System.err.println("Failed to accept COM connection port");
        } finally {
            if (serialManager != null) {
                serialManager.close();
                serialManager = null;
            }
        }

        if(isDebug) {
            System.out.println("Stopping remaining MessageListenerEngines. ");
        }

        Iterator iterator = engineList.iterator();

        while(true) {

            do {
                if (!iterator.hasNext()) {
                    isRunning = false;
                    return;
                }
                seEngine = (MessageListenerEngineSerial) iterator.next();

            } while(!seEngine.isAlive());

            if(isDebug) {
                System.out.println("Halting MessageListenenServiceSerial #"+seEngine.engineID);
            }

            seEngine.halt();

            while (seEngine.isAlive()) {
                try {
                    Thread.sleep( 10L );
                } catch (InterruptedException e) {
                }
            }

            if(isDebug) {
                System.out.println("MessageListenerEngine #" + seEngine.engineID + " halted.");
            }
        }

    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void messageReceived(Message var1) {
        if(messageListener != null){
            messageListener.messageReceived(var1);
        }
    }

}
