package com.alien.enterpriseRFID.externalio;

import java.util.Date;

public class ExternalIO {
    public static final int DI = 1;
    public static final int DO = 2;
    private int type;
    private long eventTime;
    private long eventTimeHost;
    private int value;

    public ExternalIO() {
        this(0, 0, 0L);
    }

    public ExternalIO(int type) {
        this(type, 0, 0L);
    }

    public ExternalIO(int type, int value, long eventTime) {
        this.eventTime = 0L;
        this.value = 0;
        this.setEventTimeHost();
        this.setType(type);
        this.setValue(value);
        if (eventTime > 0L) {
            this.setEventTime(eventTime);
        } else {
            this.setEventTime(this.getEventTimeHost());
        }

    }

    public int getType() {
        return this.type;
    }

    public String getTypeString() {
        switch(this.type) {
            case 1:
                return "DI";
            case 2:
                return "DO";
            default:
                return "D?";
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(String typeString) {
        if (typeString.equalsIgnoreCase("DI")) {
            this.setType(1);
        } else if (typeString.equalsIgnoreCase("DO")) {
            this.setType(2);
        } else {
            this.setType(0);
        }

    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getEventTime() {
        return this.eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public long getEventTimeHost() {
        return this.eventTimeHost;
    }

    public void setEventTimeHost(long eventTimeHost) {
        this.eventTimeHost = eventTimeHost;
    }

    public void setEventTimeHost() {
        this.setEventTimeHost(System.currentTimeMillis());
    }

    public Object clone() {
        ExternalIO io = new ExternalIO(this.getType());
        io.setValue(this.getValue());
        io.setEventTime(this.getEventTime());
        io.setEventTimeHost(this.getEventTimeHost());
        return io;
    }

    public String toString() {
        return this.getTypeString() + ": " + this.value;
    }

    public String toLongString() {
        StringBuffer result = new StringBuffer();
        result.append(this.getTypeString());
        result.append(", Value=" + this.value);
        result.append(", Time=" + ExternalIOUtil.DATE_FORMATTER.format(new Date(this.getEventTime())));
        result.append(", HostTime=" + ExternalIOUtil.DATE_FORMATTER.format(new Date(this.getEventTimeHost())));
        return result.toString();
    }
}
