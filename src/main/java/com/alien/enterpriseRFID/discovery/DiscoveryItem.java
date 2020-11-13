package com.alien.enterpriseRFID.discovery;

import com.alien.enterpriseRFID.reader.AbstractReader;
import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienClassBPTReader;
import com.alien.enterpriseRFID.reader.AlienClassOEMReader;
import java.util.Date;

public class DiscoveryItem {
    public static final String NETWORK = "Network";
    public static final String SERIAL = "Serial";
    public static final String AUTOMATIC = "Automatic";
    public static final String MANUAL = "Manual";
    public boolean isUpdated = false;
    private String readerName;
    private String readerType;
    private String readerAddress;
    private String readerMACAddress;
    private String readerVersion;
    private String connection;
    private int commandPort;
    private int leaseTime;
    private long firstHeartbeat;
    private long lastHeartbeat;
    private String username;
    private String password;
    private String discoveryMethod;

    public DiscoveryItem() {
        this.setFirstHeartbeat((new Date()).getTime());
        this.setLastHeartbeat((new Date()).getTime());
        this.setUsername((String)null);
        this.setPassword((String)null);
        this.setDiscoveryMethod("Automatic");
    }

    public String getReaderName() {
        return this.readerName;
    }

    public boolean setReaderName(String readerName) {
        if (readerName == null) {
            return false;
        } else {
            readerName = readerName.trim();
            if (this.readerName != null && this.readerName.equals(readerName)) {
                return false;
            } else {
                this.readerName = readerName;
                return true;
            }
        }
    }

    public String getReaderType() {
        return this.readerType;
    }

    public boolean setReaderType(String readerType) {
        if (readerType == null) {
            return false;
        } else {
            readerType = readerType.trim();
            if (this.readerType != null && this.readerType.equals(readerType)) {
                return false;
            } else {
                this.readerType = readerType;
                return true;
            }
        }
    }

    public String getReaderVersion() {
        return this.readerVersion == null ? "???" : this.readerVersion;
    }

    public boolean setReaderVersion(String readerVersion) {
        if (readerVersion == null) {
            readerVersion = "???";
        }

        readerVersion = readerVersion.trim();
        if (this.readerVersion != null && this.readerVersion.equals(readerVersion)) {
            return false;
        } else {
            this.readerVersion = readerVersion;
            return true;
        }
    }

    public String getReaderAddress() {
        return this.readerAddress;
    }

    public boolean setReaderAddress(String readerAddress) {
        if (readerAddress == null) {
            return false;
        } else {
            readerAddress = readerAddress.trim();
            if (this.readerAddress != null && this.readerAddress.equals(readerAddress)) {
                return false;
            } else {
                this.readerAddress = readerAddress;
                return true;
            }
        }
    }

    public String getReaderMACAddress() {
        return this.readerMACAddress;
    }

    public boolean setReaderMACAddress(String readerMACAddress) {
        if (readerMACAddress == null) {
            return false;
        } else {
            readerMACAddress = readerMACAddress.trim();
            if (this.readerMACAddress != null && this.readerMACAddress.equals(readerMACAddress)) {
                return false;
            } else {
                this.readerMACAddress = readerMACAddress;
                return true;
            }
        }
    }

    public String getConnection() {
        return this.connection;
    }

    public boolean setConnection(String connection) {
        if (connection == null) {
            return false;
        } else {
            connection = connection.trim();
            if (this.connection != null && this.connection.equals(connection)) {
                return false;
            } else {
                this.connection = connection;
                return true;
            }
        }
    }

    public int getCommandPort() {
        return this.commandPort;
    }

    public boolean setCommandPort(int commandPort) {
        if (this.commandPort != commandPort) {
            this.commandPort = commandPort;
            return true;
        } else {
            return false;
        }
    }

    public boolean setCommandPort(String commandPort) {
        try {
            return this.setCommandPort(new Integer(commandPort));
        } catch (Exception var3) {
            return false;
        }
    }

    public int getLeaseTime() {
        return this.leaseTime;
    }

    public boolean setLeaseTime(int leaseTime) {
        if (this.leaseTime != leaseTime) {
            this.leaseTime = leaseTime;
            return true;
        } else {
            return false;
        }
    }

    public boolean setLeaseTime(String leaseTime) {
        try {
            return this.setLeaseTime(new Integer(leaseTime));
        } catch (Exception var3) {
            return false;
        }
    }

    public long getFirstHeartbeat() {
        return this.firstHeartbeat;
    }

    public void setFirstHeartbeat(long firstHeartbeat) {
        this.firstHeartbeat = firstHeartbeat;
    }

    public long getLastHeartbeat() {
        return this.lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDiscoveryMethod() {
        return this.discoveryMethod;
    }

    public void setDiscoveryMethod(String discoveryMethod) {
        this.discoveryMethod = discoveryMethod;
    }

    protected boolean update(DiscoveryItem newDiscoveryItem) {
        this.isUpdated = false;
        this.setLastHeartbeat((new Date()).getTime());
        this.isUpdated |= this.setLeaseTime(newDiscoveryItem.getLeaseTime());
        this.isUpdated |= this.setCommandPort(newDiscoveryItem.getCommandPort());
        this.isUpdated |= this.setReaderAddress(newDiscoveryItem.getReaderAddress());
        this.isUpdated |= this.setReaderName(newDiscoveryItem.getReaderName());
        this.isUpdated |= this.setReaderVersion(newDiscoveryItem.getReaderVersion());
        return this.isUpdated;
    }

    public String toKey() {
        StringBuffer buffer = new StringBuffer();
        if (this.readerMACAddress != null) {
            buffer.append(this.readerMACAddress + "," + this.connection);
        } else {
            buffer.append(this.readerAddress + "," + this.connection);
        }

        return buffer.toString();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Reader Name      = " + this.getReaderName() + "\n");
        buffer.append("Reader Type      = " + this.getReaderType() + "\n");
        buffer.append("Reader Address   = " + this.getReaderAddress() + "\n");
        if (this.readerMACAddress != null) {
            buffer.append("Reader MACAddress = " + this.getReaderMACAddress() + "\n");
        }

        if (this.readerVersion != null) {
            buffer.append("Reader Version    = " + this.getReaderVersion() + "\n");
        }

        buffer.append("Connection      = " + this.getConnection() + "\n");
        buffer.append("Command Port    = " + this.getCommandPort() + "\n");
        buffer.append("Lease Time      = " + this.getLeaseTime() + "\n");
        buffer.append("Discovery Method = " + this.getDiscoveryMethod() + "\n");
        return buffer.toString();
    }

    public String toTerseString() {
        return this.readerName + "(" + this.getReaderAddress() + ")";
    }

    public boolean equals(Object comparison) {
        try {
            DiscoveryItem otherItem = (DiscoveryItem)comparison;
            if (!this.getConnection().equals(otherItem.getConnection())) {
                return false;
            } else {
                String mac1 = this.getReaderMACAddress();
                String mac2 = otherItem.getReaderMACAddress();
                return mac1 != null && mac2 != null ? mac1.equals(mac2) : this.getReaderAddress().equals(otherItem.getReaderAddress());
            }
        } catch (Exception var5) {
            return false;
        }
    }

    public AlienClass1Reader getReader() throws AlienDiscoveryUnknownReaderException {
        Object reader = null;

        try {
            if (this.readerType.startsWith("Alien RFID Tag Reader")) {
                reader = new AlienClass1Reader();
            }

            if (this.readerType.indexOf("Class BPT") >= 0) {
                reader = new AlienClassBPTReader();
            } else if (this.readerType.indexOf("DLE") >= 0) {
                reader = new AlienClassOEMReader();
            } else if (this.readerType.indexOf("Unknown") >= 0 || this.readerType.indexOf("Undetermined") >= 0) {
                reader = new AlienClass1Reader();
            }

            if (reader == null) {
                throw new Exception();
            }
        } catch (Exception var3) {
            throw new AlienDiscoveryUnknownReaderException("Unknown Reader Type : \n" + this.toString());
        }

        if (this.getConnection().equals("Serial")) {
            ((AbstractReader)reader).setSerialConnection(this.getReaderAddress());
        }

        if (this.getConnection().equals("Network")) {
            ((AbstractReader)reader).setNetworkConnection(this.getReaderAddress(), this.getCommandPort());
            ((AlienClass1Reader)reader).setUsername(this.getUsername());
            ((AlienClass1Reader)reader).setPassword(this.getPassword());
        }

        return (AlienClass1Reader)reader;
    }
}
