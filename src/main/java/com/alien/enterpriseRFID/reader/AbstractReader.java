package com.alien.enterpriseRFID.reader;

import com.alien.enterpriseRFID.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class AbstractReader {
    public static final int DEBUG_OFF = 0;
    public static final int DEBUG_TEXT = 1;
    public static final int DEBUG_BYTES = 2;
    public static final int DEFAULT_TIMEOUT = 10000;
    public static final int TIMEOUTMODE_COMMAND = 0;
    public static final int TIMEOUTMODE_CHARACTER = 1;
    private SerialManager serialManager;
    protected Socket socket;
    private String networkAddress;
    private int networkPort;
    private int timeOutMilliseconds;
    private int timeOutMode;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean validateOpen;
    private int debugLevel;
    private static final int INPUT_BUFFER_SIZE = 256000;
    private byte[] inputBuffer;
    private int inputBufferPosition;
    private String readerReply;
    private String readerReplyKeyword;
    private String readerReplyValueString;
    private int readerReplyValueInt;

    public AbstractReader() {
        this.setDebugLevel(0);
        this.setValidateOpen(true);
        this.setTimeOutMilliseconds(10000);
        this.inputBuffer = new byte[256000];
        this.inputBufferPosition = 0;
        this.setTimeOutMode(0);
        boolean serialLibsPresent = false;

        try {
            Class.forName("com.fazecast.jSerialComm.SerialPort");
            serialLibsPresent = true;
        } catch (ClassNotFoundException var5) {
            try {
                Class.forName("gnu.io.CommPort");
                serialLibsPresent = true;
            } catch (ClassNotFoundException var4) {
                System.err.println("Serial Mananger Instance Failed - Serial Classes Not Present");
            }
        }

        if (serialLibsPresent) {
            this.serialManager = new SerialManager();
            this.setSerialBaudRate(115200);
        }

    }

    public int getDebugLevel() {
        return this.debugLevel;
    }

    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    public boolean isValidateOpen() {
        return this.validateOpen;
    }

    public void setValidateOpen(boolean validateOpen) {
        this.validateOpen = validateOpen;
    }

    public int getTimeOutMilliseconds() {
        return this.timeOutMilliseconds;
    }

    public void setTimeOutMilliseconds(int timeOutMilliseconds) {
        this.timeOutMilliseconds = timeOutMilliseconds;
    }

    public void setTimeOutMode(int timeoutMode) {
        this.timeOutMode = timeoutMode;
    }

    public int getTimeOutMode() {
        return this.timeOutMode;
    }

    public void setAddress(String address, int port) {
        if (port > 0) {
            this.setNetworkConnection(address, port);
        } else {
            this.setSerialConnection(address);
        }

    }

    public String getAddress() {
        if (this.networkAddress != null) {
            return "Network: " + this.networkAddress + ":" + this.networkPort;
        } else {
            return this.serialManager != null ? "Serial: " + this.serialManager.getSerialPortName() : "Unknown";
        }
    }

    public void setSerialConnection(String serialPortName) {
        this.close();
        if (this.serialManager != null) {
            this.serialManager.setSerialPortName(serialPortName);
        }

        this.networkAddress = null;
    }

    public void setSerialBaudRate(int serialBaudRate) {
        this.close();
        if (this.serialManager != null) {
            this.serialManager.setSerialBaudRate(serialBaudRate);
        }

    }

    public void setNetworkConnection(String networkAddress, int networkPort) {
        this.close();
        this.networkAddress = networkAddress;
        this.networkPort = networkPort;
        if (this.serialManager != null) {
            this.serialManager.setSerialPortName((String)null);
        }

    }

    public void setConnection(String connectionMethod) {
        int colonPosition = connectionMethod.indexOf(":");
        if (colonPosition > 0) {
            String networkAddress = connectionMethod.substring(0, colonPosition);
            String networkPort = connectionMethod.substring(colonPosition + 1);
            int port = Integer.valueOf(networkPort);
            this.setNetworkConnection(networkAddress, port);
        } else {
            this.setSerialConnection(connectionMethod);
        }

    }

    public void setConnection(String networkAddress, int networkPort) {
        this.setNetworkConnection(networkAddress, networkPort);
    }

    public void open() throws AlienReaderNotValidException, AlienReaderTimeoutException, AlienReaderConnectionRefusedException, AlienReaderConnectionException {
        if (!this.isOpen()) {
            this.close();

            StringBuffer result;
            try {
                if (this.networkAddress != null) {
                    this.openNetworkConnection();
                } else {
                    this.openSerialConnection();
                }

                if (this.isValidateOpen()) {
                    try {
                        this.receiveString(false);
                        this.doReaderCommand("");
                    } catch (AlienReaderException var4) {
                    }

                    if (this instanceof AlienClassOEMReader) {
                        this.doReaderCommand("Get ReaderVersion");
                    } else {
                        String readerName = null;

                        try {
                            this.sendString("\u0001Get ReaderName\r\n");
                            readerName = this.receiveString(true);
                        } catch (AlienReaderCommandErrorException var3) {
                        }

                        if (readerName != null && !readerName.startsWith("ReaderName = ")) {
                            result = new StringBuffer();
                            result.append("Not A Valid Alien RFID Reader.\n");
                            result.append("A device has been found at \"" + this.getAddress() + "\" but is not recognized as a valid Alien RFID Reader.\n");
                            throw new AlienReaderNotValidException(result.toString());
                        }
                    }
                }

            } catch (AlienReaderTimeoutException var5) {
                this.close();
                result = new StringBuffer();
                result.append("Time Out trying to open a connection with a Reader.\n");
                result.append("There is either no Reader connected at \"" + this.getAddress() + "\" ");
                result.append("or the Reader is switched off.\n");
                result.append("Please check all cables to the Reader and make sure the Reader power is on.\n");
                throw new AlienReaderTimeoutException(result.toString());
            } catch (AlienReaderCommandErrorException var6) {
                result = new StringBuffer();
                result.append("Not A Valid Alien RFID Reader.\n");
                result.append("A device has been found at \"" + this.getAddress() + "\" but the reader couldn't report a valid ReaderType.\n");
                result.append(var6.getMessage());
                throw new AlienReaderNotValidException(result.toString());
            }
        }
    }

    public boolean isOpen() {
        if (this.socket != null) {
            return true;
        } else {
            return this.serialManager != null && this.serialManager.getSerialPort() != null;
        }
    }

    protected void openSerialConnection() throws AlienReaderConnectionException {
        if (this.serialManager == null) {
            StringBuffer buffer = new StringBuffer("No Serial Ports Available.\n");
            buffer.append("The Java Class Libraries for Serial Communication are not present on this machine.\n");
            throw new AlienReaderConnectionException(buffer.toString());
        } else {
            if (this.debugLevel > 0) {
                System.out.println("Opening Serial Connection:");
                System.out.println("    Serial Port = " + this.serialManager.getSerialPortName());
                System.out.println("    Baud Rate = " + this.serialManager.getSerialBaudRate());
            }

            this.serialManager.setSerialPortTimeout(this.getTimeOutMilliseconds());

            try {
                this.serialManager.openSerialConnection();
            } catch (Exception var4) {
                this.close();
                StringBuffer result = new StringBuffer();
                if (var4.getMessage().indexOf("Port currently owned") >= 0) {
                    result.append(var4.getMessage() + ".\n");
                    result.append("If a Reader is connected to this port, make sure that no\n");
                    result.append("other software is running that could also be using this port.\n");
                    result.append("For example turn off PalmPilot HotSync or Microsoft PDA ActiveSync software.\n");
                    throw new AlienReaderConnectionException(result.toString());
                }

                throw new AlienReaderConnectionException(var4.getMessage());
            }

            this.setInputStream(this.serialManager.getInputStream());
            this.setOutputStream(this.serialManager.getOutputStream());

            try {
                Thread.sleep(200L);
            } catch (InterruptedException var3) {
            }

        }
    }

    protected void openNetworkConnection() throws AlienReaderConnectionRefusedException, AlienReaderTimeoutException, AlienReaderConnectionException {
        if (this.debugLevel > 0) {
            System.out.println("Opening Network Connection:");
            System.out.println("    Network Address = " + this.networkAddress);
            System.out.println("    Network Port = " + this.networkPort);
        }

        try {
            this.socket = TimedSocket.getSocket(this.networkAddress, this.networkPort, this.timeOutMilliseconds);
            this.setInputStream(this.socket.getInputStream());
            this.setOutputStream(this.socket.getOutputStream());
        } catch (UnknownHostException var3) {
            this.close();
            throw new AlienReaderConnectionException("Unknown Host: " + this.networkAddress + " is not a valid network address");
        } catch (IOException var4) {
            this.close();
            StringBuffer buffer;
            if (var4.getMessage().indexOf("timed out") >= 0) {
                buffer = new StringBuffer("Connection Time Out\n");
                buffer.append("Time Out trying to communicate with a Reader.\n");
                buffer.append("There is either no Reader at the specified address or the Reader is switched off.\n");
                buffer.append("Please check all cables to the Reader and make sure the Reader power is on.\n");
                throw new AlienReaderTimeoutException(buffer.toString());
            } else if (var4.getMessage().indexOf("Connection refused") >= 0) {
                buffer = new StringBuffer("Connection Refused\n");
                buffer.append("The device is either a Reader that is already busy and accepting no more connections \n");
                buffer.append("or the device is not an Alien RFID Reader.\n");
                throw new AlienReaderConnectionRefusedException(buffer.toString());
            } else {
                throw new AlienReaderConnectionException(var4.getMessage());
            }
        }
    }

    public void test() throws AlienReaderConnectionException {
        try {
            this.open();
        } catch (AlienReaderException var6) {
            throw new AlienReaderConnectionException(var6.getMessage());
        } finally {
            this.close();
        }

    }

    public void close() {
        if (this.networkAddress != null) {
            try {
                this.sendString("q\r\n");
            } catch (AlienReaderException var6) {
            }

            try {
                Thread.sleep(10L);
            } catch (InterruptedException var5) {
            }
        }

        try {
            this.inputStream.close();
        } catch (Exception var4) {
        }

        try {
            this.outputStream.close();
        } catch (Exception var3) {
        }

        try {
            this.socket.close();
        } catch (Exception var2) {
        }

        if (this.serialManager != null) {
            this.serialManager.close();
        }

        this.socket = null;
        this.inputStream = null;
        this.outputStream = null;
        this.inputBufferPosition = 0;
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

    public synchronized String doReaderCommand(String command) throws AlienReaderTimeoutException, AlienReaderConnectionException, AlienReaderCommandErrorException {
        this.readerReply = "";
        this.readerReplyKeyword = "";
        this.readerReplyValueString = "";
        this.readerReplyValueInt = 0;
        if (command != null && command.length() > 300) {
            System.err.println("Truncating Command to 300 bytes.");
            command = command.substring(0, 300);
        }

        this.sendString("\u0001" + command + "\r\n");

        try {
            this.readerReply = this.receiveString(true).trim();
            this.readerReplyValueString = this.readerReply;
        } catch (AlienReaderTimeoutException var5) {
            throw new AlienReaderTimeoutException("Reader Time Out waiting for Reply to command: \"" + command + "\".");
        } catch (AlienReaderException var6) {
            this.readerReply = "Error: " + var6.getMessage();
        }

        if (this.readerReply.startsWith("Boot> ")) {
            throw new AlienReaderConnectionException("Reader is still Booting... please try again in a few seconds.");
        } else {
            int index = this.readerReply.indexOf("=");
            if (index >= 0) {
                this.readerReplyKeyword = this.readerReply.substring(0, index - 1).trim();
                this.readerReplyValueString = this.readerReply.substring(index + 1).trim();

                try {
                    this.readerReplyValueInt = new Integer(this.readerReplyValueString);
                } catch (NumberFormatException var4) {
                }
            }

            if (!this.readerReply.toLowerCase().startsWith("error") && !this.readerReplyValueString.toLowerCase().startsWith("error")) {
                return this.readerReply;
            } else {
                throw new AlienReaderCommandErrorException(this.readerReply);
            }
        }
    }

    public String getReaderReply() {
        return this.readerReply;
    }

    public String getReaderReplyKeyword() {
        return this.readerReplyKeyword;
    }

    public String getReaderReplyValueString() {
        return this.readerReplyValueString;
    }

    public int getReaderReplyValueInt() {
        return this.readerReplyValueInt;
    }

    public void clearInputBuffer() {
        this.inputBufferPosition = 0;
    }

    public void sendString(String text) throws AlienReaderConnectionException {
        if (this.isOpen()) {
            if (text == null) {
                text = "";
            }

            byte[] byteArray = text.getBytes();
            if (this.debugLevel == 1) {
                System.out.println("\nWrite: " + text.trim());
            }

            if (this.debugLevel == 2) {
                System.out.println("\nWrite: " + Converters.toHexString(byteArray, 0, byteArray.length, " "));
                System.out.println("     = " + Converters.toAsciiString(byteArray));
            }

            try {
                this.outputStream.write(byteArray);
                this.outputStream.flush();
            } catch (IOException var4) {
                throw new AlienReaderConnectionException(var4.getMessage());
            }
        }
    }

    public String receiveString(boolean blockForInput) throws AlienReaderTimeoutException, AlienReaderCommandErrorException {
        if (!this.isOpen()) {
            return "";
        } else {
            long startTime = System.currentTimeMillis();
            boolean exit = false;
            if (!blockForInput) {
                exit = true;
            }

            int nullPosition = -1;
            int readLength = 0;
            int currentBufferPosition = this.inputBufferPosition;
            boolean var8 = false;

            do {
                try {
                    int i;
                    int availableBytes;
                    while(this.inputStream != null && (availableBytes = this.inputStream.available()) > 0) {
                        readLength = this.inputStream.read(this.inputBuffer, currentBufferPosition, availableBytes);
                        if (readLength > 0) {
                            if (this.debugLevel == 2) {
                                System.out.println(" Read: " + Converters.toHexString(this.inputBuffer, currentBufferPosition, readLength, " "));
                                System.out.println("     = " + Converters.toAsciiString(this.inputBuffer, currentBufferPosition, readLength));
                            } else if (this.debugLevel == 1) {
                                System.out.println(" Read: " + Converters.toAsciiString(this.inputBuffer, currentBufferPosition, readLength));
                            }

                            if (this.getTimeOutMode() == 1) {
                                startTime = System.currentTimeMillis();
                            }

                            currentBufferPosition += readLength;

                            for(i = 0; i < currentBufferPosition; ++i) {
                                if (i < 256000 && this.inputBuffer[i] == 0) {
                                    exit = true;
                                    nullPosition = i;
                                }
                            }
                        }

                        if (exit) {
                            break;
                        }

                        try {
                            Thread.sleep(1L);
                        } catch (InterruptedException var11) {
                        }
                    }

                    if (!exit) {
                        for(i = 0; i < currentBufferPosition; ++i) {
                            if (i < 256000 && this.inputBuffer[i] == 0) {
                                exit = true;
                                nullPosition = i;
                                break;
                            }
                        }
                    }
                } catch (IOException var12) {
                    if (var12.getMessage().toLowerCase().indexOf("rxreadycount") >= 0) {
                        throw new AlienReaderCommandErrorException("Stream closed");
                    }

                    throw new AlienReaderCommandErrorException(var12.getMessage());
                }

                if (System.currentTimeMillis() - startTime > (long)this.getTimeOutMilliseconds()) {
                    if (!this.isOpen()) {
                        return "";
                    }

                    if (blockForInput) {
                        throw new AlienReaderTimeoutException("Time Out Waiting for Input Data");
                    }
                }
            } while(!exit);

            String result = "";
            if (nullPosition >= 0) {
                result = new String(this.inputBuffer, 0, nullPosition + 1);
                int remainingBytes = currentBufferPosition - (nullPosition + 1);
                System.arraycopy(this.inputBuffer, nullPosition + 1, this.inputBuffer, 0, remainingBytes);
                this.inputBufferPosition = remainingBytes;
            } else {
                result = new String(this.inputBuffer, 0, currentBufferPosition);
                this.inputBufferPosition = 0;
            }

            return result;
        }
    }

    public String receiveLine() throws AlienReaderTimeoutException, AlienReaderCommandErrorException {
        if (!this.isOpen()) {
            return "";
        } else {
            long startTime = System.currentTimeMillis();
            boolean exit = false;
            int terminatorPosition = -1;
            int readLength = 0;
            int currentBufferPosition = this.inputBufferPosition;
            boolean var7 = false;

            do {
                try {
                    int i;
                    int availableBytes;
                    while((availableBytes = this.inputStream.available()) > 0) {
                        readLength = this.inputStream.read(this.inputBuffer, currentBufferPosition, availableBytes);
                        if (readLength > 0) {
                            if (this.debugLevel == 2) {
                                System.out.println(" Read: " + Converters.toHexString(this.inputBuffer, currentBufferPosition, readLength, " "));
                                System.out.println("     = " + Converters.toAsciiString(this.inputBuffer, currentBufferPosition, readLength));
                            }

                            currentBufferPosition += readLength;

                            for(i = 0; i < currentBufferPosition - 1; ++i) {
                                if (i + 1 < 256000 && this.inputBuffer[i] == 13 && this.inputBuffer[i + 1] == 10) {
                                    exit = true;
                                    terminatorPosition = i;
                                    break;
                                }
                            }
                        }
                    }

                    if (!exit) {
                        for(i = 0; i < currentBufferPosition - 1; ++i) {
                            if (i + 1 < 256000 && this.inputBuffer[i] == 13 && this.inputBuffer[i + 1] == 10) {
                                exit = true;
                                terminatorPosition = i;
                                break;
                            }
                        }
                    }
                } catch (IOException var10) {
                    throw new AlienReaderCommandErrorException(var10.getMessage());
                }

                if (System.currentTimeMillis() - startTime > (long)this.getTimeOutMilliseconds()) {
                    if (!this.isOpen()) {
                        return "";
                    }

                    throw new AlienReaderTimeoutException("Time Out Waiting for Input Data");
                }
            } while(!exit);

            String result = "";
            if (terminatorPosition >= 0) {
                result = new String(this.inputBuffer, 0, terminatorPosition + 2);
                int remainingBytes = currentBufferPosition - (terminatorPosition + 2);
                System.arraycopy(this.inputBuffer, terminatorPosition + 2, this.inputBuffer, 0, remainingBytes);
                this.inputBufferPosition = remainingBytes;
            } else {
                result = new String(this.inputBuffer, 0, currentBufferPosition);
                this.inputBufferPosition = 0;
            }

            if (this.debugLevel == 1) {
                System.out.println(" Read: " + result.trim());
            }

            return result;
        }
    }

    public int bringIntoRange(int value, int min, int max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    protected void dumpStack() {
        try {
            throw new Exception("Stack Dump");
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }
}
