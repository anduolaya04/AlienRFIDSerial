package com.alien.enterpriseRFID.reader;

import com.alien.enterpriseRFID.tags.Tag;
import com.alien.enterpriseRFID.tags.TagTable;
import com.alien.enterpriseRFID.util.Converters;
import java.io.IOException;
import java.util.StringTokenizer;

public class AlienClass01Reader extends AlienClass1Reader implements Runnable{

    public TagTable tagTable;
    private int autoModeState;
    private int acquireMode;
    private int activeAntenna;
    private int progAntenna;
    private String antennaSequence;
    private int tagType;
    private int queryTimeout;
    private int timerTotalCommands;
    private int timerTotalTime;
    private Thread motor;
    private static final String ERROR_PREFIX = "Error: ";
    private String readerCommand;
    private String readerPreviousCommand;
    private String readerCommandValueString;
    private int readerCommandValueInt;
    private int[] readerCommandValueIntArray;
    private int[] readerCommandValueHexArray;
    private String[] readerCommandValueStringArray;
    private String internalReaderReply;
    public byte[] lastResponseBytes;
    public byte[] lastCommandBytes;
    public int lastResponseByteCount;
    public int lastCommandByteCount;

    public AlienClass01Reader(String address) {
        this.autoModeState = 0;
        this.acquireMode = 0;
        this.activeAntenna = 0;
        this.progAntenna = 0;
        this.antennaSequence = "0";
        this.tagType = 15;
        this.lastResponseBytes = new byte[4096];
        this.lastCommandBytes = new byte[4096];
        this.lastResponseByteCount = 0;
        this.lastCommandByteCount = 0;
        this.tagTable = new TagTable();
        this.setPersistTime(-1);
        if (address != "") {
            this.setConnection(address);
        }

        this.setValidateOpen(false);
    }

    public AlienClass01Reader() {
        this("");
    }

    public AlienClass01Reader(String ipAddress, int port) {
        this(ipAddress + ":" + port);
    }

    public void setDebugLevel(int level) {
        super.setDebugLevel(level);
    }

    public String issueReaderCommand(String commandString) throws AlienReaderConnectionException, AlienReaderTimeoutException, AlienReaderCommandErrorException {
        if (commandString != null && !commandString.equals("")) {
            commandString = this.terminateString(commandString);
            this.sendBytes(commandString.getBytes());
            String r = new String(this.receiveBytes());
            if (r.indexOf("Error") != -1) {
                throw new AlienReaderCommandErrorException("Reader responded with an error to the command:\n" + commandString + "\n" + r);
            } else {
                return r;
            }
        } else {
            return null;
        }
    }

    private String terminateString(String s) {
        if (s.endsWith(";\n")) {
            return s;
        } else if (s.endsWith(";")) {
            return s + "\n";
        } else {
            return s.endsWith("\n") ? s.trim() + ";\n" : s + ";\n";
        }
    }

    public int getTagType() {
        return this.tagType;
    }

    public void setTagType(int tagType) {
        if (tagType == 7) {
            tagType = 15;
        }

        this.tagType = tagType;
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

    public Tag[] getTagList(long readMillis) throws AlienReaderException {
        if (this.autoModeState == 0) {
            this.tagTable.clearTagList();
            this.timerTotalCommands = 1;
            long startTime = System.currentTimeMillis();

            while(System.currentTimeMillis() - startTime < readMillis) {
                this.acquireTags(1);
            }
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
        if (autoMode == 0) {
            this.autoModeState = 0;
            this.motor = null;

            try {
                this.issueReaderCommand("SET AUTO = OFF");
            } catch (AlienReaderException var6) {
                System.err.println("TM.setAutoMode(OFF): " + var6.getMessage());
            }
        } else {
            try {
                this.motor = new Thread(this);
                String cursor = this.constructAcquireQuery();

                try {
                    this.issueReaderCommand("CLOSE q1");
                } catch (AlienReaderException var4) {
                }

                this.issueReaderCommand("DECLARE q1 CURSOR FOR " + cursor);
                this.issueReaderCommand("SET AUTO q1 = ON");
                this.autoModeState = 1;
                this.motor.start();
            } catch (AlienReaderException var5) {
                this.autoModeState = 0;
            }
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

    public String getReaderName() {
        return "Alien Class 1/0 Reader";
    }

    public String getReaderType() {
        String frequencyString = "915 MHz";
        String modelString = "9774";
        String numAntennasString = "Four Antennas";
        return "Alien Class 1/0 RFID Reader, Model: ALR-" + modelString + " (" + numAntennasString + " / EPC Class 1/0 / " + frequencyString + ")";
    }

    public String getReaderVersion() throws AlienReaderException {
        try {
            String r = this.issueReaderCommand("SELECT version FROM firmware");
            return r;
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: getting ReaderVersion";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void reboot() throws AlienReaderException {
        try {
            this.issueReaderCommand("RESET");
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: rebooting the reader";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void setActiveAntenna(int antennaNumber) {
        this.activeAntenna = antennaNumber;
    }

    public int getActiveAntenna() throws AlienReaderException {
        return this.activeAntenna;
    }

    public void setProgAntenna(int antennaNumber) throws AlienReaderException {
        this.progAntenna = antennaNumber;
    }

    public int getProgAntenna() throws AlienReaderException {
        return this.progAntenna;
    }

    public void setAntennaSequence(String sequenceString) throws AlienReaderException {
        this.antennaSequence = sequenceString;
    }

    public String getAntennaSequence() throws AlienReaderException {
        return this.antennaSequence;
    }

    public String verifyTag() throws AlienReaderException {
        try {
            String tagID = this.getTagID();
            return tagID == null ? "No Tag Detected, or Tag Is Locked." : tagID;
        } catch (AlienReaderCommandErrorException var3) {
            String msg = "Error: verifying tag";
            msg = msg + "\n" + var3.getMessage();
            throw new AlienReaderCommandErrorException(msg);
        }
    }

    public void programTag(String tagID) throws AlienReaderException {
    }

    public void eraseTag() throws AlienReaderException {
    }

    public void killTag(String tagIDAndPassCode) throws AlienReaderException {
    }

    public void lockTag(String passCode) throws AlienReaderException {
    }

    @Override
    public void run() {
        int currentTimeout = this.getTimeOutMilliseconds();
        this.setTimeOutMilliseconds(currentTimeout + 1000);
        Thread thisThread = Thread.currentThread();

        while(this.motor == thisThread) {
            if (this.getDebugLevel() > 0) {
                System.out.println("\nTM.run(): Waiting for tag data.");
            }

            try {
                String tagListString = new String(this.receiveBytes());
                this.parseTagListString(tagListString);
                if (this.getDebugLevel() > 0) {
                    System.out.println("TM.run(): Received tag data.");
                }

                this.tagTable.removeOldTags();
            } catch (AlienReaderException var5) {
                if (this.autoModeState != 1) {
                    if (this.getDebugLevel() > 0) {
                        System.out.println("TM.run(): failing silently");
                    }

                    this.setTimeOutMilliseconds(currentTimeout);
                    return;
                }

                System.out.println("TM.run(): " + var5.getMessage());
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException var4) {
            }
        }

        this.setTimeOutMilliseconds(currentTimeout);
    }

    private void acquireTags(int multiplier) throws AlienReaderException {
        String query = this.constructAcquireQuery();

        for(int i = 0; i < multiplier; ++i) {
            String tagListString = this.issueReaderCommand(query);
            this.parseTagListString(tagListString);
        }

    }

    private String constructAcquireQuery() {
        StringTokenizer antennaTokenizer = new StringTokenizer(this.antennaSequence, ",");
        String antennaString = "";
        this.queryTimeout = 0;
        boolean firstAntenna = true;
        int antennaCount = 0;

        String protocolString;
        while(antennaTokenizer.hasMoreTokens()) {
            protocolString = antennaTokenizer.nextToken();
            ++antennaCount;

            int readAntenna;
            try {
                readAntenna = new Integer(protocolString) + 1;
            } catch (NumberFormatException var8) {
                readAntenna = 1;
            }

            if (firstAntenna) {
                antennaString = antennaString + "(antenna_id=" + readAntenna;
                firstAntenna = false;
            } else {
                antennaString = antennaString + " OR antenna_id=" + readAntenna;
            }
        }

        antennaString = antennaString + ")";
        protocolString = "(";
        boolean firstProtocol = true;
        if ((this.tagType & 8) > 0) {
            firstProtocol = false;
            protocolString = protocolString + "protocol_id='EPC0'";
            this.queryTimeout += 100 * antennaCount;
        }

        if ((this.tagType & 7) > 0) {
            if (!firstProtocol) {
                protocolString = protocolString + " OR ";
            }

            protocolString = protocolString + "protocol_id='CC915'";
            this.queryTimeout += 100 * antennaCount;
        }

        protocolString = protocolString + ")";
        if (this.queryTimeout < 250) {
            this.queryTimeout = 250;
        }

        String query = "SELECT id,antenna_id,read_count FROM tag_id WHERE (" + antennaString + " AND " + protocolString + ") " + "SET time_out=" + this.queryTimeout;
        return query;
    }

    public void parseTagListString(String tagListString) {
        StringTokenizer tagListTokenizer = new StringTokenizer(tagListString, "\n");

        while(true) {
            while(tagListTokenizer.hasMoreTokens()) {
                String tagString = tagListTokenizer.nextToken();
                String[] tagBits = tagString.split("\\|");
                if (tagBits != null && tagBits.length == 3) {
                    String tagID = tagBits[0];
                    tagID = this.formatTagID(tagID);
                    String antennaString = tagBits[1];
                    boolean var7 = false;

                    int antenna;
                    try {
                        antenna = new Integer(antennaString) - 1;
                    } catch (NumberFormatException var14) {
                        antenna = 0;
                    }

                    String countString = tagBits[2];
                    boolean var9 = false;

                    int count;
                    try {
                        count = new Integer(countString);
                    } catch (NumberFormatException var13) {
                        count = 1;
                    }

                    Tag tag = new Tag(tagID);
                    tag.setAntenna(antenna);
                    tag.setRenewCount(count);
                    long time = System.currentTimeMillis();
                    tag.setDiscoverTime(time);
                    tag.setHostDiscoverTime(time);
                    tag.setRenewTime(time);
                    tag.setHostRenewTime(time);
                    this.tagTable.addTag(tag);
                } else {
                    System.err.println("Error parsing taglist: " + tagString);
                    if (tagBits == null) {
                        System.out.println("tagBits = null");
                    } else {
                        for(int j = 0; j < tagBits.length; ++j) {
                            System.err.println("tagBits " + j + " = " + tagBits[j]);
                        }
                    }
                }
            }

            return;
        }
    }

    private String formatTagID(String id) {
        String r = "";
        int startPos = 6;

        for(int i = startPos; i <= id.length() - 4; i += 4) {
            r = r + id.substring(i, i + 4) + " ";
        }

        return r.trim();
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

    private String getTagTypeCLI() {
        return "TagType = " + this.tagType + "\n";
    }

    private String setTagTypeCLI() {
        this.setTagType(this.readerCommandValueInt);
        return this.getTagTypeCLI();
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

    private String verifyTagCLI() {
        try {
            return "Verify Tag = " + this.verifyTag() + "\n";
        } catch (AlienReaderException var2) {
            return "Verify Tag = " + var2.getMessage();
        }
    }

    private String programTagCLI() {
        try {
            this.programTag(this.readerCommandValueString);
            return "Program Tag = Success!\n";
        } catch (AlienReaderInvalidArgumentException var2) {
            return var2.getMessage() + "\n";
        } catch (AlienReaderException var3) {
            return "Error: Error Programming Tag - " + var3.getMessage() + "\n";
        }
    }

    private String eraseTagCLI() {
        try {
            this.eraseTag();
            return "Erase Tag = Success\n";
        } catch (AlienReaderException var2) {
            return "Error: Error Erasing Tag - " + var2.getMessage() + "\n";
        }
    }

    private String killTagCLI() {
        try {
            this.killTag(this.readerCommandValueString);
            return "Kill Tag = Success\n";
        } catch (AlienReaderException var2) {
            return "Error: Error Killing Tag - " + var2.getMessage() + "\n";
        }
    }

    private String lockTagCLI() {
        try {
            this.lockTag("" + this.readerCommandValueHexArray[0]);
            return "Lock Tag = Success\n";
        } catch (AlienReaderException var2) {
            return "Error: Error Locking Tag - " + var2.getMessage() + "\n";
        }
    }

    public String readerCommandCLI() {
        try {
            String r = this.issueReaderCommand(this.readerCommandValueString);
            String reply = "To Reader  :  " + this.readerCommandValueString + "\n";
            reply = reply + "From Reader:  " + r + "\n";
            return reply;
        } catch (AlienReaderException var3) {
            return var3.getMessage();
        }
    }

    private String getDebugCLI() {
        switch(this.getDebugLevel()) {
            case 0:
                return "Debug = Off\n";
            case 1:
                return "Debug = Text\n";
            case 2:
                return "Debug = Bytes\n";
            default:
                return "Debug = Unknown\n";
        }
    }

    private String setDebugCLI() {
        String level = this.readerCommandValueString.toLowerCase();
        if (level.equals("bytes")) {
            this.setDebugLevel(2);
        } else if (level.equals("text")) {
            this.setDebugLevel(1);
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
        reply = reply + "   Get|Set AntennaSequence (i, j, k...)\n";
        reply = reply + "   Get|Set TagType\n";
        reply = reply + "   Reboot\n";
        reply = reply + "TAGLIST:\n";
        reply = reply + "   Get|Set AcquireMode\n";
        reply = reply + "   Get|Set PersistTime (secs)\n";
        reply = reply + "   Get TagList {n}\n";
        reply = reply + "   Clear TagList\n";
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
        reply = reply + "   RC = [RQL command]\n";
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
        reply = reply + "   " + this.getAntennaSequenceCLI();
        reply = reply + "   " + this.getTagTypeCLI();
        reply = reply + "TAG LIST:\n";
        reply = reply + "   " + this.getPersistTimeCLI();
        reply = reply + "   " + this.getAcquireModeCLI();
        reply = reply + "AUTONOMOUS MODE:\n";
        reply = reply + "   " + this.getAutoModeCLI();
        reply = reply + "PROGRAMMING:\n";
        reply = reply + "   " + this.getProgAntennaCLI();
        return reply;
    }

    public void sendString(String command) throws AlienReaderConnectionException {
        if (command.trim().equals("!")) {
            command = this.readerPreviousCommand;
        } else {
            this.readerPreviousCommand = command;
        }

        this.processReaderCommand(command.trim());
        String lowerCommand = command.toLowerCase().trim();
        String reply = "";

        try {
            if (lowerCommand.equals("get readername")) {
                reply = this.getReaderNameCLI();
            } else if (lowerCommand.equals("get readertype")) {
                reply = this.getReaderTypeCLI();
            } else if (lowerCommand.equals("get readerversion")) {
                reply = this.getReaderVersion() + "\n";
            } else if (!lowerCommand.equals("h") && !lowerCommand.equals("help")) {
                if (!lowerCommand.equals("i") && !lowerCommand.startsWith("info")) {
                    if (lowerCommand.equals("get antennasequence")) {
                        reply = this.getAntennaSequenceCLI();
                    } else if (lowerCommand.startsWith("set antennasequence")) {
                        reply = this.setAntennaSequenceCLI();
                    } else if (lowerCommand.equals("get tagtype")) {
                        reply = this.getTagTypeCLI();
                    } else if (lowerCommand.startsWith("set tagtype")) {
                        reply = this.setTagTypeCLI();
                    } else if (lowerCommand.equals("reboot")) {
                        reply = this.rebootCLI();
                    } else if (lowerCommand.equals("get timer")) {
                        reply = this.getTimer() + "\n";
                    } else if (lowerCommand.equals("")) {
                        reply = "";
                    } else if (!lowerCommand.startsWith("get taglist") && !lowerCommand.startsWith("t")) {
                        if (lowerCommand.equals("get persisttime")) {
                            reply = this.getPersistTimeCLI();
                        } else if (lowerCommand.startsWith("set persisttime")) {
                            reply = this.setPersistTimeCLI();
                        } else if (lowerCommand.equals("clear taglist")) {
                            reply = this.clearTagListCLI();
                        } else if (lowerCommand.equals("get acquiremode")) {
                            reply = this.getAcquireModeCLI();
                        } else if (lowerCommand.startsWith("set acquiremode")) {
                            reply = this.setAcquireModeCLI();
                        } else if (lowerCommand.equals("get automode")) {
                            reply = this.getAutoModeCLI();
                        } else if (lowerCommand.startsWith("set automode")) {
                            reply = this.setAutoModeCLI();
                        } else if (lowerCommand.equals("automodereset")) {
                            reply = this.resetAutoModeCLI();
                        } else if (lowerCommand.startsWith("set function")) {
                            reply = "Command Not Supported\n";
                        } else if (lowerCommand.equals("verify tag")) {
                            reply = this.verifyTagCLI();
                        } else if (lowerCommand.startsWith("program tag")) {
                            reply = this.programTagCLI();
                        } else if (lowerCommand.equals("erase tag")) {
                            reply = this.eraseTagCLI();
                        } else if (lowerCommand.startsWith("kill tag")) {
                            reply = this.killTagCLI();
                        } else if (lowerCommand.startsWith("lock tag")) {
                            reply = this.lockTagCLI();
                        } else if (lowerCommand.startsWith("set debug")) {
                            reply = this.setDebugCLI();
                        } else {
                            reply = "Command Not Understood\n";
                        }
                    } else {
                        reply = this.getTagListCLI();
                    }
                } else {
                    reply = this.getInfoCLI();
                }
            } else {
                reply = this.getHelpCLI();
            }
        } catch (AlienReaderException var5) {
            reply = "Error: " + var5.getMessage();
        } catch (Exception var6) {
            reply = "Error: Unhandled Exception.\n" + var6.getMessage();
            var6.printStackTrace();
        }

        if (command.getBytes()[0] == 1) {
            this.internalReaderReply = reply + "\n";
        } else {
            this.internalReaderReply = command + reply + "\nAlien>";
        }

    }

    private void processReaderCommand(String command) {
        this.readerCommand = "";
        this.readerCommandValueString = "";
        this.readerCommandValueInt = 0;
        this.readerCommandValueIntArray = null;
        this.readerCommandValueHexArray = null;
        this.readerCommandValueStringArray = null;
        this.readerCommand = command.trim();
        int index = this.readerCommand.indexOf("=");
        if (index <= 0) {
            index = this.readerCommand.lastIndexOf(" ");
        }

        if (index >= 0) {
            this.readerCommandValueString = this.readerCommand.substring(index + 1).trim();
        }

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

        }
    }

    public String receiveString(boolean blockForInput) {
        String result = this.internalReaderReply;
        this.internalReaderReply = "";
        return result;
    }

    public void sendBytes(byte[] bytes) throws AlienReaderConnectionException {
        if (bytes != null && bytes.length != 0) {
            if (this.getDebugLevel() == 2) {
                System.out.println("  To Reader: " + Converters.toHexString(bytes, " "));
            } else if (this.getDebugLevel() == 1) {
                System.out.println("  To Reader: " + (new String(bytes)).trim());
            }

            this.lastCommandByteCount = bytes.length;

            for(int i = 0; i < this.lastCommandByteCount; ++i) {
                this.lastCommandBytes[i] = bytes[i];
            }

            try {
                this.getOutputStream().write(bytes);
                this.getOutputStream().flush();
            } catch (IOException var3) {
                throw new AlienReaderConnectionException(var3.getMessage());
            }
        }
    }

    public byte[] receiveBytes() throws AlienReaderConnectionException, AlienReaderTimeoutException {
        long startTime = System.currentTimeMillis();
        boolean done = false;
        byte[] inputBuffer = new byte[4096];
        int readLength = 0;
        int bufferPos = 0;
        if (this.getDebugLevel() != 0) {
            System.out.print("From Reader: ");
        }

        do {
            try {
                if (this.getInputStream() != null && this.getInputStream().available() > 0) {
                    readLength = this.getInputStream().read(inputBuffer, bufferPos, inputBuffer.length - bufferPos);
                    if (readLength > 0) {
                        if (this.getDebugLevel() == 2) {
                            System.out.print(Converters.toHexString(inputBuffer, bufferPos, readLength, " "));
                        } else if (this.getDebugLevel() == 1) {
                            System.out.print(Converters.toAsciiString(inputBuffer, bufferPos, readLength));
                        }

                        bufferPos += readLength;
                        if (inputBuffer[0] == 10) {
                            done = true;
                        } else {
                            for(int i = 0; i < bufferPos - 1; ++i) {
                                if (i < inputBuffer.length && inputBuffer[i] == 10 && inputBuffer[i + 1] == 10) {
                                    done = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IOException var9) {
                System.out.println("TM: Unable to read data. " + var9.getMessage());
                var9.printStackTrace(System.out);
            }

            if (System.currentTimeMillis() - startTime > (long)this.getTimeOutMilliseconds()) {
                if (this.getDebugLevel() == 2) {
                    System.out.println(" ... TIMEOUT\n");
                }

                throw new AlienReaderTimeoutException("Error: Timeout Waiting for Input Data");
            }
        } while(!done);

        if (this.getDebugLevel() != 0) {
            System.out.println(" ... DONE\n");
        }

        if (bufferPos < 0) {
            return null;
        } else {
            byte[] result = new byte[bufferPos];

            for(int i = 0; i < bufferPos; ++i) {
                result[i] = inputBuffer[i];
            }

            return result;
        }
    }

    public String toString() {
        return "Alien Class 1/0 RFID Reader";
    }
}
