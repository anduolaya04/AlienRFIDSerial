package com.alien.enterpriseRFID.tags;

import java.util.ArrayList;
import java.util.regex.Pattern;

class CustomFormat {
    public static final int TOKEN_EPC = 1;
    public static final int TOKEN_DISC = 2;
    public static final int TOKEN_DISC_MILLIS = 3;
    public static final int TOKEN_DISC_DATE = 4;
    public static final int TOKEN_DISC_TIME = 5;
    public static final int TOKEN_LAST = 6;
    public static final int TOKEN_LAST_MILLIS = 7;
    public static final int TOKEN_LAST_DATE = 8;
    public static final int TOKEN_LAST_TIME = 9;
    public static final int TOKEN_COUNT = 10;
    public static final int TOKEN_TX = 11;
    public static final int TOKEN_RX = 12;
    public static final int TOKEN_PROTO = 13;
    public static final int TOKEN_PROTO_STR = 14;
    public static final int TOKEN_G2D1 = 15;
    public static final int TOKEN_G2D2 = 16;
    public static final int TOKEN_G2D3 = 17;
    public static final int TOKEN_G2D4 = 18;
    public static final int TOKEN_SPEED = 19;
    public static final int TOKEN_DIR = 20;
    public static final int TOKEN_RSSI = 21;
    public static final int TOKEN_TAGAUTH = 22;
    public static final int TOKEN_PCWORD = 23;
    public static final int TOKEN_XPC = 24;
    public static final int TOKEN_G2OPS1 = 25;
    public static final int TOKEN_G2OPS2 = 26;
    public static final int TOKEN_G2OPS3 = 27;
    public static final int TOKEN_G2OPS4 = 28;
    public static final int TOKEN_G2OPS5 = 29;
    public static final int TOKEN_G2OPS6 = 30;
    public static final int TOKEN_G2OPS7 = 31;
    public static final int TOKEN_G2OPS8 = 32;
    public static final int TOKEN_G2OPS = 33;
    private String reString;
    private String reHexString;
    private String reDate;
    private String reTime;
    private String reDateTime;
    private String reInteger;
    private String reFloat;
    private String reDir;
    private String textFormatStr;
    private String xmlFormatStr;
    private String fString;
    private int[] fTokens;
    private String fRegExp;
    private Pattern fPattern;

    public CustomFormat() {
        this("");
    }

    public CustomFormat(String formatString) {
        this.reString = "(.*)";
        this.reHexString = "((?:[0-9A-F][0-9A-F] ?)*)";
        this.reDate = "(\\d\\d\\d\\d/\\d\\d/\\d\\d)";
        this.reTime = "(\\d\\d:\\d\\d:\\d\\d(?:\\.\\d\\d\\d)?)";
        this.reDateTime = "(" + this.reDate + " " + this.reTime + ")";
        this.reInteger = "(\\d*)";
        this.reFloat = "(-?[0-9]+\\.[0-9]+)";
        this.reDir = "([\\+0-])";
        this.textFormatStr = "Tag:${TAGIDB}, Disc:${DATE1} ${TIME1}, Last:${DATE2} ${TIME2}, Count:${COUNT}, Ant:${TX}(?:, Proto:${PROTO#})?(?:, D1:${G2DATA1})?(?:, D2:${G2DATA2})?(?:, D3:${G2DATA3})?(?:, D4:${G2DATA4})?";
        this.xmlFormatStr = "<TagID>${TAGIDW}</TagID>\r\n *<DiscoveryTime>${DATE1} ${TIME1}</DiscoveryTime>\r\n *<LastSeenTime>${DATE2} ${TIME2}</LastSeenTime>\r\n *<Antenna>${TX}</Antenna>\r\n *<ReadCount>${COUNT}</ReadCount>(?:\r\n *<Protocol>${PROTO}</Protocol>)?(?:\r\n *<D1>${G2DATA1}</D1>)?(?:\r\n *<D2>${G2DATA2}</D2>)?(?:\r\n *<D3>${G2DATA3}</D3>)?(?:\r\n *<D4>${G2DATA4}</D4>)?";
        if (formatString == null) {
            formatString = "";
        }

        this.setFormatString(formatString);
    }

    public void setFormatString(String formatString) {
        this.fString = formatString;
        this.generatePattern();
    }

    public Pattern getPattern() {
        return this.fPattern;
    }

    public String getRegExp() {
        return this.fRegExp;
    }

    public int[] getTokens() {
        return this.fTokens;
    }

    private void generatePattern() {
        int formatPos = 0;
        StringBuffer patternBuf = new StringBuffer();
        String tokenChars = "ikaArdDtTpPsm";
        String regexpChars = ".*+?()\\";
        ArrayList tokenList = new ArrayList();

        while(true) {
            while(formatPos < this.fString.length()) {
                char c = this.fString.charAt(formatPos);
                if (c == '%' && formatPos < this.fString.length() - 1) {
                    char token = this.fString.charAt(formatPos + 1);
                    if (tokenChars.indexOf(token) >= 0) {
                        switch(token) {
                            case 'A':
                                patternBuf.append(this.reInteger);
                                tokenList.add(new Integer(12));
                                break;
                            case 'D':
                                patternBuf.append(this.reDate);
                                tokenList.add(new Integer(8));
                                break;
                            case 'P':
                                patternBuf.append(this.reInteger);
                                tokenList.add(new Integer(13));
                                break;
                            case 'T':
                                patternBuf.append(this.reTime);
                                tokenList.add(new Integer(9));
                                break;
                            case 'a':
                                patternBuf.append(this.reInteger);
                                tokenList.add(new Integer(11));
                                break;
                            case 'd':
                                patternBuf.append(this.reDate);
                                tokenList.add(new Integer(4));
                                break;
                            case 'i':
                            case 'k':
                                patternBuf.append(this.reHexString);
                                tokenList.add(new Integer(1));
                                break;
                            case 'm':
                                patternBuf.append(this.reFloat);
                                tokenList.add(new Integer(21));
                                break;
                            case 'p':
                                patternBuf.append(this.reString);
                                tokenList.add(new Integer(14));
                                break;
                            case 'r':
                                patternBuf.append(this.reInteger);
                                tokenList.add(new Integer(10));
                                break;
                            case 's':
                                patternBuf.append(this.reFloat);
                                tokenList.add(new Integer(19));
                                break;
                            case 't':
                                patternBuf.append(this.reTime);
                                tokenList.add(new Integer(5));
                        }

                        formatPos += 2;
                    } else {
                        patternBuf.append("%").append(token);
                        ++formatPos;
                    }
                } else if (c == '$' && formatPos < this.fString.length() - 1) {
                    if (this.fString.charAt(formatPos + 1) == '{') {
                        int endVariablePos = this.fString.indexOf(125, formatPos);
                        if (endVariablePos < 0) {
                            patternBuf.append("${");
                            formatPos += 2;
                        } else {
                            String var = this.fString.substring(formatPos + 2, endVariablePos);
                            String regexp = "";
                            boolean varIsValid = true;
                            if (!var.equalsIgnoreCase("TAGID") && !var.equalsIgnoreCase("TAGIDB") && !var.equalsIgnoreCase("TAGIDW")) {
                                if (var.equalsIgnoreCase("TX")) {
                                    regexp = this.reInteger;
                                    tokenList.add(new Integer(11));
                                } else if (var.equalsIgnoreCase("RX")) {
                                    regexp = this.reInteger;
                                    tokenList.add(new Integer(12));
                                } else if (var.equalsIgnoreCase("COUNT")) {
                                    regexp = this.reInteger;
                                    tokenList.add(new Integer(10));
                                } else if (var.equalsIgnoreCase("DATE1")) {
                                    regexp = this.reDate;
                                    tokenList.add(new Integer(4));
                                } else if (var.equalsIgnoreCase("DATE2")) {
                                    regexp = this.reDate;
                                    tokenList.add(new Integer(8));
                                } else if (var.equalsIgnoreCase("TIME1")) {
                                    regexp = this.reTime;
                                    tokenList.add(new Integer(5));
                                } else if (var.equalsIgnoreCase("TIME2")) {
                                    regexp = this.reTime;
                                    tokenList.add(new Integer(9));
                                } else if (var.equalsIgnoreCase("MSEC1")) {
                                    regexp = this.reInteger;
                                    tokenList.add(new Integer(3));
                                } else if (var.equalsIgnoreCase("MSEC2")) {
                                    regexp = this.reInteger;
                                    tokenList.add(new Integer(7));
                                } else if (var.equalsIgnoreCase("PROTO#")) {
                                    regexp = this.reInteger;
                                    tokenList.add(new Integer(13));
                                } else if (var.equalsIgnoreCase("PROTO")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(14));
                                } else if (var.equalsIgnoreCase("G2DATA1")) {
                                    regexp = this.reHexString;
                                    tokenList.add(new Integer(15));
                                } else if (var.equalsIgnoreCase("G2DATA2")) {
                                    regexp = this.reHexString;
                                    tokenList.add(new Integer(16));
                                } else if (var.equalsIgnoreCase("G2DATA3")) {
                                    regexp = this.reHexString;
                                    tokenList.add(new Integer(17));
                                } else if (var.equalsIgnoreCase("G2DATA4")) {
                                    regexp = this.reHexString;
                                    tokenList.add(new Integer(18));
                                } else if (var.equalsIgnoreCase("SPEED")) {
                                    regexp = this.reFloat;
                                    tokenList.add(new Integer(19));
                                } else if (var.equalsIgnoreCase("RSSI")) {
                                    regexp = this.reFloat;
                                    tokenList.add(new Integer(21));
                                } else if (var.equalsIgnoreCase("DIR")) {
                                    regexp = this.reDir;
                                    tokenList.add(new Integer(20));
                                } else if (var.equalsIgnoreCase("AUTH")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(22));
                                } else if (var.equalsIgnoreCase("PCWORD")) {
                                    regexp = this.reHexString;
                                    tokenList.add(new Integer(23));
                                } else if (var.equalsIgnoreCase("XPC")) {
                                    regexp = this.reHexString;
                                    tokenList.add(new Integer(24));
                                } else if (var.equalsIgnoreCase("G2OPS")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(33));
                                } else if (var.equalsIgnoreCase("G2OPS1")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(25));
                                } else if (var.equalsIgnoreCase("G2OPS2")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(26));
                                } else if (var.equalsIgnoreCase("G2OPS3")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(27));
                                } else if (var.equalsIgnoreCase("G2OPS4")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(28));
                                } else if (var.equalsIgnoreCase("G2OPS5")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(29));
                                } else if (var.equalsIgnoreCase("G2OPS6")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(30));
                                } else if (var.equalsIgnoreCase("G2OPS7")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(31));
                                } else if (var.equalsIgnoreCase("G2OPS8")) {
                                    regexp = this.reString;
                                    tokenList.add(new Integer(32));
                                } else {
                                    varIsValid = false;
                                }
                            } else {
                                regexp = this.reHexString;
                                tokenList.add(new Integer(1));
                            }

                            if (varIsValid) {
                                patternBuf.append(regexp);
                                formatPos = endVariablePos + 1;
                            } else {
                                patternBuf.append("${");
                                formatPos += 2;
                            }
                        }
                    } else {
                        patternBuf.append('$');
                        ++formatPos;
                    }
                } else if (regexpChars.indexOf(c) >= 0) {
                    patternBuf.append('\\').append(c);
                    ++formatPos;
                } else {
                    patternBuf.append(c);
                    ++formatPos;
                }
            }

            this.fRegExp = patternBuf.toString();
            this.fPattern = Pattern.compile(this.fRegExp);
            this.fTokens = new int[tokenList.size()];

            for(int i = 0; i < tokenList.size(); ++i) {
                this.fTokens[i] = (Integer)tokenList.get(i);
            }

            return;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.fString + "\n");
        buf.append(this.fRegExp + "\n");

        for(int i = 0; i < this.fTokens.length; ++i) {
            buf.append(this.fTokens[i] + " ");
        }

        buf.append("\n");
        return buf.toString();
    }
}
