package com.alien.enterpriseRFID.reader;

import com.alien.enterpriseRFID.tags.Tag;
import com.alien.enterpriseRFID.tags.TagUtil;
import com.alien.enterpriseRFID.util.Converters;

import java.util.StringTokenizer;

public class AlienClassBPTReader extends AlienClass1Reader {
    public static final short NO_READING = 9999;
    public static final short NO_WRITING = 9999;
    public static final int DEFAULT_TIMEOUT = 20000;

    public AlienClassBPTReader() {
        this.setTimeOutMilliseconds(20000);
    }

    public Tag getTagID(String tagID) throws Exception {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        this.doReaderCommand("get tagID");
        Tag[] tagList = (Tag[])null;
        if (this.getReaderReply().startsWith("<")) {
            tagList = TagUtil.decodeXMLTagList(this.getReaderReply());
        } else {
            tagList = TagUtil.decodeTagList(this.getReaderReply());
        }

        if (tagList == null) {
            return null;
        } else {
            return tagList.length == 0 ? null : tagList[0];
        }
    }

    public String getTagInfo(String tagID) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        return this.doReaderCommand("Get TagInfo");
    }

    public int getSensorValue(String tagID) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        this.doReaderCommand("get sensorvalue");
        if (this.getReaderReply().toLowerCase().indexOf("sensorvalue") < 0) {
            throw new AlienReaderCommandErrorException(this.getReaderReplyValueString());
        } else if (this.getReaderReplyValueString().indexOf("No Tag") >= 0) {
            throw new AlienReaderNoTagException(this.getReaderReplyValueString());
        } else {
            return this.getReaderReplyValueInt();
        }
    }

    public boolean isLogging(String tagID) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        this.doReaderCommand("get loggingmode");
        if (this.getReaderReplyValueString().indexOf("No Tag") >= 0) {
            throw new AlienReaderNoTagException(this.getReaderReplyValueString());
        } else {
            return this.getReaderReplyValueString().toLowerCase().indexOf("on") >= 0;
        }
    }

    public void setLogging(String tagID, boolean isLogging) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        String value = "Off";
        if (isLogging) {
            value = "On";
        }

        this.doReaderCommand("set loggingmode = " + value);
        if (this.getReaderReplyValueString().indexOf("No Tag") >= 0) {
            throw new AlienReaderNoTagException(this.getReaderReplyValueString());
        }
    }

    public byte[] getLoggingInterval(String tagID) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        this.doReaderCommand("get loggingInterval");
        if (this.getReaderReplyValueString().indexOf("No Tag") >= 0) {
            throw new AlienReaderNoTagException(this.getReaderReplyValueString());
        } else {
            byte[] result = new byte[3];
            StringTokenizer tokenizer = new StringTokenizer(this.getReaderReplyValueString(), ":");

            try {
                result[0] = Integer.valueOf(tokenizer.nextToken()).byteValue();
                result[1] = Integer.valueOf(tokenizer.nextToken()).byteValue();
                result[2] = Integer.valueOf(tokenizer.nextToken()).byteValue();
                return result;
            } catch (Exception var5) {
                throw new AlienReaderCommandErrorException("Data Format Error : " + this.getReaderReplyValueString());
            }
        }
    }

    public void setLoggingInterval(String tagID, byte[] byteArray) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        this.doReaderCommand("set loggingInterval = " + byteArray[0] + ":" + byteArray[1] + ":" + byteArray[2]);
        if (this.getReaderReplyValueString().indexOf("No Tag") >= 0) {
            throw new AlienReaderNoTagException(this.getReaderReplyValueString());
        }
    }

    public void setLoggingInterval(String tagID, int hours, int mins, int secs) throws AlienReaderException {
        byte[] byteArray = new byte[]{(byte)(hours & 255), (byte)(mins & 255), (byte)(secs & 255)};
        this.setLoggingInterval(tagID, byteArray);
    }

    public short[] getMemory(String tagID, int lengthIndex, int startIndex) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        this.doReaderCommand("get memory = " + lengthIndex + ", " + startIndex);
        int resultIndex = 0;
        short[] resultArray = new short[lengthIndex];

        for(int i = 0; i < resultArray.length; ++i) {
            resultArray[i] = 9999;
        }

        StringTokenizer tokenizer = new StringTokenizer(this.getReaderReplyValueString(), "\n\r");

        while(tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            int index = line.indexOf("=");
            if (index > 0) {
                line = line.substring(index + 1).trim();
            }

            index = 0;

            while(index < line.length() - 1) {
                short result = 0;
                int b1 = line.charAt(index++);
                int b2 = line.charAt(index++);
                if (b1 != 42 && b2 != 42) {
                    if (b1 >= 65) {
                        b1 = b1 - 65 + 57 + 1;
                    }

                    if (b1 >= 48) {
                        b1 -= 48;
                    }

                    if (b2 >= 65) {
                        b2 = b2 - 65 + 57 + 1;
                    }

                    if (b2 >= 48) {
                        b2 -= 48;
                    }

                    result = (short)(b1 * 16 + b2);
                } else {
                    result = 9999;
                }

                ++index;
                if (resultIndex < resultArray.length) {
                    resultArray[resultIndex++] = result;
                }
            }
        }

        return resultArray;
    }

    public boolean setMemory(String tagID, int startIndex, byte[] byteArray) {
        try {
            if (tagID != null) {
                this.setTagMask(tagID);
            }

            String readerCommand = "set memory = " + startIndex + ", ";
            readerCommand = readerCommand + Converters.toHexString(byteArray, " ");
            this.doReaderCommand(readerCommand);
            return true;
        } catch (Exception var5) {
            return false;
        }
    }

    public void clearMemory(String tagID) throws AlienReaderException {
        if (tagID != null) {
            this.setTagMask(tagID);
        }

        this.doReaderCommand("clear memory ");
    }

    public int getMemoryPacketSize() throws AlienReaderException {
        this.doReaderCommand("Get MemoryPacketSize");
        return this.getReaderReplyValueInt();
    }

    public void setMemoryPacketSize(int memoryPacketSize) throws AlienReaderException {
        this.doReaderCommand("Set MemoryPacketSize = " + memoryPacketSize);
    }

    public String toString() {
        return "Alien Class BPT Reader";
    }
}
