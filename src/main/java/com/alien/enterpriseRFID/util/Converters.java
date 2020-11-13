package com.alien.enterpriseRFID.util;

import java.util.StringTokenizer;

public class Converters {
    public Converters() {
    }

    public static String toHexString(byte[] byteArray, String separator) {
        return toHexString(byteArray, separator, false);
    }

    public static String toHexString(byte[] byteArray, String separator, boolean isTagID) {
        return byteArray == null ? "NULL Bytes" : toHexString(byteArray, 0, byteArray.length, separator, isTagID);
    }

    public static String toHexString(byte[] byteArray, int offset, int length, String separator) {
        return toHexString(byteArray, offset, length, separator, false);
    }

    public static String toHexString(byte[] byteArray, int offset, int length, String separator, boolean isTagID) {
        if (byteArray == null) {
            return "NULL Bytes";
        } else {
            if (separator == null) {
                separator = "";
            }

            StringBuffer buffer = new StringBuffer();
            int evenCounter = 0;

            for(int i = length - 1; i >= 0; --i) {
                if (offset + i < byteArray.length) {
                    String token = Integer.toHexString(byteArray[offset + i] & 255);
                    buffer.insert(0, token);
                    if (token.length() == 0) {
                        buffer.insert(0, "00");
                    }

                    if (token.length() == 1) {
                        buffer.insert(0, "0");
                    }

                    ++evenCounter;
                    if (i > 0) {
                        if (!isTagID) {
                            buffer.insert(0, separator);
                        }

                        if (isTagID && evenCounter == 2) {
                            buffer.insert(0, separator);
                        }
                    }

                    if (evenCounter == 2) {
                        evenCounter = 0;
                    }
                }
            }

            return buffer.toString().toUpperCase().trim();
        }
    }

    public static String toBinaryString(byte[] byteArray, int padLength) {
        if (byteArray == null) {
            return "NULL Bytes";
        } else {
            StringBuffer buffer = new StringBuffer();

            int l;
            for(l = byteArray.length - 1; l >= 0; --l) {
                String token = Integer.toBinaryString(byteArray[l] & 255);
                buffer.insert(0, token);
                if (l > 0) {
                    for(int j = token.length(); j < 8; ++j) {
                        buffer.insert(0, "0");
                    }

                    buffer.insert(0, " ");
                    ++padLength;
                }
            }

            if (padLength > 0) {
                l = buffer.length();

                for(int i = l; i < padLength; ++i) {
                    buffer.insert(0, "0");
                }
            }

            return buffer.toString();
        }
    }

    public static String toHexString(long value, int byteCount) {
        byte[] byteArray = new byte[byteCount];
        if (byteCount >= 1) {
            byteArray[byteCount - 1] = (byte)((int)(value & 255L));
        }

        if (byteCount >= 2) {
            byteArray[byteCount - 2] = (byte)((int)(value >> 8 & 255L));
        }

        if (byteCount >= 3) {
            byteArray[byteCount - 3] = (byte)((int)(value >> 16 & 255L));
        }

        if (byteCount >= 4) {
            byteArray[byteCount - 4] = (byte)((int)(value >> 24 & 255L));
        }

        if (byteCount >= 5) {
            byteArray[byteCount - 5] = (byte)((int)(value >> 32 & 255L));
        }

        if (byteCount >= 6) {
            byteArray[byteCount - 6] = (byte)((int)(value >> 40 & 255L));
        }

        if (byteCount >= 7) {
            byteArray[byteCount - 7] = (byte)((int)(value >> 48 & 255L));
        }

        if (byteCount >= 8) {
            byteArray[byteCount - 8] = (byte)((int)(value >> 56 & 255L));
        }

        return toHexString(byteArray, "");
    }

    public static String toHexString(int value) {
        String result = "";
        if (value < 16) {
            result = result + "0";
        }

        result = result + Integer.toHexString(value).toUpperCase();
        return result;
    }

    public static String toHexString(byte value) {
        String result = "";
        if ((value & 255) < 16) {
            result = result + "0";
        }

        result = result + Integer.toHexString(value & 255).toUpperCase();
        return result;
    }

    public static String toBinaryString(long value, int byteCount) {
        byte[] byteArray = new byte[byteCount];
        if (byteCount >= 1) {
            byteArray[byteCount - 1] = (byte)((int)(value & 255L));
        }

        if (byteCount >= 2) {
            byteArray[byteCount - 2] = (byte)((int)(value >> 8 & 255L));
        }

        if (byteCount >= 3) {
            byteArray[byteCount - 3] = (byte)((int)(value >> 16 & 255L));
        }

        if (byteCount >= 4) {
            byteArray[byteCount - 4] = (byte)((int)(value >> 24 & 255L));
        }

        if (byteCount >= 5) {
            byteArray[byteCount - 5] = (byte)((int)(value >> 32 & 255L));
        }

        if (byteCount >= 6) {
            byteArray[byteCount - 6] = (byte)((int)(value >> 40 & 255L));
        }

        if (byteCount >= 7) {
            byteArray[byteCount - 7] = (byte)((int)(value >> 48 & 255L));
        }

        if (byteCount >= 8) {
            byteArray[byteCount - 8] = (byte)((int)(value >> 56 & 255L));
        }

        return toBinaryString(byteArray, byteCount * 8);
    }

    public static byte[] fromHexString(String displayString) {
        if (displayString == null) {
            return new byte[0];
        } else if (displayString.length() < 1) {
            return new byte[0];
        } else {
            displayString = displayString.toUpperCase();
            StringTokenizer tokens = new StringTokenizer(displayString, " ");

            StringBuffer buffer;
            StringBuffer buf;
            int i;
            for(buffer = new StringBuffer(); tokens.hasMoreTokens(); buffer.append(buf.toString())) {
                buf = new StringBuffer();
                String chunk = tokens.nextToken();

                for(i = 0; i < chunk.length(); ++i) {
                    char c = chunk.charAt(i);
                    if (c >= '0' && c <= '9') {
                        buf.append(c);
                    }

                    if (c >= 'A' && c <= 'F') {
                        buf.append(c);
                    }
                }

                if (buf.length() / 2 * 2 != buf.length()) {
                    buf.insert(0, "0");
                }
            }

            String bufferString = buffer.toString();
            byte[] byteArray = new byte[bufferString.length() / 2];

            for(i = 0; i < byteArray.length; ++i) {
                try {
                    String token = bufferString.substring(i * 2, i * 2 + 2);
                    token = "0x" + token;
                    Integer integer = Integer.decode(token);
                    byteArray[i] = integer.byteValue();
                } catch (Exception var8) {
                }
            }

            return byteArray;
        }
    }

    public static byte[] fromBinaryString(String displayString) {
        if (displayString == null) {
            return new byte[0];
        } else if (displayString.length() < 1) {
            return new byte[0];
        } else {
            StringBuffer buffer = new StringBuffer();

            int byteCount;
            for(byteCount = 0; byteCount < displayString.length(); ++byteCount) {
                char c = displayString.charAt(byteCount);
                if (c == '0' || c == '1') {
                    buffer.append(c);
                }
            }

            byteCount = buffer.length() / 8;
            if (byteCount * 8 != buffer.length()) {
                ++byteCount;

                do {
                    buffer.insert(0, "0");
                } while(buffer.length() < byteCount * 8);
            }

            int stringIndex = buffer.length() - 1;
            byte[] byteArray = new byte[byteCount];

            for(int byteIndex = byteCount - 1; byteIndex >= 0; --byteIndex) {
                int b = 0;

                for(int bitIndex = 7; bitIndex >= 0; --bitIndex) {
                    b *= 2;
                    if (buffer.charAt(stringIndex - bitIndex) == '1') {
                        ++b;
                    }
                }

                stringIndex -= 8;
                byteArray[byteIndex] = (byte)b;
            }

            return byteArray;
        }
    }

    public static byte[] fromBinaryStringMSB(String displayString) {
        if (displayString == null) {
            return new byte[0];
        } else if (displayString.length() < 1) {
            return new byte[0];
        } else {
            StringBuffer buffer = new StringBuffer();

            int byteCount;
            for(byteCount = 0; byteCount < displayString.length(); ++byteCount) {
                char c = displayString.charAt(byteCount);
                if (c == '0' || c == '1') {
                    buffer.append(c);
                }
            }

            byteCount = buffer.length() / 8;
            if (byteCount * 8 < buffer.length()) {
                ++byteCount;

                do {
                    buffer.append("0");
                } while(buffer.length() < byteCount * 8);
            }

            int stringIndex = buffer.length() - 1;
            byte[] byteArray = new byte[byteCount];

            for(int byteIndex = byteCount - 1; byteIndex >= 0; --byteIndex) {
                int b = 0;

                for(int bitIndex = 7; bitIndex >= 0; --bitIndex) {
                    b *= 2;
                    if (buffer.charAt(stringIndex - bitIndex) == '1') {
                        ++b;
                    }
                }

                stringIndex -= 8;
                byteArray[byteIndex] = (byte)b;
            }

            return byteArray;
        }
    }

    public static String toAsciiString(byte[] byteArray, int offset, int length) {
        String text = new String(byteArray, offset, length);
        String result = "";

        for(int i = 0; i < text.length(); ++i) {
            char character = text.charAt(i);
            if (character <= 26) {
                result = result + "[";
                if (character == '\n') {
                    result = result + "lf";
                } else if (character == '\r') {
                    result = result + "cr";
                } else if (character == 0) {
                    result = result + "null";
                } else {
                    result = result + toHexString((int)character);
                }

                result = result + "]";
            } else {
                result = result + text.substring(i, i + 1);
            }
        }

        return result;
    }

    public static String toAsciiString(byte[] byteArray) {
        return toAsciiString(byteArray, 0, byteArray.length);
    }

    public static String toAsciiString(String string) {
        return toAsciiString(string.getBytes());
    }

    public static String toAsciiString(byte byteValue) {
        byte[] byteArray = new byte[]{byteValue};
        return toAsciiString(byteArray, 0, 1);
    }

    public static String reformatTagID(String tagID) {
        byte[] byteArray = fromHexString(tagID);
        return toHexString(byteArray, " ");
    }
}
