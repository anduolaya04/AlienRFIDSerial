package com.alien.enterpriseRFID.tags;


import com.alien.enterpriseRFID.util.XMLReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

public class TagUtil {
    private static String customFormatStr = "";
    private static CustomFormat cf;

    static {
        cf = new CustomFormat(customFormatStr);
    }

    public TagUtil() {
    }

    public static void setCustomFormatString(String customFormatString) {
        customFormatStr = customFormatString;
        String javaVer = System.getProperty("java.version");
        if (!javaVer.startsWith("1.3") && !javaVer.startsWith("1.2") && !javaVer.startsWith("1.1")) {
            cf = new CustomFormat(customFormatStr);
        } else {
            cf = null;
        }

    }

    public static String getCustomFormatString() {
        return customFormatStr;
    }

    public static Tag[] decodeXMLTagList(String xmlData) {
        Hashtable uniqueTags = new Hashtable();
        ArrayList tempTaglist = new ArrayList();

        try {
            XMLReader xmlReader = new XMLReader(xmlData);
            Hashtable table = xmlReader.readXML();
            String tagData = (String)table.get("ALIEN-RFID-TAG-LIST");
            if (tagData != null) {
                xmlReader.setXMLString(tagData);
            } else {
                xmlReader.setXMLString(xmlData);
            }

            while(xmlReader.readXMLNode(table)) {
                tagData = (String)table.get("ALIEN-RFID-TAG");
                if (tagData != null) {
                    Tag tag = decodeXMLTag(tagData);
                    if (tag != null) {
                        String taghash = tag.getTagID() + "," + tag.getTransmitAntenna();
                        Integer existingIndex = (Integer)uniqueTags.get(taghash);
                        if (existingIndex != null) {
                            Tag existingTag = (Tag)tempTaglist.get(existingIndex);
                            existingTag.updateTag(tag);
                        } else {
                            tag.setHostDiscoverTime(System.currentTimeMillis());
                            tempTaglist.add(tag);
                            uniqueTags.put(taghash, new Integer(tempTaglist.size()));
                        }
                    }
                }
            }
        } catch (Exception var10) {
        }

        Tag[] taglist = new Tag[tempTaglist.size()];
        tempTaglist.toArray(taglist);
        return taglist;
    }

    public static Tag decodeXMLTag(String xmlData) {
        try {
            XMLReader xmlReader = new XMLReader(xmlData);
            Hashtable table = xmlReader.readXML();
            String tagData = (String)table.get("ALIEN-RFID-TAG-LIST");
            if (tagData != null) {
                xmlReader.setXMLString(tagData);
                table = xmlReader.readXML();
            }

            tagData = (String)table.get("ALIEN-RFID-TAG");
            if (tagData != null) {
                xmlReader.setXMLString(tagData);
                table = xmlReader.readXML();
            }

            Tag tag = new Tag("");
            tag.setTagID((String)table.get("TAGID"));
            String countString = (String)table.get("READCOUNT");
            int count = new Integer(countString);
            tag.setRenewCount(count);
            String dateString = (String)table.get("DISCOVERYTIME");
            Date date;
            if (dateString != null) {
                date = parseDateAndTime(dateString);
                tag.setDiscoverTime(date.getTime());
            }

            dateString = (String)table.get("LASTSEENTIME");
            if (dateString != null) {
                date = parseDateAndTime(dateString);
                tag.setRenewTime(date.getTime());
                tag.lastRenewTime = 0L;
            }

            String antennaString = (String)table.get("ANTENNA");
            if (antennaString != null) {
                int antenna = new Integer(antennaString);
                tag.setAntenna(antenna);
            }

            String protocolString = (String)table.get("PROTOCOL");
            int i;
            if (protocolString != null) {
                try {
                    i = new Integer(protocolString);
                    tag.setProtocol(i);
                } catch (NumberFormatException var12) {
                    tag.setProtocol(protocolString);
                }
            }

            for(i = 1; i <= 4; ++i) {
                String g2Data = (String)table.get("D" + i);
                if (g2Data == null) {
                    break;
                }

                tag.setG2Data(i - 1, g2Data);
            }

            return tag;
        } catch (Exception var13) {
            return null;
        }
    }

    public static Tag[] decodeTagList(String tagLines) {
        Hashtable uniqueTags = new Hashtable();
        ArrayList tempTaglist = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(tagLines, "\n\r");

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            Tag tag = decodeTag(token);
            if (tag != null) {
                String taghash = tag.getTagID() + "," + tag.getTransmitAntenna();
                Integer existingIndex = (Integer)uniqueTags.get(taghash);
                if (existingIndex != null) {
                    Tag existingTag = (Tag)tempTaglist.get(existingIndex);
                    existingTag.updateTag(tag);
                } else {
                    tag.setHostDiscoverTime(System.currentTimeMillis());
                    tempTaglist.add(tag);
                    uniqueTags.put(taghash, new Integer(tempTaglist.size() - 1));
                }
            }
        }

        Tag[] taglist = new Tag[tempTaglist.size()];
        tempTaglist.toArray(taglist);
        return taglist;
    }

    public static Tag decodeTag(String tagLine) {
        if (tagLine != null && !tagLine.equals("")) {
            if (tagLine.startsWith("(No Tags)")) {
                return null;
            } else {
                Tag tag = new Tag("");
                if (!tagLine.startsWith("Tag:")) {
                    return null;
                } else {
                    int start = 4;
                    int end = tagLine.indexOf(",", start);
                    if (end >= start) {
                        tag.setTagID(tagLine.substring(start, end).trim());
                    } else {
                        tag.setTagID(tagLine.substring(start));
                    }

                    start = tagLine.indexOf("Disc:");
                    String dateString;
                    Date date;
                    if (start >= 0) {
                        end = tagLine.indexOf(",", start);
                        if (end > start) {
                            dateString = "";

                            try {
                                dateString = " " + tagLine.substring(start + 5, end).trim() + " ";
                                date = parseDateAndTime(dateString);
                                if (date != null) {
                                    tag.setDiscoverTime(date.getTime());
                                }
                            } catch (Exception var10) {
                                var10.printStackTrace();
                            }
                        }
                    }

                    start = tagLine.indexOf("Last:");
                    if (start >= 0) {
                        end = tagLine.indexOf(",", start);
                        if (end > start) {
                            dateString = "";

                            try {
                                dateString = tagLine.substring(start + 5, end).trim();
                                date = parseDateAndTime(dateString);
                                if (date != null) {
                                    tag.setRenewTime(date.getTime());
                                    tag.lastRenewTime = 0L;
                                }
                            } catch (Exception var9) {
                                var9.printStackTrace();
                            }
                        }
                    }

                    start = tagLine.indexOf("Count:");
                    int i;
                    if (start >= 0) {
                        end = tagLine.indexOf(",", start);
                        if (end > start) {
                            try {
                                i = new Integer(tagLine.substring(start + 6, end).trim());
                                tag.setRenewCount(i);
                            } catch (NumberFormatException var8) {
                            }
                        }
                    }

                    start = tagLine.indexOf("Ant:");
                    if (start >= 0) {
                        end = tagLine.indexOf(",", start);
                        if (end < 0) {
                            end = tagLine.indexOf("\n", start);
                            if (end < 0) {
                                end = tagLine.length();
                            }
                        }

                        try {
                            i = new Integer(tagLine.substring(start + 4, end).trim());
                            tag.setAntenna(i);
                        } catch (NumberFormatException var7) {
                        }
                    }

                    start = tagLine.indexOf("Proto:");
                    if (start >= 0) {
                        end = tagLine.indexOf(",", start);
                        if (end < 0) {
                            end = tagLine.indexOf("\n", start);
                            if (end < 0) {
                                end = tagLine.length();
                            }
                        }

                        try {
                            i = new Integer(tagLine.substring(start + 6, end).trim());
                            tag.setProtocol(i);
                        } catch (NumberFormatException var6) {
                        }
                    }

                    for(i = 1; i <= 4; ++i) {
                        start = tagLine.indexOf("D" + i + ":");
                        if (start < 0) {
                            break;
                        }

                        end = tagLine.indexOf(",", start);
                        if (end < 0) {
                            end = tagLine.indexOf("\n", start);
                            if (end < 0) {
                                end = tagLine.length();
                            }
                        }

                        tag.setG2Data(i - 1, tagLine.substring(start + 3, end).trim());
                    }

                    return tag;
                }
            }
        } else {
            return null;
        }
    }

    public static Tag[] decodeCustomTagList(String tagLines, String customFormatString) {
        setCustomFormatString(customFormatString);
        return decodeCustomTagList(tagLines);
    }

    public static Tag[] decodeCustomTagList(String tagLines) {
        Hashtable uniqueTags = new Hashtable();
        ArrayList tempTaglist = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(tagLines, "\n\r");

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            Tag tag = decodeCustomTag(token);
            if (tag != null) {
                String taghash = tag.getTagID() + "," + tag.getTransmitAntenna();
                Integer existingIndex = (Integer)uniqueTags.get(taghash);
                if (existingIndex != null) {
                    Tag existingTag = (Tag)tempTaglist.get(existingIndex);
                    existingTag.updateTag(tag);
                } else {
                    tag.setHostDiscoverTime(System.currentTimeMillis());
                    tempTaglist.add(tag);
                    uniqueTags.put(taghash, new Integer(tempTaglist.size() - 1));
                }
            }
        }

        Tag[] taglist = new Tag[tempTaglist.size()];
        tempTaglist.toArray(taglist);
        return taglist;
    }

    public static Tag decodeCustomTag(String tagLine) {
        if (cf == null) {
            return null;
        } else {
            String discTime = " ";
            String lastTime = " ";
            Tag tag = new Tag("");
            Matcher matcher = cf.getPattern().matcher(tagLine);
            if (tagLine != null && !tagLine.equals("") && !tagLine.startsWith("(No Tags)")) {
                if (matcher.matches()) {
                    int[] tokens = cf.getTokens();

                    Date date;
                    for(int i = 1; i <= matcher.groupCount(); ++i) {
                        String match = matcher.group(i);
                        switch(tokens[i - 1]) {
                            case 1:
                                tag.setTagID(match);
                                break;
                            case 2:
                                date = parseDateAndTime(match);
                                if (date != null) {
                                    tag.setDiscoverTime(date.getTime());
                                }
                                break;
                            case 3:
                                tag.setDiscoverTime(Long.parseLong(match));
                                break;
                            case 4:
                                discTime = match + discTime;
                                break;
                            case 5:
                                discTime = discTime + match;
                                break;
                            case 6:
                                date = parseDateAndTime(match);
                                if (date != null) {
                                    tag.setRenewTime(date.getTime());
                                }
                                break;
                            case 7:
                                tag.setRenewTime(Long.parseLong(match));
                                break;
                            case 8:
                                lastTime = match + lastTime;
                                break;
                            case 9:
                                lastTime = lastTime + match;
                                break;
                            case 10:
                                tag.setRenewCount(Integer.parseInt(match));
                                break;
                            case 11:
                                tag.setTransmitAntenna(Integer.parseInt(match));
                                break;
                            case 12:
                                tag.setReceiveAntenna(Integer.parseInt(match));
                                break;
                            case 13:
                                if (match != null) {
                                    tag.setProtocol(Integer.parseInt(match));
                                }
                                break;
                            case 14:
                                tag.setProtocol(match);
                                break;
                            case 15:
                                if (match != null) {
                                    tag.setG2Data(0, match);
                                }
                                break;
                            case 16:
                                if (match != null) {
                                    tag.setG2Data(1, match);
                                }
                                break;
                            case 17:
                                if (match != null) {
                                    tag.setG2Data(2, match);
                                }
                                break;
                            case 18:
                                if (match != null) {
                                    tag.setG2Data(3, match);
                                }
                                break;
                            case 19:
                                tag.setSpeed(Double.parseDouble(match));
                                break;
                            case 20:
                                if ("-".equals(match)) {
                                    tag.setDirection(-1);
                                } else if ("+".equals(match)) {
                                    tag.setDirection(1);
                                } else {
                                    tag.setDirection(0);
                                }
                                break;
                            case 21:
                                tag.setRSSI(Double.parseDouble(match));
                                break;
                            case 22:
                                tag.setTagAuth(match);
                                break;
                            case 23:
                                tag.setPCWord(match);
                                break;
                            case 24:
                                tag.setXPC(match);
                                break;
                            case 25:
                                tag.setG2Ops(1, match);
                                break;
                            case 26:
                                tag.setG2Ops(2, match);
                                break;
                            case 27:
                                tag.setG2Ops(3, match);
                                break;
                            case 28:
                                tag.setG2Ops(4, match);
                                break;
                            case 29:
                                tag.setG2Ops(5, match);
                                break;
                            case 30:
                                tag.setG2Ops(6, match);
                                break;
                            case 31:
                                tag.setG2Ops(7, match);
                                break;
                            case 32:
                                tag.setG2Ops(8, match);
                                break;
                            case 33:
                                tag.setG2Ops(0, match);
                        }
                    }

                    switch(discTime.length()) {
                        case 9:
                        case 13:
                            date = parseTime(discTime);
                            if (date != null) {
                                tag.setDiscoverTime(date.getTime());
                            }
                        case 19:
                        case 23:
                            date = parseDateAndTime(discTime);
                            if (date != null) {
                                tag.setDiscoverTime(date.getTime());
                            }
                            break;
                        case 11:
                            date = parseDate(discTime);
                            if (date != null) {
                                tag.setDiscoverTime(date.getTime());
                            }
                    }

                    switch(lastTime.length()) {
                        case 9:
                        case 13:
                            date = parseTime(lastTime);
                            if (date != null) {
                                tag.setRenewTime(date.getTime());
                            }
                        case 19:
                        case 23:
                            date = parseDateAndTime(lastTime);
                            if (date != null) {
                                tag.setRenewTime(date.getTime());
                            }
                            break;
                        case 11:
                            date = parseDate(lastTime);
                            if (date != null) {
                                tag.setRenewTime(date.getTime());
                            }
                    }

                    return tag;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public static Date parseDateAndTime(String dateString) {
        if (dateString == null) {
            return null;
        } else {
            dateString = dateString.trim();
            int year = Integer.parseInt(dateString.substring(0, 4));
            int month = Integer.parseInt(dateString.substring(5, 7));
            int day = Integer.parseInt(dateString.substring(8, 10));
            int hour = Integer.parseInt(dateString.substring(11, 13));
            int minute = Integer.parseInt(dateString.substring(14, 16));
            int second = Integer.parseInt(dateString.substring(17, 19));
            GregorianCalendar cal = new GregorianCalendar(year, month - 1, day, hour, minute, second);
            if (dateString.length() >= 23) {
                int millis = Integer.parseInt(dateString.substring(20, 23));
                cal.set(14, millis);
            }

            return cal.getTime();
        }
    }

    public static Date parseDate(String dateString) {
        if (dateString == null) {
            return null;
        } else {
            dateString = dateString.trim();
            int year = Integer.parseInt(dateString.substring(0, 4));
            int month = Integer.parseInt(dateString.substring(5, 7));
            int day = Integer.parseInt(dateString.substring(8, 10));
            GregorianCalendar cal = new GregorianCalendar(year, month - 1, day);
            return cal.getTime();
        }
    }

    public static Date parseTime(String timeString) {
        if (timeString == null) {
            return null;
        } else {
            timeString = timeString.trim();
            int hour = Integer.parseInt(timeString.substring(0, 2));
            int minute = Integer.parseInt(timeString.substring(3, 5));
            int second = Integer.parseInt(timeString.substring(6, 8));
            GregorianCalendar cal = new GregorianCalendar(1970, 0, 1, hour, minute, second);
            if (timeString.length() >= 12) {
                int millis = Integer.parseInt(timeString.substring(9, 12));
                cal.set(14, millis);
            }

            return cal.getTime();
        }
    }

    public static String taglistAsString(Tag[] taglist) {
        if (taglist == null) {
            taglist = new Tag[0];
        }

        StringBuffer buf = new StringBuffer("Taglist (" + taglist.length + " tags):\r\n");

        for(int i = 0; i < taglist.length; ++i) {
            buf.append(taglist[i].toLongString()).append("\r\n");
        }

        return buf.toString();
    }
}
