package com.alien.enterpriseRFID.reader;

import com.alien.enterpriseRFID.externalio.ExternalIO;
import com.alien.enterpriseRFID.externalio.ExternalIOUtil;
import com.alien.enterpriseRFID.tags.Tag;
import com.alien.enterpriseRFID.tags.TagUtil;
import com.alien.enterpriseRFID.util.Converters;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class AlienClass1Reader extends AbstractReader{
    public static final int ON = 1;
    public static final int OFF = 0;
    public static final String GLOBAL_SCROLL = "Global Scroll";
    public static final String INVENTORY = "Inventory";
    public static final String XML_FORMAT = "XML";
    public static final String TEXT_FORMAT = "Text";
    public static final String CUSTOM_FORMAT = "Custom";
    public static final String TERSE_FORMAT = "Terse";
    public static final String ALL_MASK = "All";
    public static final String RFMOD_STD = "STD";
    public static final String RFMOD_DRM = "DRM";
    public static final String RFMOD_HS = "HS";
    public static final String FUNCTION_READER = "Reader";
    public static final String FUNCTION_PROGRAMMER = "Programmer";
    public static final int PROG_CLASS0 = 0;
    public static final int PROG_CLASS1GEN1 = 1;
    public static final int PROG_CLASS1GEN2 = 2;
    public static final int CLASS1GEN1_QUARK = 1;
    public static final int CLASS1GEN1_OMEGA = 2;
    public static final int CLASS1GEN1_64BITS = 3;
    public static final int CLASS1GEN1_LEPTON = 4;
    public static final int CLASS1GEN1_96BITS = 4;
    public static final int CLASS1GEN1_ALL = 7;
    public static final int CLASS0 = 8;
    public static final int CLASS1GEN2 = 16;
    public static final String DATA_INC_OFF = "OFF";
    public static final String DATA_INC_SUCCESS = "Success";
    public static final String DATA_INC_FAIL = "Fail";
    public static final String DATA_INC_ALWAYS = "Always";
    public static final String DATA_INC_WRITE = "Write";
    public static final String LOCKTYPE_LOCK = "Lock";
    public static final String LOCKTYPE_PERMALOCK = "PermaLock";
    public static final String LOCKTYPE_PERMAUNLOCK = "PermaUnlock";
    public static final String G2TARGET_A = "A";
    public static final String G2TARGET_B = "B";
    public static final String G2TARGET_AB = "AB";
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final int LOG_OFF = 0;
    public static final int LOG_ON = 1;
    private int logLevel = 0;
    private String logPrefix = "AlienClass1Reader.";
    private String username = "alien";
    private String password = "password";

    public AlienClass1Reader() {
    }

    public AlienClass1Reader(String address) {
        this.setConnection(address);
    }

    public AlienClass1Reader(String networkAddress, int networkPort) {
        this.setConnection(networkAddress, networkPort);
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    private void log(String logText) {
        if (this.logLevel != 0) {
            System.out.println(logText);
        }
    }

    public String getUsername() {
        this.log(this.logPrefix + "getUsername() => \"" + this.username + "\"");
        return this.username;
    }

    public void setUsername(String username) {
        this.log(this.logPrefix + "setUsername(\"" + username + "\")");
        if (username == null) {
            username = "";
        }

        this.username = username;
    }

    public String getReaderUsername() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderUsername() => ");
        this.doReaderCommand("get username");
        this.log("=> ReaderUsername = " + this.getReaderReplyValueString());
        return this.getReaderReplyValueString();
    }

    public void setReaderUsername(String username) throws AlienReaderException {
        this.log(this.logPrefix + "setReaderUsername(\"" + username + "\")");
        if (username == null) {
            username = "";
        }

        if (username.length() > 255) {
            throw new AlienReaderInvalidArgumentException("Error: The username and password must be 255 characters or fewer.");
        } else {
            this.doReaderCommand("set username=" + username);
            this.username = username;
        }
    }

    public String getPassword() {
        this.log(this.logPrefix + "getPassword() => \"" + this.password + "\"");
        return this.password;
    }

    public void setPassword(String password) {
        this.log(this.logPrefix + "setPassword(\"" + password + "\")");
        if (password == null) {
            password = "";
        }

        this.password = password;
    }

    public String getReaderPassword() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderPassword() => ");
        this.doReaderCommand("get password");
        this.log("=> ReaderPassword = " + this.getReaderReplyValueString());
        return this.getReaderReplyValueString();
    }

    public void setReaderPassword(String password) throws AlienReaderException {
        this.log(this.logPrefix + "setReaderPassword(\"" + password + "\")");
        if (password == null) {
            password = "";
        }

        if (password.length() > 255) {
            throw new AlienReaderInvalidArgumentException("Error: The username and password must be 255 characters or fewer.");
        } else {
            this.doReaderCommand("set password=" + password);
            this.password = password;
        }
    }

    protected void openNetworkConnection() throws AlienReaderTimeoutException, AlienReaderConnectionRefusedException, AlienReaderConnectionException {
        this.log(this.logPrefix + "openNetworkConnection()");
        super.openNetworkConnection();
        if (this.isValidateOpen()) {
            try {
                String reply = this.receiveString(true);
                if (reply.indexOf("Busy") >= 0) {
                    throw new AlienReaderConnectionRefusedException(reply);
                } else {
                    this.log("Sending Username & Password =>");
                    this.doReaderCommand(this.getUsername());
                    this.doReaderCommand(this.getPassword());
                }
            } catch (AlienReaderCommandErrorException var2) {
                throw new AlienReaderConnectionException(var2.getMessage());
            }
        }
    }

    public String getReaderName() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderName() => ");
        this.doReaderCommand("Get ReaderName");
        return this.getReaderReplyValueString();
    }

    public void setReaderName(String readerName) throws AlienReaderException {
        this.log(this.logPrefix + "setReaderName(\"" + readerName + "\")");
        this.doReaderCommand("Set ReaderName=" + readerName);
    }

    public String getReaderType() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderType() => ");
        this.doReaderCommand("Get ReaderType");
        return this.getReaderReplyValueString();
    }

    public String getReaderVersion() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderVersion() => ");
        this.doReaderCommand("Get ReaderVersion");
        return this.getReaderReplyValueString();
    }

    public int getReaderNumber() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderNumber() => ");
        this.doReaderCommand("get ReaderNumber");
        return this.getReaderReplyValueInt();
    }

    public void setReaderNumber(int readerNumber) throws AlienReaderException {
        this.log(this.logPrefix + "setReaderNumber(" + readerNumber + ")");
        this.doReaderCommand("set ReaderNumber = " + readerNumber);
    }

    public String getMyData() throws AlienReaderException {
        this.log(this.logPrefix + "getMyData() => ");
        this.doReaderCommand("Get MyData");
        return this.getReaderReplyValueString();
    }

    public void setMyData(String myData) throws AlienReaderException {
        this.log(this.logPrefix + "setMyData(\"" + myData + "\")");
        this.doReaderCommand("Set MyData=" + myData);
    }

    public int getReaderBaudRate() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderBaudRate() => ");
        this.doReaderCommand("get BaudRate");
        switch(this.getReaderReplyValueInt()) {
            case 0:
                return 115200;
            case 1:
                return 57600;
            case 2:
                return 38400;
            case 3:
                return 19200;
            case 4:
                return 9600;
            default:
                return this.getReaderReplyValueInt();
        }
    }

    public void setReaderBaudRate(int readerBaudRate) throws AlienReaderException {
        this.log(this.logPrefix + "setReaderBaudRate(" + readerBaudRate + ")");
        switch(readerBaudRate) {
            case 9600:
                readerBaudRate = 4;
                break;
            case 19200:
                readerBaudRate = 3;
                break;
            case 38400:
                readerBaudRate = 2;
                break;
            case 57600:
                readerBaudRate = 1;
                break;
            case 115200:
                readerBaudRate = 0;
        }

        this.doReaderCommand("set BaudRate = " + readerBaudRate);
    }

    public int getUptime() throws AlienReaderException {
        this.log(this.logPrefix + "getUptime() => ");
        this.doReaderCommand("Get Uptime");
        return this.getReaderReplyValueInt();
    }

    public int getMaxAntenna() throws AlienReaderException {
        this.log(this.logPrefix + "getMaxAntenna() => ");
        this.doReaderCommand("Get MaxAntenna");
        return this.getReaderReplyValueInt();
    }

    public String getAntennaSequence() throws AlienReaderException {
        this.log(this.logPrefix + "getAntennaSequence() => ");
        this.doReaderCommand("Get AntennaSequence");
        String readerReply = this.getReaderReplyValueString();
        String antennaSequence = readerReply;
        int pos = readerReply.indexOf("*");
        if (pos >= 0) {
            antennaSequence = readerReply.substring(0, pos) + readerReply.substring(pos + 1);
        }

        return antennaSequence;
    }

    public void setAntennaSequence(String antennaSequence) throws AlienReaderException {
        this.log(this.logPrefix + "setAntennaSequence(\"" + antennaSequence + "\")");
        this.doReaderCommand("Set AntennaSequence=" + antennaSequence);
    }

    public String getMACAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getMACAddress() => ");
        this.doReaderCommand("get MACAddress");
        return this.getReaderReplyValueString();
    }

    public int getRFAttenuation() throws AlienReaderException {
        this.log(this.logPrefix + "getRFAttenuation() => ");
        this.doReaderCommand("get RFAttenuation");
        String[] parts = this.getReaderReplyValueString().split(" ");
        return parts != null && parts.length > 0 ? new Integer(parts[0]) : 0;
    }

    public int getRFAttenuation(int antenna) throws AlienReaderException {
        this.log(this.logPrefix + "getRFAttenuation(" + antenna + ") => ");
        this.doReaderCommand("get RFAttenuation " + antenna);
        String[] parts = this.getReaderReplyValueString().split(" ");
        return parts != null && parts.length > 0 ? new Integer(parts[0]) : 0;
    }

    public int[] getRFAttenuations() throws AlienReaderException {
        this.log(this.logPrefix + "getRFAttenuations() => ");
        this.doReaderCommand("get RFAttenuation");
        int[] levels = new int[2];
        String[] parts = this.getReaderReplyValueString().split(" ");
        if (parts != null && parts.length > 0) {
            levels[0] = new Integer(parts[0]);
        }

        if (parts != null && parts.length > 1) {
            levels[1] = new Integer(parts[1]);
        } else {
            levels[1] = levels[0];
        }

        return levels;
    }

    public int[] getRFAttenuations(int antenna) throws AlienReaderException {
        this.log(this.logPrefix + "getRFAttenuations(" + antenna + ") => ");
        this.doReaderCommand("get RFAttenuation " + antenna);
        int[] levels = new int[2];
        String[] parts = this.getReaderReplyValueString().split(" ");
        if (parts != null && parts.length > 0) {
            levels[0] = new Integer(parts[0]);
        }

        if (parts != null && parts.length > 1) {
            levels[1] = new Integer(parts[1]);
        } else {
            levels[1] = levels[0];
        }

        return levels;
    }

    public void setRFAttenuation(int attenuation) throws AlienReaderException {
        this.log(this.logPrefix + "setRFAttenuation(" + attenuation + ")");
        this.doReaderCommand("set RFAttenuation = " + attenuation);
    }

    public void setRFAttenuation(int antenna, int attenuation) throws AlienReaderException {
        this.log(this.logPrefix + "setRFAttenuation(" + antenna + ", " + attenuation + ")");
        this.doReaderCommand("set RFAttenuation = " + antenna + " " + attenuation);
    }

    public void setRFAttenuations(int antenna, int readLevel, int writeLevel) throws AlienReaderException {
        this.log(this.logPrefix + "setRFAttenuations(" + antenna + ", " + readLevel + ", " + writeLevel + ")");
        this.doReaderCommand("set RFAttenuation = " + antenna + " " + readLevel + " " + writeLevel);
    }

    public int getRFLevel() throws AlienReaderException {
        this.log(this.logPrefix + "getRFLevel() => ");
        this.doReaderCommand("get RFLevel");
        String[] parts = this.getReaderReplyValueString().split(" ");
        return parts != null && parts.length > 0 ? new Integer(parts[0]) : 0;
    }

    public int getRFLevel(int antenna) throws AlienReaderException {
        this.log(this.logPrefix + "getRFLevel(" + antenna + ") => ");
        this.doReaderCommand("get RFLevel " + antenna);
        String[] parts = this.getReaderReplyValueString().split(" ");
        return parts != null && parts.length > 0 ? new Integer(parts[0]) : 0;
    }

    public int[] getRFLevels() throws AlienReaderException {
        this.log(this.logPrefix + "getRFLevels() => ");
        this.doReaderCommand("get RFLevel");
        int[] levels = new int[2];
        String[] parts = this.getReaderReplyValueString().split(" ");
        if (parts != null && parts.length > 0) {
            levels[0] = new Integer(parts[0]);
        }

        if (parts != null && parts.length > 1) {
            levels[1] = new Integer(parts[1]);
        } else {
            levels[1] = levels[0];
        }

        return levels;
    }

    public int[] getRFLevels(int antenna) throws AlienReaderException {
        this.log(this.logPrefix + "getRFLevels(" + antenna + ") => ");
        this.doReaderCommand("get RFLevel " + antenna);
        int[] levels = new int[2];
        String[] parts = this.getReaderReplyValueString().split(" ");
        if (parts != null && parts.length > 0) {
            levels[0] = new Integer(parts[0]);
        }

        if (parts != null && parts.length > 1) {
            levels[1] = new Integer(parts[1]);
        } else {
            levels[1] = levels[0];
        }

        return levels;
    }

    public void setRFLevel(int level) throws AlienReaderException {
        this.log(this.logPrefix + "setRFLevel(" + level + ")");
        this.doReaderCommand("set RFLevel = " + level);
    }

    public void setRFLevel(int antenna, int level) throws AlienReaderException {
        this.log(this.logPrefix + "setRFLevel(" + antenna + ", " + level + ")");
        this.doReaderCommand("set RFLevel = " + antenna + " " + level);
    }

    public void setRFLevels(int antenna, int readLevel, int writeLevel) throws AlienReaderException {
        this.log(this.logPrefix + "setRFLevels(" + antenna + ", " + readLevel + ", " + writeLevel + ")");
        this.doReaderCommand("set RFLevel = " + antenna + " " + readLevel + " " + writeLevel);
    }

    public String getReaderFunction() throws AlienReaderException {
        this.log(this.logPrefix + "getReaderFunction() => ");
        this.doReaderCommand("get Function");
        return this.getReaderReplyValueString();
    }

    public void setReaderFunction(String function) throws AlienReaderException {
        this.log(this.logPrefix + "setReaderFunction(\"" + function + "\")");
        this.doReaderCommand("set Function = " + function);
    }

    public String getRFModulation() throws AlienReaderException {
        this.log(this.logPrefix + "getRFModulation() => ");
        this.doReaderCommand("get RFModulation");
        return this.getReaderReplyValueString();
    }

    public void setRFModulation(String rfModulationMode) throws AlienReaderException {
        this.log(this.logPrefix + "setRFModulation(\"" + rfModulationMode + "\")");
        this.doReaderCommand("set RFModulation = " + rfModulationMode);
    }

    public void setFactorySettings() throws AlienReaderException {
        this.log(this.logPrefix + "setFactorySettings() =>");
        this.setFactorySettings(true);
    }

    public void setFactorySettings(boolean waitForReboot) throws AlienReaderException {
        this.log(this.logPrefix + "setFactorySettings(" + waitForReboot + ")");
        this.doReaderCommand("factorysettings");
        String reply = this.getReaderReply();
        boolean readerWantsToReboot = reply.indexOf("Rebooting System...") >= 0;
        if (readerWantsToReboot && waitForReboot) {
            this.waitForReboot();
        }

    }

    public void saveSettings() throws AlienReaderException {
        this.log(this.logPrefix + "saveSettings()");
        this.doReaderCommand("save");
    }

    public void reboot(boolean waitForReboot) throws AlienReaderException {
        this.log(this.logPrefix + "reboot(" + waitForReboot + ")");
        this.doReaderCommand("reboot");
        if (waitForReboot) {
            this.waitForReboot();
        }

    }

    public void waitForReboot() throws AlienReaderException {
        this.log(this.logPrefix + "waitForReboot()");
        boolean exit;
        long startTime;
        if (super.socket == null) {
            exit = false;
            startTime = System.currentTimeMillis();

            do {
                String result = this.receiveLine();
                if (result.startsWith("Boot> Ready")) {
                    this.doReaderCommand("");
                    exit = true;
                } else {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException var8) {
                    }
                }

                if (System.currentTimeMillis() - startTime > 90000L) {
                    throw new AlienReaderTimeoutException("Timeout Waiting for Reader to Reboot");
                }
            } while(!exit);

            this.clearInputBuffer();
        } else {
            this.close();
            exit = false;
            startTime = System.currentTimeMillis();

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException var7) {
            }

            do {
                if (System.currentTimeMillis() - startTime > 90000L) {
                    throw new AlienReaderTimeoutException("Timeout Waiting for Reader to Reboot");
                }

                try {
                    Thread.sleep(1000L);
                    this.open();
                    exit = true;
                } catch (Exception var6) {
                }
            } while(!exit);
        }

    }

    public String getInfo() throws AlienReaderException {
        this.log(this.logPrefix + "getInfo() => ");
        this.doReaderCommand("i");
        return this.getReaderReplyValueString();
    }

    public int getDHCP() throws AlienReaderException {
        this.log(this.logPrefix + "getDHCP() => ");
        this.doReaderCommand("get DHCP");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setDHCP(int dhcp) throws AlienReaderException {
        if (dhcp == 1) {
            this.log(this.logPrefix + "setDHCP(\"ON\")");
            this.doReaderCommand("set DHCP=ON");
        } else if (dhcp == 0) {
            this.log(this.logPrefix + "setDHCP(\"OFF\")");
            this.doReaderCommand("set DHCP=OFF");
        } else {
            this.log(this.logPrefix + "setDHCP(\"" + dhcp + "\")");
            this.doReaderCommand("set DHCP=" + dhcp);
        }

    }

    public String getIPAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getIPAddress() => ");
        this.doReaderCommand("get IPAddress");
        return this.getReaderReplyValueString();
    }

    public void setIPAddress(String ipAddress) throws AlienReaderException {
        this.log(this.logPrefix + "setIPAddress(\"" + ipAddress + "\")");
        this.doReaderCommand("set IPAddress=" + ipAddress.trim());
    }

    public String getGateway() throws AlienReaderException {
        this.log(this.logPrefix + "getGateway() => ");
        this.doReaderCommand("get Gateway");
        return this.getReaderReplyValueString();
    }

    public void setGateway(String gatewayAddress) throws AlienReaderException {
        this.log(this.logPrefix + "setGateway(\"" + gatewayAddress + "\")");
        this.doReaderCommand("set Gateway=" + gatewayAddress.trim());
    }

    public String getNetmask() throws AlienReaderException {
        this.log(this.logPrefix + "getNetmask() => ");
        this.doReaderCommand("get Netmask");
        return this.getReaderReplyValueString();
    }

    public void setNetmask(String netmask) throws AlienReaderException {
        this.log(this.logPrefix + "setNetmask(\"" + netmask + "\")");
        this.doReaderCommand("set Netmask=" + netmask.trim());
    }

    public String getDNS() throws AlienReaderException {
        this.log(this.logPrefix + "getDNS() => ");
        this.doReaderCommand("get DNS");
        return this.getReaderReplyValueString();
    }

    public void setDNS(String dns) throws AlienReaderException {
        this.log(this.logPrefix + "setDNS(\"" + dns + "\")");
        this.doReaderCommand("set DNS=" + dns.trim());
    }

    public String getHostname() throws AlienReaderException {
        this.log(this.logPrefix + "getHostname() => ");
        this.doReaderCommand("get Hostname");
        return this.getReaderReplyValueString();
    }

    public void setHostname(String hostname) throws AlienReaderException {
        this.log(this.logPrefix + "setHostname(\"" + hostname + "\")");
        this.doReaderCommand("set Hostname=" + hostname.trim());
    }

    public int getNetworkTimeout() throws AlienReaderException {
        this.log(this.logPrefix + "getNetworkTimeout() => ");
        this.doReaderCommand("get NetworkTimeout");
        return this.getReaderReplyValueInt();
    }

    public void setNetworkTimeout(int networkTimeout) throws AlienReaderException {
        this.log(this.logPrefix + "setNetworkTimeout(\"" + networkTimeout + "\")");
        this.doReaderCommand("set NetworkTimeout=" + networkTimeout);
    }

    public String getHeartbeatAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getHeartbeatAddress() => ");
        this.doReaderCommand("get HeartbeatAddress");
        return this.getReaderReplyValueString();
    }

    public void setHeartbeatAddress(String heartbeatAddress) throws AlienReaderException {
        this.log(this.logPrefix + "setHeartbeatAddress(\"" + heartbeatAddress + "\")");
        this.doReaderCommand("set HeartbeatAddress=" + heartbeatAddress.trim());
    }

    public int getHeartbeatPort() throws AlienReaderException {
        this.log(this.logPrefix + "getHeartbeatPort() => ");
        this.doReaderCommand("get HeartbeatPort");
        return this.getReaderReplyValueInt();
    }

    public void setHeartbeatPort(int heartbeatPort) throws AlienReaderException {
        this.log(this.logPrefix + "setHeartbeatPort(\"" + heartbeatPort + "\")");
        this.doReaderCommand("set HeartbeatPort=" + heartbeatPort);
    }

    public int getHeartbeatTime() throws AlienReaderException {
        this.log(this.logPrefix + "getHeartbeatTime() => ");
        this.doReaderCommand("get HeartbeatTime");
        return this.getReaderReplyValueInt();
    }

    public void setHeartbeatTime(int heartbeatTime) throws AlienReaderException {
        this.log(this.logPrefix + "setHeartbeatTime(" + heartbeatTime + ")");
        this.doReaderCommand("set HeartBeatTime=" + heartbeatTime);
    }

    public int getHeartbeatCount() throws AlienReaderException {
        this.log(this.logPrefix + "getHeartbeatCount() => ");
        this.doReaderCommand("get HeartbeatCount");
        return this.getReaderReplyValueInt();
    }

    public void setHeartbeatCount(int heartbeatCount) throws AlienReaderException {
        this.log(this.logPrefix + "setHeartbeatCount(" + heartbeatCount + ")");
        this.doReaderCommand("set HeartbeatCount=" + heartbeatCount);
    }

    public String heartbeatNow() throws AlienReaderException {
        this.log(this.logPrefix + "heartbeatNow()");
        this.doReaderCommand("HeartbeatNow");
        return this.getReaderReply();
    }

    public int getCommandPort() throws AlienReaderException {
        this.log(this.logPrefix + "getCommandPort() => ");
        this.doReaderCommand("get CommandPort");
        return this.getReaderReplyValueInt();
    }

    public void setCommandPort(int commandPort) throws AlienReaderException {
        this.log(this.logPrefix + "setCommandPort(" + commandPort + ")");
        this.doReaderCommand("set CommandPort=" + commandPort);
    }

    public int getNetworkUpgrade() throws AlienReaderException {
        this.log(this.logPrefix + "getNetworkUpgrade() => ");
        this.doReaderCommand("get NetworkUpgrade");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setNetworkUpgrade(int value) throws AlienReaderException {
        if (value == 1) {
            this.log(this.logPrefix + "setNetworkUpgrade(\"ON\")");
            this.doReaderCommand("set NetworkUpgrade=ON");
        } else if (value == 0) {
            this.log(this.logPrefix + "setNetworkUpgrade(\"OFF\")");
            this.doReaderCommand("set NetworkUpgrade=OFF");
        } else {
            this.log(this.logPrefix + "setNetworkUpgrade(" + value + ")");
            this.doReaderCommand("set NetworkUpgrade=" + value);
        }

    }

    public String getUpgradeAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getUpgradeAddress() => ");
        this.doReaderCommand("get UpgradeAddress");
        return this.getReaderReplyValueString();
    }

    public void setUpgradeAddress(String upgradeAddress) throws AlienReaderException {
        this.log(this.logPrefix + "setUpgradeAddress(" + upgradeAddress + ")");
        this.doReaderCommand("set UpgradeAddress=" + upgradeAddress);
    }

    public String getUpgradeIPAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getUpgradeIPAddress() => ");
        this.doReaderCommand("get UpgradeIPAddress");
        return this.getReaderReplyValueString();
    }

    public void setUpgradeIPAddress(String upgradeIPAddress) throws AlienReaderException {
        this.log(this.logPrefix + "setUpgradeIPAddress(" + upgradeIPAddress + ")");
        this.doReaderCommand("set UpgradeIPAddress=" + upgradeIPAddress);
    }

    public int getUpgradePort() throws AlienReaderException {
        this.log(this.logPrefix + "getUpgradePort() => ");
        this.doReaderCommand("get UpgradePort");
        return this.getReaderReplyValueInt();
    }

    public void setUpgradePort(int upgradePort) throws AlienReaderException {
        this.log(this.logPrefix + "setUpgradePort(" + upgradePort + ")");
        this.doReaderCommand("set UpgradePort=" + upgradePort);
    }

    public String upgradeNow() throws AlienReaderException {
        return this.upgradeNow("");
    }

    public String upgradeNow(String upgradeAddress) throws AlienReaderException {
        if (upgradeAddress == null) {
            upgradeAddress = "";
        }

        this.log(this.logPrefix + "upgradeNow(" + upgradeAddress + ") => ");
        int originalTimeOut = this.getTimeOutMilliseconds();
        int originalTimeOutMode = this.getTimeOutMode();
        this.setTimeOutMilliseconds(60000);
        this.setTimeOutMode(1);
        this.doReaderCommand("UpgradeNow " + upgradeAddress);
        this.setTimeOutMilliseconds(originalTimeOut);
        this.setTimeOutMode(originalTimeOutMode);
        return this.getReaderReplyValueString();
    }

    public String upgradeNowList() throws AlienReaderException {
        this.log(this.logPrefix + "upgradeNowList() => ");
        this.doReaderCommand("UpgradeNow list");
        return this.getReaderReplyValueString();
    }

    public String upgradeNowList(String upgradeAddress) throws AlienReaderException {
        this.log(this.logPrefix + "upgradeNowList(" + upgradeAddress + ") => ");
        this.doReaderCommand("UpgradeNow list " + upgradeAddress);
        return this.getReaderReplyValueString();
    }

    public int getWWWPort() throws AlienReaderException {
        this.log(this.logPrefix + "getWWWPort() => ");
        this.doReaderCommand("get WWWPort");
        return this.getReaderReplyValueInt();
    }

    public void setWWWPort(int wwwPort) throws AlienReaderException {
        this.log(this.logPrefix + "setWWWPort(" + wwwPort + ")");
        this.doReaderCommand("set WWWPort=" + wwwPort);
    }

    public String ping(String networkAddress) throws AlienReaderException {
        this.log(this.logPrefix + "ping(" + networkAddress + ")");
        this.doReaderCommand("ping " + networkAddress);
        return this.getReaderReply();
    }

    public String ping(String networkAddress, int port) throws AlienReaderException {
        this.log(this.logPrefix + "ping(" + networkAddress + ", " + port + ")");
        this.doReaderCommand("ping " + networkAddress + ":" + port);
        return this.getReaderReply();
    }

    public int getExternalInput() throws AlienReaderException {
        this.log(this.logPrefix + "getExternalInput() => ");
        this.doReaderCommand("get externalinput");
        return this.getReaderReplyValueInt();
    }

    public int getExternalOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getExternalOutput() => ");
        this.doReaderCommand("get externaloutput");
        return this.getReaderReplyValueInt();
    }

    public void setExternalOutput(int value) throws AlienReaderException {
        this.log(this.logPrefix + "setExternalOutput(" + value + ")");
        this.doReaderCommand("set externaloutput=" + value);
    }

    public int getInitExternalOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getInitExternalOutput() => ");
        this.doReaderCommand("get InitExternalOutput");
        return this.getReaderReplyValueInt();
    }

    public void setInitExternalOutput(int value) throws AlienReaderException {
        this.log(this.logPrefix + "setInitExternalOutput(" + value + ")");
        this.doReaderCommand("set InitExternalOutput=" + value);
    }

    public int getInvertExternalOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getInvertExternalOutput() => ");
        this.doReaderCommand("get InvertExternalOutput");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setInvertExternalOutput(int value) throws AlienReaderException {
        if (value == 1) {
            this.log(this.logPrefix + "setInvertExternalOutput(\"ON\")");
            this.doReaderCommand("set InvertExternalOutput=ON");
        } else if (value == 0) {
            this.log(this.logPrefix + "setInvertExternalOutput(\"OFF\")");
            this.doReaderCommand("set InvertExternalOutput=OFF");
        } else {
            this.log(this.logPrefix + "setInvertExternalOutput(" + value + ")");
            this.doReaderCommand("set InvertExternalOutput=" + value);
        }

    }

    public int getInvertExternalInput() throws AlienReaderException {
        this.log(this.logPrefix + "getInvertExternalInput() => ");
        this.doReaderCommand("get InvertExternalInput");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setInvertExternalInput(int value) throws AlienReaderException {
        if (value == 1) {
            this.log(this.logPrefix + "setInvertExternalInput(\"ON\")");
            this.doReaderCommand("set InvertExternalInput=ON");
        } else if (value == 0) {
            this.log(this.logPrefix + "setInvertExternalInput(\"OFF\")");
            this.doReaderCommand("set InvertExternalInput=OFF");
        } else {
            this.log(this.logPrefix + "setInvertExternalInput(" + value + ")");
            this.doReaderCommand("set InvertExternalInput=" + value);
        }

    }

    public String getAcquireMode() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireMode() => ");
        this.doReaderCommand("get acquireMode");
        return this.getReaderReplyValueString();
    }

    public void setAcquireMode(String acquireMode) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireMode(\"" + acquireMode + "\")");
        this.doReaderCommand("set acquireMode=" + acquireMode);
    }

    public int getAcquireWakeCount() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireWakeCount() => ");
        this.doReaderCommand("get acquireWakeCount");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireWakeCount(int acquireWakeCount) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireWakeCount(" + acquireWakeCount + ")");
        this.doReaderCommand("set acquireWakeCount=" + acquireWakeCount);
    }

    public int getAcquireSleep() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireSleep() => ");
        this.doReaderCommand("get acquireSleep");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setAcquireSleep(int acquireSleep) throws AlienReaderException {
        String param = "On";
        if (acquireSleep == 0) {
            param = "Off";
        }

        this.log(this.logPrefix + "setAcquireSleep(\"" + param + "\")");
        this.doReaderCommand("set acquireSleep=" + param);
    }

    /** @deprecated */
    public String getAcqMask() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqMask() => ");
        this.doReaderCommand("Get AcqMask");
        return this.getReaderReplyValueString();
    }

    /** @deprecated */
    public String getMask() throws AlienReaderException {
        this.log(this.logPrefix + "getMask() => ");
        this.doReaderCommand("Get Mask");
        return this.getReaderReplyValueString();
    }

    /** @deprecated */
    public void setAcqMask(String maskString) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqMask(\"" + maskString + "\")");
        this.doReaderCommand("Set AcqMask=" + maskString);
    }

    /** @deprecated */
    public void setMask(String maskString) throws AlienReaderException {
        this.log(this.logPrefix + "setMask(\"" + maskString + "\")");
        this.doReaderCommand("Set Mask=" + maskString);
    }

    /** @deprecated */
    public void setAcqMask(int bitLength, int bitPointer, String hexString) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqMask(" + bitLength + ", " + bitPointer + ", \"" + hexString + "\") =>");
        this.setAcqMask(bitLength + ", " + bitPointer + ", " + hexString);
    }

    /** @deprecated */
    public void setMask(int bitLength, int bitPointer, String hexString) throws AlienReaderException {
        this.log(this.logPrefix + "setMask(" + bitLength + ", " + bitPointer + ", \"" + hexString + "\") =>");
        this.setMask(bitLength + ", " + bitPointer + ", " + hexString);
    }

    /** @deprecated */
    public void setMaskBits(int bitPointer, String bitString) throws AlienReaderException {
        this.log(this.logPrefix + "setMaskBits(" + bitPointer + ", \"" + bitString + "\") =>");
        String byteString = Converters.toHexString(Converters.fromBinaryStringMSB(bitString), " ");
        this.setMask(bitString.length(), bitPointer, byteString);
    }

    public void setTagMask(String tagID) throws AlienReaderException {
        this.log(this.logPrefix + "setTagMask(\"" + tagID + "\")");
        if (tagID == null || tagID.equals("")) {
            tagID = "All";
        }

        if (tagID.equalsIgnoreCase("All")) {
            this.setAcqG2Mask("All");
        } else {
            byte[] byteArray = Converters.fromHexString(tagID);
            this.setAcqG2Mask(1, 32, byteArray.length * 8, Converters.toHexString(byteArray, " "));
        }

    }

    /** @deprecated */
    public String getAcqC1Mask() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqC1Mask() => ");
        this.doReaderCommand("Get AcqC1Mask");
        return this.getReaderReplyValueString();
    }

    /** @deprecated */
    public void setAcqC1Mask(String maskString) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqC1Mask(\"" + maskString + "\")");
        this.doReaderCommand("Set AcqC1Mask=" + maskString);
    }

    /** @deprecated */
    public void setAcqC1Mask(int bitPointer, int bitLength, String hexString) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqC1Mask(" + bitPointer + ", " + bitLength + ", \"" + hexString + "\") =>");
        this.setAcqC1Mask(bitPointer + ", " + bitLength + ", " + hexString);
    }

    /** @deprecated */
    public void setC1MaskBits(int bitPointer, String bitString) throws AlienReaderException {
        this.log(this.logPrefix + "setC1MaskBits(" + bitPointer + ", \"" + bitString + "\") =>");
        String byteString = Converters.toHexString(Converters.fromBinaryStringMSB(bitString), " ");
        this.setAcqC1Mask(bitPointer, bitString.length(), byteString);
    }

    /** @deprecated */
    public void setC1TagMask(String tagID) throws AlienReaderException {
        this.log(this.logPrefix + "setC1TagMask(\"" + tagID + "\")");
        if (tagID == null || tagID.equals("")) {
            tagID = "All";
        }

        if (tagID.equalsIgnoreCase("All")) {
            this.doReaderCommand("set AcqC1Mask=All");
        } else {
            byte[] byteArray = Converters.fromHexString(tagID);
            StringBuffer command = new StringBuffer("set AcqC1Mask=16, " + byteArray.length * 8 + ", ");
            command.append(Converters.toHexString(byteArray, " "));
            this.doReaderCommand(command.toString());
        }

    }

    public String getAcqG2Mask() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2Mask() => ");
        this.doReaderCommand("Get AcqG2Mask");
        return this.getReaderReplyValueString();
    }

    public void setAcqG2Mask(String maskString) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqG2Mask(\"" + maskString + "\")");
        if (maskString.equalsIgnoreCase("All")) {
            maskString = "0";
        }

        this.doReaderCommand("Set AcqG2Mask=" + maskString);
    }

    public void setAcqG2Mask(int bank, int bitPointer, int bitLength, String hexString) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqG2Mask(" + bank + ", " + bitPointer + ", " + bitLength + ", \"" + hexString + "\") =>");
        this.setAcqG2Mask(bank + ", " + bitPointer + ", " + bitLength + ", " + hexString);
    }

    public void setG2MaskBits(int bitPointer, String bitString) throws AlienReaderException {
        this.log(this.logPrefix + "setG2MaskBits(" + bitPointer + ", \"" + bitString + "\") =>");
        String byteString = Converters.toHexString(Converters.fromBinaryStringMSB(bitString), " ");
        this.setAcqG2Mask(1, bitPointer, bitString.length(), byteString);
    }

    public void setG2TagMask(String tagID) throws AlienReaderException {
        this.log(this.logPrefix + "setG2TagMask(\"" + tagID + "\")");
        if (tagID == null || tagID.equals("")) {
            tagID = "0";
        }

        if (!tagID.equalsIgnoreCase("0") && !tagID.equalsIgnoreCase("All")) {
            byte[] byteArray = Converters.fromHexString(tagID);
            this.setAcqG2Mask(1, 32, byteArray.length * 8, Converters.toHexString(byteArray, " "));
        } else {
            this.setAcqG2Mask("0");
        }

    }

    public String getAcqG2MaskAction() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2MaskAction() => ");
        this.doReaderCommand("Get AcqG2MaskAction");
        return this.getReaderReplyValueString();
    }

    public void setAcqG2MaskAction(String maskAction) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqG2MaskAction(\"" + maskAction + "\")");
        this.doReaderCommand("Set AcqG2MaskAction=" + maskAction);
    }

    public String getAcqG2MaskAntenna() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2MaskAntenna() => ");
        this.doReaderCommand("Get AcqG2MaskAntenna");
        return this.getReaderReplyValueString();
    }

    public void setAcqG2MaskAntenna(String maskAntenna) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqG2MaskAntenna(\"" + maskAntenna + "\")");
        this.doReaderCommand("Set AcqG2MaskAntenna=" + maskAntenna);
    }

    public String getAcqG2SL() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2SL() => ");
        this.doReaderCommand("Get AcqG2SL");
        return this.getReaderReplyValueString();
    }

    public void setAcqG2SL(String acqG2SL) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqG2SL(\"" + acqG2SL + "\")");
        this.doReaderCommand("Set AcqG2SL=" + acqG2SL);
    }

    public String getAcqG2AccessPwd() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2AccessPwd()");
        this.doReaderCommand("get AcqG2AccessPwd");
        return this.getReaderReplyValueString();
    }

    public void setAcqG2AccessPwd(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "setAcqG2AccessPwd(\"" + accessPwd + "\")");
        this.doReaderCommand("set AcqG2AccessPwd = " + accessPwd);
    }

    public String getTagAuth() throws AlienReaderException {
        this.log(this.logPrefix + "getTagAuth()");
        this.doReaderCommand("get TagAuth");
        return this.getReaderReplyValueString();
    }

    public void setTagAuth(String tagAuth) throws AlienReaderException {
        this.log(this.logPrefix + "setTagAuth(\"" + tagAuth + "\")");
        this.doReaderCommand("set TagAuth = " + tagAuth);
    }

    public int getPersistTime() throws AlienReaderException {
        this.log(this.logPrefix + "getPersistTime() => ");
        this.doReaderCommand("Get PersistTime");
        return this.getReaderReplyValueInt();
    }

    public void setPersistTime(int persistTime) throws AlienReaderException {
        this.log(this.logPrefix + "setPersistTime(" + persistTime + ")");
        this.doReaderCommand("Set PersistTime=" + persistTime);
    }

    public String getTagListFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getTagListFormat() => ");
        this.doReaderCommand("get tagListFormat");
        return this.getReaderReplyValueString();
    }

    public void setTagListFormat(String formatMode) throws AlienReaderException {
        this.log(this.logPrefix + "setTagListFormat(\"" + formatMode + "\")");
        this.doReaderCommand("set tagListFormat=" + formatMode);
    }

    public void setDefaultTagListFormat() throws AlienReaderException {
        this.log(this.logPrefix + "setDefaultTagListFormat()");
        this.setTagListFormat("Text");
    }

    public String getTagListCustomFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getTagListCustomFormat()");
        this.doReaderCommand("get tagListCustomFormat");
        return this.getReaderReplyValueString();
    }

    public void setTagListCustomFormat(String customFormat) throws AlienReaderException {
        this.log(this.logPrefix + "setTagListCustomFormat(\"" + customFormat + "\")");
        this.doReaderCommand("set tagListCustomFormat=" + customFormat);
    }

    public int getTagListAntennaCombine() throws AlienReaderException {
        this.log(this.logPrefix + "getTagListAntennaCombine()");
        this.doReaderCommand("get TagListAntennaCombine");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setTagListAntennaCombine(int tagListAntennaCombineMode) throws AlienReaderException {
        if (tagListAntennaCombineMode == 1) {
            this.log(this.logPrefix + "setTagListAntennaCombine(\"On\")");
            this.doReaderCommand("set TagListAntennaCombine=ON");
        } else if (tagListAntennaCombineMode == 0) {
            this.log(this.logPrefix + "setTagListAntennaCombine(\"Off\")");
            this.doReaderCommand("set TagListAntennaCombine=OFF");
        } else {
            this.log(this.logPrefix + "setTagListAntennaCombine(" + tagListAntennaCombineMode + ")");
            this.doReaderCommand("set TagListAntennaCombine=" + tagListAntennaCombineMode);
        }

    }

    public int getTagListMillis() throws AlienReaderException {
        this.log(this.logPrefix + "getTagListMillis()");
        this.doReaderCommand("get TagListMillis");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setTagListMillis(int tagListMillis) throws AlienReaderException {
        if (tagListMillis == 1) {
            this.log(this.logPrefix + "setTagListMillis(\"On\")");
            this.doReaderCommand("set TagListMillis=ON");
        } else if (tagListMillis == 0) {
            this.log(this.logPrefix + "setTagListMillis(\"Off\")");
            this.doReaderCommand("set TagListMillis=OFF");
        } else {
            this.log(this.logPrefix + "setTagListMillis(" + tagListMillis + ")");
            this.doReaderCommand("set TagListMillis=" + tagListMillis);
        }

    }

    public void clearTagList() throws AlienReaderException {
        this.log(this.logPrefix + "clearTagList()");
        this.doReaderCommand("Clear TagList");
    }

    public Tag[] getTagList(int multiplier) throws AlienReaderException {
        this.log(this.logPrefix + "getTagList(" + multiplier + ")");
        if (multiplier < 1) {
            multiplier = 1;
        }

        this.doReaderCommand("get taglist " + multiplier);
        Tag[] tagList = (Tag[])null;
        if (this.getReaderReply().startsWith("<")) {
            tagList = TagUtil.decodeXMLTagList(this.getReaderReply());
        } else {
            tagList = TagUtil.decodeTagList(this.getReaderReply());
        }

        if (tagList == null) {
            return null;
        } else {
            return tagList.length == 0 ? null : tagList;
        }
    }

    public Tag[] getTagList() throws AlienReaderException {
        this.log(this.logPrefix + "getTagList() =>");
        return this.getTagList(1);
    }

    public Tag[] getCustomTagList(int multiplier) throws AlienReaderException {
        this.log(this.logPrefix + "getCustomTagList(" + multiplier + ") =>");
        if (multiplier < 1) {
            multiplier = 1;
        }

        String taglistString = this.doReaderCommand("get taglist " + multiplier);
        if (TagUtil.getCustomFormatString() == null || TagUtil.getCustomFormatString().equals("")) {
            TagUtil.setCustomFormatString(this.getTagListCustomFormat());
            System.out.println("loading TagUtil with " + TagUtil.getCustomFormatString());
        }

        Tag[] tagList = TagUtil.decodeCustomTagList(taglistString);
        if (tagList == null) {
            return null;
        } else {
            return tagList.length == 0 ? null : tagList;
        }
    }

    public Tag[] getCustomTagList() throws AlienReaderException {
        this.log(this.logPrefix + "getCustomTagList() =>");
        return this.getCustomTagList(1);
    }

    public Tag getTag(int multiplier) throws AlienReaderException {
        this.log(this.logPrefix + "getTag(" + multiplier + ") =>");
        if (multiplier < 1) {
            multiplier = 1;
        }

        Tag[] tagList = this.getTagList(multiplier);
        if (tagList == null) {
            return null;
        } else {
            return tagList.length <= 0 ? null : tagList[0];
        }
    }

    public Tag getTag() throws AlienReaderException {
        this.log(this.logPrefix + "getTag() =>");
        Tag[] tagList = this.getTagList(1);
        if (tagList == null) {
            return null;
        } else {
            return tagList.length <= 0 ? null : tagList[0];
        }
    }

    public String getTagID(int multiplier) throws AlienReaderException {
        this.log(this.logPrefix + "getTagID(" + multiplier + ") =>");
        Tag tag = this.getTag(multiplier);
        return tag != null ? tag.getTagID() : null;
    }

    public String getTagID() throws AlienReaderException {
        this.log(this.logPrefix + "getTagID() =>");
        return this.getTagID(1);
    }

    public int getStreamHeader() throws AlienReaderException {
        this.log(this.logPrefix + "getStreamHeader()");
        this.doReaderCommand("get StreamHeader");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setStreamHeader(int streamHeader) throws AlienReaderException {
        if (streamHeader == 1) {
            this.log(this.logPrefix + "setStreamHeader(\"ON\")");
            this.doReaderCommand("set StreamHeader=ON");
        } else if (streamHeader == 0) {
            this.log(this.logPrefix + "setStreamHeader(\"OFF\")");
            this.doReaderCommand("set StreamHeader=OFF");
        } else {
            this.log(this.logPrefix + "setStreamMode()");
            this.doReaderCommand("set StreamHeader=" + streamHeader);
        }

    }

    public int getTagStreamMode() throws AlienReaderException {
        this.log(this.logPrefix + "getTagStreamMode()");
        this.doReaderCommand("get TagStreamMode");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setTagStreamMode(int streamMode) throws AlienReaderException {
        if (streamMode == 1) {
            this.log(this.logPrefix + "setTagStreamMode(\"ON\")");
            this.doReaderCommand("set TagStreamMode=ON");
        } else if (streamMode == 0) {
            this.log(this.logPrefix + "setTagStreamMode(\"OFF\")");
            this.doReaderCommand("set TagStreamMode=OFF");
        } else {
            this.log(this.logPrefix + "setTagStreamMode()");
            this.doReaderCommand("set TagStreamMode=" + streamMode);
        }

    }

    public String getTagStreamAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getTagStreamAddress()");
        this.doReaderCommand("get TagStreamAddress");
        return this.getReaderReplyValueString();
    }

    public void setTagStreamAddress(String address) throws AlienReaderException {
        this.log(this.logPrefix + "setTagStreamAddress(\"" + address + "\")");
        this.doReaderCommand("set TagStreamAddress=" + address);
    }

    public void setTagStreamAddress(String ipAddress, int portNumber) throws AlienReaderException {
        this.log(this.logPrefix + "setTagStreamAddress(\"" + ipAddress + "\", " + portNumber + ")");
        this.doReaderCommand("set TagStreamAddress=" + ipAddress.trim() + ":" + portNumber);
    }

    public int getTagStreamKeepAliveTime() throws AlienReaderException {
        this.log(this.logPrefix + "getTagStreamKeepAliveTime() => ");
        this.doReaderCommand("get TagStreamKeepAliveTime");
        return this.getReaderReplyValueInt();
    }

    public void setTagStreamKeepAliveTime(int keepAliveTime) throws AlienReaderException {
        this.log(this.logPrefix + "setTagStreamKeepAliveTime(\"" + keepAliveTime + "\")");
        this.doReaderCommand("set TagStreamKeepAliveTime=" + keepAliveTime);
    }

    public String getTagStreamFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getTagStreamFormat()");
        this.doReaderCommand("get TagStreamFormat");
        return this.getReaderReplyValueString();
    }

    public void setTagStreamFormat(String format) throws AlienReaderException {
        this.log(this.logPrefix + "setTagStreamFormat(\"" + format + "\")");
        this.doReaderCommand("set TagStreamFormat=" + format);
    }

    public String getTagStreamCustomFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getTagStreamCustomFormat()");
        this.doReaderCommand("get TagStreamCustomFormat");
        return this.getReaderReplyValueString();
    }

    public void setTagStreamCustomFormat(String customFormat) throws AlienReaderException {
        this.log(this.logPrefix + "setTagStreamCustomFormat(\"" + customFormat + "\")");
        this.doReaderCommand("set TagStreamCustomFormat=" + customFormat);
    }

    public int getTagStreamCountFilterMinimum() throws AlienReaderException {
        this.log(this.logPrefix + "getTagStreamCountFilter() => ");
        this.doReaderCommand("get TagStreamCountFilter");
        String reply = this.getReaderReplyValueString();
        int spacePos = reply.indexOf(" ");
        if (spacePos < 0) {
            return this.getReaderReplyValueInt();
        } else {
            String minString = reply.substring(0, spacePos).trim();

            try {
                int min = new Integer(minString);
                return min;
            } catch (NumberFormatException var5) {
                return 0;
            }
        }
    }

    public int[] getTagStreamCountFilter() throws AlienReaderException {
        int[] filter = new int[2];
        this.log(this.logPrefix + "getTagStreamCountFilter() => ");
        this.doReaderCommand("get TagStreamCountFilter");
        String reply = this.getReaderReplyValueString();
        String[] parts = reply.split(" ");
        if (parts != null && parts.length >= 2) {
            try {
                filter[0] = new Integer(parts[0]);
                filter[1] = new Integer(parts[1]);
            } catch (NumberFormatException var5) {
            }
        } else {
            filter[0] = this.getReaderReplyValueInt();
            filter[1] = 0;
        }

        return filter;
    }

    public void setTagStreamCountFilter(int minCount) throws AlienReaderException {
        this.log(this.logPrefix + "setTagStreamCountFilter(\"" + minCount + "\")");
        this.doReaderCommand("set TagStreamCountFilter=" + minCount);
    }

    public void setTagStreamCountFilter(int minCount, int maxCount) throws AlienReaderException {
        this.log(this.logPrefix + "setTagStreamCountFilter(\"" + minCount + " " + maxCount + "\")");
        this.doReaderCommand("set TagStreamCountFilter=" + minCount + " " + maxCount);
    }

    public void clearIOList() throws AlienReaderException {
        this.log(this.logPrefix + "clearIOList()");
        this.doReaderCommand("Clear IOList");
    }

    public ExternalIO[] getIOList() throws AlienReaderException {
        this.log(this.logPrefix + "getIOList()");
        this.doReaderCommand("get IOList");
        ExternalIO[] ioList = (ExternalIO[])null;
        if (this.getReaderReply().startsWith("<")) {
            ioList = ExternalIOUtil.decodeXMLIOList(this.getReaderReply());
        } else {
            ioList = ExternalIOUtil.decodeIOList(this.getReaderReply());
        }

        if (ioList == null) {
            return null;
        } else {
            return ioList.length == 0 ? null : ioList;
        }
    }

    public String getIOListDump() throws AlienReaderException {
        this.log(this.logPrefix + "getIOListDump()");
        this.doReaderCommand("get IOList");
        return this.getReaderReply();
    }

    public String getIOType() throws AlienReaderException {
        this.log(this.logPrefix + "getIOType()");
        this.doReaderCommand("get IOType");
        return this.getReaderReplyValueString();
    }

    public void setIOType(String ioType) throws AlienReaderException {
        this.log(this.logPrefix + "setIOType()");
        this.doReaderCommand("set IOType = " + ioType);
    }

    public String getIOListFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getIOListFormat() => ");
        this.doReaderCommand("get IOListFormat");
        return this.getReaderReplyValueString();
    }

    public void setIOListFormat(String formatMode) throws AlienReaderException {
        this.log(this.logPrefix + "setIOListFormat(\"" + formatMode + "\")");
        this.doReaderCommand("set IOListFormat=" + formatMode);
    }

    public void setDefaultIOListFormat() throws AlienReaderException {
        this.log(this.logPrefix + "setDefaultIOListFormat()");
        this.setIOListFormat("Text");
    }

    public String getIOListCustomFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getIOListCustomFormat()");
        this.doReaderCommand("get IOListCustomFormat");
        return this.getReaderReplyValueString();
    }

    public void setIOListCustomFormat(String customFormat) throws AlienReaderException {
        this.log(this.logPrefix + "setIOListCustomFormat(\"" + customFormat + "\")");
        this.doReaderCommand("set IOListCustomFormat=" + customFormat);
    }

    public int getIOStreamMode() throws AlienReaderException {
        this.log(this.logPrefix + "getIOStreamMode()");
        this.doReaderCommand("get IOStreamMode");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setIOStreamMode(int streamMode) throws AlienReaderException {
        if (streamMode == 1) {
            this.log(this.logPrefix + "setIOStreamMode(\"ON\")");
            this.doReaderCommand("set IOStreamMode=ON");
        } else if (streamMode == 0) {
            this.log(this.logPrefix + "setIOStreamMode(\"OFF\")");
            this.doReaderCommand("set IOStreamMode=OFF");
        } else {
            this.log(this.logPrefix + "setIOStreamMode()");
            this.doReaderCommand("set IOStreamMode=" + streamMode);
        }

    }

    public String getIOStreamAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getIOStreamAddress()");
        this.doReaderCommand("get IOStreamAddress");
        return this.getReaderReplyValueString();
    }

    public void setIOStreamAddress(String address) throws AlienReaderException {
        this.log(this.logPrefix + "setIOStreamAddress(\"" + address + "\")");
        this.doReaderCommand("set IOStreamAddress=" + address);
    }

    public void setIOStreamAddress(String ipAddress, int portNumber) throws AlienReaderException {
        this.log(this.logPrefix + "setIOStreamAddress(\"" + ipAddress + "\", " + portNumber + ")");
        this.doReaderCommand("set IOStreamAddress=" + ipAddress.trim() + ":" + portNumber);
    }

    public int getIOStreamKeepAliveTime() throws AlienReaderException {
        this.log(this.logPrefix + "getIOStreamKeepAliveTime() => ");
        this.doReaderCommand("get IOStreamKeepAliveTime");
        return this.getReaderReplyValueInt();
    }

    public void setIOStreamKeepAliveTime(int keepAliveTime) throws AlienReaderException {
        this.log(this.logPrefix + "setIOStreamKeepAliveTime(\"" + keepAliveTime + "\")");
        this.doReaderCommand("set IOStreamKeepAliveTime=" + keepAliveTime);
    }

    public String getIOStreamFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getIOStreamFormat()");
        this.doReaderCommand("get IOStreamFormat");
        return this.getReaderReplyValueString();
    }

    public void setIOStreamFormat(String format) throws AlienReaderException {
        this.log(this.logPrefix + "setIOStreamFormat(\"" + format + "\")");
        this.doReaderCommand("set IOStreamFormat=" + format);
    }

    public String getIOStreamCustomFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getIOStreamCustomFormat()");
        this.doReaderCommand("get IOStreamCustomFormat");
        return this.getReaderReplyValueString();
    }

    public void setIOStreamCustomFormat(String customFormat) throws AlienReaderException {
        this.log(this.logPrefix + "setIOStreamCustomFormat(\"" + customFormat + "\")");
        this.doReaderCommand("set IOStreamCustomFormat=" + customFormat);
    }

    public int getAutoMode() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoMode()");
        this.doReaderCommand("get autoMode");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setAutoMode(int autoMode) throws AlienReaderException {
        if (autoMode == 1) {
            this.log(this.logPrefix + "setAutoMode(\"On\")");
            this.doReaderCommand("set autoMode=ON");
        } else {
            for(int i = 0; i < 3; ++i) {
                this.log(this.logPrefix + "setAutoMode(\"Off\")");
                String reply = this.doReaderCommand("set autoMode=OFF");
                if (reply != null && reply.toLowerCase().indexOf("off") > 0) {
                    return;
                }
            }

            throw new AlienReaderCommandErrorException("Failed to Turn Auto Mode Off (Tried 3 Times)");
        }
    }

    /** @deprecated */
    public void resetAutoMode() throws AlienReaderException {
        this.log(this.logPrefix + "resetAutoMode()");
        this.doReaderCommand("autoModeReset");
    }

    public void autoModeReset() throws AlienReaderException {
        this.log(this.logPrefix + "autoModeReset()");
        this.doReaderCommand("autoModeReset");
    }

    public void autoModeTriggerNow() throws AlienReaderException {
        this.log(this.logPrefix + "autoModeTriggerNow()");
        this.doReaderCommand("AutoModeTriggerNow");
    }

    public int getAutoWaitOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoWaitOutput()");
        this.doReaderCommand("get autoWaitOutput");
        return this.getReaderReplyValueInt();
    }

    public void setAutoWaitOutput(int autoWaitOutput) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoWaitOutput(" + autoWaitOutput + ")");
        this.doReaderCommand("set autoWaitOutput=" + autoWaitOutput);
    }

    public int[] getAutoStartTrigger() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoStartTrigger()");
        this.doReaderCommand("get autoStartTrigger");
        int[] result = new int[2];

        try {
            StringTokenizer tokenizer = new StringTokenizer(this.getReaderReplyValueString(), " ,");
            result[0] = Integer.parseInt(tokenizer.nextToken().trim());
            result[1] = Integer.parseInt(tokenizer.nextToken().trim());
            return result;
        } catch (Exception var3) {
            throw new AlienReaderCommandErrorException("Couldn't decode AutoStartTrigger.\n" + this.getReaderReplyValueString());
        }
    }

    public void setAutoStartTrigger(int risingEdge, int fallingEdge) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoStartTrigger(" + risingEdge + ", " + fallingEdge + ")");
        this.doReaderCommand("set AutoStartTrigger=" + risingEdge + ", " + fallingEdge);
    }

    public void setAutoStartTrigger(String trigger) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoStartTrigger(\"" + trigger + "\")");
        this.doReaderCommand("set AutoStartTrigger=" + trigger);
    }

    public int getAutoStartPause() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoStartPause()");
        this.doReaderCommand("get AutoStartPause");
        return this.getReaderReplyValueInt();
    }

    public void setAutoStartPause(int autoStartPause) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoStartPause(" + autoStartPause + ")");
        this.doReaderCommand("set AutoStartPause=" + autoStartPause);
    }

    public int getAutoWorkOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoWorkOutput()");
        this.doReaderCommand("get autoWorkOutput");
        return this.getReaderReplyValueInt();
    }

    public void setAutoWorkOutput(int autoWorkOutput) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoWorkOutput(" + autoWorkOutput + ")");
        this.doReaderCommand("set autoWorkOutput=" + autoWorkOutput);
    }

    public String getAutoAction() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoAction()");
        this.doReaderCommand("get autoAction");
        return this.getReaderReplyValueString();
    }

    public void setAutoAction(String autoAction) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoAction(\"" + autoAction + "\")");
        this.doReaderCommand("set autoAction=" + autoAction);
    }

    public int[] getAutoStopTrigger() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoStopTrigger()");
        this.doReaderCommand("get autoStopTrigger");
        int[] result = new int[2];

        try {
            StringTokenizer tokenizer = new StringTokenizer(this.getReaderReplyValueString(), " ,");
            result[0] = Integer.parseInt(tokenizer.nextToken().trim());
            result[1] = Integer.parseInt(tokenizer.nextToken().trim());
            return result;
        } catch (Exception var3) {
            throw new AlienReaderCommandErrorException("Couldn't decode AutoStopTrigger.\n" + this.getReaderReplyValueString());
        }
    }

    public void setAutoStopTrigger(int risingEdge, int fallingEdge) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoStopTrigger(" + risingEdge + ", " + fallingEdge + ")");
        this.doReaderCommand("set autoStopTrigger=" + risingEdge + ", " + fallingEdge);
    }

    public void setAutoStopTrigger(String trigger) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoStopTrigger(\"" + trigger + "\")");
        this.doReaderCommand("set autoStopTrigger=" + trigger);
    }

    public int getAutoStopPause() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoStopPause()");
        this.doReaderCommand("get AutoStopPause");
        return this.getReaderReplyValueInt();
    }

    public void setAutoStopPause(int autoStopPause) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoStopPause(" + autoStopPause + ")");
        this.doReaderCommand("set AutoStopPause=" + autoStopPause);
    }

    public int getAutoStopTimer() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoStopTimer()");
        this.doReaderCommand("get autoStopTimer");
        return this.getReaderReplyValueInt();
    }

    public void setAutoStopTimer(int timer) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoStopTimer(" + timer + ")");
        this.doReaderCommand("set autoStopTimer=" + timer);
    }

    public int getAutoTrueOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoTrueOutput()");
        this.doReaderCommand("get autoTrueOutput");
        return this.getReaderReplyValueInt();
    }

    public void setAutoTrueOutput(int autoTrueOutput) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoTrueOutput(" + autoTrueOutput + ")");
        this.doReaderCommand("set autoTrueOutput=" + autoTrueOutput);
    }

    public int getAutoTruePause() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoTruePause()");
        this.doReaderCommand("get autoTruePause");
        return this.getReaderReplyValueInt();
    }

    public void setAutoTruePause(int autoTruePause) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoTruePause(" + autoTruePause + ")");
        this.doReaderCommand("set autoTruePause=" + autoTruePause);
    }

    public int getAutoFalseOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoFalseOutput()");
        this.doReaderCommand("get autoFalseOutput");
        return this.getReaderReplyValueInt();
    }

    public void setAutoFalseOutput(int autoFalseOutput) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoFalseOutput(" + autoFalseOutput + ")");
        this.doReaderCommand("set autoFalseOutput=" + autoFalseOutput);
    }

    public int getAutoFalsePause() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoFalsePause()");
        this.doReaderCommand("get autoFalsePause");
        return this.getReaderReplyValueInt();
    }

    public void setAutoFalsePause(int autoFalsePause) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoFalsePause(" + autoFalsePause + ")");
        this.doReaderCommand("set autoFalsePause=" + autoFalsePause);
    }

    public String getAutoModeStatus() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoModeStatus()");
        this.doReaderCommand("get autoModeStatus");
        return this.getReaderReplyValueString();
    }

    public String getAutoErrorOutput() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoErrorOutput()");
        this.doReaderCommand("get AutoErrorOutput");
        return this.getReaderReplyValueString();
    }

    public void setAutoErrorOutput(String errorOutput) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoErrorOutput(\"" + errorOutput + "\")");
        this.doReaderCommand("set AutoErrorOutput = " + errorOutput);
    }

    public int getAutoProgError() throws AlienReaderException {
        this.log(this.logPrefix + "getAutoProgError()");
        this.doReaderCommand("get AutoProgError");
        return this.getReaderReplyValueInt();
    }

    public void setAutoProgError(int autoProgError) throws AlienReaderException {
        this.log(this.logPrefix + "setAutoProgError(" + autoProgError + ")");
        this.doReaderCommand("set AutoProgError=" + autoProgError);
    }

    public int getNotifyMode() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyMode()");
        this.doReaderCommand("get notifyMode");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setNotifyMode(int notifyMode) throws AlienReaderException {
        if (notifyMode == 1) {
            this.log(this.logPrefix + "setNotifyMode(\"ON\")");
            this.doReaderCommand("set NotifyMode=ON");
        } else if (notifyMode == 0) {
            this.log(this.logPrefix + "setNotifyMode(\"OFF\")");
            this.doReaderCommand("set NotifyMode=OFF");
        } else {
            this.log(this.logPrefix + "setNotifyMode(" + notifyMode + ")");
            this.doReaderCommand("set NotifyMode=" + notifyMode);
        }

    }

    public int getNotifyHeader() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyHeader()");
        this.doReaderCommand("get NotifyHeader");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setNotifyHeader(int notifyHeader) throws AlienReaderException {
        if (notifyHeader == 1) {
            this.log(this.logPrefix + "setNotifyHeader(\"ON\")");
            this.doReaderCommand("set NotifyHeader=ON");
        } else if (notifyHeader == 0) {
            this.log(this.logPrefix + "setNotifyHeader(\"OFF\")");
            this.doReaderCommand("set NotifyHeader=OFF");
        } else {
            this.log(this.logPrefix + "getAutoMode()");
            this.doReaderCommand("set NotifyHeader=" + notifyHeader);
        }

    }

    public String getNotifyFormat() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyFormat()");
        this.doReaderCommand("get notifyFormat");
        return this.getReaderReplyValueString();
    }

    public void setNotifyFormat(String format) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyFormat(\"" + format + "\")");
        this.doReaderCommand("set notifyFormat=" + format);
    }

    public String getNotifyAddress() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyAddress()");
        this.doReaderCommand("get notifyAddress");
        return this.getReaderReplyValueString();
    }

    public void setNotifyAddress(String address) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyAddress(\"" + address + "\")");
        this.doReaderCommand("set notifyAddress=" + address);
    }

    public void setNotifyAddress(String ipAddress, int portNumber) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyAddress(\"" + ipAddress + "\", " + portNumber + ")");
        this.doReaderCommand("set notifyAddress=" + ipAddress.trim() + ":" + portNumber);
    }

    public int getNotifyTime() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyTime()");
        this.doReaderCommand("get notifyTime");
        return this.getReaderReplyValueInt();
    }

    public void setNotifyTime(int seconds) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyTime(" + seconds + ")");
        this.doReaderCommand("set notifyTime=" + seconds);
    }

    public String getNotifyTrigger() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyTrigger()");
        this.doReaderCommand("get notifyTrigger");
        return this.getReaderReplyValueString();
    }

    public void setNotifyTrigger(String trigger) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyTrigger(\"" + trigger + "\")");
        this.doReaderCommand("set notifyTrigger=" + trigger);
    }

    public int getNotifyKeepAliveTime() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyKeepAliveTime() => ");
        this.doReaderCommand("get NotifyKeepAliveTime");
        return this.getReaderReplyValueInt();
    }

    public void setNotifyKeepAliveTime(int keepAliveTime) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyKeepAliveTime(\"" + keepAliveTime + "\")");
        this.doReaderCommand("set NotifyKeepAliveTime=" + keepAliveTime);
    }

    public int getNotifyRetryCount() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyRetryCount()");
        this.doReaderCommand("get NotifyRetryCount");
        return this.getReaderReplyValueInt();
    }

    public void setNotifyRetryCount(int retryCount) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyRetryCount(" + retryCount + ")");
        this.doReaderCommand("set NotifyRetryCount=" + retryCount);
    }

    public int getNotifyRetryPause() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyRetryPause()");
        this.doReaderCommand("get NotifyRetryPause");
        return this.getReaderReplyValueInt();
    }

    public void setNotifyRetryPause(int retryPause) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyRetryPause(" + retryPause + ")");
        this.doReaderCommand("set notifyRetryPause=" + retryPause);
    }

    public String getNotifyInclude() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyInclude()");
        this.doReaderCommand("get NotifyInclude");
        return this.getReaderReplyValueString();
    }

    public void setNotifyInclude(String notifyInclude) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyInclude(" + notifyInclude + ")");
        this.doReaderCommand("set NotifyInclude=" + notifyInclude);
    }

    public int getNotifyQueueLimit() throws AlienReaderException {
        this.log(this.logPrefix + "getNotifyQueueLimit()");
        this.doReaderCommand("get NotifyQueueLimit");
        return this.getReaderReplyValueInt();
    }

    public void setNotifyQueueLimit(int queueLimit) throws AlienReaderException {
        this.log(this.logPrefix + "setNotifyQueueLimit(" + queueLimit + ")");
        this.doReaderCommand("set NotifyQueueLimit=" + queueLimit);
    }

    public String getMailServer() throws AlienReaderException {
        this.log(this.logPrefix + "getMailServer()");
        this.doReaderCommand("get mailServer");
        return this.getReaderReplyValueString();
    }

    public void setMailServer(String mailServer) throws AlienReaderException {
        this.log(this.logPrefix + "setMailServer(\"" + mailServer + "\")");
        this.doReaderCommand("set mailServer=" + mailServer);
    }

    public String getMailFrom() throws AlienReaderException {
        this.log(this.logPrefix + "getMailFrom()");
        this.doReaderCommand("get mailFrom");
        return this.getReaderReplyValueString();
    }

    public void setMailFrom(String mailFrom) throws AlienReaderException {
        this.log(this.logPrefix + "setMailFrom(\"" + mailFrom + "\")");
        this.doReaderCommand("set mailFrom=" + mailFrom);
    }

    public void notifyNow() throws AlienReaderException {
        this.log(this.logPrefix + "notifyNow()");
        this.doReaderCommand("notifynow");
    }

    public void wakeTag(String tagID) throws AlienReaderException {
        this.log(this.logPrefix + "wakeTag(\"" + tagID + "\")");
        if (tagID != null && tagID != "") {
            this.setTagMask(Converters.reformatTagID(tagID));
        }

        this.doReaderCommand("wake");
    }

    /** @deprecated */
    public void wake(String tagID) throws AlienReaderException {
        this.log(this.logPrefix + "wake(\"" + tagID + "\") =>");
        this.wakeTag(tagID);
    }

    public void g2Wake(int numWakes) throws AlienReaderException {
        this.log(this.logPrefix + "gwWake(" + numWakes + ")");
        this.doReaderCommand("G2Wake " + numWakes);
    }

    public void g2Wake() throws AlienReaderException {
        this.g2Wake(1);
    }

    public void sleepTag(String tagID) throws AlienReaderException {
        this.log(this.logPrefix + "sleepTag(\"" + tagID + "\")");
        if (tagID != null && tagID != "") {
            this.setTagMask(Converters.reformatTagID(tagID));
        }

        this.doReaderCommand("sleep");
    }

    /** @deprecated */
    public void sleep(String tagID) throws AlienReaderException {
        this.log(this.logPrefix + "sleep(\"" + tagID + "\") =>");
        this.sleepTag(tagID);
    }

    public int getProgAntenna() throws AlienReaderException {
        this.log(this.logPrefix + "getProgAntenna()");
        this.doReaderCommand("get ProgAntenna");
        return this.getReaderReplyValueInt();
    }

    public void setProgAntenna(int antenna) throws AlienReaderException {
        this.log(this.logPrefix + "setProgAntenna(" + antenna + ")");
        this.doReaderCommand("set ProgAntenna = " + antenna);
    }

    public int getProgProtocol() throws AlienReaderException {
        this.log(this.logPrefix + "getProgProtocol()");
        this.doReaderCommand("get ProgProtocol");
        return this.getReaderReplyValueInt();
    }

    public void setProgProtocol(int progProtocol) throws AlienReaderException {
        this.log(this.logPrefix + "setProgProtocol(" + progProtocol + ")");
        this.doReaderCommand("set ProgProtocol = " + progProtocol);
    }

    public String verifyTag() throws AlienReaderException {
        this.log(this.logPrefix + "verifyTag()");
        this.doReaderCommand("verify tag");
        return this.getReaderReplyValueString();
    }

    public void programTag(String epcData) throws AlienReaderException {
        this.log(this.logPrefix + "programTag(\"" + epcData + "\")");
        this.doReaderCommand("program tag = " + Converters.reformatTagID(epcData));
    }

    public void programEPC(String epcData) throws AlienReaderException {
        this.log(this.logPrefix + "programEPC(\"" + epcData + "\")");
        this.doReaderCommand("ProgramEPC = " + Converters.reformatTagID(epcData));
    }

    public void programEPC() throws AlienReaderException {
        this.log(this.logPrefix + "programEPC()");
        this.doReaderCommand("ProgramEPC");
    }

    public void programUser(String userData) throws AlienReaderException {
        this.log(this.logPrefix + "programUser(\"" + userData + "\")");
        this.doReaderCommand("ProgramUser = " + Converters.reformatTagID(userData));
    }

    public void programUser() throws AlienReaderException {
        this.log(this.logPrefix + "programUser()");
        this.doReaderCommand("programUser");
    }

    public void programAlienImage() throws AlienReaderException {
        this.log(this.logPrefix + "programAlienImage()");
        this.doReaderCommand("ProgramAlienImage");
    }

    public void programAndLockEPC(String epcData) throws AlienReaderException {
        this.log(this.logPrefix + "programAndLockEPC(\"" + epcData + "\")");
        this.doReaderCommand("ProgramAndLockEPC = " + Converters.reformatTagID(epcData));
    }

    public void programAndLockEPC() throws AlienReaderException {
        this.log(this.logPrefix + "programAndLockEPC()");
        this.doReaderCommand("programAndLockEPC");
    }

    public void programAndLockUser(String userData) throws AlienReaderException {
        this.log(this.logPrefix + "programAndLockUser(\"" + userData + "\")");
        this.doReaderCommand("ProgramAndLockUser = " + Converters.reformatTagID(userData));
    }

    public void programAndLockUser() throws AlienReaderException {
        this.log(this.logPrefix + "programAndLockUser()");
        this.doReaderCommand("ProgramAndLockUser");
    }

    public void programAccessPwd(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "programAccessPwd(\"" + accessPwd + "\")");
        this.doReaderCommand("ProgramAccessPwd = " + Converters.reformatTagID(accessPwd));
    }

    public void programAccessPwd() throws AlienReaderException {
        this.log(this.logPrefix + "programAccessPwd()");
        this.doReaderCommand("ProgramAccessPwd");
    }

    public void programKillPwd(String killPwd) throws AlienReaderException {
        this.log(this.logPrefix + "programKillPwd(\"" + killPwd + "\")");
        this.doReaderCommand("ProgramKillPwd = " + Converters.reformatTagID(killPwd));
    }

    public void programKillPwd() throws AlienReaderException {
        this.log(this.logPrefix + "programKillPwd()");
        this.doReaderCommand("ProgramKillPwd");
    }

    public void eraseTag() throws AlienReaderException {
        this.log(this.logPrefix + "eraseTag()");
        this.doReaderCommand("erase tag");
    }

    public void lockTag(String lockData) throws AlienReaderException {
        this.log(this.logPrefix + "lockTag(\"" + lockData + "\")");
        this.doReaderCommand("lock tag = " + Converters.reformatTagID(lockData));
    }

    public void lockEPC(String lockData) throws AlienReaderException {
        this.log(this.logPrefix + "lockEPC(\"" + lockData + "\")");
        this.doReaderCommand("LockEPC = " + Converters.reformatTagID(lockData));
    }

    public void lockEPC() throws AlienReaderException {
        this.log(this.logPrefix + "lockEPC()");
        this.doReaderCommand("LockEPC");
    }

    public void lockUser(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "lockUser(\"" + accessPwd + "\")");
        this.doReaderCommand("LockUser = " + Converters.reformatTagID(accessPwd));
    }

    public void lockUser() throws AlienReaderException {
        this.log(this.logPrefix + "lockUser()");
        this.doReaderCommand("LockUser");
    }

    public void lockAccessPwd(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "lockAccessPwd(\"" + accessPwd + "\")");
        this.doReaderCommand("LockAccessPwd = " + Converters.reformatTagID(accessPwd));
    }

    public void lockAccessPwd() throws AlienReaderException {
        this.log(this.logPrefix + "lockAccessPwd()");
        this.doReaderCommand("LockAccessPwd");
    }

    public void lockKillPwd(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "lockKillPwd(\"" + accessPwd + "\")");
        this.doReaderCommand("LockKillPwd = " + Converters.reformatTagID(accessPwd));
    }

    public void lockKillPwd() throws AlienReaderException {
        this.log(this.logPrefix + "lockKillPwd()");
        this.doReaderCommand("LockKillPwd");
    }

    public void unlockEPC(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "unlockEPC(\"" + accessPwd + "\")");
        this.doReaderCommand("UnlockEPC = " + Converters.reformatTagID(accessPwd));
    }

    public void unlockEPC() throws AlienReaderException {
        this.log(this.logPrefix + "unlockEPC()");
        this.doReaderCommand("UnlockEPC");
    }

    public void unlockUser(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "unlockUser(\"" + accessPwd + "\")");
        this.doReaderCommand("UnlockUser = " + Converters.reformatTagID(accessPwd));
    }

    public void unlockUser() throws AlienReaderException {
        this.log(this.logPrefix + "unlockUser()");
        this.doReaderCommand("UnlockUser");
    }

    public void unlockAccessPwd(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "unlockAccessPwd(\"" + accessPwd + "\")");
        this.doReaderCommand("UnlockAccessPwd = " + Converters.reformatTagID(accessPwd));
    }

    public void unlockAccessPwd() throws AlienReaderException {
        this.log(this.logPrefix + "unlockAccessPwd()");
        this.doReaderCommand("unlockAccessPwd");
    }

    public void unlockKillPwd(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "unlockKillPwd(\"" + accessPwd + "\")");
        this.doReaderCommand("UnlockKillPwd = " + Converters.reformatTagID(accessPwd));
    }

    public void unlockKillPwd() throws AlienReaderException {
        this.log(this.logPrefix + "unlockKillPwd()");
        this.doReaderCommand("UnlockKillPwd");
    }

    public void lockUserBlocks(byte blocksToLockMask) throws AlienReaderException {
        this.log(this.logPrefix + "lockUserBlocks(\"" + blocksToLockMask + "\")");
        this.doReaderCommand("LockUserBlocks = 0 " + Converters.toHexString(blocksToLockMask) + " 00");
    }

    public void hideAlienUserBlocks(byte blocksToHideMask) throws AlienReaderException {
        this.log(this.logPrefix + "hideAlienUserBlocks(\"" + blocksToHideMask + "\")");
        this.doReaderCommand("HideAlienUserBlocks = " + Converters.toHexString(blocksToHideMask));
    }

    public void killTag(String tagIDAndPassCode) throws AlienReaderException {
        this.log(this.logPrefix + "killTag(\"" + tagIDAndPassCode + "\")");
        this.doReaderCommand("kill tag = " + Converters.reformatTagID(tagIDAndPassCode));
    }

    public String getProgEPCData() throws AlienReaderException {
        this.log(this.logPrefix + "getProgEPCData()");
        this.doReaderCommand("get ProgEPCData");
        return this.getReaderReplyValueString();
    }

    public void setProgEPCData(String epcData) throws AlienReaderException {
        this.log(this.logPrefix + "setProgEPCData(\"" + epcData + "\")");
        this.doReaderCommand("set ProgEPCData = " + Converters.reformatTagID(epcData));
    }

    public String getProgUserData() throws AlienReaderException {
        this.log(this.logPrefix + "getProgUserData()");
        this.doReaderCommand("get ProgUserData");
        return this.getReaderReplyValueString();
    }

    public void setProgUserData(String userData) throws AlienReaderException {
        this.log(this.logPrefix + "setProgUserData(\"" + userData + "\")");
        this.doReaderCommand("set ProgUserData = " + Converters.reformatTagID(userData));
    }

    public String getProgAlienImageMap() throws AlienReaderException {
        this.log(this.logPrefix + "getProgAlienImageMap()");
        this.doReaderCommand("get ProgAlienImageMap");
        return this.getReaderReplyValueString();
    }

    public void setProgAlienImageMap(String imageMap) throws AlienReaderException {
        this.log(this.logPrefix + "setProgAlienImageMap(\"" + imageMap + "\")");
        this.doReaderCommand("set ProgAlienImageMap = " + imageMap);
    }

    public String getProgG2NSI() throws AlienReaderException {
        this.log(this.logPrefix + "getProgG2NSI()");
        this.doReaderCommand("get ProgG2NSI");
        return this.getReaderReplyValueString();
    }

    public void setProgG2NSI(String nsiData) throws AlienReaderException {
        this.log(this.logPrefix + "setProgG2NSI(\"" + nsiData + "\")");
        this.doReaderCommand("set ProgG2NSI = " + Converters.reformatTagID(nsiData));
    }

    public String getProgAlienImageNSI() throws AlienReaderException {
        this.log(this.logPrefix + "getProgAlienImageNSI()");
        this.doReaderCommand("get ProgAlienImageNSI");
        return this.getReaderReplyValueString();
    }

    public void setProgAlienImageNSI(String nsiData) throws AlienReaderException {
        this.log(this.logPrefix + "setProgAlienImageNSI(\"" + nsiData + "\")");
        this.doReaderCommand("set ProgAlienImageNSI = " + Converters.reformatTagID(nsiData));
    }

    public String getProgramID() throws AlienReaderException {
        this.log(this.logPrefix + "getProgramID()");
        this.doReaderCommand("get ProgramID");
        return this.getReaderReplyValueString();
    }

    public void setProgramID(String programID) throws AlienReaderException {
        this.log(this.logPrefix + "setProgramID(\"" + programID + "\")");
        this.doReaderCommand("set ProgramID = " + Converters.reformatTagID(programID));
    }

    public String getProgramPassCode() throws AlienReaderException {
        this.log(this.logPrefix + "getProgramPassCode()");
        this.doReaderCommand("get ProgramPassCode");
        return this.getReaderReplyValueString();
    }

    public void setProgramPassCode(String passCode) throws AlienReaderException {
        this.log(this.logPrefix + "setProgramPassCode(\"" + passCode + "\")");
        this.doReaderCommand("set ProgramPassCode = " + passCode);
    }

    public String getProgC1KillPwd() throws AlienReaderException {
        this.log(this.logPrefix + "getProgC1KillPwd()");
        this.doReaderCommand("get ProgC1KillPwd");
        return this.getReaderReplyValueString();
    }

    public void setProgC1KillPwd(String killPwd) throws AlienReaderException {
        this.log(this.logPrefix + "setProgC1KillPwd(\"" + killPwd + "\")");
        this.doReaderCommand("set ProgC1KillPwd = " + Converters.reformatTagID(killPwd));
    }

    public String getProgG2KillPwd() throws AlienReaderException {
        this.log(this.logPrefix + "getProgG2KillPwd()");
        this.doReaderCommand("get ProgG2KillPwd");
        return this.getReaderReplyValueString();
    }

    public void setProgG2KillPwd(String killPwd) throws AlienReaderException {
        this.log(this.logPrefix + "setProgG2KillPwd(\"" + killPwd + "\")");
        this.doReaderCommand("set ProgG2KillPwd = " + Converters.reformatTagID(killPwd));
    }

    public String getProgG2AccessPwd() throws AlienReaderException {
        this.log(this.logPrefix + "getProgG2AccessPwd()");
        this.doReaderCommand("get ProgG2AccessPwd");
        return this.getReaderReplyValueString();
    }

    public void setProgG2AccessPwd(String accessPwd) throws AlienReaderException {
        this.log(this.logPrefix + "setProgG2AccessPwd(\"" + accessPwd + "\")");
        this.doReaderCommand("set ProgG2AccessPwd = " + Converters.reformatTagID(accessPwd));
    }

    public String getProgG2LockType() throws AlienReaderException {
        this.log(this.logPrefix + "getProgG2LockType()");
        this.doReaderCommand("get ProgG2LockType");
        return this.getReaderReplyValueString();
    }

    public void setProgG2LockType(String lockType) throws AlienReaderException {
        this.log(this.logPrefix + "setProgG2LockType(\"" + lockType + "\")");
        this.doReaderCommand("set ProgG2LockType = " + lockType);
    }

    public String getProgDataUnit() throws AlienReaderException {
        this.log(this.logPrefix + "getProgDataUnit()");
        this.doReaderCommand("get ProgDataUnit");
        return this.getReaderReplyValueString();
    }

    public void setProgDataUnit(String dataUnit) throws AlienReaderException {
        this.log(this.logPrefix + "setProgDataUnit(\"" + dataUnit + "\")");
        this.doReaderCommand("set ProgDataUnit = " + dataUnit);
    }

    public int getProgReadAttempts() throws AlienReaderException {
        this.log(this.logPrefix + "getProgReadAttempts()");
        this.doReaderCommand("get ProgReadAttempts");
        return this.getReaderReplyValueInt();
    }

    public void setProgReadAttempts(int readAttempts) throws AlienReaderException {
        this.log(this.logPrefix + "setProgReadAttempts(" + readAttempts + ")");
        this.doReaderCommand("set ProgReadAttempts = " + readAttempts);
    }

    public int getProgEraseAttempts() throws AlienReaderException {
        this.log(this.logPrefix + "getProgEraseAttempts()");
        this.doReaderCommand("get ProgEraseAttempts");
        return this.getReaderReplyValueInt();
    }

    public void setProgEraseAttempts(int eraseAttempts) throws AlienReaderException {
        this.log(this.logPrefix + "setProgEraseAttempts(" + eraseAttempts + ")");
        this.doReaderCommand("set ProgEraseAttempts = " + eraseAttempts);
    }

    public int getProgAttempts() throws AlienReaderException {
        this.log(this.logPrefix + "getProgAttempts()");
        this.doReaderCommand("get ProgAttempts");
        return this.getReaderReplyValueInt();
    }

    public void setProgAttempts(int progAttempts) throws AlienReaderException {
        this.log(this.logPrefix + "setProgAttempts(" + progAttempts + ")");
        this.doReaderCommand("set ProgAttempts = " + progAttempts);
    }

    public int getProgIncrementOnFail() throws AlienReaderException {
        this.log(this.logPrefix + "getProgIncrementOnFail()");
        this.doReaderCommand("get ProgIncrementOnFail");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setProgIncrementOnFail(int incrementOnFail) throws AlienReaderException {
        if (incrementOnFail == 1) {
            this.log(this.logPrefix + "setProgIncrementOnFail(\"On\")");
            this.doReaderCommand("set ProgIncrementOnFail=ON");
        } else {
            this.log(this.logPrefix + "setProgIncrementOnFail(\"Off\")");
            this.doReaderCommand("set ProgIncrementOnFail=OFF");
        }
    }

    public String getProgEPCDataInc() throws AlienReaderException {
        this.log(this.logPrefix + "getProgEPCDataInc()");
        this.doReaderCommand("get ProgEPCDataInc");
        if (this.getReaderReplyValueString().equalsIgnoreCase("Success")) {
            return "Success";
        } else if (this.getReaderReplyValueString().equalsIgnoreCase("Fail")) {
            return "Fail";
        } else if (this.getReaderReplyValueString().equalsIgnoreCase("OFF")) {
            return "OFF";
        } else {
            return this.getReaderReplyValueString().equalsIgnoreCase("Write") ? "Write" : "Always";
        }
    }

    public void setProgEPCDataInc(String epcDataInc) throws AlienReaderException {
        this.log(this.logPrefix + "setProgEPCDataInc(\"" + epcDataInc + "\")");
        this.doReaderCommand("set ProgEPCDataInc=" + epcDataInc);
    }

    public int getProgEPCDataIncCount() throws AlienReaderException {
        this.log(this.logPrefix + "getProgEPCDataIncCount()");
        this.doReaderCommand("get ProgEPCDataIncCount");
        return this.getReaderReplyValueInt();
    }

    public void setProgEPCDataIncCount(int epcDataIncCount) throws AlienReaderException {
        this.log(this.logPrefix + "setProgEPCDataIncCount(\"" + epcDataIncCount + "\")");
        this.doReaderCommand("set ProgEPCDataIncCount=" + epcDataIncCount);
    }

    public String getProgUserDataInc() throws AlienReaderException {
        this.log(this.logPrefix + "getProgUserDataInc()");
        this.doReaderCommand("get ProgUserDataInc");
        if (this.getReaderReplyValueString().equalsIgnoreCase("Success")) {
            return "Success";
        } else if (this.getReaderReplyValueString().equalsIgnoreCase("Fail")) {
            return "Fail";
        } else if (this.getReaderReplyValueString().equalsIgnoreCase("OFF")) {
            return "OFF";
        } else {
            return this.getReaderReplyValueString().equalsIgnoreCase("Write") ? "Write" : "Always";
        }
    }

    public void setProgUserDataInc(String userDataInc) throws AlienReaderException {
        this.log(this.logPrefix + "setProgUserDataInc(\"" + userDataInc + "\")");
        this.doReaderCommand("set ProgUserDataInc=" + userDataInc);
    }

    public int getProgUserDataIncCount() throws AlienReaderException {
        this.log(this.logPrefix + "getProgUserDataIncCount()");
        this.doReaderCommand("get ProgUserDataIncCount");
        return this.getReaderReplyValueInt();
    }

    public void setProgUserDataIncCount(int userDataIncCount) throws AlienReaderException {
        this.log(this.logPrefix + "setProgUserDataIncCount(\"" + userDataIncCount + "\")");
        this.doReaderCommand("set ProgUserDataIncCount=" + userDataIncCount);
    }

    public int getProgBlockSize() throws AlienReaderException {
        this.log(this.logPrefix + "getProgBlockSize()");
        this.doReaderCommand("get ProgBlockSize");
        return this.getReaderReplyValueInt();
    }

    public void setProgBlockSize(int progBlockSize) throws AlienReaderException {
        this.log(this.logPrefix + "setProgBlockSize(\"" + progBlockSize + "\")");
        this.doReaderCommand("set ProgBlockSize=" + progBlockSize);
    }

    public int getProgBlockAlign() throws AlienReaderException {
        this.log(this.logPrefix + "getProgBlockAlign()");
        this.doReaderCommand("get ProgBlockAlign");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setProgBlockAlign(int progBlockAlign) throws AlienReaderException {
        if (progBlockAlign == 1) {
            this.log(this.logPrefix + "setProgBlockAlign(\"ON\")");
            this.doReaderCommand("set ProgBlockAlign=ON");
        } else if (progBlockAlign == 0) {
            this.log(this.logPrefix + "setProgBlockAlign(\"OFF\")");
            this.doReaderCommand("set ProgBlockAlign=OFF");
        } else {
            this.log(this.logPrefix + "setProgBlockAlign(" + progBlockAlign + ")");
            this.doReaderCommand("set ProgBlockAlign=" + progBlockAlign);
        }

    }

    public void g2Write(int bank, int wordPtr, byte[] bytes, int offset, int len) throws AlienReaderException {
        this.log(this.logPrefix + "g2Write(\"(bytes)\", " + offset + ", " + len + ")");
        if (bytes == null) {
            bytes = new byte[0];
        }

        if (offset < 0 || offset > bytes.length - 1) {
            offset = 0;
        }

        if (len > bytes.length - offset) {
            len = bytes.length - offset;
        }

        String hexStr = Converters.toHexString(bytes, offset, len, " ");
        this.doReaderCommand("g2Write=" + bank + ", " + wordPtr + ", " + hexStr);
    }

    public byte[] g2Read(int bank, int wordPtr, int wordLen) throws AlienReaderException {
        this.log(this.logPrefix + "g2Read(" + bank + ", " + wordPtr + ", " + wordLen + ")");
        this.doReaderCommand("g2Read = " + bank + " " + wordPtr + " " + wordLen);
        return Converters.fromHexString(this.getReaderReplyValueString());
    }

    public void g2Erase(int bank, int wordPtr, int wordLen) throws AlienReaderException {
        this.log(this.logPrefix + "g2Erase(" + bank + ", " + wordPtr + ", " + wordLen + ")");
        this.doReaderCommand("g2Erase = " + bank + " " + wordPtr + " " + wordLen);
    }

    public int getProgSingulate() throws AlienReaderException {
        this.log(this.logPrefix + "getProgSingulate()");
        this.doReaderCommand("get ProgSingulate");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setProgSingulate(int progSingulate) throws AlienReaderException {
        if (progSingulate == 1) {
            this.log(this.logPrefix + "setProgSingulate(\"ON\")");
            this.doReaderCommand("set ProgSingulate=ON");
        } else if (progSingulate == 0) {
            this.log(this.logPrefix + "setProgSingulate(\"OFF\")");
            this.doReaderCommand("set ProgSingulate=OFF");
        } else {
            this.log(this.logPrefix + "setProgSingulate(" + progSingulate + ")");
            this.doReaderCommand("set ProgSingulate=" + progSingulate);
        }

    }

    public String getTagInfo() throws AlienReaderException {
        this.log(this.logPrefix + "getTagInfo()");
        this.doReaderCommand("get taginfo");
        return this.getReaderReplyValueString();
    }

    public int getAcquireTime() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireTime()");
        this.doReaderCommand("get AcqTime");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireTime(int acquireTime) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireTime(" + acquireTime + ")");
        this.doReaderCommand("set AcqTime=" + acquireTime);
    }

    public int getAcquireCycles() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireCycles()");
        this.doReaderCommand("Get AcqCycles");
        return this.getReaderReplyValueInt();
    }

    public int getAcquireC1Cycles() throws AlienReaderException {
        return this.getAcquireCycles();
    }

    public void setAcquireCycles(int acqC1Cycles) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireCycles(" + acqC1Cycles + ")");
        this.doReaderCommand("Set AcqCycles=" + acqC1Cycles);
    }

    public void setAcquireC1Cycles(int acqC1Cycles) throws AlienReaderException {
        this.setAcquireCycles(acqC1Cycles);
    }

    public int getAcquireEnterWakeCount() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireEnterWakeCount()");
        this.doReaderCommand("Get AcqEnterWakeCount");
        return this.getReaderReplyValueInt();
    }

    public int getAcquireC1EnterWakeCount() throws AlienReaderException {
        return this.getAcquireEnterWakeCount();
    }

    public void setAcquireEnterWakeCount(int acqC1EnterWakeCount) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireEnterWakeCount(" + acqC1EnterWakeCount + ")");
        this.doReaderCommand("Set AcqEnterWakeCount=" + acqC1EnterWakeCount);
    }

    public void setAcquireC1EnterWakeCount(int acqC1EnterWakeCount) throws AlienReaderException {
        this.setAcquireEnterWakeCount(acqC1EnterWakeCount);
    }

    public int getAcquireCount() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireCount()");
        this.doReaderCommand("Get AcqCount");
        return this.getReaderReplyValueInt();
    }

    public int getAcquireC1Count() throws AlienReaderException {
        return this.getAcquireCount();
    }

    public void setAcquireCount(int acqC1Count) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireCount(" + acqC1Count + ")");
        this.doReaderCommand("Set AcqCount=" + acqC1Count);
    }

    public void setAcquireC1Count(int acqC1Count) throws AlienReaderException {
        this.setAcquireCount(acqC1Count);
    }

    public int getAcquireSleepCount() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireSleepCount()");
        this.doReaderCommand("Get AcqSleepCount");
        return this.getReaderReplyValueInt();
    }

    public int getAcquireC1SleepCount() throws AlienReaderException {
        return this.getAcquireSleepCount();
    }

    public void setAcquireSleepCount(int acqC1SleepCount) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireSleepCount(" + acqC1SleepCount + ")");
        this.doReaderCommand("Set AcqSleepCount=" + acqC1SleepCount);
    }

    public void setAcquireC1SleepCount(int acqC1SleepCount) throws AlienReaderException {
        this.setAcquireSleepCount(acqC1SleepCount);
    }

    public int getAcquireExitWakeCount() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireExitWakeCount()");
        this.doReaderCommand("Get AcqExitWakeCount");
        return this.getReaderReplyValueInt();
    }

    public int getAcquireC1ExitWakeCount() throws AlienReaderException {
        return this.getAcquireExitWakeCount();
    }

    public void setAcquireExitWakeCount(int acqC1ExitWakeCount) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireExitWakeCount(" + acqC1ExitWakeCount + ")");
        this.doReaderCommand("Set AcqExitWakeCount=" + acqC1ExitWakeCount);
    }

    public void setAcquireC1ExitWakeCount(int acqC1ExitWakeCount) throws AlienReaderException {
        this.setAcquireExitWakeCount(acqC1ExitWakeCount);
    }

    public int getAcquireC0Cycles() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireC0Cycles()");
        this.doReaderCommand("Get AcqC0Cycles");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireC0Cycles(int acqC0Cycles) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireC0Cycles(" + acqC0Cycles + ")");
        this.doReaderCommand("Set AcqC0Cycles=" + acqC0Cycles);
    }

    public int getAcquireC0Count() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireC0Count()");
        this.doReaderCommand("Get AcqC0Count");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireC0Count(int acqC0Count) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireC0Count(" + acqC0Count + ")");
        this.doReaderCommand("Set AcqC0Count=" + acqC0Count);
    }

    public int getAcquireG2Cycles() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2Cycles()");
        this.doReaderCommand("Get AcqG2Cycles");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireG2Cycles(int acqG2Cycles) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireG2Cycles(" + acqG2Cycles + ")");
        this.doReaderCommand("Set AcqG2Cycles=" + acqG2Cycles);
    }

    public int getAcquireG2Count() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2Count()");
        this.doReaderCommand("Get AcqG2Count");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireG2Count(int acqG2Count) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireG2Count(" + acqG2Count + ")");
        this.doReaderCommand("Set AcqG2Count=" + acqG2Count);
    }

    public int getAcquireG2Q() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2Q()");
        this.doReaderCommand("Get AcqG2Q");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireG2Q(int acqG2Q) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireG2Q(" + acqG2Q + ")");
        this.doReaderCommand("Set AcqG2Q=" + acqG2Q);
    }

    public int getAcquireG2Selects() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2Selects()");
        this.doReaderCommand("Get AcqG2Select");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireG2Selects(int acqG2Select) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireG2Selects(" + acqG2Select + ")");
        this.doReaderCommand("Set AcqG2Select=" + acqG2Select);
    }

    public int getAcquireG2Session() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2Session()");
        this.doReaderCommand("Get AcqG2Session");
        return this.getReaderReplyValueInt();
    }

    public void setAcquireG2Session(int acqG2Session) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireG2Session(" + acqG2Session + ")");
        this.doReaderCommand("Set AcqG2Session=" + acqG2Session);
    }

    public void setAcquireG2TagData(String g2TagDataString) throws AlienReaderException {
        if (g2TagDataString == null || g2TagDataString.equals("")) {
            g2TagDataString = "None";
        }

        this.log(this.logPrefix + "setAcquireG2TagData(\"" + g2TagDataString + "\")");
        this.doReaderCommand("set AcqG2TagData=" + g2TagDataString);
    }

    public void setAcquireG2TagData(int bank, int wordPtr, int wordCount) throws AlienReaderException {
        this.setAcquireG2TagData(bank + "," + wordPtr + "," + wordCount);
    }

    public String getAcquireG2TagData() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2TagData()");
        this.doReaderCommand("get AcqG2TagData");
        return this.getReaderReplyValueString();
    }

    public int getAcquireG2OpsMode() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2OpsMode()");
        this.doReaderCommand("get AcqG2OpsMode");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setAcquireG2OpsMode(int g2OpsMode) throws AlienReaderException {
        if (g2OpsMode == 1) {
            this.log(this.logPrefix + "setAcquireG2OpsMode(\"ON\")");
            this.doReaderCommand("set AcqG2OpsMode=ON");
        } else if (g2OpsMode == 0) {
            this.log(this.logPrefix + "setAcquireG2OpsMode(\"OFF\")");
            this.doReaderCommand("set AcqG2OpsMode=OFF");
        } else {
            this.log(this.logPrefix + "setAcqG2OpsMode(" + g2OpsMode + ")");
            this.doReaderCommand("set AcqG2OpsMode=" + g2OpsMode);
        }

    }

    public String getAcquireG2Ops() throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2Ops()");
        this.doReaderCommand("get AcqG2Ops");
        return this.getReaderReply();
    }

    public String getAcquireG2Ops(int opsNum) throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2Ops(" + opsNum + ")");
        this.doReaderCommand("get AcqG2Ops " + opsNum);
        return this.getReaderReply().substring(2);
    }

    public void setAcquireG2Ops(String g2OpsCommand) throws AlienReaderException {
        this.log(this.logPrefix + "getAcqG2Ops(" + g2OpsCommand + ")");
        this.doReaderCommand("set AcqG2Ops = " + g2OpsCommand);
    }

    public void setAcquireG2Ops(int opNum, String op) throws AlienReaderException {
        if (opNum == 0) {
            this.setAcquireG2Ops("0");
        } else {
            this.setAcquireG2Ops(opNum + " " + op);
        }

    }

    public void setAcquireG2Ops(int opNum, String opAction, String opArgs) throws AlienReaderException {
        if (opNum == 0) {
            this.setAcquireG2Ops("0");
        } else {
            this.setAcquireG2Ops(opNum + " " + opAction + " " + opArgs);
        }

    }

    public void setAcquireG2Target(String g2Target) throws AlienReaderException {
        this.log(this.logPrefix + "setAcquireG2Target(\"" + g2Target + "\")");
        this.doReaderCommand("set AcqG2Target=" + g2Target);
    }

    public String getAcquireG2Target() throws AlienReaderException {
        this.log(this.logPrefix + "getAcquireG2Target()");
        this.doReaderCommand("get AcqG2Target");
        return this.getReaderReplyValueString();
    }

    public String getSpeedFilter() throws AlienReaderException {
        this.log(this.logPrefix + "getSpeedFilter()");
        this.doReaderCommand("get SpeedFilter");
        return this.getReaderReplyValueString();
    }

    public void setSpeedFilter(String filterStr) throws AlienReaderException {
        if (filterStr == null || filterStr.equals("")) {
            filterStr = "0";
        }

        this.log(this.logPrefix + "setSpeedFilter(\"" + filterStr + "\")");
        this.doReaderCommand("set SpeedFilter=" + filterStr);
    }

    public void setSpeedFilter(float S1, float S2) throws AlienReaderException {
        if (S1 == 0.0F && S2 == 0.0F) {
            this.setSpeedFilter("0");
        } else {
            this.setSpeedFilter(S1 + ", " + S2);
        }

    }

    public String getRSSIFilter() throws AlienReaderException {
        this.log(this.logPrefix + "getRSSIFilter()");
        this.doReaderCommand("get RSSIFilter");
        return this.getReaderReplyValueString();
    }

    public void setRSSIFilter(String filterStr) throws AlienReaderException {
        if (filterStr == null || filterStr.equals("")) {
            filterStr = "0";
        }

        this.log(this.logPrefix + "setRSSIFilter(\"" + filterStr + "\")");
        this.doReaderCommand("set RSSIFilter=" + filterStr);
    }

    public void setRSSIFilter(float R1, float R2) throws AlienReaderException {
        if (R1 == 0.0F && R2 == 0.0F) {
            this.setRSSIFilter("0");
        } else {
            this.setRSSIFilter(R1 + ", " + R2);
        }

    }

    public int getLBT() throws AlienReaderException {
        this.log(this.logPrefix + "getLBT() => ");
        this.doReaderCommand("get LBT");
        return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0 ? 1 : 0;
    }

    public void setLBT(int lbt) throws AlienReaderException {
        if (lbt == 1) {
            this.log(this.logPrefix + "setLBT(\"ON\")");
            this.doReaderCommand("set LBT=ON");
        } else if (lbt == 0) {
            this.log(this.logPrefix + "setLBT(\"OFF\")");
            this.doReaderCommand("set LBT=OFF");
        } else {
            this.log(this.logPrefix + "setLBT(\"" + lbt + "\")");
            this.doReaderCommand("set LBT=" + lbt);
        }

    }

    public int getLBTLimit() throws AlienReaderException {
        this.log(this.logPrefix + "getLBTLimit()");
        this.doReaderCommand("get LBTLimit");
        return this.getReaderReplyValueInt();
    }

    public void setLBTLimit(int lbtLimit) throws AlienReaderException {
        this.log(this.logPrefix + "setLBTLimit(" + lbtLimit + ")");
        this.doReaderCommand("set LBTLimit=" + lbtLimit);
    }

    public int getLBTValue() throws AlienReaderException {
        this.log(this.logPrefix + "getLBTValue()");
        this.doReaderCommand("get LBTValue");
        return this.getReaderReplyValueInt();
    }

    public int getTagType() throws AlienReaderException {
        this.log(this.logPrefix + "getTagType()");
        this.doReaderCommand("get TagType");
        return this.getReaderReplyValueInt();
    }

    public void setTagType(int tagType) throws AlienReaderException {
        this.log(this.logPrefix + "setTagType(" + tagType + ")");
        this.doReaderCommand("set TagType=" + tagType);
    }

    public Date getTime() throws AlienReaderException {
        this.log(this.logPrefix + "getTime()");
        this.doReaderCommand("get time");
        String date = this.getReaderReplyValueString();
        return DATE_FORMATTER.parse(date, new ParsePosition(0));
    }

    public void setTime(String dateTimeString) throws AlienReaderException {
        this.log(this.logPrefix + "setTime(\"" + dateTimeString + "\")");
        this.doReaderCommand("set time=" + dateTimeString);
    }

    public void setTime() throws AlienReaderException {
        this.log(this.logPrefix + "setTime() =>");
        String timeString = DATE_FORMATTER.format(new Date());
        this.setTime(timeString);
    }

    public void setTime(Date theDate) throws AlienReaderException {
        this.log(this.logPrefix + "setTime(\"" + theDate + "\") =>");
        String timeString = DATE_FORMATTER.format(theDate);
        this.setTime(timeString);
    }

    public int getTimeZone() throws AlienReaderException {
        this.log(this.logPrefix + "getTimeZone()");
        this.doReaderCommand("get timeZone");
        return this.getReaderReplyValueInt();
    }

    public void setTimeZone(int timeZone) throws AlienReaderException {
        this.log(this.logPrefix + "setTimeZone(" + timeZone + ")");
        this.doReaderCommand("set timeZone=" + timeZone);
    }

    public String getTimeServer() throws AlienReaderException {
        this.log(this.logPrefix + "getTimeServer()");
        this.doReaderCommand("get timeServer");
        return this.getReaderReplyValueString();
    }

    public void setTimeServer(String timeServer) throws AlienReaderException {
        this.log(this.logPrefix + "setTimeServer(\"" + timeServer + "\")");
        this.doReaderCommand("set timeServer=" + timeServer);
    }

    public String macroList() throws AlienReaderException {
        this.log(this.logPrefix + "macroList()");
        this.doReaderCommand("MacroList");
        return this.getReaderReply();
    }

    public String macroView(String macroName) throws AlienReaderException {
        this.log(this.logPrefix + "macroView(\"" + macroName + "\")");
        this.doReaderCommand("MacroView " + macroName);
        return this.getReaderReply();
    }

    public void macroRun(String macroName) throws AlienReaderException {
        this.log(this.logPrefix + "macroRun(\"" + macroName + "\")");
        this.doReaderCommand("MacroRun " + macroName);
    }

    public void macroDel(String macroName) throws AlienReaderException {
        this.log(this.logPrefix + "macroDel(\"" + macroName + "\")");
        this.doReaderCommand("MacroDel " + macroName);
    }

    public void macroDelAll() throws AlienReaderException {
        this.log(this.logPrefix + "macroDelAll()");
        this.doReaderCommand("MacroDelAll");
    }

    public void macroStartRec(String macroName) throws AlienReaderException {
        this.log(this.logPrefix + "macroStartRec(\"" + macroName + "\")");
        this.doReaderCommand("MacroStartRec " + macroName);
    }

    public void macroStopRec() throws AlienReaderException {
        this.log(this.logPrefix + "macroStopRec()");
        this.doReaderCommand("MacroStopRec");
    }

    public String toString() {
        return "Alien Class 1 Reader";
    }
}
