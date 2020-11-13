package com.alien.enterpriseRFID.notify;

import com.alien.enterpriseRFID.externalio.ExternalIO;
import com.alien.enterpriseRFID.tags.Tag;

import java.util.Date;

public class Message {
    private String readerName;
    private String readerType;
    private String readerIPAddress;
    private int readerCommandPort;
    private String readerMACAddress;
    private String readerHostname;
    private String reason;
    private Date date;
    private Tag[] tagList;
    private ExternalIO[] ioList;
    private String rawData;
    private int startTriggerLines;
    private int stopTriggerLines;
    public static final String REASON_TAGSTREAM = "TAG STREAM";
    public static final String REASON_IOSTREAM = "IO STREAM";

    public Message() {
    }

    public String getRawData() {
        return this.rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    /** @deprecated */
    public String getXML() {
        return this.rawData;
    }

    /** @deprecated */
    public void setXML(String rawXML) {
        this.rawData = rawXML;
    }

    public String getReaderName() {
        return this.readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public String getReaderType() {
        return this.readerType;
    }

    public void setReaderType(String readerType) {
        this.readerType = readerType;
    }

    public String getReaderIPAddress() {
        return this.readerIPAddress;
    }

    public void setReaderIPAddress(String readerIPAddress) {
        this.readerIPAddress = readerIPAddress;
    }

    public int getReaderCommandPort() {
        return this.readerCommandPort;
    }

    public void setReaderCommandPort(int readerCommandPort) {
        this.readerCommandPort = readerCommandPort;
    }

    public String getReaderMACAddress() {
        return this.readerMACAddress;
    }

    public void setReaderMACAddress(String readerMACAddress) {
        this.readerMACAddress = readerMACAddress;
    }

    public String getReaderHostname() {
        return this.readerHostname;
    }

    public void setReaderHostname(String readerHostname) {
        this.readerHostname = readerHostname;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStartTriggerLines() {
        return this.startTriggerLines;
    }

    public void setStartTriggerLines(int startTriggerLines) {
        this.startTriggerLines = startTriggerLines;
    }

    public int getStopTriggerLines() {
        return this.stopTriggerLines;
    }

    public void setStopTriggerLines(int stopTriggerLines) {
        this.stopTriggerLines = stopTriggerLines;
    }

    public void setTagList(Tag[] tagList) {
        this.tagList = tagList;
    }

    public Tag[] getTagList() {
        return this.tagList;
    }

    public int getTagCount() {
        return this.tagList == null ? 0 : this.tagList.length;
    }

    public Tag getTag(int index) {
        try {
            return this.tagList[index];
        } catch (Exception var3) {
            return null;
        }
    }

    public void setIOList(ExternalIO[] ioList) {
        this.ioList = ioList;
    }

    public ExternalIO[] getIOList() {
        return this.ioList;
    }

    public int getIOCount() {
        return this.ioList == null ? 0 : this.ioList.length;
    }

    public ExternalIO getIO(int index) {
        try {
            return this.ioList[index];
        } catch (Exception var3) {
            return null;
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Reader Notification Message:\n");
        result.append("Reader Name = " + this.getReaderName() + "\n");
        result.append("Reader Type = " + this.getReaderType() + "\n");
        result.append("Reader IP Address = " + this.getReaderIPAddress() + "\n");
        result.append("Reader Command Port = " + this.getReaderCommandPort() + "\n");
        if (this.getReaderMACAddress() != null) {
            result.append("Reader MAC Address = " + this.getReaderMACAddress() + "\n");
        }

        if (this.getReaderHostname() != null) {
            result.append("Reader Hostname = " + this.getReaderHostname() + "\n");
        }

        result.append("Date & Time = " + this.getDate() + "\n");
        result.append("Reason = " + this.getReason() + "\n");
        result.append("Start Trigger Lines = " + this.getStartTriggerLines() + "\n");
        result.append("Stop Trigger Lines = " + this.getStopTriggerLines() + "\n");
        result.append("Tags Found = " + this.getTagCount() + "\n");
        result.append("IOs  Found = " + this.getIOCount() + "\n");
        return result.toString();
    }
}
