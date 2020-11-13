package com.alien.enterpriseRFID.discovery;

public interface DiscoveryListener {
    void readerAdded(DiscoveryItem var1);

    void readerRenewed(DiscoveryItem var1);

    void readerRemoved(DiscoveryItem var1);
}
