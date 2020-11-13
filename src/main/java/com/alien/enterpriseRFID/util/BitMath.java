package com.alien.enterpriseRFID.util;

public class BitMath {
    public BitMath() {
    }

    public static byte[] reverseBitArray(byte[] bitArray) {
        byte[] reply = new byte[bitArray.length];

        for(int i = 0; i < bitArray.length; ++i) {
            reply[bitArray.length - 1 - i] = bitArray[i];
        }

        return reply;
    }

    public static byte[] getBitArray(byte[] byteArray) throws IllegalArgumentException {
        return getBitArray(byteArray, 0, byteArray.length);
    }

    public static byte[] getBitArray(byte[] byteArray, int offset, int length) throws IllegalArgumentException {
        if (byteArray == null) {
            throw new IllegalArgumentException("Byte Array is NULL in getBitArray method");
        } else {
            byte[] bitArray = new byte[length * 8];

            for(int i = 0; i < length; ++i) {
                byte b = byteArray[i + offset];

                for(int j = 0; j < 8; ++j) {
                    byte result = 0;
                    if ((b & 128) != 0) {
                        result = 1;
                    }

                    bitArray[i * 8 + j] = result;
                    b = (byte)(b << 1);
                }
            }

            return bitArray;
        }
    }

    public static byte[] getByteArray(byte[] bitArray) throws IllegalArgumentException {
        if (bitArray == null) {
            throw new IllegalArgumentException("Bit Array is NULL in getByteArray method");
        } else if (bitArray.length == 0) {
            return new byte[1];
        } else {
            int byteLength = bitArray.length / 8;
            if (byteLength * 8 < bitArray.length) {
                ++byteLength;
            }

            byte[] byteArray = new byte[byteLength];
            int byteIndex = byteLength - 1;
            int bitIndex = bitArray.length - 1;
            int bitValue = 1;

            do {
                if (bitArray[bitIndex] == 1) {
                    byteArray[byteIndex] = (byte)(byteArray[byteIndex] | bitValue);
                }

                --bitIndex;
                bitValue *= 2;
                if (bitValue > 128) {
                    bitValue = 1;
                    --byteIndex;
                }
            } while(bitIndex >= 0);

            return byteArray;
        }
    }

    public static long getValue(byte[] bitArray, int start, int length) throws IllegalArgumentException {
        if (bitArray == null) {
            throw new IllegalArgumentException("Bit Array is NULL in getValue method");
        } else if (start >= 0 && start <= bitArray.length) {
            if (length + start > 64) {
                throw new IllegalArgumentException("Length Parameter is out of range in getValue method");
            } else {
                long results = 0L;

                for(int i = 0; i < length; ++i) {
                    results <<= 1;
                    if (bitArray[i + start] == 1) {
                        results |= 1L;
                    }
                }

                return results;
            }
        } else {
            throw new IllegalArgumentException("Start Parameter is out of range in getValue method");
        }
    }

    public static byte[] setValue(byte[] bitArray, long value, int start, int length) throws IllegalArgumentException {
        if (bitArray == null) {
            throw new IllegalArgumentException("Bit Array is NULL in setValue method");
        } else if (start >= 0 && start <= bitArray.length) {
            if (length + start > 64) {
                throw new IllegalArgumentException("Length Parameter is out of range in setValue method");
            } else {
                for(int i = 0; i < length; ++i) {
                    int bitIndex = start + length - i - 1;
                    bitArray[bitIndex] = 0;
                    if ((value & 1L) == 1L) {
                        bitArray[bitIndex] = 1;
                    }

                    value >>= 1;
                }

                return bitArray;
            }
        } else {
            throw new IllegalArgumentException("Start Parameter is out of range in setValue method");
        }
    }

    public static void validate(String valueName, long value, int bitCount) throws IllegalArgumentException {
        long maximumValue = 0L;

        for(int i = 0; i < bitCount; ++i) {
            maximumValue = (maximumValue << 2) + 1L;
        }

        if (value < 0L || value > maximumValue) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(valueName + " value is out of Range : " + value + "\n");
            buffer.append("Range is limited by " + bitCount + " bits which equates to numerical");
            buffer.append(" values of 0-" + maximumValue + " decimal or ");
            buffer.append("0-" + Long.toHexString(maximumValue) + " hex");
            throw new IllegalArgumentException(buffer.toString());
        }
    }

    public static short generateLRC16Checksum(byte[] byteArray, int offset, int length) {
        byte loByte = 0;

        for(int i = 0; i < length; ++i) {
            if ((loByte & 128) == 0) {
                loByte = (byte)(loByte * 2);
            } else {
                loByte = (byte)(loByte * 2 + 1);
            }

            loByte ^= byteArray[i + offset];
        }

        byte hiByte = (byte)(255 & ~loByte);
        short result = (short)((hiByte & 255) << 8);
        result = (short)(result + (loByte & 255));
        return result;
    }

    public static int generateCRC16Checksum09(byte[] byteArray, int offset, int length) throws IllegalArgumentException {
        int polynomial = 2065;
        int value = 65535;
        int reverseValue = 0;
        byte[] bitArray = getBitArray(byteArray);

        int pop;
        int i;
        for(i = bitArray.length - 1; i >= 0; --i) {
            pop = value & '\u8000';
            value = (value & 32767) << 1;
            if (bitArray[i] == 1) {
                value |= 1;
            }

            if (pop != 0) {
                value ^= polynomial;
            }
        }

        for(i = 0; i < 16; ++i) {
            pop = value & '\u8000';
            value = (value & 32767) << 1;
            if (pop != 0) {
                value ^= polynomial;
            }
        }

        for(i = 0; i < 16; ++i) {
            pop = value & '\u8000';
            value = (value & 32767) << 1;
            reverseValue >>= 1;
            if (pop != 0) {
                reverseValue |= 32768;
            }
        }

        return reverseValue;
    }

    public static int generateCRC16Checksum10(byte[] byteArray, int offset, int length) throws IllegalArgumentException {
        int polynomial = 4129;
        int preload = '\uffff';
        byte[] bitArray = getBitArray(byteArray);
        int CRCReg = preload;

        int carry;
        int i;
        for(i = 0; i < bitArray.length; ++i) {
            carry = CRCReg & '\u8000';
            CRCReg = (CRCReg & 32767) << 1;
            if (bitArray[i] == 1) {
                ++CRCReg;
            }

            if (carry != 0) {
                CRCReg ^= polynomial;
            }
        }

        for(i = 0; i < 16; ++i) {
            carry = CRCReg & '\u8000';
            CRCReg = (CRCReg & 32767) << 1;
            if (carry != 0) {
                CRCReg ^= polynomial;
            }
        }

        return CRCReg;
    }

    public static int generateCRC16Checksum10(String tagID) throws IllegalArgumentException {
        byte[] byteArray = Converters.fromHexString(tagID);
        return generateCRC16Checksum10(byteArray, 0, byteArray.length);
    }
}
