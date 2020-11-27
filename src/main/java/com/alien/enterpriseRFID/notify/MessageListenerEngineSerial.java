package com.alien.enterpriseRFID.notify;

import com.alien.enterpriseRFID.externalio.ExternalIOUtil;
import com.alien.enterpriseRFID.tags.TagUtil;
import com.alien.enterpriseRFID.util.Converters;
import com.alien.enterpriseRFID.util.SerialManager;
import com.alien.enterpriseRFID.util.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

public class MessageListenerEngineSerial extends Thread implements MessageListener {

    private SerialManager serialManager;
    private InputStream inputStream;
    private OutputStream outputStream;
    private MessageListenerServiceSerial listenerService;
    private MessageListener listener;
    private MessageListener messageListener;
    private Message messageTemplate;
    private boolean isDebug;
    private boolean stillRunning = true;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    protected int engineID;

    public MessageListenerEngineSerial(SerialManager serialManager, MessageListenerServiceSerial listenerService, boolean isDebug, int engineID) {

        super ("MessageListenerEngineSerial");
        this.serialManager = serialManager;
        this.isDebug = isDebug;
        this.engineID = engineID;
        this.listenerService = listenerService;
        if (listenerService != null) {
            this.listener = listenerService;
        } else {
            this.listener = this;
        }
    }

    public void halt() {
        stillRunning = false;
        if (serialManager != null && serialManager.getSerialPort() != null ) {
            serialManager.close();
        }
    }

    @Override
    public void run() {
        messageTemplate = new Message();
        if (isDebug) {
            System.out.println("\nMessage Listener " + this.engineID + ": Reader Connected from " + serialManager.getSerialPort());
        }

        InputStreamReader inputStreamReader = null;
        OutputStream outputStream = null;

        ErrorMessage errorMessage;

        try {
            inputStreamReader = new InputStreamReader( serialManager.getInputStream());
            outputStream = serialManager.getOutputStream();
        } catch (Exception e) {
            errorMessage = new ErrorMessage();
            errorMessage.setReason(e.getMessage());
            this.deliverMessageToListener(errorMessage);
        }

        if (inputStreamReader != null && outputStream != null) {
            readMessages( inputStreamReader, outputStream );
        }

    }

    private void readMessages(InputStreamReader inputStreamReader, OutputStream outputStream) {
        if (inputStreamReader != null && outputStream != null) {
            byte[] testBytes = new byte[1];
            StringBuffer stringBuffer = new StringBuffer();

            do {
                try {
                    byte nextByte = (byte)inputStreamReader.read();
                    if (nextByte > 0) {
                        stringBuffer.append((char)nextByte);
                    } else {
                        if (nextByte != 0) {
                            if (this.isDebug && stringBuffer.length() > 0) {
                                System.out.println("\nMessage Listener " + this.engineID + ": Read = \n" + Converters.toAsciiString(stringBuffer.toString()) + " ...TIMEOUT");
                            }

                            return;
                        }

                        String messageText = stringBuffer.toString().trim();
                        if (this.isDebug) {
                            System.out.println("\nMessage Listener " + this.engineID + ": Read = \n" + Converters.toAsciiString(messageText) + " ...DONE");
                        }

                        Message message;
                        if (messageText.startsWith("<")) {
                            message = this.decodeMessageXML(messageText);
                        } else {
                            message = this.decodeMessageText(messageText);
                        }

                        this.deliverMessageToListener(message);
                        stringBuffer = new StringBuffer();
                    }
                } catch (IOException var8) {
                    Message errorMessage = new ErrorMessage();
                    errorMessage.setReason(var8.getMessage());
                    errorMessage.setRawData(stringBuffer.toString());
                    this.deliverMessageToListener(errorMessage);
                }
            } while(this.stillRunning);

        }
    }

    private boolean deliverMessageToListener(Message message) {
        if (message != null && this.listener != null) {
            try {
                this.listener.messageReceived(message);
                return true;
            } catch (Exception var3) {
                if (this.isDebug) {
                    System.out.println("\nMessage Listener " + this.engineID + ": Caught unchecked exception in messageReceived() callback:\n" + var3.getMessage());
                }

                return false;
            }
        } else {
            return true;
        }
    }

    public Message decodeMessageXML(String messageXML) {
        String headerText = null;
        this.messageTemplate.setRawData(messageXML);
        XMLReader xmlReader = new XMLReader(messageXML);
        Hashtable hashtable = null;

        ErrorMessage errorMessage;
        try {
            hashtable = xmlReader.readXML();
        } catch (ParseException var8) {
            errorMessage = new ErrorMessage();
            errorMessage.setReason(var8.getMessage());
            errorMessage.setRawData(messageXML);
            return errorMessage;
        }

        headerText = (String)hashtable.get("ALIEN-RFID-READER-AUTO-NOTIFICATION");
        if (headerText == null) {
            headerText = (String)hashtable.get("ALIEN-RFID-READER-IO-STREAM");
            if (headerText != null) {
                this.messageTemplate.setReason("IO STREAM");
            } else {
                headerText = (String)hashtable.get("ALIEN-RFID-READER-TAG-STREAM");
                if (headerText != null) {
                    this.messageTemplate.setReason("TAG STREAM");
                } else {
                    headerText = messageXML;
                }
            }
        }

        try {
            this.decodeMessageXMLBody(this.messageTemplate, headerText);
            return this.messageTemplate;
        } catch (AlienMessageFormatException var7) {
            errorMessage = new ErrorMessage();
            errorMessage.setReason(var7.getMessage());
            errorMessage.setRawData(messageXML);
            return errorMessage;
        }
    }

    public void decodeMessageXMLBody(Message message, String messageXML) throws AlienMessageFormatException {
        XMLReader xmlReader = new XMLReader(messageXML);

        try {
            Hashtable table = xmlReader.readXML();
            String str = (String)table.get("READERNAME");
            if (str != null) {
                message.setReaderName(str);
            }

            str = (String)table.get("READERTYPE");
            if (str != null) {
                message.setReaderType(str);
            }

            str = (String)table.get("IPADDRESS");
            if (str != null) {
                message.setReaderIPAddress(str);
            }

            str = (String)table.get("MACADDRESS");
            if (str != null) {
                message.setReaderMACAddress(str);
            }

            str = (String)table.get("COMMANDPORT");
            if (str != null) {
                message.setReaderCommandPort(new Integer(str));
            }

            str = (String)table.get("HOSTNAME");
            if (str != null) {
                message.setReaderHostname(str);
            }

            str = (String)table.get("REASON");
            if (str != null) {
                message.setReason(str);
            }

            try {
                str = (String)table.get("STARTTRIGGERLINES");
                if (str != null) {
                    message.setStartTriggerLines(new Integer(str));
                }

                str = (String)table.get("STOPTRIGGERLINES");
                if (str != null) {
                    message.setStopTriggerLines(new Integer(str));
                }
            } catch (NumberFormatException var7) {
                message.setStartTriggerLines(0);
                message.setStopTriggerLines(0);
            }

            str = (String)table.get("TIME");
            if (str != null) {
                message.setDate(this.dateTimeFormat.parse(str));
            }

            str = (String)table.get("ALIEN-RFID-TAG-LIST");
            if (str != null) {
                message.setTagList( TagUtil.decodeXMLTagList(str));
            } else {
                message.setTagList(TagUtil.decodeXMLTagList(messageXML));
            }

            str = (String)table.get("ALIEN-RFID-IO-LIST");
            if (str != null) {
                message.setIOList( ExternalIOUtil.decodeXMLIOList(str));
            } else {
                message.setIOList(ExternalIOUtil.decodeXMLIOList(messageXML));
            }

        } catch (NumberFormatException var8) {
            throw new AlienMessageFormatException("Message Listener " + this.engineID + ": Unable to parse a value. " + var8.getMessage());
        } catch (ParseException var9) {
            throw new AlienMessageFormatException(var9.getMessage());
        }
    }

    public Message decodeMessageText(String messageText) {
        this.messageTemplate.setRawData(messageText);
        if (messageText.startsWith("#Alien RFID Reader Tag Stream")) {
            this.messageTemplate.setReason("TAG STREAM");
        } else if (messageText.startsWith("#Alien RFID Reader IO Stream")) {
            this.messageTemplate.setReason("IO STREAM");
        }

        String str = this.extractTextMessageData(messageText, "#ReaderName: ");
        if (str != null) {
            this.messageTemplate.setReaderName(str);
        }

        str = this.extractTextMessageData(messageText, "#ReaderType: ");
        if (str != null) {
            this.messageTemplate.setReaderType(str);
        }

        str = this.extractTextMessageData(messageText, "#IPAddress: ");
        if (str != null) {
            this.messageTemplate.setReaderIPAddress(str);
        }

        str = this.extractTextMessageData(messageText, "#CommandPort: ");
        if (str != null) {
            this.messageTemplate.setReaderCommandPort(new Integer(str));
        }

        str = this.extractTextMessageData(messageText, "MACAddress: ");
        if (str != null) {
            this.messageTemplate.setReaderMACAddress(str);
        }

        str = this.extractTextMessageData(messageText, "Hostname: ");
        if (str != null) {
            this.messageTemplate.setReaderHostname(str);
        }

        str = this.extractTextMessageData(messageText, "#Reason: ");
        if (str != null) {
            this.messageTemplate.setReason(str);
        }

        try {
            str = this.extractTextMessageData(messageText, "#StartTriggerLines: ");
            if (str != null) {
                this.messageTemplate.setStartTriggerLines(new Integer(str));
            }

            str = this.extractTextMessageData(messageText, "#StopTriggerLines: ");
            if (str != null) {
                this.messageTemplate.setStopTriggerLines(new Integer(str));
            }
        } catch (NumberFormatException var8) {
            this.messageTemplate.setStartTriggerLines(0);
            this.messageTemplate.setStopTriggerLines(0);
        }

        str = this.extractTextMessageData(messageText, "#Time: ");
        if (str != null) {
            try {
                this.messageTemplate.setDate(this.dateTimeFormat.parse(str));
            } catch (ParseException var7) {
                Message errorMessage = new ErrorMessage();
                errorMessage.setReaderIPAddress(this.messageTemplate.getReaderIPAddress());
                errorMessage.setReason(var7.getMessage());
                errorMessage.setRawData(messageText);
                return errorMessage;
            }
        }

        int startPos = messageText.indexOf("#StopTriggerLines:");
        if (startPos == -1) {
            startPos = messageText.indexOf("#Reason:");
            if (startPos == -1) {
                startPos = 0;
            }
        }

        if (startPos > 0) {
            int endOfLinePos = messageText.indexOf("\r\n", startPos);
            if (endOfLinePos > 0) {
                startPos += 2;
            }
        }

        int endPos = messageText.indexOf("IO:", startPos);
        if (endPos == -1) {
            endPos = messageText.indexOf("#End of Notification Message");
            if (endPos == -1) {
                endPos = messageText.length();
            }
        }

        str = messageText.substring(startPos, endPos);
        if (this.listenerService.isCustomTagList()) {
            this.messageTemplate.setTagList(TagUtil.decodeCustomTagList(str));
        } else {
            this.messageTemplate.setTagList(TagUtil.decodeTagList(str));
        }

        startPos = endPos;
        endPos = messageText.indexOf("#End of Notification Message", endPos);
        if (endPos == -1) {
            endPos = messageText.length();
        }

        str = messageText.substring(startPos, endPos);
        this.messageTemplate.setIOList(ExternalIOUtil.decodeIOList(str));
        return this.messageTemplate;
    }

    private String extractTextMessageData(String messageText, String dataHeader) {
        String result = null;
        int startPos = messageText.indexOf(dataHeader);
        if (startPos >= 0) {
            startPos += dataHeader.length();
            int endPos = messageText.indexOf("\r\n", startPos);
            if (endPos > startPos) {
                result = messageText.substring(startPos, endPos);
            }
        }

        return result;
    }

    @Override
    public void messageReceived(Message var1) {

    }
}
