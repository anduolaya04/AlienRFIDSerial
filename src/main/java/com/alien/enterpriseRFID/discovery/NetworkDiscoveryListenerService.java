package com.alien.enterpriseRFID.discovery;

import com.alien.enterpriseRFID.util.XMLReader;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

public class NetworkDiscoveryListenerService implements Runnable{

    public static final int DEFAULT_LISTENER_PORT = 3988;
    public static final int DEFAULT_LISTENER_TIMEOUT_SECONDS = 2;
    private DiscoveryListener discoveryListener;
    private Hashtable discoveryTable;
    private DatagramSocket datagramSocket;
    private int listenerPort;
    private Thread motor;
    public boolean isDebug;
    private boolean isRunning;

    public NetworkDiscoveryListenerService() {
        this(3988);
    }

    public NetworkDiscoveryListenerService(int listenerPort) {
        this.isDebug = false;
        this.isRunning = false;
        this.listenerPort = listenerPort;
        this.discoveryTable = new Hashtable();
    }

    public DiscoveryListener getDiscoveryListener() {
        return this.discoveryListener;
    }

    public void setDiscoveryListener(DiscoveryListener discoveryListener) {
        this.discoveryListener = discoveryListener;
    }

    public void startService() throws AlienDiscoverySocketException {
        this.startService(true);
    }

    public void startService(boolean doPing) throws AlienDiscoverySocketException {
        this.stopService();

        try {
            this.datagramSocket = new DatagramSocket(this.listenerPort);
            this.datagramSocket.setSoTimeout(2000);
        } catch (SocketException var6) {
            throw new AlienDiscoverySocketException("Unable to open a socket to receive reader heartbeats.\n" + var6.getMessage());
        }

        this.motor = new Thread(this);
        this.motor.setPriority(1);
        this.motor.start();
        if (this.isDebug) {
            System.out.println("Listening for reader heartbeats on UDP port " + this.listenerPort);
        }

        if (doPing) {
            try {
                while(!this.isRunning) {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException var5) {
                    }
                }

                if (this.isDebug) {
                    System.out.println("Sending ping packet");
                }

                String buf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<Alien-RFID-Reader-Command>\r\n  <COMMAND>HeartbeatNow</COMMAND>\r\n</Alien-RFID-Reader-Command>\r\n\u0000";
                InetAddress socketAddr = InetAddress.getByName("255.255.255.255");
                DatagramPacket pingPacket = new DatagramPacket(buf.getBytes(), buf.length(), socketAddr, this.listenerPort);
                this.datagramSocket.send(pingPacket);
            } catch (Exception var7) {
                throw new AlienDiscoverySocketException("Unable to send a heartbeat ping packet.\n" + var7.getMessage());
            }
        }

    }

    public void stopService() {
        if (this.datagramSocket != null || this.motor != null) {
            try {
                this.datagramSocket.close();
            } catch (Exception var2) {
            }

            this.datagramSocket = null;
            this.motor = null;
        }
    }

    @Override
    public void run() {
        this.isRunning = true;

        do {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                this.datagramSocket.receive(datagramPacket);
                this.discoveryItemReceived(datagramPacket);
                this.checkForExpiredLeases();
                Thread.sleep(100L);
            } catch (InterruptedIOException var3) {
                this.checkForExpiredLeases();
            } catch (Exception var4) {
                if (this.datagramSocket != null) {
                    System.out.println("Error Listening for Discovery Packets... stopping Service");
                    System.out.println(var4.toString());
                }

                this.motor = null;
            }
        } while(this.motor != null);

        this.isRunning = false;
        this.stopService();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    private void discoveryItemReceived(DatagramPacket datagramPacket) {
        String datagramText = "Not Decoded";

        try {
            datagramText = new String(datagramPacket.getData());
            if (this.isDebug) {
                System.out.println("heartbeat = " + datagramText);
            }

            XMLReader reader = new XMLReader(datagramText);
            Hashtable topLevelTable = reader.readXML();
            String dataString = (String)topLevelTable.get("ALIEN-RFID-READER-HEARTBEAT");
            if (dataString == null) {
                throw new Exception("No <ALIEN-RFID-READER-HEARTBEAT> Tag Found");
            }

            reader.setXMLString(dataString);
            Hashtable table = reader.readXML();
            DiscoveryItem discoveryItem = new DiscoveryItem();
            discoveryItem.setReaderName((String)table.get("READERNAME"));
            discoveryItem.setReaderType((String)table.get("READERTYPE"));
            discoveryItem.setCommandPort((String)table.get("COMMANDPORT"));
            discoveryItem.setLeaseTime((String)table.get("HEARTBEATTIME"));
            discoveryItem.setReaderMACAddress((String)table.get("MACADDRESS"));
            discoveryItem.setReaderAddress((String)table.get("IPADDRESS"));
            discoveryItem.setReaderVersion((String)table.get("READERVERSION"));
            discoveryItem.setConnection("Network");
            this.discoveryItemReceived(discoveryItem);
        } catch (Exception var8) {
            if (this.isDebug) {
                System.out.println("Error decoding a Discovery Item Datagram Packet");
                System.out.println("   DatagramText : " + datagramText);
                System.out.println("   Exception : " + var8.toString());
            }
        }

    }

    private void discoveryItemReceived(DiscoveryItem discoveryItem) {
        if (this.discoveryTable == null) {
            this.discoveryTable = new Hashtable();
        }

        String key = discoveryItem.toKey();
        DiscoveryItem existingDiscoveryItem = (DiscoveryItem)this.discoveryTable.get(key);
        if (existingDiscoveryItem != null) {
            existingDiscoveryItem.update(discoveryItem);
            if (this.discoveryListener != null) {
                this.discoveryListener.readerRenewed(existingDiscoveryItem);
            }
        } else {
            this.discoveryTable.put(key, discoveryItem);
            if (this.discoveryListener != null) {
                this.discoveryListener.readerAdded(discoveryItem);
            }
        }

    }

    private void checkForExpiredLeases() {
        if (this.discoveryTable == null) {
            this.discoveryTable = new Hashtable();
        }

        if (this.discoveryTable.size() > 0) {
            long currentTime = (new Date()).getTime();
            Enumeration elements = this.discoveryTable.elements();

            while(elements.hasMoreElements()) {
                DiscoveryItem discoveryItem = (DiscoveryItem)elements.nextElement();
                long deltaTime = currentTime - discoveryItem.getLastHeartbeat();
                if (deltaTime > (long)((discoveryItem.getLeaseTime() + 10) * 1000)) {
                    this.discoveryItemExpired(discoveryItem);
                }
            }

        }
    }

    public void discoveryItemExpired(DiscoveryItem discoveryItem) {
        if (this.discoveryTable == null) {
            this.discoveryTable = new Hashtable();
        }

        this.discoveryTable.remove(discoveryItem.toKey());
        if (this.discoveryListener != null) {
            this.discoveryListener.readerRemoved(discoveryItem);
        }

    }

    public void allDiscoveryItemsExpired() {
        Enumeration elements = this.discoveryTable.elements();

        while(elements.hasMoreElements()) {
            DiscoveryItem di = (DiscoveryItem)elements.nextElement();
            this.discoveryItemExpired(di);
        }

    }

    public DiscoveryItem[] getDiscoveryItems() {
        if (this.discoveryTable == null) {
            this.discoveryTable = new Hashtable();
        }

        DiscoveryItem[] results = new DiscoveryItem[this.discoveryTable.size()];
        Enumeration items = this.discoveryTable.elements();

        for(int var3 = 0; items.hasMoreElements(); results[var3++] = (DiscoveryItem)items.nextElement()) {
        }

        return results;
    }

    private InetAddress getLocalWiredInetAddress() {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();

            while(true) {
                NetworkInterface netface;
                do {
                    if (!e.hasMoreElements()) {
                        return null;
                    }

                    netface = (NetworkInterface)e.nextElement();
                } while(netface.getName().equals("lo"));

                Enumeration e2 = netface.getInetAddresses();

                while(e2.hasMoreElements()) {
                    InetAddress ip = (InetAddress)e2.nextElement();
                    if (netface.getDisplayName().toLowerCase().indexOf("wireless") < 0) {
                        return ip;
                    }
                }
            }
        } catch (SocketException var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public int getListenerPort() {
        return this.listenerPort;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }
}
