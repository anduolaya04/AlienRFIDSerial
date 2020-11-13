package com.alien.enterpriseRFID.externalio;

import com.alien.enterpriseRFID.util.XMLReader;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class ExternalIOUtil {
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    public ExternalIOUtil() {
    }

    public static ExternalIO[] decodeXMLIOList(String xmlData) {
        ArrayList ioList = new ArrayList();

        try {
            XMLReader xmlReader = new XMLReader(xmlData);
            Hashtable table = xmlReader.readXML();
            String ioData = (String)table.get("ALIEN-RFID-IO-LIST");
            if (ioData != null) {
                xmlReader.setXMLString(ioData);
            } else {
                xmlReader.setXMLString(xmlData);
            }

            while(xmlReader.readXMLNode(table)) {
                ioData = (String)table.get("ALIEN-RFID-IO");
                if (ioData != null) {
                    ExternalIO io = decodeXMLIO(ioData);
                    if (io != null) {
                        ioList.add(io);
                    }
                }
            }
        } catch (Exception var6) {
        }

        return (ExternalIO[])ioList.toArray(new ExternalIO[ioList.size()]);
    }

    public static ExternalIO decodeXMLIO(String xmlData) {
        try {
            XMLReader xmlReader = new XMLReader(xmlData);
            Hashtable table = xmlReader.readXML();
            String ioData = (String)table.get("ALIEN-RFID-IO-LIST");
            if (ioData != null) {
                xmlReader.setXMLString(ioData);
                table = xmlReader.readXML();
            }

            ioData = (String)table.get("ALIEN-RFID-IO");
            if (ioData != null) {
                xmlReader.setXMLString(ioData);
                table = xmlReader.readXML();
            }

            ExternalIO io = new ExternalIO();
            io.setType((String)table.get("TYPE"));
            String dateString = (String)table.get("TIME");
            if (dateString != null) {
                Date date = DATE_FORMATTER.parse(dateString, new ParsePosition(0));
                io.setEventTime(date.getTime());
            }

            String valueString = (String)table.get("DATA");
            int value = new Integer(valueString);
            io.setValue(value);
            return io;
        } catch (Exception var8) {
            return null;
        }
    }

    public static ExternalIO[] decodeIOList(String ioLines) {
        ArrayList ioList = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(ioLines, "\n\r");

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            ExternalIO io = decodeIO(token);
            if (io != null) {
                ioList.add(io);
            }
        }

        return (ExternalIO[])ioList.toArray(new ExternalIO[ioList.size()]);
    }

    public static ExternalIO decodeIO(String ioLine) {
        if (ioLine != null && !ioLine.equals("")) {
            if (ioLine.startsWith("(No IOs)")) {
                return null;
            } else {
                ExternalIO io = new ExternalIO();
                if (ioLine.startsWith("IO:")) {
                    int start = 3;
                    int end = ioLine.indexOf(",", start);
                    if (end >= start) {
                        io.setType(ioLine.substring(start, end).trim());
                    }

                    start = ioLine.indexOf("Time:");
                    if (start >= 0) {
                        end = ioLine.indexOf(",", start);
                        if (end > start) {
                            String dateString = ioLine.substring(start + 5, end).trim();
                            Date date = DATE_FORMATTER.parse(dateString, new ParsePosition(0));
                            if (date != null) {
                                io.setEventTime(date.getTime());
                            }
                        }
                    }

                    start = ioLine.indexOf("Data:");
                    if (start >= 0) {
                        try {
                            int count = new Integer(ioLine.substring(start + 5).trim());
                            io.setValue(count);
                        } catch (NumberFormatException var6) {
                        }
                    }

                    return io;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }
}
