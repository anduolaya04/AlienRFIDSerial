package com.alien.enterpriseRFID.util;

import java.util.Enumeration;
import java.util.Hashtable;

public class XMLWriter {
    private String lineTerminator;
    private Hashtable hashtable;
    private String topLevelTag;
    private int indentSize;

    public XMLWriter(Hashtable hashtable) {
        this(hashtable, (String)null);
    }

    public XMLWriter(Hashtable hashtable, String topLevelTag) {
        this(hashtable, topLevelTag, "\r\n");
    }

    public XMLWriter(Hashtable hashtable, String topLevelTag, String lineTerminator) {
        this.setHashtable(hashtable);
        this.setLineTerminator(lineTerminator);
        this.setTopLevelTag(topLevelTag);
        this.setIndentSize(0);
    }

    public Hashtable getHashtable() {
        return this.hashtable;
    }

    public void setHashtable(Hashtable hashtable) {
        this.hashtable = hashtable;
    }

    public String getLineTerminator() {
        return this.lineTerminator;
    }

    public void setLineTerminator(String lineTerminator) {
        this.lineTerminator = lineTerminator;
    }

    public String getTopLevelTag() {
        return this.topLevelTag;
    }

    public void setTopLevelTag(String topLevelTag) {
        this.topLevelTag = topLevelTag;
    }

    public int getIndentSize() {
        return this.indentSize;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    public String writeXML() {
        if (this.getHashtable() == null) {
            return "";
        } else {
            if (this.getLineTerminator() == null) {
                this.setLineTerminator("");
            }

            StringBuffer buffer = new StringBuffer();
            if (this.getTopLevelTag() != null) {
                for(int i = 0; i < this.indentSize; ++i) {
                    buffer.append(" ");
                }

                buffer.append("<" + this.getTopLevelTag() + ">" + this.getLineTerminator());
                this.indentSize += 4;
            }

            Enumeration keys = this.getHashtable().keys();

            while(keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = (String)this.getHashtable().get(key);

                for(int i = 0; i < this.indentSize; ++i) {
                    buffer.append(" ");
                }

                buffer.append("<" + key + ">" + value + "</" + key + ">" + this.getLineTerminator());
            }

            if (this.getTopLevelTag() != null) {
                this.indentSize -= 4;

                for(int i = 0; i < this.indentSize; ++i) {
                    buffer.append(" ");
                }

                buffer.append("</" + this.getTopLevelTag() + ">" + this.getLineTerminator());
            }

            return buffer.toString();
        }
    }
}
