package com.alien.enterpriseRFID.util;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class SerialManager {

    private SerialPort serialPort;
    private String serialPortName;
    private int serialBaudRate;
    private int serialPortTimeout;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SerialManager() {
    }

    public String getSerialPortName() {
        return this.serialPortName;
    }

    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    public int getSerialBaudRate() {
        return this.serialBaudRate;
    }

    public void setSerialBaudRate(int serialBaudRate) {
        this.serialBaudRate = serialBaudRate;
    }

    public SerialPort getSerialPort() {
        return this.serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public int getSerialPortTimeout() {
        return this.serialPortTimeout;
    }

    public void setSerialPortTimeout(int serialPortTimeout) {
        this.serialPortTimeout = serialPortTimeout;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void openSerialConnection() throws Exception {
        serialPort = getSerialPort(serialPortName);
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT ,SerialPort.NO_PARITY);
        Thread.sleep( 500 );
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, serialPortTimeout, 0);
        serialPort.setFlowControl( SerialPort.FLOW_CONTROL_DISABLED );
        setInputStream(serialPort.getInputStream());
        setOutputStream(serialPort.getOutputStream());
    }

    public void close() {
        try {
            serialPort.closePort();
        } catch (Throwable var2) {
        }
        setInputStream(null);
        setOutputStream(null);
        serialPort = null;
    }

    public static Vector getSerialPortList() {
        Vector vector = new Vector();

        try {
            SerialPort[] ports = SerialPort.getCommPorts();

            for(SerialPort port : ports){
                if (port != null && port.getSystemPortName() != null ) {
                    vector.addElement( port.getSystemPortName() );
                }
            }
        } catch (Throwable var3) {
            vector = null;
        }

        return vector;
    }

    public SerialPort getSerialPort(String serialPortName) throws IOException {
        SerialPort portID = null;
        SerialPort[] ports = SerialPort.getCommPorts();
        StringBuffer buffer = new StringBuffer();
        buffer.append("Serial Port Not Found : " + serialPortName + "\n");
        buffer.append("Serial Ports Available on this Machine : \n");

        for (SerialPort port : ports){
            portID = port;
            if (portID != null ) {
                buffer.append("   " + portID.getSystemPortName() + "\n");
                if (portID.getSystemPortName().equals(serialPortName)) {
                    break;
                }
            }
        }

        if (portID == null) {
            portID = SerialPort.getCommPort(serialPortName);
            if (portID == null) {
                throw new IOException(buffer.toString());
            }
        }

        portID.openPort();
        if (portID == null) {
            throw new IOException(buffer.toString());
        } else {
            return portID;
        }
    }

    public static void addPortName(String portName) {
        StringBuffer buffer = new StringBuffer();
        try {
            SerialPort commPort = null;

            try {
                commPort = SerialPort.getCommPort( portName );

            } catch (Exception var3) {
            }

            if (commPort == null) {

                buffer.append("Serial Port not available on this Machine : \n");
            }
        } catch (Exception var4) {
        }

    }

    public static void main(String[] args) {
        new SerialManager();
        Vector portList = getSerialPortList();
        if (portList != null) {
            System.out.println(portList.toString());
        } else {
            System.out.println("No Serial Ports found");
        }

    }
}
