package com.alien.enterpriseRFID.tags;

public interface TagTableListener {

    void tagAdded(Tag var1);

    void tagRenewed(Tag var1);

    void tagRemoved(Tag var1);
}
