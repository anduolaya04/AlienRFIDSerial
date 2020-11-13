package com.alien.enterpriseRFID.reader;

import com.alien.enterpriseRFID.tags.Tag;
import com.alien.enterpriseRFID.tags.TagTable;
import com.alien.enterpriseRFID.util.Converters;

public class AlienDLEObject {

    public static final int COMMAND_BUFFER_SIZE = 100;
    public static final int REPLY_BUFFER_SIZE = 100;
    public static final int COMMAND_STATUS_UNKNOWN = 0;
    public static final int COMMAND_STATUS_RESET = 1;
    public static final int COMMAND_STATUS_SEND_ME = 2;
    public static final int COMMAND_STATUS_RECEIVE_ME = 3;
    public static final int COMMAND_STATUS_INVENTORY_BREAK = 5;
    public static final int COMMAND_STATUS_COMPLETE = 10;
    public static final int COMMAND_STATUS_ERROR = 11;
    public static final int COMMAND_STATUS_TIMEOUT = 12;
    public static final int COMMAND_STATUS_READER_ERROR = 20;
    public static final int WAITING_FOR_DLE = 0;
    public static final int WAITING_FOR_SOF = 1;
    public static final int WAITING_FOR_MSG = 2;
    public static final int WAITING_FOR_DLE_EMBEDDED = 3;
    public static final int COMMAND_DLE = 16;
    public static final int COMMAND_SOF = 1;
    public static final int COMMAND_EOF = 2;
    public static final int CMD_GET_FIRMWARE_VERSION = 0;
    public static final int CMD_SET_READER_NUMBER = 1;
    public static final int CMD_GET_READER_NUMBER = 2;
    public static final int CMD_SET_BAUD_RATE = 3;
    public static final int CMD_REBOOT = 4;
    public static final int CMD_SET_IO_PORT_VALUE = 5;
    public static final int CMD_GET_IO_PORT_VALUE = 6;
    public static final int CMD_SET_ACTIVE_ANTENNA = 7;
    public static final int CMD_GET_ACTIVE_ANTENNA = 8;
    public static final int CMD_SET_RF_ATTENUATION = 9;
    public static final int CMD_GET_RF_ATTENUATION = 10;
    public static final int CMD_SET_IO_INVERSION_MASK = 11;
    public static final int CMD_GET_IO_INVERSION_MASK = 12;
    public static final int CMD_INTERNAL_TEST_PROCEDURE = 13;
    public static final int CMD_SUSPEND_READER = 14;
    public static final int CMD_GET_READER_STATUS = 15;
    public static final int CMD_RESTORE_FACTORY_DEFAULTS = 16;
    public static final int CMD_GET_HARDWARE_INFO = 17;
    public static final int CMD_GET_MANUFACTURING_INFO = 18;
    public static final int CMD_MANAGE_RESERVED_PARAMS = 19;
    public static final int CMD_SET_TAG_MASK = 20;
    public static final int CMD_GET_TAG_MASK = 21;
    public static final int CMD_SET_BIDIRECTIONAL_DDR = 22;
    public static final int CMD_GET_BIDIRECTIONAL_DDR = 23;
    public static final int CMD_SET_IO_MASK = 24;
    public static final int CMD_GET_IO_STATUS_LATCH = 25;
    public static final int CMD_SET_RF_ONOFF = 26;
    public static final int CMD_GET_RF_ONOFF = 27;
    public static final int CMD_SET_ANTENNA_SEQUENCE = 28;
    public static final int CMD_GET_ANTENNA_SEQUENCE = 29;
    public static final int CMD_SET_OUTPUT_INITIAL_STATE = 30;
    public static final int CMD_GET_OUTPUT_INITIAL_STATE = 31;
    public static final int CMD_SET_ANTENNA_RF_POWER = 97;
    public static final int CMD_GET_ANTENNA_RF_POWER = 98;
    public static final int CMD_ENTER_BOOTLOADER = 209;
    public static final int CMD_UPLOAD_FIRMWARE_LINE = 210;
    public static final int CMD_GET_TAG_FIRMWARE_VERSION = 32;
    public static final int CMD_SLEEP_TAG = 33;
    public static final int CMD_WAKE_TAG = 34;
    public static final int CMD_PROGRAM_ROW = 35;
    public static final int CMD_GET_TAG_ID = 36;
    public static final int CMD_SET_TAG_MEMORY = 37;
    public static final int CMD_GET_TAG_MEMORY = 38;
    public static final int CMD_FORMAT_TAG_MEMORY = 39;
    public static final int CMD_SET_LOGGING = 40;
    public static final int CMD_GET_LOGGING = 41;
    public static final int CMD_SET_LOGGING_INTERVAL = 42;
    public static final int CMD_GET_LOGGING_INTERVAL = 43;
    public static final int CMD_SET_TIME = 44;
    public static final int CMD_GET_TIME = 45;
    public static final int CMD_SET_TAG_TYPE = 46;
    public static final int CMD_GET_TAG_TYPE = 47;
    public static final int CMD_SET_TAG_MODE = 48;
    public static final int CMD_GET_TAG_MODE = 49;
    public static final int CMD_GET_SENSOR_IMMEDIATE = 50;
    public static final int CMD_VERIFY_TAG = 61;
    public static final int CMD_GLOBALSCROLL_TAG = 62;
    public static final int CMD_INVENTORY = 64;
    public static final int CMD_WAKE_N = 65;
    public static final int CMD_DIRECTED_INV_LIST_CONTROL = 66;
    public static final int CMD_INV_DIAGNOSTIC_CONTROL = 78;
    public static final int CMD_PROGRAM_TAG = 80;
    public static final int CMD_ERASE_TAG = 81;
    public static final int CMD_KILL_TAG = 82;
    public static final int CMD_LOCK_TAG = 83;
    public static final int CMD_READ_TAG_DATA = 255;
    public static final int SUBCMD_PROGRAMMER_ENABLE = 0;
    public static final int SUBCMD_GENERAL_PURPOSE_OUTPUTS = 1;
    public static final int SUBCMD_GENERAL_PURPOSE_INPUTS = 2;
    public static final int SUBCMD_BIDIRECTIONAL_IO = 3;
    public static final int SUBCMD_MAXIMUM_HOP_TABLE_INDEX = 4;
    public static final int SUBCMD_READER_TYPE = 5;
    public static final int SUBCMD_LOCALIZATION = 6;
    public static final int SUBCMD_RADIO_TYPE = 7;
    public static final int SUBCMD_MINIMUM_FREQUENCY = 8;
    public static final int SUBCMD_MAXIMUM_FREQUENCY = 9;
    public static final int SUBCMD_HOP_STEP_SIZE = 10;
    public static final int SUBCMD_PLL_TYPE = 11;
    public static final int SUBCMD_MAXIMUM_RF_CHANNEL = 12;
    public static final int SUBCMD_MAXIMUM_RF_POWER = 13;
    public static final int SUBCMD_OPERATING_VOLTAGE = 14;
    public static final int SUBCMD_AVAILABLE_BAUD_RATES = 16;
    public static final int SUBCMD_MAX_DIRECTED_SORT_LIST = 17;
    public static final int SUBCMD_SUPPORTED_TAG_TYPES = 18;
    public static final int SUBCMD_HOP_INTERVAL = 19;
    public static final int SUBCMD_RF_CHANNEL = 21;
    public static final int SUBCMD_MAXIMUM_ANTENNA_NUMBER = 22;
    public static final int SUBCMD_SEND_MODULATION_CONT = 128;
    public static final int SUBCMD_SET_MOD_CONTROL_MANUAL = 129;
    public static final int SUBCMD_GET_MOD_CONTROL_MANUAL = 130;
    public static final int SUBCMD_SET_PA_BIAS = 131;
    public static final int SUBCMD_GET_PA_BIAS = 132;
    public static final int SUBCMD_SET_PA_VDD = 133;
    public static final int SUBCMD_GET_PA_VDD = 134;
    public static final int SUBCMD_SET_RECEIVER_GAIN = 135;
    public static final int SUBCMD_GET_RECEIVER_GAIN = 136;
    public static final int SUBCMD_SET_AIR_INTERFACE_BAUD = 137;
    public static final int SUBCMD_GET_AIR_INTERFACE_BAUD = 138;
    public static final int SUBCMD_SET_CAL_TABLE_ENTRY = 139;
    public static final int SUBCMD_GET_CAL_TABLE_ENTRY = 140;
    public static final int SUBCMD_SET_NV_RAW = 141;
    public static final int SUBCMD_GET_NV_RAW = 142;
    public static final int SUBCMD_RESTORE_DEFAULT_NV = 143;
    public static final int SUBCMD_RESTORE_DEFAULT_HOP = 144;
    public static final int SUBCMD_SET_HOP_TABLE_ENTRY = 145;
    public static final int SUBCMD_GET_HOP_TABLE_ENTRY = 146;
    public static final int SUBCMD_SET_READER_MANUF_INFO = 147;
    public static final int SUBCMD_SET_RADIO_MANUF_INFO = 148;
    public static final int SUBCMD_SET_RAW_RF_POWER = 149;
    public static final int SUBCMD_GET_RAW_RF_POWER = 150;
    public static final int SUBCMD_VOLTAGE_CALIBRATION = 151;
    public static final int SUBCMD_SET_DIAGNOSTIC_MODE = 161;
    public static final int SUBCMD_GET_DIAGNOSTIC_MODE = 162;
    public static final int SUBCMD_SEND_PRIMITIVE_ONE_SHOT = 163;
    public static final int SUBCMD_SET_RAW_MEMORY = 164;
    public static final int SUBCMD_GET_RAW_MEMORY = 165;
    public static final int SUBCMD_SET_PROGRAM_LENGTH = 166;
    public static final int SUBCMD_GET_PROGRAM_LENGTH = 167;
    public static final int SUBCMD_SET_PROGRAM_POWER_LEVELS = 168;
    public static final int SUBCMD_GET_PROGRAM_POWER_LEVELS = 169;
    public static final int SUBCMD_SET_DECODE_PARAMETERS = 170;
    public static final int SUBCMD_GET_DECODE_PARAMETERS = 171;
    public static final int SUBCMD_SET_DECODE_DIAG = 172;
    public static final int SUBCMD_SET_TREE_INV_METHOD = 173;
    public static final int SUBCMD_GET_TREE_INV_METHOD = 174;
    public static final int SUBCMD_SET_ETSI_LISTEN_CHANNEL = 175;
    public static final int SUBCMD_SET_MOD_FUDGE = 176;
    public static final int SUBCMD_GET_MOD_FUDGE = 177;
    public static final int SUBCMD_SET_SUSPENDERS_PARAMS = 178;
    public static final int SUBCMD_GET_SUSPENDERS_PARAMS = 179;
    public static final int SUBCMD_SET_PING_PARAMS = 180;
    public static final int SUBCMD_GET_PING_PARAMS = 181;
    public static final int SUBCMD_SET_ETSI_LISTEN_PARAMS = 182;
    public static final int SUBCMD_GET_ETSI_LISTEN_PARAMS = 183;
    public static final int RESPONSE_MESSAGE_OK = 0;
    public static final int RESPONSE_INVENTORY_START = 1;
    public static final int RESPONSE_INVENTORY_TAG = 2;
    public static final int RESPONSE_INVENTORY_END = 3;
    public static final int RESPONSE_LIST_DUMP_START = 4;
    public static final int RESPONSE_LIST_DUMP_RECORD = 5;
    public static final int RESPONSE_LIST_DUMP_END = 6;
    public static final int RESPONSE_DIAGNOSTIC_START = 7;
    public static final int RESPONSE_DIAGNOSTIC_DATA = 8;
    public static final int RESPONSE_DIAGNOSTIC_END = 9;
    public static final int RESPONSE_UPLOAD_OK = 10;
    public static final int RESPONSE_UPLOAD_END = 11;
    public static final int RESPONSE_SUSPEND = 12;
    public static final int RESPONSE_RESUME = 13;
    public static final int RESPONSE_MESSAGE_OK_G2 = 16;
    public static final int RESPONSE_UNKNOWN_LENGTH = 129;
    public static final int RESPONSE_UNKNOWN_VALUE = 130;
    public static final int RESPONSE_UNKNOWN_COMMAND = 131;
    public static final int RESPONSE_UNKNOWN_TAG_COMMAND = 132;
    public static final int RESPONSE_OVERFLOW_ERROR = 133;
    public static final int RESPONSE_NO_TAG = 134;
    public static final int RESPONSE_ERASE_FAILED = 135;
    public static final int RESPONSE_PROGRAM_FAILED = 136;
    public static final int RESPONSE_TAG_LOCKED = 137;
    public static final int RESPONSE_KILL_FAILED = 138;
    public static final int RESPONSE_LOCK_FAILED = 139;
    public static final int RESPONSE_DATA_SIZE_MISMATCH = 140;
    public static final int RESPONSE_HARDWARE_ERROR = 141;
    public static final int RESPONSE_LIST_FULL = 142;
    public static final int RESPONSE_UPLOAD_LINE_ERROR = 143;
    public static final int RESPONSE_UPLOAD_INVALID = 144;
    public static final int RESPONSE_UPLOAD_CRC_ERROR = 145;
    public static final int RESPONSE_LOCK_CRC_ERROR = 146;
    public static final int RESPONSE_TAG_LOST = 147;
    public static final int RESPONSE_INVALID_KILL_CODE = 148;
    public static final int TAGDECODE_GOOD_ID = 0;
    public static final int TAGDECODE_NO_TAG = 1;
    public static final int TAGDECODE_COLLISION = 2;
    public static final int TAGDECODE_CRC_ERROR = 3;
    public static final String[] RESPONSE_STRINGS = new String[]{"Message OK", "Inventory Start", "Inventory Tag", "Inventory End", "List Dump Start", "List Dump Record", "List Dump End", "Diagnostic Start", "Diagnostic Data", "Diagnostic End", "Upload OK", "Upload End", "Reader Entering Suspend State", "Reader Resuming From Suspend State", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Unknown Message Length", "Unknown Value", "Unknown Command", "Unknown Tag Command", "Overflow Error", "No Tag Found", "Erase Failed", "Program Failed", "Tag is Locked", "Kill Failed", "Lock Failed", "Data Memory Size Mismatch", "Hardware Error", "List Full", "Upload Line Contains Error", "Invalid Bootloader Command", "Upload Program Memory CRC Error", "Lock Failed CRC Check", "Previous Found Tag Is Lost While Programming", "Lock Failed - Kill Code Would Not Verify"};
    public static int SESSION_ID;
    public byte[] commandBuffer = new byte[100];
    public byte[] unpackedCommandBuffer;
    public int commandLength;
    public byte[] replyBuffer = new byte[100];
    public byte[] replyBufferRaw = new byte[100];
    public int replyLength;
    public int replyLengthRaw;
    public int replyCommType;
    public String replyCommTypeHexString;
    public String replyCommTypeMessage;
    public int replyValueInt;
    public int[] replyValueIntArray;
    public byte[] replyValueHexArray;
    public TagTable tagTable;
    public int status;
    public int sessionID;
    private int DLE_STATE;
    public boolean isCyclops = false;

    public AlienDLEObject() {
        this.reset();
    }

    private void reset() {
        this.commandLength = 0;
        this.replyLength = 0;
        this.replyLengthRaw = 0;
        this.replyCommType = 0;
        this.replyCommTypeHexString = "";
        this.replyCommTypeMessage = "";
        this.replyValueInt = 0;
        this.replyValueIntArray = new int[255];
        this.replyValueHexArray = new byte[255];
        this.status = 1;
        this.DLE_STATE = 0;
        this.sessionID = 0;
        this.tagTable = new TagTable(false);
    }

    public void prepareGenericCommand(int commandID) {
        this.prepareStartCommand(commandID);
        this.prepareEndCommand();
    }

    public void prepareGenericCommand(int commandID, int commandParam1) {
        this.prepareStartCommand(commandID);
        this.commandBuffer[this.commandLength++] = (byte)commandParam1;
        this.prepareEndCommand();
    }

    public void prepareGenericCommand(int commandID, int commandParam1, int commandParam2) {
        this.prepareStartCommand(commandID);
        this.commandBuffer[this.commandLength++] = (byte)commandParam1;
        this.commandBuffer[this.commandLength++] = (byte)commandParam2;
        this.prepareEndCommand();
    }

    public void prepareGenericCommand(int commandID, int commandParam1, int commandParam2, int commandParam3) {
        this.prepareStartCommand(commandID);
        this.commandBuffer[this.commandLength++] = (byte)commandParam1;
        this.commandBuffer[this.commandLength++] = (byte)commandParam2;
        this.commandBuffer[this.commandLength++] = (byte)commandParam3;
        this.prepareEndCommand();
    }

    public void prepareGenericCommand(int commandID, int commandParam1, int commandParam2, int commandParam3, int commandParam4) {
        this.prepareStartCommand(commandID);
        this.commandBuffer[this.commandLength++] = (byte)commandParam1;
        this.commandBuffer[this.commandLength++] = (byte)commandParam2;
        this.commandBuffer[this.commandLength++] = (byte)commandParam3;
        this.commandBuffer[this.commandLength++] = (byte)commandParam4;
        this.prepareEndCommand();
    }

    public void prepareGenericCommand(int commandID, int[] commandParamInts) {
        this.prepareStartCommand(commandID);

        for(int i = 0; i < commandParamInts.length; ++i) {
            this.commandBuffer[this.commandLength++] = (byte)commandParamInts[i];
        }

        this.prepareEndCommand();
    }

    public void prepareGenericCommand(int commandID, byte[] commandParamBytes) {
        this.prepareStartCommand(commandID);

        for(int i = 0; i < commandParamBytes.length; ++i) {
            this.commandBuffer[this.commandLength++] = commandParamBytes[i];
        }

        this.prepareEndCommand();
    }

    public void prepareGenericCommand(byte[] commandByteSequence) {
        this.prepareStartCommand(commandByteSequence[0]);

        for(int i = 1; i < commandByteSequence.length; ++i) {
            this.commandBuffer[this.commandLength++] = commandByteSequence[i];
        }

        this.prepareEndCommand();
    }

    private void prepareStartCommand(int command) {
        this.reset();
        ++SESSION_ID;
        if (SESSION_ID < 20) {
            SESSION_ID = 20;
        }

        if (SESSION_ID > 255) {
            SESSION_ID = 20;
        }

        this.sessionID = SESSION_ID;
        this.commandBuffer[this.commandLength++] = (byte)SESSION_ID;
        this.commandBuffer[this.commandLength++] = 0;
        this.commandBuffer[this.commandLength++] = (byte)command;
    }

    private void prepareEndCommand() {
        this.status = 2;
        this.calculateCheckSum();
        this.packCommand();
    }

    private void packCommand() {
        this.unpackedCommandBuffer = new byte[100];

        int unpackedLength;
        for(unpackedLength = 0; unpackedLength < this.commandLength; ++unpackedLength) {
            this.unpackedCommandBuffer[unpackedLength] = this.commandBuffer[unpackedLength];
        }

        unpackedLength = this.commandLength;
        this.commandLength = 0;
        this.commandBuffer[this.commandLength++] = 16;
        this.commandBuffer[this.commandLength++] = 1;

        for(int i = 0; i < unpackedLength; ++i) {
            this.commandBuffer[this.commandLength++] = this.unpackedCommandBuffer[i];
            if (this.unpackedCommandBuffer[i] == 16) {
                this.commandBuffer[this.commandLength++] = this.unpackedCommandBuffer[i];
            }
        }

        this.commandBuffer[this.commandLength++] = 16;
        this.commandBuffer[this.commandLength++] = 2;
    }

    public int addReply(int b) {
        this.status = 3;
        this.replyBufferRaw[this.replyLengthRaw++] = (byte)b;
        switch(this.DLE_STATE) {
            case 0:
                this.replyLength = 0;
                this.replyLengthRaw = 0;
                if (b == 16) {
                    this.DLE_STATE = 1;
                }
                break;
            case 1:
                if (b == 1) {
                    this.DLE_STATE = 2;
                } else {
                    this.DLE_STATE = 0;
                }
                break;
            case 2:
                if (b == 16) {
                    this.DLE_STATE = 3;
                } else {
                    this.replyBuffer[this.replyLength++] = (byte)b;
                }
                break;
            case 3:
                if (b == 16) {
                    this.replyBuffer[this.replyLength++] = (byte)b;
                    this.DLE_STATE = 2;
                } else if (b == 2) {
                    this.processResponse();
                    this.DLE_STATE = 0;
                } else {
                    this.DLE_STATE = 0;
                }
        }

        if (this.replyLength >= 98) {
            this.status = 11;
            this.DLE_STATE = 0;
        }

        return this.status;
    }

    private void processResponse() {
        if ((this.replyBuffer[0] & 255) != (this.sessionID & 255) && this.replyBuffer[0] != 0) {
            System.err.println("Error: Session ID Wrong in Received Packet: expected " + this.sessionID + ", received " + this.replyBuffer[0]);
            this.status = 3;
        } else {
            this.status = 10;
            int numResponseBytes = 0;
            int commandCode = this.replyBuffer[2];
            byte subcommandCode;
            switch(commandCode) {
                case 0:
                    this.validateResponse(5);
                    break;
                case 1:
                case 3:
                case 4:
                case 5:
                case 7:
                case 9:
                case 11:
                case 14:
                case 16:
                case 20:
                case 22:
                case 24:
                case 26:
                case 28:
                case 30:
                case 33:
                case 34:
                case 35:
                case 65:
                case 80:
                case 81:
                case 82:
                case 83:
                case 209:
                case 210:
                    this.validateResponse(0);
                    break;
                case 2:
                case 6:
                case 8:
                case 10:
                case 23:
                case 27:
                case 31:
                    if (this.validateResponse(1)) {
                        this.replyValueInt = this.byteToInt(this.replyValueHexArray[0]);
                    }
                    break;
                case 12:
                    this.validateResponse(2);
                    break;
                case 13:
                    subcommandCode = this.unpackedCommandBuffer[3];
                    switch(subcommandCode) {
                        case 0:
                            this.validateResponse(3);
                            return;
                        case 1:
                            this.validateResponse(0);
                            return;
                        default:
                            return;
                    }
                case 15:
                    if (this.commandBuffer[5] == 0) {
                        this.validateResponse(1);
                        this.replyValueInt = this.byteToInt(this.replyValueHexArray[0]);
                    } else {
                        this.validateResponse(2);
                        this.replyValueInt = this.byteToInt(this.replyValueHexArray[0]) * 256 + this.byteToInt(this.replyValueHexArray[1]);
                    }
                    break;
                case 17:
                    subcommandCode = this.unpackedCommandBuffer[3];
                    switch(subcommandCode) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 11:
                        case 12:
                        case 16:
                        case 18:
                        case 21:
                        case 22:
                            if (this.validateResponse(1)) {
                                this.replyValueInt = this.byteToInt(this.replyValueHexArray[0]);
                            }

                            return;
                        case 8:
                        case 9:
                        case 10:
                        case 13:
                        case 19:
                            if (this.validateResponse(2)) {
                                this.replyValueInt = this.byteToInt(this.replyValueHexArray[0]) * 256 + this.byteToInt(this.replyValueHexArray[1]);
                            }

                            return;
                        case 14:
                        case 15:
                        case 17:
                        case 20:
                        default:
                            this.validateResponse(-1);
                            return;
                    }
                case 18:
                    this.validateResponse(18);
                    break;
                case 19:
                    int subcommandNumb = this.unpackedCommandBuffer[3];
                    switch(subcommandNumb) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 16:
                        case 17:
                        case 18:
                        case 19:
                        case 21:
                        case 22:
                        case 128:
                        case 129:
                        case 131:
                        case 133:
                        case 135:
                        case 137:
                        case 139:
                        case 141:
                        case 143:
                        case 144:
                        case 145:
                        case 147:
                        case 148:
                        case 149:
                        case 161:
                        case 164:
                        case 166:
                        case 168:
                        case 170:
                            this.validateResponse(0);
                            return;
                        case 130:
                        case 136:
                        case 138:
                        case 142:
                        case 150:
                        case 171:
                            if (this.validateResponse(1)) {
                                this.replyValueInt = this.byteToInt(this.replyValueHexArray[0]);
                            }

                            return;
                        case 132:
                            this.validateResponse(3);
                            return;
                        case 134:
                        case 146:
                        case 169:
                            this.validateResponse(2);
                            return;
                        case 140:
                        case 167:
                            if (this.validateResponse(2)) {
                                this.replyValueInt = this.byteToInt(this.replyValueHexArray[0]) * 256 + this.byteToInt(this.replyValueHexArray[1]);
                            }

                            return;
                        case 162:
                        case 163:
                        default:
                            this.validateResponse(-1);
                            return;
                        case 165:
                            if (this.validateCommType()) {
                                numResponseBytes = this.replyBuffer[4];
                                this.validateResponse(numResponseBytes);
                            }

                            return;
                    }
                case 21:
                    numResponseBytes = 1;
                    int maskBitLength = this.replyBuffer[4];
                    if (maskBitLength > 0) {
                        ++numResponseBytes;
                        numResponseBytes += (maskBitLength + 7) / 8;
                    }

                    this.validateResponse(numResponseBytes);
                    break;
                case 25:
                    this.validateResponse(3);
                    break;
                case 29:
                    this.validateResponse(8);
                    break;
                case 36:
                    if (this.validateCommType()) {
                        if (this.replyBuffer[4] == 0) {
                            this.extractTagData();
                        } else {
                            this.validateResponse(2);
                        }
                    }
                    break;
                case 61:
                case 62:
                    if (this.validateCommType()) {
                        int tagDecodeStatus = this.byteToInt(this.replyBuffer[4]);
                        if (tagDecodeStatus != 0 && tagDecodeStatus != 3) {
                            this.validateResponse(2);
                        } else {
                            int tagLength = this.byteToInt(this.replyBuffer[6]);
                            this.replyValueInt = tagLength;
                            numResponseBytes = 3 + tagLength;
                            this.validateResponse(numResponseBytes);
                        }
                    }
                    break;
                case 64:
                    if (this.validateCommType() && 2 == this.replyBuffer[3] && (this.replyBuffer[4] == 0 || 16 == this.replyBuffer[4])) {
                        this.extractTagData();
                    }
                    break;
                default:
                    this.validateResponse(-1);
            }

        }
    }

    private boolean validateResponse(int expectedResponseBytes) {
        if (!this.validateCommType()) {
            return false;
        } else {
            int numHeaderBytes = 4;
            int extraBaggageBytes = numHeaderBytes + 2;
            if (expectedResponseBytes == -1) {
                expectedResponseBytes = this.replyLength - extraBaggageBytes;
            }

            if (this.replyLength != expectedResponseBytes + extraBaggageBytes) {
                this.status = 11;
                this.replyCommType = 129;
                this.replyCommTypeHexString = Converters.toHexString(this.replyCommType);

                try {
                    this.replyCommTypeMessage = RESPONSE_STRINGS[this.replyCommType];
                } catch (Exception var5) {
                    this.replyCommTypeMessage = "(code=" + this.replyCommTypeHexString + ")";
                }

                System.out.println("\nResponse Bytes wrong: Received " + this.replyLength + ", Expected " + (expectedResponseBytes + extraBaggageBytes));
                return false;
            } else {
                this.replyValueHexArray = new byte[expectedResponseBytes];
                this.replyValueIntArray = new int[expectedResponseBytes];

                for(int i = 0; i < expectedResponseBytes; ++i) {
                    this.replyValueHexArray[i] = this.replyBuffer[i + numHeaderBytes];
                    this.replyValueIntArray[i] = this.byteToInt(this.replyValueHexArray[i]);
                }

                return true;
            }
        }
    }

    private boolean validateCommType() {
        this.replyCommType = this.byteToInt(this.replyBuffer[3]);
        this.replyCommTypeHexString = Converters.toHexString(this.replyCommType);

        try {
            this.replyCommTypeMessage = RESPONSE_STRINGS[this.replyCommType];
        } catch (Exception var2) {
            this.replyCommTypeMessage = "(code=" + this.replyCommTypeHexString + ")";
        }

        if (this.byteToInt(this.replyBuffer[3]) >= 128) {
            this.status = 11;
            return false;
        } else {
            return true;
        }
    }

    private void extractTagData() {
        int tagLength;
        Tag tag;
        String tagID;
        if (this.isCyclops) {
            tagLength = this.byteToInt(this.replyBuffer[8]);
            this.replyValueInt = tagLength;
            this.validateResponse(5 + tagLength);
            tagID = Converters.toHexString(this.replyValueHexArray, 5, tagLength, " ", true);
            tag = new Tag(tagID);
            tag.setTransmitAntenna(this.byteToInt(this.replyBuffer[5]));
            tag.setReceiveAntenna(this.byteToInt(this.replyBuffer[6]));
            tag.setProtocol(this.byteToInt(this.replyBuffer[7]) & 15);
        } else {
            tagLength = this.byteToInt(this.replyBuffer[6]);
            this.replyValueInt = tagLength;
            this.validateResponse(3 + tagLength);
            tagID = Converters.toHexString(this.replyValueHexArray, 3, tagLength, " ", true);
            tag = new Tag(tagID);
            tag.setAntenna(this.byteToInt(this.replyBuffer[5]));
        }

        long currentTime = System.currentTimeMillis();
        tag.setDiscoverTime(currentTime);
        tag.setRenewTime(currentTime);
        tag.setHostDiscoverTime(currentTime);
        tag.setHostRenewTime(currentTime);
        this.tagTable.addTag(tag);
    }

    private void calculateCheckSum() {
        int polynomial = 4129;
        int value = 65535;
        byte[] bitArray = new byte[8];

        int i;
        int b;
        for(i = 0; i < this.commandLength; ++i) {
            b = this.commandBuffer[i];

            int j;
            for(j = 0; j < 8; ++j) {
                bitArray[j] = 0;
                if ((b & 128) != 0) {
                    bitArray[j] = 1;
                }

                b <<= 1;
            }

            for(j = 0; j < 8; ++j) {
                int pop = value & '\u8000';
                value = (value & 32767) << 1;
                if (bitArray[j] == 1) {
                    value |= 1;
                }

                if (pop != 0) {
                    value ^= polynomial;
                }
            }
        }

        for(i = 0; i < 16; ++i) {
            b = value & '\u8000';
            value = (value & 32767) << 1;
            if (b != 0) {
                value ^= polynomial;
            }
        }

        this.commandBuffer[this.commandLength++] = (byte)(value >> 8 & 255);
        this.commandBuffer[this.commandLength++] = (byte)(value & 255);
    }

    private int byteToInt(byte b) {
        return b & 255;
    }
}
