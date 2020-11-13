package com.alien.enterpriseRFID.util;

import java.text.ParseException;
import java.util.Hashtable;

public class XMLReader {
    private String XMLString;
    private Hashtable resultsTable;
    private int openTagStartIndex;
    private int closeTagStartIndex;
    private String tagName;

    public XMLReader(String string) {
        this.setXMLString(string);
        this.openTagStartIndex = 0;
        this.closeTagStartIndex = 0;
    }

    public String getXMLString() {
        return this.XMLString;
    }

    public void setXMLString(String XMLString) {
        this.XMLString = XMLString;
        this.openTagStartIndex = 0;
        this.closeTagStartIndex = 0;
    }

    public Hashtable readXML() throws ParseException {
        return this.readXML((Hashtable)null);
    }

    public Hashtable readXML(Hashtable hashtable) throws ParseException {
        this.openTagStartIndex = 0;
        this.closeTagStartIndex = 0;
        if (hashtable == null) {
            hashtable = new Hashtable();
        }

        boolean isMoreNodes;
        do {
            isMoreNodes = this.readXMLNode(hashtable);
        } while(isMoreNodes);

        return hashtable;
    }

    public boolean readXMLNode(Hashtable hashtable) throws ParseException {
        this.resultsTable = hashtable;
        if (hashtable == null) {
            throw new ParseException("Hashtable in ReadXMLNode is NULL", 0);
        } else {
            this.openTagStartIndex = this.getOpenTag(this.closeTagStartIndex);
            if (this.openTagStartIndex == -1) {
                return false;
            } else {
                this.openTagStartIndex = this.openTagStartIndex + this.tagName.length() + 2;
                this.closeTagStartIndex = this.getCloseTag(this.openTagStartIndex);
                String tagData = this.XMLString.substring(this.openTagStartIndex, this.closeTagStartIndex).trim();
                this.resultsTable.put(this.tagName, tagData);
                this.closeTagStartIndex = this.closeTagStartIndex + this.tagName.length() + 3;
                return true;
            }
        }
    }

    private int getOpenTag(int startIndex) throws ParseException {
        this.tagName = "";
        int openIndex = this.XMLString.indexOf(60, startIndex);
        if (openIndex == -1) {
            return -1;
        } else if (this.XMLString.charAt(openIndex + 1) == '?') {
            return this.getOpenTag(startIndex + 1);
        } else if (this.XMLString.charAt(openIndex + 1) == '/') {
            throw new ParseException("Expected <OPEN TAG> but found unmatched </CLOSE TAG> at character position " + openIndex, openIndex);
        } else {
            int closeIndex = this.XMLString.indexOf(62, openIndex);
            if (closeIndex < 0) {
                throw new ParseException("Found open bracket (<) but no matching close bracket (>) at character position " + openIndex, openIndex);
            } else {
                this.tagName = this.XMLString.substring(openIndex + 1, closeIndex).trim();
                this.tagName = this.tagName.toUpperCase();
                if (this.tagName.length() <= 0) {
                    throw new ParseException("Found Tag with no name, i.e., <> at character position " + openIndex, openIndex);
                } else {
                    return openIndex;
                }
            }
        }
    }

    private int getCloseTag(int startIndex) throws ParseException {
        String closeTag = "</" + this.tagName + ">";
        int endIndex = this.XMLString.toUpperCase().indexOf(closeTag, startIndex);
        if (endIndex < 0) {
            throw new ParseException("No " + closeTag + " closing tag found in XML File", -1);
        } else {
            return endIndex;
        }
    }
}
