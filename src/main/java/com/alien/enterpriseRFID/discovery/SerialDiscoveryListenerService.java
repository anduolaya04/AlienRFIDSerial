package com.alien.enterpriseRFID.discovery;

import com.alien.enterpriseRFID.reader.*;
import com.alien.enterpriseRFID.util.SerialManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class SerialDiscoveryListenerService implements Runnable{
    public static final int SCANNING_PORT = 1;
    public static final int SCANNING_END = 2;
    public static final int SCANNING_PORTBUSY = 3;
    private SerialManager serialManager;
    private DiscoveryListener discoveryListener;
    private ActionListener actionListener;
    private Hashtable discoveryTable = new Hashtable();
    private Thread motor;
    public boolean isDebug = false;
    private boolean isRunning;
    private int maxSerialPort;
    private int serialBaudRate = 115200;
    private Vector serialPortVector = new Vector();

    public SerialDiscoveryListenerService() throws AlienDiscoverySerialException {
        try {
            Class.forName("com.fazecast.jSerialComm.SerialPort");
        } catch (ClassNotFoundException var4) {
            try {
                Class.forName("gnu.io.CommPort");
            } catch (ClassNotFoundException var3) {
                this.serialManager = null;
                throw new AlienDiscoverySerialException( "Serial Discovery Instance Failed - Serial Classes Not Present" );
            }
        }

        this.serialManager = new SerialManager();
    }

    public DiscoveryListener getDiscoveryListener() {
        return this.discoveryListener;
    }

    public void setDiscoveryListener(DiscoveryListener discoveryListener) {
        this.discoveryListener = discoveryListener;
    }

    public ActionListener getActionListener() {
        return this.actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /** @deprecated */
    public int getMaxSerialPort() {
        return this.maxSerialPort;
    }

    /** @deprecated */
    public void setMaxSerialPort(int maxPort) {
        this.maxSerialPort = maxPort;
    }

    public int getSerialBaudRate() {
        return this.serialBaudRate;
    }

    public void setSerialBaudRate(int serialBaudRate) {
        this.serialBaudRate = serialBaudRate;
    }

    public String getSerialPortList() {
        String portListString = this.serialPortVector.toString();
        return portListString.substring(1, portListString.length() - 1);
    }

    public void setSerialPortList(String portList) {
        this.serialPortVector = new Vector();
        if (portList != null && !portList.equals("")) {
            String[] portArray = portList.split(",");

            for(int i = 0; i < portArray.length; ++i) {
                String portName = portArray[i].trim();

                try {
                    int portNum = Integer.parseInt(portName);
                    portName = "COM" + portNum;
                } catch (Exception var6) {
                }

                SerialManager.addPortName(portName);
                this.serialPortVector.add(portName);
            }

        }
    }

    public void startService() {
        if (this.motor == null) {
            this.isRunning = true;
            this.motor = new Thread(this);
            this.motor.setPriority(1);
            this.motor.start();
            if (this.isDebug) {
                System.out.println("Serial Listener Service : Started");
            }
        }
    }

    public void stopService() {
        if (this.isDebug && this.motor != null) {
            System.out.println("Serial Listener Service : Stopped");
        }

        this.motor = null;
        this.isRunning = false;
        this.fireActionEvent(2, "");
    }

    @Override
    public void run() {
        this.isRunning = true;
        if (this.discoveryTable == null) {
            this.discoveryTable = new Hashtable();
        }

        Enumeration elements = this.discoveryTable.elements();

        while(elements.hasMoreElements()) {
            DiscoveryItem discoveryItem = (DiscoveryItem)elements.nextElement();
            discoveryItem.setLeaseTime(0);
        }

        if (this.serialManager != null) {
            Vector serialPortNames = SerialManager.getSerialPortList();
            if (serialPortNames != null) {
                for(int i = 0; i < serialPortNames.size() && this.isRunning; ++i) {
                    String name = (String)serialPortNames.elementAt(i);
                    if (name != null && this.isValidSerialPort(name)) {
                        this.checkSerialPort(name);
                    }
                }
            }
        }

        this.checkForExpiredLeases();
        this.stopService();
        this.isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    private boolean isValidSerialPort(String portName) {
        return this.serialPortVector.size() > 0 ? this.serialPortVector.contains(portName) : true;
    }

    public void checkSerialPort(String serialPortName) {
        this.fireActionEvent(1, serialPortName);
        DiscoveryItem item = new DiscoveryItem();
        item.setConnection("Serial");
        item.setReaderAddress(serialPortName);
        item.setLeaseTime(10000);
        AlienClass1Reader reader = new AlienClass1Reader(serialPortName);
        reader.setSerialBaudRate(this.serialBaudRate);
        if (this.isDebug) {
            reader.setDebugLevel(2);
        }

        try {
            reader.setTimeOutMilliseconds(1000);
            reader.setValidateOpen(false);
            reader.open();
            reader.sendString("\r\n");
            Thread.sleep(10L);
            String testResponse = reader.receiveString(false);
            reader.sendString("\r\n");
            testResponse = reader.receiveString(true).trim();
            if (testResponse.endsWith("Alien>")) {
                Thread.sleep(200L);
                String readerName = null;
                String readerType = null;

                try {
                    try {
                        item.setReaderMACAddress(reader.getMACAddress());
                    } catch (Exception var17) {
                    }

                    readerName = reader.getReaderName();
                    readerType = reader.getReaderType();
                } catch (AlienReaderException var18) {
                    if (this.isDebug) {
                        System.out.println(var18.toString());
                    }

                    if (readerName == null) {
                        readerName = "< Undetermined >";
                    }

                    if (readerType == null) {
                        readerType = "< Undetermined >";
                    }
                } finally {
                    reader.close();
                    item.setReaderName(readerName);
                    item.setReaderType(readerType);
                    if (this.isDebug) {
                        System.out.println("SerialPort : " + serialPortName + " ... Present");
                    }

                    this.discoveryItemReceived(serialPortName, item);
                }

                return;
            }

            reader.close();
            return;
        } catch (AlienReaderConnectionException var21) {
            this.fireActionEvent(3, serialPortName);
            reader.close();
            if (this.isDebug) {
                System.out.println("SerialPort : " + serialPortName + " ... In Use");
            }
        } catch (AlienReaderTimeoutException var22) {
            if (this.isDebug) {
                System.out.println("Trying Alien OEM Reader Module...");
            }

            reader.close();

            try {
                reader = new AlienClassOEMReader(serialPortName);
                reader.setTimeOutMilliseconds(1000);
                reader.setValidateOpen(false);
                reader.open();
                reader.getReaderVersion();
                item.setReaderName(reader.getReaderName());
                item.setReaderType(reader.getReaderType());
                if (this.isDebug) {
                    System.out.println("SerialPort : " + serialPortName + " ... Present");
                }

                reader.close();
                this.discoveryItemReceived(serialPortName, item);
                return;
            } catch (Exception var20) {
                reader.close();
                if (this.isDebug) {
                    System.out.println(var20);
                    System.out.println("SerialPort : " + serialPortName + " ... Not Present");
                }
            }
        } catch (Exception var23) {
            reader.close();
            if (this.isDebug) {
                System.out.println(var23);
                System.out.println("SerialPort : " + serialPortName + " ... Not Present");
            }
        }

    }

    private void discoveryItemReceived(String serialPort, DiscoveryItem discoveryItem) {
        if (this.discoveryTable == null) {
            this.discoveryTable = new Hashtable();
        }

        DiscoveryItem existingDiscoveryItem = (DiscoveryItem)this.discoveryTable.get(serialPort);

        try {
            if (!discoveryItem.equals(existingDiscoveryItem)) {
                existingDiscoveryItem = null;
            }
        } catch (Exception var6) {
        }

        if (existingDiscoveryItem != null) {
            existingDiscoveryItem.setLastHeartbeat((new Date()).getTime());
            existingDiscoveryItem.setLeaseTime(discoveryItem.getLeaseTime());
            if (this.discoveryListener != null) {
                this.discoveryListener.readerRenewed(existingDiscoveryItem);
            }
        } else {
            this.discoveryTable.put(serialPort, discoveryItem);
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
            Enumeration keys = this.discoveryTable.keys();

            while(keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                DiscoveryItem discoveryItem = (DiscoveryItem)this.discoveryTable.get(key);
                if (discoveryItem.getLeaseTime() <= 0) {
                    this.discoveryItemExpired(discoveryItem);
                }
            }

        }
    }

    public void discoveryItemExpired(DiscoveryItem discoveryItem) {
        if (this.discoveryTable == null) {
            this.discoveryTable = new Hashtable();
        }

        this.discoveryTable.remove(discoveryItem.getReaderAddress());
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

    private void fireActionEvent(int status, String message) {
        if (this.actionListener != null) {
            this.actionListener.actionPerformed(new ActionEvent(this, status, message));
        }
    }
}
