package com.alien.enterpriseRFID.reader;

import com.alien.enterpriseRFID.tags.Tag;
import com.alien.enterpriseRFID.tags.TagTable;
import com.alien.enterpriseRFID.util.Converters;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class AlienClassOEMReader extends AlienClass1Reader implements Runnable{
    private TagTable tagTable;
    private int autoModeState;
    private int acquireMode;
    private int tagType;
    private int acquireC1Cycles;
    private int acquireC1EnterWakeCount;
    private int acquireC1Count;
    private int acquireC1SleepCount;
    private int acquireC1ExitWakeCount;
    private int acquireC1HopEnable;
    private int acquireG2Cycles;
    private int acquireG2Count;
    private int acquireG2Q;
    private int timerTotalCommands;
    private int timerTotalTime;
    private Thread motor;
    private static int[] BAUD_RATES = new int[]{19200, 115200, 38400, 57600, 230400};
    private static final String ERROR_PREFIX = "Error: ";
    public AlienDLEObject rc;
    private static int ACT_DO = 1;
    private static int ACT_GET = 2;
    private static int ACT_SET = 3;
    private String readerPreviousCommand;
    private String readerCommandKeyword;
    private int readerCommandAction;
    private String readerCommandValueString;
    private int readerCommandValueInt;
    private int[] readerCommandValueIntArray;
    private int[] readerCommandValueHexArray;
    private String[] readerCommandValueStringArray;
    private String internalReaderReply;
    private final int BUFFER_SIZE;
    public byte[] lastResponseBytes;
    public byte[] lastCommandBytes;
    public int lastResponseByteCount;
    public int lastCommandByteCount;
    private boolean isCyclops;

    public AlienClassOEMReader(String address) {
        this.acquireMode = 0;
        this.tagType = 3;
        this.acquireC1Cycles = 1;
        this.acquireC1EnterWakeCount = 3;
        this.acquireC1Count = 1;
        this.acquireC1SleepCount = 1;
        this.acquireC1ExitWakeCount = 0;
        this.acquireC1HopEnable = 1;
        this.acquireG2Cycles = 1;
        this.acquireG2Count = 10;
        this.acquireG2Q = 3;
        this.BUFFER_SIZE = 20000;
        this.lastResponseBytes = new byte[20000];
        this.lastCommandBytes = new byte[20000];
        this.lastResponseByteCount = 0;
        this.lastCommandByteCount = 0;
        this.isCyclops = true;
        this.tagTable = new TagTable(false);
        this.setPersistTime(-1);
        this.rc = new AlienDLEObject();
        this.rc.isCyclops = this.isCyclops;
        this.autoModeState = 0;
        if (address != "") {
            this.setConnection(address);
        }

        this.setDebugLevel(0);
    }

    public AlienClassOEMReader() {
        this("");
    }

    public void setDebugLevel(int level) {
        super.setDebugLevel(level);
    }

    public void issueReaderCommand(AlienDLEObject rc) throws AlienReaderConnectionException, AlienReaderTimeoutException, AlienReaderCommandErrorException {
        this.lastCommandByteCount = rc.commandLength;

        int i;
        for(i = 0; i < rc.commandLength; ++i) {
            this.lastCommandBytes[i] = rc.commandBuffer[i];
        }

        if (rc.commandBuffer[4] >= 0) {
            if (this.getDebugLevel() == 2) {
                System.out.println("\n  To Reader: " + Converters.toHexString(rc.commandBuffer, 0, rc.commandLength, " "));
            }

            try {
                this.getOutputStream().write(rc.commandBuffer, 0, rc.commandLength);
            } catch (IOException var6) {
                throw new AlienReaderConnectionException(var6.getMessage());
            }
        } else {
            rc.commandBuffer[4] = 64;
        }

        rc.status = 3;
        rc.replyLength = 0;
        rc.replyLengthRaw = 0;

        for(i = 0; i < 5; ++i) {
            rc.replyBuffer[i] = 0;
        }

        this.lastResponseByteCount = 0;
        long startTime = System.currentTimeMillis();
        if (this.getDebugLevel() == 2) {
            System.out.print("From Reader: ");
        }

        long deltaTime;
        do {
            try {
                while(this.getInputStream().available() > 0) {
                    int b = this.getInputStream().read();
                    if (b >= 0) {
                        if (this.getDebugLevel() == 2) {
                            System.out.print(Converters.toHexString((long)b, 1) + " ");
                        }

                        startTime = System.currentTimeMillis();
                        rc.addReply(b);
                        this.lastResponseBytes[this.lastResponseByteCount++] = (byte)b;
                        if (rc.status == 10) {
                            if (this.getDebugLevel() == 2) {
                                System.out.println(" ... COMPLETE");
                            }

                            if (rc.commandBuffer[4] != 64) {
                                return;
                            }

                            if (rc.replyCommType == 3) {
                                return;
                            }

                            if (this.getDebugLevel() == 2) {
                                System.out.print("From Reader> ");
                            }
                        }

                        if (rc.status == 11) {
                            if (this.getDebugLevel() == 2) {
                                System.out.println(" ... COMMAND ERROR");
                            }

                            if (rc.commandBuffer[4] != 64) {
                                String msg = "Error: executing command - ";
                                msg = msg + rc.replyCommTypeMessage + " (" + rc.replyCommTypeHexString + ")";
                                throw new AlienReaderCommandErrorException(msg);
                            }

                            if (this.getDebugLevel() == 2) {
                                System.out.print("From Reader> ");
                            }
                        }
                    }
                }
            } catch (IOException var7) {
                throw new AlienReaderConnectionException(var7.getMessage());
            }

            deltaTime = System.currentTimeMillis() - startTime;
        } while(deltaTime <= (long)this.getTimeOutMilliseconds());

        rc.status = 12;
        if (this.getDebugLevel() == 2) {
            System.out.println(" ... TIMEOUT");
        }

        throw new AlienReaderTimeoutException("Error: Timeout Waiting for Input Data");
    }

    public void setPersistTime(int persistTime) {
        if (persistTime != -1) {
            persistTime *= 1000;
        }

        this.tagTable.setPersistTime(persistTime);
    }

    public int getPersistTime() {
        int persistTime = this.tagTable.getPersistTime();
        if (persistTime != -1) {
            persistTime /= 1000;
        }

        return persistTime;
    }

    public void clearTagList() {
        this.tagTable.clearTagList();
    }

    public Tag[] getTagList() throws AlienReaderException {
        return this.getTagList(1);
    }

    public Tag[] getTagList(int multiplier) throws AlienReaderException {
        if (this.autoModeState == 0) {
            this.tagTable.clearTagList();
            this.timerTotalCommands = multiplier;
            long startTime = System.currentTimeMillis();
            this.acquireTags(multiplier);
            this.timerTotalTime = (int)(System.currentTimeMillis() - startTime);
        }

        Tag[] tagList = this.tagTable.getTagList();
        if (this.autoModeState == 1 && this.tagTable.getPersistTime() == -1) {
            this.tagTable.clearTagList();
        }

        if (tagList == null) {
            return null;
        } else {
            return tagList.length == 0 ? null : tagList;
        }
    }

    public Tag getTag() throws AlienReaderException {
        return this.getTag(1);
    }

    public Tag getTag(int multiplier) throws AlienReaderException {
        Tag[] tagList = this.getTagList(multiplier);
        return tagList != null && tagList.length > 0 ? tagList[0] : null;
    }

    public String getTagID() throws AlienReaderException {
        Tag tag = this.getTag();
        return tag != null ? tag.getTagID() : "No Tag";
    }

    public int getAutoMode() {
        return this.autoModeState;
    }

    public void setAutoMode(int autoMode) {
        byte[] cyclopsAutomodeBytes = new byte[]{36, 1, 5};
        if (autoMode == 0) {
            this.autoModeState = 0;
            this.motor = null;
            if (this.isCyclops) {
                try {
                    this.rc.prepareGenericCommand(cyclopsAutomodeBytes);
                    this.issueReaderCommand(this.rc);
                } catch (AlienReaderException var5) {
                    System.out.println("Error turning off AutoMode: " + var5);
                }
            }
        } else {
            this.autoModeState = 1;
            if (this.isCyclops) {
                try {
                    this.rc.prepareGenericCommand(cyclopsAutomodeBytes);
                    this.issueReaderCommand(this.rc);
                } catch (AlienReaderException var4) {
                    System.out.println("Error turning off AutoMode: " + var4);
                }
            }

            this.motor = new Thread(this);
            this.motor.start();
        }

    }

    public void autoModeReset() {
        this.setAutoMode(0);
    }

    public String getAcquireMode() {
        return this.acquireMode == 0 ? "Inventory" : "Global Scroll";
    }

    public void setAcquireMode(String acquireMode) {
        if (acquireMode.toLowerCase().equals("Inventory".toLowerCase())) {
            this.acquireMode = 0;
        } else {
            this.acquireMode = 1;
        }

    }

    public int getAcquireG2Cycles() {
        return this.acquireG2Cycles;
    }

    public void setAcquireG2Cycles(int acquireG2Cycles) {
        this.acquireG2Cycles = acquireG2Cycles;
    }

    public int getAcquireG2Count() {
        return this.acquireG2Count;
    }

    public void setAcquireG2Count(int acquireG2Count) {
        this.acquireG2Count = acquireG2Count;
    }

    public int getAcquireG2Q() {
        return this.acquireG2Q;
    }

    public void setAcquireG2Q(int acquireG2Q) {
        this.acquireG2Q = acquireG2Q;
    }

    public int getAcquireC1Cycles() {
        return this.acquireC1Cycles;
    }

    public int getAcquireCycles() {
        return this.getAcquireC1Cycles();
    }

    public void setAcquireC1Cycles(int acquireC1Cycles) {
        this.acquireC1Cycles = acquireC1Cycles;
    }

    public void setAcquireCycles(int acquireC1Cycles) {
        this.setAcquireC1Cycles(acquireC1Cycles);
    }

    public int getAcquireC1Count() {
        return this.acquireC1Count;
    }

    public int getAcquireCount() {
        return this.getAcquireC1Count();
    }

    public void setAcquireC1Count(int acquireC1Count) {
        this.acquireC1Count = acquireC1Count;
    }

    public void setAcquireCount(int acquireC1Count) {
        this.setAcquireC1Count(acquireC1Count);
    }

    public int getAcquireC1EnterWakeCount() {
        return this.acquireC1EnterWakeCount;
    }

    public int getAcquireEnterWakeCount() {
        return this.getAcquireC1EnterWakeCount();
    }

    public void setAcquireC1EnterWakeCount(int acquireC1EnterWakeCount) {
        this.acquireC1EnterWakeCount = acquireC1EnterWakeCount;
    }

    public void setAcquireEnterWakeCount(int acquireC1EnterWakeCount) {
        this.setAcquireC1EnterWakeCount(acquireC1EnterWakeCount);
    }

    public int getAcquireC1ExitWakeCount() {
        return this.acquireC1ExitWakeCount;
    }

    public int getAcquireExitWakeCount() {
        return this.getAcquireC1ExitWakeCount();
    }

    public void setAcquireC1ExitWakeCount(int acquireC1ExitWakeCount) {
        this.acquireC1ExitWakeCount = acquireC1ExitWakeCount;
    }

    public void setAcquireExitWakeCount(int acquireC1ExitWakeCount) {
        this.setAcquireC1ExitWakeCount(acquireC1ExitWakeCount);
    }

    public int getAcquireC1SleepCount() {
        return this.acquireC1SleepCount;
    }

    public int getAcquireSleepCount() {
        return this.getAcquireC1SleepCount();
    }

    public void setAcquireC1SleepCount(int acquireC1SleepCount) {
        this.acquireC1SleepCount = acquireC1SleepCount;
    }

    public void setAcquireSleepCount(int acquireC1SleepCount) {
        this.setAcquireC1SleepCount(acquireC1SleepCount);
    }

    public String getReaderName() {
        return "Alien OEM Reader Module";
    }

    public String getReaderType() {
        int radioType = 18;
        String frequencyString = "915 MHz";
        String modelString = "9930";
        String numAntennasString = "Two Antennas";

        try {
            radioType = this.getRadioType();
        } catch (Exception var10) {
        }

        int maxAntenna = (radioType & 240) >> 4;
        int frequencyType = radioType & 15;
        String[] frequencyStrings = new String[]{"", "868 MHz", "915 MHz", "950 MHz", "2450 MHz", "866 MHz (Co-Channel)"};
        String[] modelStrings = new String[]{"", "8930", "9930", "7930", "2930", "8930"};
        if (frequencyType > 0 && frequencyType <= 6) {
            frequencyString = frequencyStrings[frequencyType];
            modelString = modelStrings[frequencyType];
        }

        String[] antennaStrings = new String[]{"One Antenna", "Two Antennas", "Three Antennas", "Four Antennas"};
        if (maxAntenna >= 0 && maxAntenna <= 3) {
            numAntennasString = antennaStrings[maxAntenna];
        }

        return "Alien OEM RFID Reader Module, Model: ALR-" + modelString + " (" + numAntennasString + " / EPC Class 1 / " + frequencyString + " / DLE interface)";
    }

    public String getReaderVersion() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(0);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting ReaderVersion";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        String reply = "ReaderVersion = ";
        reply = reply + Converters.toHexString(this.rc.replyValueHexArray, 2, 3, ".");
        reply = reply + "\nReaderLocale = " + Converters.toHexString(this.rc.replyValueHexArray, 0, 1, "");
        reply = reply + "\nReaderType = " + Converters.toHexString(this.rc.replyValueHexArray, 1, 1, "");
        return reply;
    }

    public void setReaderNumber(int readerNumber) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(1, readerNumber);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var4) {
            String msg = "Error: setting ReaderNumber(" + readerNumber + ")";
            msg = msg + "\n" + var4.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public int getReaderNumber() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(2);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting ReaderNumber";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getBaudRate() throws AlienReaderConnectionException {
        int currentTimeOut = this.getTimeOutMilliseconds();
        this.setTimeOutMilliseconds(500);
        int currentRate = 0;
        int i = 0;

        while(i < BAUD_RATES.length) {
            currentRate = BAUD_RATES[i];

            try {
                if (this.getDebugLevel() > 0) {
                    System.out.println("\nTrying " + currentRate + " baud...");
                }

                this.setSerialBaudRate(currentRate);
                this.open();
                this.getReaderVersion();
                if (this.getDebugLevel() > 0) {
                    System.out.println("found correct baud!");
                }

                this.setTimeOutMilliseconds(currentTimeOut);
                return currentRate;
            } catch (AlienReaderException var5) {
                ++i;
            }
        }

        this.setTimeOutMilliseconds(currentTimeOut);
        throw new AlienReaderConnectionException("Could not determine the reader's baud rate for serial communication.");
    }

    public void setBaudRate(int baudRate) throws AlienReaderException {
        int baudRateCode = -1;
        switch(baudRate) {
            case 19200:
                baudRateCode = 0;
                break;
            case 38400:
                baudRateCode = 1;
                break;
            case 57600:
                baudRateCode = 2;
                break;
            case 115200:
                baudRateCode = 4;
                break;
            case 230400:
                baudRateCode = 5;
        }

        if (baudRateCode >= 0) {
            try {
                this.rc.prepareGenericCommand(3, baudRateCode);
                this.issueReaderCommand(this.rc);
                this.close();
                this.setSerialBaudRate(baudRate);
                this.open();
            } catch (AlienReaderCommandErrorException var5) {
                String msg = "Error: setting BaudRate";
                msg = msg + "\n" + var5.getMessage();
                throw new AlienReaderCommandErrorException(msg);
            }
        }

    }

    public int resetBaudRate() throws AlienReaderConnectionException {
        int currentTimeOut = this.getTimeOutMilliseconds();
        this.setTimeOutMilliseconds(500);
        int oldBaudRate = 0;
        int i = 0;

        while(i < BAUD_RATES.length) {
            try {
                if (this.getDebugLevel() > 0) {
                    System.out.println("\nTrying " + BAUD_RATES[i] + " baud...");
                }

                this.setSerialBaudRate(BAUD_RATES[i]);
                this.open();
                this.getReaderVersion();
                if (this.getDebugLevel() > 0) {
                    System.out.println("found correct baud!");
                }

                oldBaudRate = BAUD_RATES[i];
                if (oldBaudRate != 115200) {
                    AlienDLEObject rc = new AlienDLEObject();
                    rc.prepareGenericCommand(3, 4);
                    this.issueReaderCommand(rc);
                }
                break;
            } catch (AlienReaderException var6) {
                ++i;
            }
        }

        try {
            this.setSerialBaudRate(115200);
            this.open();
            this.setTimeOutMilliseconds(currentTimeOut);
        } catch (AlienReaderException var5) {
        }

        if (oldBaudRate == 0) {
            throw new AlienReaderConnectionException("Unable to reset the reader's serial baud rate.");
        } else {
            return oldBaudRate;
        }
    }

    public void reboot() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(4);
            this.issueReaderCommand(this.rc);

            try {
                Thread.sleep(50L);
            } catch (InterruptedException var3) {
            }

        } catch (AlienReaderCommandErrorException var4) {
            String msg = "Error: rebooting the reader";
            msg = msg + "\n" + var4.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void setIOPortValue(int portValue) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(5, portValue);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var4) {
            String msg = "Error: setting IOPortValue(" + portValue + ")";
            msg = msg + "\n" + var4.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public int getIOPortValue() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(6);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting IOPortValue";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getExternalInput() throws AlienReaderException {
        return this.getIOPortValue();
    }

    public void setExternalOutput(int value) throws AlienReaderException {
        this.setIOPortValue(value);
    }

    public void setActiveAntenna(int antennaNumber) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(7, antennaNumber);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var4) {
            String msg = "Error: setting ActiveAntenna(" + antennaNumber + ")";
            msg = msg + "\n" + var4.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public int getActiveAntenna() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(8);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting ActiveAntenna";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public void setProgAntenna(int antennaNumber) throws AlienReaderException {
        this.setActiveAntenna(antennaNumber);
    }

    public int getProgAntenna() throws AlienReaderException {
        return this.getActiveAntenna();
    }

    public void setRFAttenuation(int attenuationValue) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(9, attenuationValue);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var4) {
            String msg = "Error: setting RFAttenuation(" + attenuationValue + ")";
            msg = msg + "\n" + var4.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public int getRFAttenuation() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(10);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting RFAttenuation";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getProgramEnableMode() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 0);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting ProgramEnableMode";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getGeneralPurposeOutputs() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 1);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting GeneralPurposeOutputs";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getGeneralPurposeInputs() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 2);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting GeneralPurposeInputs";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getBidirectionalIOPorts() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 3);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting BidirectionalIOPorts";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getMaxHopTableIndex() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 4);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting MaxHopTableIndex";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getHardwareReaderType() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 5);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting HardwareReaderType";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getLocalizationCode() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 6);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting LocalizationCode";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getRadioType() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 7);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting RadioType";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getMinFrequency() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 8);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting MinFrequency";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getMaxFrequency() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 9);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting MaxFrequency";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getHopStepSize() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 10);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting HopStepSize";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getPLLType() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 11);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting PLLType";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getMaxRFChannel() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 12);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting MaxRFChannel";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getMaxRFPower() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 13);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting MaxRFPower";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getAvailableBaudRates() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 16);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting AvailableBaudRates";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getSupportedTagTypes() throws AlienReaderException {
        if (!this.isCyclops) {
            try {
                this.rc.prepareGenericCommand(17, 18);
                this.issueReaderCommand(this.rc);
            } catch (AlienReaderCommandErrorException var3) {
                String msg = "Error: getting SupportedTagTypes";
                msg = msg + "\n" + var3.getMessage();
                throw new AlienReaderCommandErrorException(msg);
            }

            this.tagType = this.rc.replyValueInt;
        }

        return this.tagType;
    }

    public void setSupportedTagTypes(int tagType) throws AlienReaderException {
        this.tagType = tagType;
        if (!this.isCyclops) {
            try {
                this.rc.prepareGenericCommand(19, 18, tagType);
                this.issueReaderCommand(this.rc);
            } catch (AlienReaderCommandErrorException var4) {
                String msg = "Error: setting SupportedTagTypes=" + tagType;
                msg = msg + "\n" + var4.getMessage();
                throw new AlienReaderCommandErrorException(msg);
            }
        }

    }

    public int getHopInterval() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 19);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting HopInterval";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getRFChannel() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 21);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting RFChannel";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getMaxAntenna() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(17, 22);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting MaxAntennaNumber";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public String getReaderSerialNumber() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(18, 0, 1);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting ReaderSerialNumber";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        String reply = new String(this.rc.replyValueHexArray, 2, 11);
        return reply;
    }

    public String manageReserved(String reservedCommand) throws AlienReaderException {
        byte[] bytes = Converters.fromHexString(reservedCommand);

        try {
            this.rc.prepareGenericCommand(19, bytes);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var6) {
            String msg = "Error: performing ManageReserverParameters command";
            msg = msg + "\n" + var6.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        int startPos = 4;
        int numBytes = this.rc.replyLength - 6;
        String reply = "";
        if (numBytes == 0) {
            reply = "00";
        }

        reply = reply + Converters.toHexString(this.rc.replyBuffer, startPos, numBytes, " ");
        return reply;
    }

    public void setMask(int bitLength, int bitPointer, String tagMaskBytes) throws AlienReaderException {
        try {
            if (bitLength == 0) {
                this.rc.prepareGenericCommand(20, 0);
            } else {
                byte[] maskBytes = Converters.fromHexString(tagMaskBytes);
                byte[] bytes = new byte[maskBytes.length + 2];
                bytes[0] = (byte)bitLength;
                bytes[1] = (byte)bitPointer;

                for(int i = 0; i < maskBytes.length; ++i) {
                    bytes[i + 2] = maskBytes[i];
                }

                this.rc.prepareGenericCommand(20, bytes);
            }

            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var7) {
            String msg = "Error: setting TagMask";
            msg = msg + "\n" + var7.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void setMask(String maskString) throws AlienReaderException {
        int commaPosition1 = maskString.indexOf(",");
        if (commaPosition1 < 0) {
            if (maskString.equalsIgnoreCase("all")) {
                this.setTagMask(maskString);
            } else {
                throw new AlienReaderInvalidArgumentException("setMask requires <bitLength, bitPointer, maskString> as arguments");
            }
        } else {
            int bitLength = Integer.parseInt(maskString.substring(0, commaPosition1).trim());
            int commaPosition2 = maskString.indexOf(",", commaPosition1 + 1);
            if (commaPosition2 < 0) {
                throw new AlienReaderInvalidArgumentException("setMask requires <bitLength, bitPointer, maskString> as arguments");
            } else {
                int bitPointer = Integer.parseInt(maskString.substring(commaPosition1 + 1, commaPosition2).trim());
                String tagMask = maskString.substring(commaPosition2 + 1).trim();
                this.setMask(bitLength, bitPointer, tagMask);
            }
        }
    }

    public void setTagMask(String tagID) throws AlienReaderException {
        if (tagID == null || tagID == "") {
            tagID = "All";
        }

        if (tagID.equalsIgnoreCase("All")) {
            this.setMask(0, 0, "");
        } else {
            byte[] byteArray = Converters.fromHexString(tagID);
            this.setMask(byteArray.length * 8, 0, tagID);
        }

    }

    public String getMask() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(21);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting TagMask";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        String result = "" + this.rc.replyValueHexArray[0];
        if (this.rc.replyValueHexArray[0] > 0) {
            result = result + ", " + this.rc.replyValueHexArray[1] + ", " + Converters.toHexString(this.rc.replyValueHexArray, 2, this.rc.replyValueHexArray.length - 2, " ");
        } else {
            result = "All Tags";
        }

        return result;
    }

    public void setG2Mask(int bitLength, int bitPointer, String tagMaskBytes) throws AlienReaderException {
        try {
            byte[] maskBytes = Converters.fromHexString(tagMaskBytes);
            byte[] bytes = new byte[maskBytes.length + 4];
            if (bitLength == 0) {
                bytes[0] = 1;
                bytes[1] = 1;
                bytes[2] = 0;
                bytes[3] = 32;
            } else {
                bytes[0] = 1;
                bytes[1] = 1;
                bytes[2] = (byte)bitLength;
                bytes[3] = (byte)bitPointer;
            }

            for(int i = 0; i < maskBytes.length; ++i) {
                bytes[i + 4] = maskBytes[i];
            }

            this.rc.prepareGenericCommand(20, bytes);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var7) {
            String msg = "Error: setting TagMask";
            msg = msg + "\n" + var7.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void setG2TagMask(String tagID) throws AlienReaderException {
        if (tagID == null || tagID == "") {
            tagID = "All";
        }

        if (tagID.equalsIgnoreCase("All")) {
            this.setG2Mask(0, 32, "0000 0000 0000 0000 0000 0000");
        } else {
            byte[] byteArray = Converters.fromHexString(tagID);
            this.setG2Mask(byteArray.length * 8, 32, tagID);
        }

    }

    public void setBidirectionalDDR(int bitmap) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(22, bitmap);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var4) {
            String msg = "Error: setting BidirectionalDDR(" + bitmap + ")";
            msg = msg + "\n" + var4.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public int getBidirectionalDDR() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(23);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting BidirectionalDDR";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public void setRFOnOff(int powerOnOff) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(26, powerOnOff);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var4) {
            String msg = "Error: setting RFOnOff(" + powerOnOff + ")";
            msg = msg + "\n" + var4.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public int getRFOnOff() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(27);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting RFOnOff";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public void setAntennaSequence(int[] antennaSequenceBytes) throws AlienReaderException {
        int[] sequence = new int[]{0, 255, 255, 255, 255, 255, 255, 255};

        for(int i = 0; i < antennaSequenceBytes.length; ++i) {
            sequence[i] = antennaSequenceBytes[i];
        }

        try {
            this.rc.prepareGenericCommand(28, sequence);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var5) {
            String msg = "Error: setting AntennaSequence";
            msg = msg + "\n" + var5.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void setAntennaSequence(String sequenceString) throws AlienReaderException {
        int[] sequence = new int[]{255, 255, 255, 255, 255, 255, 255, 255};
        int i = 0;

        for(StringTokenizer st = new StringTokenizer(sequenceString, ","); st.hasMoreTokens(); sequence[i++] = new Integer(st.nextToken().trim())) {
        }

        this.setAntennaSequence(sequence);
    }

    public String getAntennaSequence() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(29);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting AntennaSequence";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        String sequence = "" + this.rc.replyValueIntArray[0];

        for(int i = 1; i < 8; ++i) {
            if (this.rc.replyValueIntArray[i] < 255) {
                sequence = sequence + ", " + this.rc.replyValueIntArray[i];
            }
        }

        return sequence;
    }

    public int getAntennaStatus() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(15, 0);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting AntennaStatus";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public int getOperatingVoltage() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(15, 1);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting OperatingVoltage";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt;
    }

    public void sleepTag() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(33);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: sleeping tag";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void wakeTag() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(34);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: waking tag";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public String verifyTag() throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(61);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: verifying tag";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }

        return this.rc.replyValueInt > 0 ? Converters.toHexString(this.rc.replyValueHexArray, 3, this.rc.replyValueInt, " ", false) : "No Tag Detected, or Tag Is Locked.";
    }

    public Tag[] doInventory() throws AlienReaderException {
        ArrayList tags = new ArrayList();
        if ((this.tagType & 3) > 0) {
            int[] c1Bytes = new int[]{this.acquireMode, this.acquireC1Cycles, this.acquireC1Count, this.acquireC1EnterWakeCount, this.acquireC1ExitWakeCount, this.acquireC1HopEnable, this.acquireC1SleepCount};
            this.rc.prepareGenericCommand(64, c1Bytes);
            this.issueReaderCommand(this.rc);
            Tag[] c1TagList = this.rc.tagTable.getTagList();
            if (c1TagList != null) {
                for(int i = 0; i < c1TagList.length; ++i) {
                    c1TagList[i].setProtocol(1);
                    tags.add(c1TagList[i]);
                }
            }
        }

        if ((this.tagType & 16) > 0) {
            if (this.autoModeState != 0 && this.isCyclops) {
                this.rc.prepareGenericCommand(255);
                this.issueReaderCommand(this.rc);
            } else {
                int acqG2Mode;
                if (this.isCyclops) {
                    acqG2Mode = this.acquireMode == 0 ? 37 : 40;
                } else {
                    acqG2Mode = this.acquireMode == 0 ? 32 : 33;
                }

                int[] g2Bytes = new int[]{acqG2Mode, this.acquireG2Cycles, this.acquireG2Count, this.acquireG2Q};
                this.rc.prepareGenericCommand(64, g2Bytes);
                this.issueReaderCommand(this.rc);
            }

            Tag[] g2TagList = this.rc.tagTable.getTagList();
            if (g2TagList != null) {
                for(int i = 0; i < g2TagList.length; ++i) {
                    g2TagList[i].setProtocol(2);
                    tags.add(g2TagList[i]);
                }
            }
        }

        return (Tag[])tags.toArray(new Tag[tags.size()]);
    }

    public void programTag(int numVerifies, int numErases, int numPrograms, String tagID) throws AlienReaderException {
        byte[] tagIDBytes = Converters.fromHexString(tagID);
        int tagIDLength = tagIDBytes.length;
        int[] bytes = new int[tagIDLength + 4];
        bytes[0] = numVerifies;
        bytes[1] = numErases;
        bytes[2] = numPrograms;
        bytes[3] = tagIDLength;

        for(int i = 0; i < tagIDLength; ++i) {
            bytes[i + 4] = tagIDBytes[i];
        }

        try {
            this.rc.prepareGenericCommand(80, bytes);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var10) {
            String msg = "Error: Programming Tag:" + this.rc.replyCommTypeMessage;
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void programTag(String tagID) throws AlienReaderException {
        this.programTag(3, 3, 3, tagID);
    }

    public void eraseTag(int numVerifies, int numErases) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(81, numVerifies, numErases);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var5) {
            String msg = "Error: Erasing Tag:" + this.rc.replyCommTypeMessage;
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void eraseTag() throws AlienReaderException {
        this.eraseTag(3, 3);
    }

    public void killTag(int numVerifies, int numKills, String tagIDAndPassCode) throws AlienReaderException {
        byte[] tagIDBytes = Converters.fromHexString(tagIDAndPassCode);
        int tagIDLength = tagIDBytes.length - 1;
        int[] bytes = new int[tagIDLength + 4];
        bytes[0] = numVerifies;
        bytes[1] = numKills;
        bytes[2] = tagIDLength;

        for(int i = 0; i < tagIDLength + 1; ++i) {
            bytes[i + 3] = tagIDBytes[i];
        }

        try {
            this.rc.prepareGenericCommand(82, bytes);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var9) {
            String msg = "Error: Killing Tag:" + this.rc.replyCommTypeMessage;
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void killTag(String tagIDAndPassCode) throws AlienReaderException {
        this.killTag(3, 3, tagIDAndPassCode);
    }

    public void lockTag(int numVerifies, int numLocks, int tagIDLength, int lockCode) throws AlienReaderException {
        try {
            this.rc.prepareGenericCommand(83, numVerifies, numLocks, tagIDLength, lockCode);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var7) {
            String msg = "Error: Locking Tag:" + this.rc.replyCommTypeMessage;
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void lockTag(int tagIDLength, int lockCode) throws AlienReaderException {
        this.lockTag(3, 3, tagIDLength, lockCode);
    }

    public void lockTag(String passCode) throws AlienReaderException {
        byte[] passCodeBytes = Converters.fromHexString(passCode);
        int passCodeInt = passCodeBytes[0] & 255;
        this.lockTag(3, 3, 8, passCodeInt);
    }

    @Override
    public void run() {
        do {
            try {
                this.acquireTags(1);
                this.tagTable.removeOldTags();
                Thread.sleep(100L);
            } catch (InterruptedException var2) {
            } catch (Exception var3) {
            }
        } while(this.motor != null);
    }

    private void acquireTags(int multiplier) throws AlienReaderException {
        for(int i = 0; i < multiplier; ++i) {
            Tag[] tagList = this.doInventory();
            if (tagList != null) {
                for(int j = 0; j < tagList.length; ++j) {
                    this.tagTable.addTag(tagList[j]);
                }
            }
        }

    }

    private String getManufacturingInfoCLI() {
        String reply = "MfgInfo = ";

        try {
            int infoType = 0;

            for(int blockNumber = 0; blockNumber <= 2; ++blockNumber) {
                this.rc.prepareGenericCommand(18, infoType, blockNumber);
                this.issueReaderCommand(this.rc);
                reply = reply + new String(this.rc.replyValueHexArray, 2, 16);
            }
        } catch (AlienReaderException var4) {
            reply = var4.getMessage();
        }

        return reply + "\n";
    }

    private String getReaderNameCLI() {
        return "ReaderName = " + this.getReaderName() + "\n";
    }

    private String getReaderTypeCLI() {
        return "ReaderType = " + this.getReaderType() + "\n";
    }

    private String rebootCLI() {
        try {
            this.reboot();
            return "Rebooting System...\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getMaxAntennaCLI() {
        try {
            int maxAntenna = this.getMaxAntenna();
            return "MaxAntenna = " + maxAntenna + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getAntennaSequenceCLI() {
        try {
            String sequence = this.getAntennaSequence();
            return "AntennaSequence (i, j, k...) = " + sequence + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setAntennaSequenceCLI() {
        try {
            this.setAntennaSequence(this.readerCommandValueString);
            return this.getAntennaSequenceCLI();
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getProgAntennaCLI() {
        try {
            int antenna = this.getProgAntenna();
            return "ProgAntenna = " + antenna + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setProgAntennaCLI() {
        try {
            this.setProgAntenna(this.readerCommandValueInt);
            return this.getProgAntennaCLI();
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getAntennaStatusCLI() {
        try {
            int status = this.getAntennaStatus();
            return "AntennaStatus = " + status + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getOperatingVoltageCLI() {
        try {
            int voltage = this.getOperatingVoltage();
            double dVoltage = (double)voltage / 100.0D;
            return "OperatingVoltage = " + dVoltage + "\n";
        } catch (AlienReaderException var4) {
            return var4.getMessage() + "\n";
        }
    }

    private String getRFOnOffCLI() {
        try {
            int rfOnOff = this.getRFOnOff();
            return rfOnOff == 1 ? "RFPower = On\n" : "RFPower = Off\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setRFOnOffCLI() {
        try {
            if (this.readerCommandValueString.equalsIgnoreCase("on")) {
                this.setRFOnOff(1);
            } else {
                this.setRFOnOff(0);
            }

            return this.getRFOnOffCLI();
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getRFAttenuationCLI() {
        try {
            int attenuation = this.getRFAttenuation();
            return "RFAttenuation = " + attenuation + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setRFAttenuationCLI() {
        try {
            this.setRFAttenuation(this.readerCommandValueInt);
            return this.getRFAttenuationCLI();
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getExternalInputCLI() {
        try {
            int portValue = this.getIOPortValue();
            return "ExternalInput = " + portValue + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setExternalOutputCLI() {
        try {
            this.setIOPortValue(this.readerCommandValueInt);
            return "ExternalOutput = " + this.readerCommandValueInt + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    public String getTimer() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("#Timing Results\n");
        buffer.append("Total Time (ms) = " + this.timerTotalTime + "\n");
        int commandsSec = 0;
        if (this.timerTotalTime > 0) {
            commandsSec = this.timerTotalCommands * 1000 / this.timerTotalTime;
        }

        buffer.append("Total Commands=" + this.timerTotalCommands + "   Commands/Sec=" + commandsSec + "\n");
        Tag[] tagList = this.tagTable.getTagList();
        int totalTags = 0;
        if (tagList != null) {
            totalTags = tagList.length;
        }

        int tagsSec = 0;
        if (this.timerTotalTime > 0) {
            tagsSec = totalTags * 1000 / this.timerTotalTime;
        }

        buffer.append("Total Unique Tags=" + totalTags + "   Unique Tags/Sec=" + tagsSec + "\n");
        int totalReads = 0;

        int readsSec;
        for(readsSec = 0; readsSec < totalTags; ++readsSec) {
            if (tagList != null && tagList[readsSec] != null) {
                totalReads += tagList[readsSec].getRenewCount();
            }
        }

        readsSec = 0;
        if (this.timerTotalTime > 0) {
            readsSec = totalReads * 1000 / this.timerTotalTime;
        }

        buffer.append("Total Tag Reads=" + totalReads + "   Reads/Sec=" + readsSec);
        return buffer.toString();
    }

    private String getTagListCLI() throws AlienReaderException {
        if (this.readerCommandValueInt < 1) {
            this.readerCommandValueInt = 1;
        }

        Tag[] tagList = this.getTagList(this.readerCommandValueInt);
        if (tagList == null) {
            return "No Tags\n";
        } else {
            StringBuffer tagBuffer = new StringBuffer();

            for(int i = 0; i < tagList.length; ++i) {
                tagBuffer.append(tagList[i].toLongString() + "\n");
            }

            return tagBuffer.toString();
        }
    }

    private String getPersistTimeCLI() {
        return "PersistTime (secs) = " + this.getPersistTime() + "\n";
    }

    private String setPersistTimeCLI() {
        this.setPersistTime(this.readerCommandValueInt);
        return this.getPersistTimeCLI();
    }

    private String clearTagListCLI() {
        this.clearTagList();
        return "Tag List has been cleared!\n";
    }

    private String wakeCLI() {
        try {
            this.wakeTag();
            return "Wake = OK\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String sleepCLI() {
        try {
            this.sleepTag();
            return "Sleep = OK\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getMaskCLI() {
        try {
            String theMask = this.getMask();
            return "Mask = " + theMask + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setMaskCLI() {
        try {
            this.setMask(this.readerCommandValueString);
            return this.getMaskCLI();
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setG2MaskCLI() {
        try {
            this.setG2TagMask(this.readerCommandValueString);
            return this.getMaskCLI();
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getTagTypeCLI() {
        try {
            int tagType = this.getSupportedTagTypes();
            return "TagType = " + tagType + "\n";
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String setTagTypeCLI() {
        try {
            this.setSupportedTagTypes(this.readerCommandValueInt);
            return this.getTagTypeCLI();
        } catch (AlienReaderException var2) {
            return var2.getMessage() + "\n";
        }
    }

    private String getAutoModeCLI() {
        return this.autoModeState == 1 ? "AutoMode = ON\n" : "AutoMode = OFF\n";
    }

    private String setAutoModeCLI() {
        if (this.readerCommandValueString.toLowerCase().equals("on")) {
            this.setAutoMode(1);
        } else {
            this.setAutoMode(0);
        }

        return this.getAutoModeCLI();
    }

    private String resetAutoModeCLI() {
        this.autoModeReset();
        return "All auto-mode settings have been reset!\n";
    }

    private String getAcquireModeCLI() {
        return this.getAcquireMode() == "Inventory" ? "AcquireMode = Inventory\n" : "AcquireMode = Global Scroll\n";
    }

    private String setAcquireModeCLI() {
        this.setAcquireMode(this.readerCommandValueString);
        return this.getAcquireModeCLI();
    }

    private String getAcquireG2CyclesCLI() {
        return "AcqG2Cycles = " + this.getAcquireG2Cycles() + "\n";
    }

    private String setAcquireG2CyclesCLI() {
        this.setAcquireG2Cycles(this.readerCommandValueInt);
        return this.getAcquireG2CyclesCLI();
    }

    private String getAcquireG2CountCLI() {
        return "AcqG2Count = " + this.getAcquireG2Count() + "\n";
    }

    private String setAcquireG2CountCLI() {
        this.setAcquireG2Count(this.readerCommandValueInt);
        return this.getAcquireG2CountCLI();
    }

    private String getAcquireG2QCLI() {
        return "AcqG2Q = " + this.getAcquireG2Q() + "\n";
    }

    private String setAcquireG2QCLI() {
        this.setAcquireG2Q(this.readerCommandValueInt);
        return this.getAcquireG2QCLI();
    }

    private String getAcquireC1CyclesCLI() {
        return "AcqC1Cycles = " + this.getAcquireC1Cycles() + "\n";
    }

    private String setAcquireC1CyclesCLI() {
        this.setAcquireC1Cycles(this.readerCommandValueInt);
        return this.getAcquireC1CyclesCLI();
    }

    private String getAcquireC1CountCLI() {
        return "AcqC1Count = " + this.getAcquireC1Count() + "\n";
    }

    private String setAcquireC1CountCLI() {
        this.setAcquireC1Count(this.readerCommandValueInt);
        return this.getAcquireC1CountCLI();
    }

    private String getAcquireC1EnterWakeCountCLI() {
        return "AcqC1EnterWakeCount = " + this.getAcquireC1EnterWakeCount() + "\n";
    }

    private String setAcquireC1EnterWakeCountCLI() {
        this.setAcquireC1EnterWakeCount(this.readerCommandValueInt);
        return this.getAcquireC1EnterWakeCountCLI();
    }

    private String getAcquireC1ExitWakeCountCLI() {
        return "AcqC1ExitWakeCount = " + this.getAcquireC1ExitWakeCount() + "\n";
    }

    private String setAcquireC1ExitWakeCountCLI() {
        this.setAcquireC1ExitWakeCount(this.readerCommandValueInt);
        return this.getAcquireC1ExitWakeCountCLI();
    }

    private String getAcquireC1SleepCountCLI() {
        return "AcqC1SleepCount = " + this.getAcquireC1SleepCount() + "\n";
    }

    private String setAcquireC1SleepCountCLI() {
        this.setAcquireC1SleepCount(this.readerCommandValueInt);
        return this.getAcquireC1SleepCountCLI();
    }

    private String verifyTagCLI() {
        try {
            return "Verify Tag = " + this.verifyTag() + "\n";
        } catch (AlienReaderCommandErrorException var2) {
            return "Verify Tag = " + var2.getMessage();
        } catch (AlienReaderException var3) {
            return "Error: Other Error - " + this.rc.replyCommTypeMessage;
        }
    }

    private String programTagCLI() {
        try {
            this.programTag(this.readerCommandValueString);
            return "Program Tag = Success!\n";
        } catch (AlienReaderInvalidArgumentException var2) {
            return var2.getMessage() + "\n";
        } catch (AlienReaderCommandErrorException var3) {
            return "Error: " + this.rc.replyCommTypeMessage + "\n";
        } catch (AlienReaderException var4) {
            return "Error: Unknown Error - " + this.rc.replyCommTypeMessage + "\n";
        }
    }

    private String eraseTagCLI() {
        try {
            this.eraseTag();
            return "Erase Tag = Success\n";
        } catch (AlienReaderCommandErrorException var2) {
            return "Error: " + this.rc.replyCommTypeMessage + "\n";
        } catch (AlienReaderException var3) {
            return "Error: Unknown Error - " + this.rc.replyCommTypeMessage + "\n";
        }
    }

    private String killTagCLI() {
        try {
            this.killTag(this.readerCommandValueString);
            return "Kill Tag = Success\n";
        } catch (AlienReaderInvalidArgumentException var2) {
            return "Kill Tag = " + var2.getMessage() + "\n";
        } catch (AlienReaderCommandErrorException var3) {
            return "Error: " + this.rc.replyCommTypeMessage + "\n";
        } catch (AlienReaderException var4) {
            return "Error: Unknown Error - " + this.rc.replyCommTypeMessage + "\n";
        }
    }

    private String lockTagCLI() {
        try {
            this.lockTag(8, this.readerCommandValueHexArray[0]);
            return "Lock Tag = Success\n";
        } catch (AlienReaderCommandErrorException var2) {
            return "Error: " + this.rc.replyCommTypeMessage + "\n";
        } catch (AlienReaderException var3) {
            return "Error: Unknown Error - " + this.rc.replyCommTypeMessage + "\n";
        }
    }

    public String readerCommandCLI() {
        byte[] bytes = Converters.fromHexString(this.readerCommandValueString);

        try {
            this.rc.prepareGenericCommand(bytes);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderException var3) {
            return var3.getMessage();
        }

        String reply = "To Reader  :  " + Converters.toHexString(this.rc.commandBuffer, 0, this.rc.commandLength, " ") + "\n";
        reply = reply + "From Reader:  " + Converters.toHexString(this.rc.replyBufferRaw, 1, this.rc.replyLengthRaw - 3, " ");
        return reply + "\n";
    }

    public String rcCLI() {
        byte[] bytes = Converters.fromHexString(this.readerCommandValueString);

        try {
            this.rc.prepareGenericCommand(bytes);
            this.issueReaderCommand(this.rc);
        } catch (AlienReaderCommandErrorException var3) {
            return "RC = Error: " + this.rc.replyCommTypeMessage + " (" + this.rc.replyCommTypeHexString + ")\n";
        } catch (AlienReaderException var4) {
            return var4.getMessage();
        }

        return this.rc.replyLength > 6 ? "RC = " + Converters.toHexString(this.rc.replyBufferRaw, 5, this.rc.replyLengthRaw - 9, " ") + "\n" : "RC = Success!\n";
    }

    public String manageReservedCLI() {
        String reply = "Reserved = ";

        try {
            reply = reply + this.manageReserved(this.readerCommandValueString);
        } catch (AlienReaderException var3) {
            return var3.getMessage();
        }

        return reply + "\n";
    }

    private String getDebugCLI() {
        switch(this.getDebugLevel()) {
            case 0:
                return "Debug = OFF\n";
            case 1:
            default:
                return "Debug = TEXT\n";
            case 2:
                return "Debug = BYTES\n";
        }
    }

    private String setDebugCLI() {
        if (this.readerCommandValueString.equalsIgnoreCase("text")) {
            this.setDebugLevel(1);
        } else if (this.readerCommandValueString.equalsIgnoreCase("bytes")) {
            this.setDebugLevel(2);
        } else {
            this.setDebugLevel(0);
        }

        return this.getDebugCLI();
    }

    private String getHelpCLI() {
        String reply = "***********************************************\n";
        reply = reply + "*\n";
        reply = reply + "* Help\n";
        reply = reply + "*\n";
        reply = reply + "***********************************************\n";
        reply = reply + "GENERAL:\n";
        reply = reply + "   Help (H)\n";
        reply = reply + "   Info (I)\n";
        reply = reply + "   ! (Repeat Last Command)\n";
        reply = reply + "   Get ReaderName\n";
        reply = reply + "   Get ReaderType\n";
        reply = reply + "   Get ReaderVersion\n";
        reply = reply + "   Get MfgInfo\n";
        reply = reply + "   Get MaxAntenna\n";
        reply = reply + "   Get|Set AntennaSequence (i, j, k...)\n";
        reply = reply + "   Get AntennaStatus\n";
        reply = reply + "   Get OperatingVoltage\n";
        reply = reply + "   Get|Set RFPower\n";
        reply = reply + "   Get|Set RFAttenuation\n";
        reply = reply + "   Reboot\n";
        reply = reply + "TAGLIST:\n";
        reply = reply + "   Get|Set AcquireMode\n";
        reply = reply + "   Get|Set AcqG2Cycles\n";
        reply = reply + "   Get|Set AcqG2Count\n";
        reply = reply + "   Get|Set AcqG2Q\n";
        reply = reply + "   Get|Set AcqC1Cycles\n";
        reply = reply + "   Get|Set AcqC1Count\n";
        reply = reply + "   Get|Set AcqC1EnterWakeCount\n";
        reply = reply + "   Get|Set AcqC1ExitWakeCount\n";
        reply = reply + "   Get|Set AcqC1SleepCount\n";
        reply = reply + "   Get|Set PersistTime (secs)\n";
        reply = reply + "   Get TagList {n}\n";
        reply = reply + "   Clear TagList\n";
        reply = reply + "   Wake\n";
        reply = reply + "   Sleep\n";
        reply = reply + "   Get|Set Mask (All | bitLen, bitPtr, XX XX)\n";
        reply = reply + "   Get|Set TagType\n";
        reply = reply + "EXTERNAL I/O:\n";
        reply = reply + "   Get ExternalInput\n";
        reply = reply + "   Set ExternalOutput\n";
        reply = reply + "AUTONOMOUS MODE:\n";
        reply = reply + "   Get|Set Automode (On or Off)\n";
        reply = reply + "   AutoModeReset\n";
        reply = reply + "PROGRAMMING:\n";
        reply = reply + "   Program Tag = XX XX XX XX XX XX XX XX\n";
        reply = reply + "   Verify Tag\n";
        reply = reply + "   Erase Tag\n";
        reply = reply + "   Kill Tag = XX XX XX XX XX XX XX XX YY\n";
        reply = reply + "   Lock Tag = YY\n";
        reply = reply + "   Get|Set ProgAntenna\n";
        reply = reply + "MISC:\n";
        reply = reply + "   Get Timer\n";
        reply = reply + "   Set ReaderCommand = XX XX XX...\n";
        reply = reply + "   RC = XX XX XX...\n";
        reply = reply + "\n";
        reply = reply + "(XX = TagID byte)\n(YY = LockCode byte)\n";
        return reply;
    }

    private String getInfoCLI() {
        String reply = "***********************************************\n";
        reply = reply + "*\n";
        reply = reply + "* Current Settings\n";
        reply = reply + "*\n";
        reply = reply + "***********************************************\n";
        reply = reply + "GENERAL:\n";
        reply = reply + "   " + this.getReaderNameCLI();
        reply = reply + "   " + this.getReaderTypeCLI();
        reply = reply + "   " + this.getMaxAntennaCLI();
        reply = reply + "   " + this.getAntennaSequenceCLI();
        reply = reply + "   " + this.getAntennaStatusCLI();
        reply = reply + "   " + this.getOperatingVoltageCLI();
        reply = reply + "   " + this.getRFOnOffCLI();
        reply = reply + "   " + this.getRFAttenuationCLI();
        reply = reply + "TAG LIST:\n";
        reply = reply + "   " + this.getPersistTimeCLI();
        reply = reply + "   " + this.getAcquireModeCLI();
        reply = reply + "   " + this.getAcquireG2CyclesCLI();
        reply = reply + "   " + this.getAcquireG2CountCLI();
        reply = reply + "   " + this.getAcquireG2QCLI();
        reply = reply + "   " + this.getAcquireC1CyclesCLI();
        reply = reply + "   " + this.getAcquireC1CountCLI();
        reply = reply + "   " + this.getAcquireC1EnterWakeCountCLI();
        reply = reply + "   " + this.getAcquireC1ExitWakeCountCLI();
        reply = reply + "   " + this.getAcquireC1SleepCountCLI();

        try {
            reply = reply + "   Mask (All | bitLen, bitPtr, XX XX) = " + this.getMask() + "\n";
        } catch (AlienReaderException var3) {
            reply = reply + "  Mask (All | bitLen, bitPtr, XX XX) = Error: getting Mask\n";
        }

        reply = reply + "   " + this.getTagTypeCLI();
        reply = reply + "EXTERNAL I/O:\n";
        reply = reply + "   " + this.getExternalInputCLI();
        reply = reply + "AUTONOMOUS MODE:\n";
        reply = reply + "   " + this.getAutoModeCLI();
        reply = reply + "PROGRAMMING:\n";
        reply = reply + "   " + this.getProgAntennaCLI();
        return reply;
    }

    public void sendString(String command) throws AlienReaderConnectionException {
        String reply = "";
        if (command.trim().equals("!")) {
            command = this.readerPreviousCommand;
        } else {
            this.readerPreviousCommand = command;
        }

        this.processReaderCommand(command.trim());

        try {
            switch(this.readerCommandAction) {
                case 1:
                    if (!this.readerCommandKeyword.equals("h") && !this.readerCommandKeyword.equals("help")) {
                        if (!this.readerCommandKeyword.equals("i") && !this.readerCommandKeyword.startsWith("info")) {
                            if (this.readerCommandKeyword.equals("reboot")) {
                                reply = this.rebootCLI();
                            } else if (this.readerCommandKeyword.equals("")) {
                                reply = "";
                            } else if (this.readerCommandKeyword.equals("t")) {
                                reply = this.getTagListCLI();
                            } else if (this.readerCommandKeyword.startsWith("clear")) {
                                reply = this.clearTagListCLI();
                            } else if (this.readerCommandKeyword.equals("wake")) {
                                reply = this.wakeCLI();
                            } else if (this.readerCommandKeyword.equals("sleep")) {
                                reply = this.sleepCLI();
                            } else if (this.readerCommandKeyword.equals("automodereset")) {
                                reply = this.resetAutoModeCLI();
                            } else if (!this.readerCommandKeyword.equals("verify tag") && !this.readerCommandKeyword.equals("verify")) {
                                if (!this.readerCommandKeyword.equals("program tag") && !this.readerCommandKeyword.equals("program")) {
                                    if (!this.readerCommandKeyword.equals("erase tag") && !this.readerCommandKeyword.equals("erase")) {
                                        if (!this.readerCommandKeyword.equals("kill tag") && !this.readerCommandKeyword.equals("kill")) {
                                            if (!this.readerCommandKeyword.equals("lock tag") && !this.readerCommandKeyword.equals("lock")) {
                                                reply = "Command Not Understood\n";
                                            } else {
                                                reply = this.lockTagCLI();
                                            }
                                        } else {
                                            reply = this.killTagCLI();
                                        }
                                    } else {
                                        reply = this.eraseTagCLI();
                                    }
                                } else {
                                    reply = this.programTagCLI();
                                }
                            } else {
                                reply = this.verifyTagCLI();
                            }
                        } else {
                            reply = this.getInfoCLI();
                        }
                    } else {
                        reply = this.getHelpCLI();
                    }
                    break;
                case 2:
                    if (this.readerCommandKeyword.equals("readername")) {
                        reply = this.getReaderNameCLI();
                    } else if (this.readerCommandKeyword.equals("readertype")) {
                        reply = this.getReaderTypeCLI();
                    } else if (this.readerCommandKeyword.equals("readerversion")) {
                        reply = this.getReaderVersion() + "\n";
                    } else if (this.readerCommandKeyword.equals("mfginfo")) {
                        reply = this.getManufacturingInfoCLI();
                    } else if (this.readerCommandKeyword.equals("maxantenna")) {
                        reply = this.getMaxAntennaCLI();
                    } else if (this.readerCommandKeyword.equals("antennastatus")) {
                        reply = this.getAntennaStatusCLI();
                    } else if (this.readerCommandKeyword.equals("operatingvoltage")) {
                        reply = this.getOperatingVoltageCLI();
                    } else if (this.readerCommandKeyword.equals("timer")) {
                        reply = this.getTimer() + "\n";
                    } else if (!this.readerCommandKeyword.equals("ronnie") && !this.readerCommandKeyword.equals("ron")) {
                        if (this.readerCommandKeyword.equals("taglist")) {
                            reply = this.getTagListCLI();
                        } else if (this.readerCommandKeyword.equals("function")) {
                            reply = "Command Not Supported\n";
                        } else if (this.readerCommandKeyword.equals("antennasequence")) {
                            reply = this.getAntennaSequenceCLI();
                        } else if (!this.readerCommandKeyword.equals("antenna") && !this.readerCommandKeyword.equals("progantenna")) {
                            if (this.readerCommandKeyword.equals("rfpower")) {
                                reply = this.getRFOnOffCLI();
                            } else if (this.readerCommandKeyword.equals("rfattenuation")) {
                                reply = this.getRFAttenuationCLI();
                            } else if (this.readerCommandKeyword.equals("externalinput")) {
                                reply = this.getExternalInputCLI();
                            } else if (this.readerCommandKeyword.equals("debug")) {
                                reply = this.getDebugCLI();
                            } else if (this.readerCommandKeyword.equals("persisttime")) {
                                reply = this.getPersistTimeCLI();
                            } else if (this.readerCommandKeyword.equals("acquiremode")) {
                                reply = this.getAcquireModeCLI();
                            } else if (this.readerCommandKeyword.equals("mask")) {
                                reply = this.getMaskCLI();
                            } else if (this.readerCommandKeyword.equals("tagtype")) {
                                reply = this.getTagTypeCLI();
                            } else if (this.readerCommandKeyword.equals("acqg2cycles")) {
                                reply = this.getAcquireG2CyclesCLI();
                            } else if (this.readerCommandKeyword.equals("acqg2count")) {
                                reply = this.getAcquireG2CountCLI();
                            } else if (this.readerCommandKeyword.equals("acqg2q")) {
                                reply = this.getAcquireG2QCLI();
                            } else if (!this.readerCommandKeyword.equals("acqcycles") && !this.readerCommandKeyword.equals("acqc1cycles")) {
                                if (!this.readerCommandKeyword.equals("acqcount") && !this.readerCommandKeyword.equals("acqc1count")) {
                                    if (!this.readerCommandKeyword.equals("acqenterwakecount") && !this.readerCommandKeyword.equals("acqc1enterwakecount")) {
                                        if (!this.readerCommandKeyword.equals("acqexitwakecount") && !this.readerCommandKeyword.equals("acqc1exitwakecount")) {
                                            if (!this.readerCommandKeyword.equals("acqsleepcount") && !this.readerCommandKeyword.equals("acqc1sleepcount")) {
                                                if (this.readerCommandKeyword.equals("automode")) {
                                                    reply = this.getAutoModeCLI();
                                                } else {
                                                    reply = "Command Not Understood\n";
                                                }
                                            } else {
                                                reply = this.getAcquireC1SleepCountCLI();
                                            }
                                        } else {
                                            reply = this.getAcquireC1ExitWakeCountCLI();
                                        }
                                    } else {
                                        reply = this.getAcquireC1EnterWakeCountCLI();
                                    }
                                } else {
                                    reply = this.getAcquireC1CountCLI();
                                }
                            } else {
                                reply = this.getAcquireC1CyclesCLI();
                            }
                        } else {
                            reply = this.getProgAntennaCLI();
                        }
                    } else {
                        reply = "Ronnie = a Squid\n";
                    }
                    break;
                case 3:
                    if (this.readerCommandKeyword.equals("readercommand")) {
                        reply = this.readerCommandCLI();
                    } else if (this.readerCommandKeyword.equals("rc")) {
                        reply = this.rcCLI();
                    } else if (this.readerCommandKeyword.equals("reserved")) {
                        reply = this.manageReservedCLI();
                    } else if (this.readerCommandKeyword.equals("function")) {
                        reply = "Command Not Supported\n";
                    } else if (this.readerCommandKeyword.equals("antennasequence")) {
                        reply = this.setAntennaSequenceCLI();
                    } else if (!this.readerCommandKeyword.equals("antenna") && !this.readerCommandKeyword.equals("progantenna")) {
                        if (this.readerCommandKeyword.equals("rfpower")) {
                            reply = this.setRFOnOffCLI();
                        } else if (this.readerCommandKeyword.equals("rfattenuation")) {
                            reply = this.setRFAttenuationCLI();
                        } else if (this.readerCommandKeyword.equals("externalinput")) {
                            reply = this.setExternalOutputCLI();
                        } else if (this.readerCommandKeyword.equals("debug")) {
                            reply = this.setDebugCLI();
                        } else if (this.readerCommandKeyword.equals("persisttime")) {
                            reply = this.setPersistTimeCLI();
                        } else if (this.readerCommandKeyword.equals("acquiremode")) {
                            reply = this.setAcquireModeCLI();
                        } else if (this.readerCommandKeyword.equals("mask")) {
                            reply = this.setMaskCLI();
                        } else if (this.readerCommandKeyword.equals("acqg2mask")) {
                            reply = this.setG2MaskCLI();
                        } else if (this.readerCommandKeyword.equals("tagtype")) {
                            reply = this.setTagTypeCLI();
                        } else if (this.readerCommandKeyword.equals("acqg2cycles")) {
                            reply = this.setAcquireG2CyclesCLI();
                        } else if (this.readerCommandKeyword.equals("acqg2count")) {
                            reply = this.setAcquireG2CountCLI();
                        } else if (this.readerCommandKeyword.equals("acqg2q")) {
                            reply = this.setAcquireG2QCLI();
                        } else if (!this.readerCommandKeyword.equals("acqcycles") && !this.readerCommandKeyword.equals("acqc1cycles")) {
                            if (!this.readerCommandKeyword.equals("acqcount") && !this.readerCommandKeyword.equals("acqc1count")) {
                                if (!this.readerCommandKeyword.equals("acqenterwakecount") && !this.readerCommandKeyword.equals("acqc1enterwakecount")) {
                                    if (!this.readerCommandKeyword.equals("acqexitwakecount") && !this.readerCommandKeyword.equals("acqc1exitwakecount")) {
                                        if (!this.readerCommandKeyword.equals("acqsleepcount") && !this.readerCommandKeyword.equals("acqc1sleepcount")) {
                                            if (this.readerCommandKeyword.equals("automode")) {
                                                reply = this.setAutoModeCLI();
                                            } else {
                                                reply = "Command Not Understood\n";
                                            }
                                        } else {
                                            reply = this.setAcquireC1SleepCountCLI();
                                        }
                                    } else {
                                        reply = this.setAcquireC1ExitWakeCountCLI();
                                    }
                                } else {
                                    reply = this.setAcquireC1EnterWakeCountCLI();
                                }
                            } else {
                                reply = this.setAcquireC1CountCLI();
                            }
                        } else {
                            reply = this.setAcquireC1CyclesCLI();
                        }
                    } else {
                        reply = this.setProgAntennaCLI();
                    }
            }
        } catch (AlienReaderException var4) {
            reply = "Error: " + var4.getMessage();
        } catch (Exception var5) {
            reply = "Error: Unhandled Exception.\n" + var5.getMessage();
            var5.printStackTrace();
        }

        if (command.getBytes()[0] == 1) {
            this.internalReaderReply = "\r\n" + reply + "\r\n";
        } else {
            this.internalReaderReply = command + "\r\n" + reply + "\r\nAlien>\u0000";
        }

    }

    private void processReaderCommand(String command) {
        this.readerCommandAction = ACT_DO;
        this.readerCommandKeyword = "";
        this.readerCommandValueString = "";
        this.readerCommandValueInt = 0;
        this.readerCommandValueIntArray = null;
        this.readerCommandValueHexArray = null;
        this.readerCommandValueStringArray = null;
        command = command.toLowerCase().trim();
        if (command.startsWith("get ")) {
            this.readerCommandAction = ACT_GET;
            this.readerCommandKeyword = command.substring(4);
        } else if (command.endsWith("?")) {
            this.readerCommandAction = ACT_GET;
            this.readerCommandKeyword = command.substring(0, command.length() - 1);
        } else {
            if (command.startsWith("set ")) {
                command = command.substring(4);
            }

            int index = command.indexOf("=");
            if (index >= 0) {
                this.readerCommandAction = ACT_SET;
                this.readerCommandKeyword = command.substring(0, index).trim();
                this.readerCommandValueString = command.substring(index + 1).trim();
                if (this.readerCommandValueString.length() > 0) {
                    try {
                        this.readerCommandValueInt = new Integer(this.readerCommandValueString);
                    } catch (Exception var9) {
                    }

                    StringTokenizer tokenizer = new StringTokenizer(this.readerCommandValueString);
                    int tokenCount = tokenizer.countTokens();
                    this.readerCommandValueIntArray = new int[tokenCount];
                    this.readerCommandValueHexArray = new int[tokenCount];
                    this.readerCommandValueStringArray = new String[tokenCount];
                    tokenizer = new StringTokenizer(this.readerCommandValueString);

                    for(index = 0; tokenizer.hasMoreTokens(); ++index) {
                        String token = tokenizer.nextToken().trim();
                        this.readerCommandValueStringArray[index] = token;

                        try {
                            this.readerCommandValueHexArray[index] = Integer.valueOf(token, 16);
                        } catch (Exception var8) {
                        }

                        try {
                            this.readerCommandValueIntArray[index] = Integer.valueOf(token, 10);
                        } catch (Exception var7) {
                        }
                    }

                    return;
                }
            }

            this.readerCommandKeyword = command;
            this.readerCommandAction = ACT_DO;
        }
    }

    public String receiveString(boolean blockForInput) {
        String result = this.internalReaderReply;
        this.internalReaderReply = "";
        return result;
    }

    public void sendBytes(byte[] bytes) throws AlienReaderException {
        if (bytes != null && bytes.length != 0) {
            if (this.getDebugLevel() == 2) {
                System.out.println("\n  To Reader: " + Converters.toHexString(bytes, " "));
            }

            this.lastCommandByteCount = bytes.length;

            for(int i = 0; i < this.lastCommandByteCount; ++i) {
                this.lastCommandBytes[i] = bytes[i];
            }

            try {
                this.getOutputStream().write(bytes);
            } catch (IOException var3) {
                throw new AlienReaderConnectionException(var3.getMessage());
            }
        }
    }

    public byte[] receiveBytes() throws AlienReaderException {
        long startTime = System.currentTimeMillis();
        if (this.getDebugLevel() == 2) {
            System.out.print("From Reader: ");
        }

        byte[] bytes = new byte[20000];
        int byteCount = 0;
        int lastResponsePtr = 0;
        int status = 0;
        boolean done = false;

        do {
            try {
                while(this.getInputStream().available() > 0) {
                    int b = this.getInputStream().read();
                    if (b >= 0) {
                        startTime = System.currentTimeMillis();
                        switch(status) {
                            case 0:
                                if (b == 16) {
                                    status = 1;
                                }
                                break;
                            case 1:
                                if (b == 1) {
                                    status = 2;
                                    if (this.getDebugLevel() == 2) {
                                        System.out.print("10 01 ");
                                    }

                                    bytes[byteCount++] = 16;
                                    bytes[byteCount++] = 1;
                                } else {
                                    status = 0;
                                }
                                break;
                            case 2:
                                if (b == 16) {
                                    status = 3;
                                } else {
                                    if (this.getDebugLevel() == 2) {
                                        System.out.print(Converters.toHexString((long)b, 1) + " ");
                                    }

                                    bytes[byteCount++] = (byte)b;
                                }
                                break;
                            case 3:
                                if (b == 16) {
                                    if (this.getDebugLevel() == 2) {
                                        System.out.print("10 ");
                                    }

                                    bytes[byteCount++] = 16;
                                    status = 2;
                                } else if (b == 2) {
                                    if (this.getDebugLevel() == 2) {
                                        System.out.print("10 02");
                                    }

                                    bytes[byteCount++] = 16;
                                    bytes[byteCount++] = 2;
                                    if (bytes[lastResponsePtr + 4] == 64) {
                                        if (bytes[lastResponsePtr + 5] >= 3) {
                                            done = true;
                                        } else {
                                            lastResponsePtr = byteCount;
                                            status = 0;
                                            if (this.getDebugLevel() == 2) {
                                                System.out.print("\nFrom Reader: ");
                                            }
                                        }
                                    } else {
                                        done = true;
                                    }
                                } else {
                                    status = 0;
                                }
                        }
                    }
                }
            } catch (IOException var10) {
                throw new AlienReaderConnectionException(var10.getMessage());
            }

            long deltaTime = System.currentTimeMillis() - startTime;
            if (deltaTime > (long)this.getTimeOutMilliseconds()) {
                if (this.getDebugLevel() == 2) {
                    System.out.println(" ... TIMEOUT");
                }

                throw new AlienReaderTimeoutException("Error: Timeout Waiting for Input Data");
            }
        } while(!done);

        this.lastResponseByteCount = byteCount;
        byte[] responseBytes = new byte[byteCount];

        for(int i = 0; i < byteCount; ++i) {
            responseBytes[i] = bytes[i];
            this.lastResponseBytes[i] = bytes[i];
        }

        return responseBytes;
    }

    public String toString() {
        return "Alien OEM Reader Module";
    }
}
