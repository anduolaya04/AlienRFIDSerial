package com.alien.enterpriseRFID.tags;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tag implements Comparable {

    public static final int FOREVER = -1;
    public static final int PROTO_UNKNOWN = -1;
    public static final int PROTO_C0 = 0;
    public static final int PROTO_C1G1 = 1;
    public static final int PROTO_C1G2 = 2;
    public static final String[] PROTOCOL_STRINGS = new String[]{"Class 0", "Class 1 Gen 1", "Class 1 Gen 2"};
    public static final int DIR_TOWARD = -1;
    public static final int DIR_AWAY = 1;
    public static final int DIR_STATIC = 0;
    private String tagID;
    private long crc;
    private long persistTime;
    private long discoverTime;
    private long hostDiscoverTime;
    private long renewTime;
    private long hostRenewTime;
    private int renewCount;
    private int transmitAntenna;
    private int receiveAntenna;
    private int protocol;
    private String tagAuth;
    private String pcWord;
    private String xpc;
    protected long lastRenewTime;
    protected double speed;
    protected double speedLast;
    protected double speedSmooth;
    protected double posSmooth;
    protected double posLast;
    protected double posMin;
    private String[] g2Data = new String[]{"", "", "", ""};
    private double rssi;
    private int direction;
    private String[] g2Ops = new String[]{"", "", "", "", "", "", "", "", ""};
    private static double speedSmoothingCoeff = 0.05D;
    private static double zeroSpeedThresholdLow = -0.2D;
    private static double zeroSpeedThresholdHigh = 0.2D;
    protected int speedHistoryCapacity = 10;
    protected List speedHistory;
    protected int speedHistoryLength;

    public Tag(String tagID) {
        this.setPersistTime(0L);
        this.setTagID(tagID);
        this.setCRC(0L);
        this.setTransmitAntenna(0);
        this.setReceiveAntenna(0);
        this.setRenewCount(1);
        this.setProtocol(1);
        this.setTagAuth((String)null);
        this.setPCWord((String)null);
        this.setXPC((String)null);
        this.speed = 0.0D;
        this.speedLast = 0.0D;
        this.speedSmooth = 0.0D;
        this.posSmooth = 0.0D;
        this.posMin = 0.0D;
        this.posLast = 0.0D;
        this.speedHistory = new ArrayList(this.speedHistoryCapacity);
        this.speedHistoryLength = 0;
    }

    public String getTagID() {
        return this.tagID;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public long getCRC() {
        return this.crc;
    }

    public void setCRC(long crc) {
        this.crc = crc;
    }

    public int getAntenna() {
        return this.transmitAntenna;
    }

    public void setAntenna(int antenna) {
        this.transmitAntenna = antenna;
        this.receiveAntenna = antenna;
    }

    public int getTransmitAntenna() {
        return this.transmitAntenna;
    }

    public void setTransmitAntenna(int antenna) {
        this.transmitAntenna = antenna;
    }

    public int getReceiveAntenna() {
        return this.receiveAntenna;
    }

    public void setReceiveAntenna(int antenna) {
        this.receiveAntenna = antenna;
    }

    public long getPersistTime() {
        return this.persistTime;
    }

    public void setPersistTime(long persistTime) {
        this.persistTime = persistTime;
    }

    public long getDiscoverTime() {
        return this.discoverTime;
    }

    public void setDiscoverTime(long discoverTime) {
        this.discoverTime = discoverTime;
        this.setRenewTime(discoverTime);
    }

    public long getHostDiscoverTime() {
        return this.hostDiscoverTime;
    }

    public void setHostDiscoverTime(long hostDiscoverTime) {
        this.hostDiscoverTime = hostDiscoverTime;
        this.setHostRenewTime(hostDiscoverTime);
    }

    public long getRenewTime() {
        return this.renewTime;
    }

    public void setRenewTime(long renewTime) {
        this.lastRenewTime = this.renewTime;
        this.renewTime = renewTime;
    }

    public long getHostRenewTime() {
        return this.hostRenewTime;
    }

    public void setHostRenewTime(long hostRenewTime) {
        this.hostRenewTime = hostRenewTime;
    }

    public int getRenewCount() {
        return this.renewCount;
    }

    public void setRenewCount(int renewCount) {
        this.renewCount = renewCount;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setProtocol(String protoStr) {
        protoStr = protoStr.toLowerCase().replaceAll(" ", "").replaceAll(",", "");
        if (!protoStr.equals("class0") && !protoStr.equals("c0")) {
            if (!protoStr.equals("class1gen1") && !protoStr.equals("c1g1") && !protoStr.equals("c1")) {
                if (!protoStr.equals("class1gen2") && !protoStr.equals("c1g2")) {
                    this.setProtocol(2);
                } else {
                    this.setProtocol(2);
                }
            } else {
                this.setProtocol(1);
            }
        } else {
            this.setProtocol(0);
        }

    }

    public String getProtocolString() {
        return getProtocolString(this.protocol);
    }

    public static String getProtocolString(int protocol) {
        return protocol >= 0 && protocol < PROTOCOL_STRINGS.length ? PROTOCOL_STRINGS[protocol] : "Unknown";
    }

    public long getTimeToLive() {
        if (this.getPersistTime() == -1L) {
            return -1L;
        } else {
            long deltaTime = System.currentTimeMillis() - this.getHostRenewTime();
            long result = this.getPersistTime() - deltaTime;
            if (result < 0L) {
                result = 0L;
            }

            if (result > this.getPersistTime()) {
                result = this.getPersistTime();
            }

            return result;
        }
    }

    public void setSpeed(double speed) {
        long dt = this.renewTime - this.lastRenewTime;
        double smoothingFactor = Math.exp(-speedSmoothingCoeff * (double)dt);
        if (speed > zeroSpeedThresholdLow && speed < zeroSpeedThresholdHigh) {
            speed = 0.0D;
        }

        this.speed = speed;
        this.speedSmooth = this.speedSmooth * smoothingFactor + this.speed * (1.0D - smoothingFactor);
        this.posLast = this.posSmooth;
        this.posSmooth += this.speedLast * (double)dt / 1000.0D;
        this.speedLast = this.speedSmooth;
        if (speed > 0.0D) {
            this.setDirection(1);
        } else if (speed < 0.0D) {
            this.setDirection(-1);
        } else {
            this.setDirection(0);
        }

        if (this.posSmooth < this.posMin) {
            this.posMin = this.posSmooth;
        }

    }

    public double getSpeed() {
        return this.speed;
    }

    public double getSmoothSpeed() {
        return this.speedSmooth;
    }

    public double getSmoothPosition() {
        return this.posSmooth;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return this.direction;
    }

    public void setG2Data(int index, String data) {
        if (index >= 0 || index < this.g2Data.length) {
            this.g2Data[index] = data.trim();
        }

    }

    public String getG2Data(int index) {
        return index < 0 && index >= this.g2Data.length ? null : this.g2Data[index];
    }

    public String[] getG2Data() {
        return this.g2Data;
    }

    public void setG2Ops(int index, String data) {
        if (index >= 0 && index < this.g2Ops.length) {
            this.g2Ops[index] = data.trim();
        }

    }

    public String getG2Ops(int index) {
        return index < 0 && index >= this.g2Ops.length ? null : this.g2Ops[index];
    }

    public String[] getG2Ops() {
        return this.g2Ops;
    }

    public void setRSSI(double rssi) {
        this.rssi = rssi;
    }

    public double getRSSI() {
        return this.rssi;
    }

    public static double getSpeedSmoothingCoefficient() {
        return speedSmoothingCoeff;
    }

    public static void setSpeedSmoothingCoefficient(double speedSmoothingCoeff) {
        speedSmoothingCoeff = speedSmoothingCoeff;
    }

    public static double getZeroSpeedThresholdLow() {
        return zeroSpeedThresholdLow;
    }

    public static void setZeroSpeedThresholdLow(double zeroSpeedThresholdLow) {
        zeroSpeedThresholdLow = zeroSpeedThresholdLow;
    }

    public static double getZeroSpeedThresholdHigh() {
        return zeroSpeedThresholdHigh;
    }

    public static void setZeroSpeedThresholdHigh(double zeroSpeedThresholdHigh) {
        zeroSpeedThresholdHigh = zeroSpeedThresholdHigh;
    }

    public void setTagAuth(String tagAuth) {
        this.tagAuth = tagAuth;
    }

    public String getTagAuth() {
        return this.tagAuth;
    }

    public void setPCWord(String pcWord) {
        this.pcWord = pcWord;
    }

    public String getPCWord() {
        return this.pcWord;
    }

    public void setXPC(String xpc) {
        this.xpc = xpc;
    }

    public String getXPC() {
        return this.xpc;
    }

    public void updateTag(Tag newTag) {
        this.setRenewTime(newTag.getRenewTime());
        this.setHostRenewTime(System.currentTimeMillis());
        this.setPersistTime(this.persistTime);
        this.setRenewCount(this.getRenewCount() + newTag.getRenewCount());
        this.setTransmitAntenna(newTag.getTransmitAntenna());
        this.setReceiveAntenna(newTag.getReceiveAntenna());
        this.setSpeed(newTag.getSpeed());
        String[] g2Data = newTag.getG2Data();

        for(int i = 0; i < g2Data.length; ++i) {
            String data = g2Data[i];
            if (data != null && !data.equals("")) {
                this.setG2Data(i, data);
            }
        }

        this.setRSSI(newTag.getRSSI());
        this.setDirection(newTag.getDirection());
        this.setTagAuth(newTag.getTagAuth());
        this.setPCWord(newTag.getPCWord());
        this.setXPC(newTag.getXPC());
    }

    public Object clone() {
        String tagID = this.getTagID();
        if (tagID == null) {
            tagID = "";
        }

        Tag tag = new Tag(tagID);
        tag.setCRC(this.getCRC());
        tag.setTransmitAntenna(this.getTransmitAntenna());
        tag.setReceiveAntenna(this.getReceiveAntenna());
        tag.setPersistTime(this.getPersistTime());
        tag.setDiscoverTime(this.getDiscoverTime());
        tag.setHostDiscoverTime(this.getHostDiscoverTime());
        tag.setRenewTime(this.getRenewTime());
        tag.setHostRenewTime(this.getHostRenewTime());
        tag.setRenewCount(this.getRenewCount());
        tag.setProtocol(tag.getProtocol());
        tag.g2Data = this.g2Data;
        tag.speed = this.speed;
        tag.speedLast = this.speedLast;
        tag.speedSmooth = this.speedSmooth;
        tag.posSmooth = this.posSmooth;
        tag.rssi = this.rssi;
        tag.direction = this.direction;
        tag.setTagAuth(this.getTagAuth());
        tag.setPCWord(this.getPCWord());
        tag.setXPC(this.getXPC());
        return tag;
    }

    public String toString() {
        return this.tagID == null ? "Unknown Tag ID" : this.getTagID();
    }

    public String toLongString() {
        if (this.tagID == null) {
            return "Unknown Tag ID";
        } else {
            StringBuffer result = new StringBuffer();
            result.append("Tag=" + this.getTagID());
            result.append("  Disc=" + (new Date(this.getDiscoverTime())).toString());
            result.append("  Last=" + (new Date(this.getRenewTime())).toString());
            result.append("  Count=" + this.getRenewCount());
            result.append("  Ant=" + this.getAntenna());
            result.append("  Proto=" + this.getProtocol());
            result.append("  v=" + this.getSpeed());
            result.append("  RSSI=" + this.getRSSI());
            result.append("  Dir=" + this.getDirection());
            result.append("  Auth=" + this.getTagAuth());
            result.append("  PC=" + this.getPCWord());
            result.append("  XPC=" + this.getXPC());
            return result.toString();
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Tag) {
            Tag objectTag = (Tag)o;
            return this.getTagID().compareTo(objectTag.getTagID());
        } else {
            return 0;
        }
    }
}
