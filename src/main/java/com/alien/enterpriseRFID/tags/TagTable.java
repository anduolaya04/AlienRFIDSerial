package com.alien.enterpriseRFID.tags;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

public class TagTable extends Hashtable {
    public static final int FOREVER = -1;
    private int persistTime;
    private TagTableListener tagTableListener;
    private boolean combineAntennaData;

    public TagTable(boolean antennaCombine) {
        this.combineAntennaData = antennaCombine;
        this.clearTagList();
    }

    public TagTable() {
        this(true);
    }

    public int getPersistTime() {
        return this.persistTime;
    }

    public void setPersistTime(int persistTime) {
        this.persistTime = persistTime;
        Enumeration tagList = this.keys();

        while(tagList.hasMoreElements()) {
            String tagHash = (String)tagList.nextElement();
            Tag tag = (Tag)this.get(tagHash);
            tag.setPersistTime((long)persistTime);
        }

    }

    public void clearTagList() {
        this.clear();
    }

    public boolean addTag(Tag tag) {
        if (tag == null) {
            return false;
        } else {
            Tag existingTag = (Tag)this.get(this.getTagHash(tag));
            if (existingTag == null) {
                tag.setHostDiscoverTime(System.currentTimeMillis());
                tag.setPersistTime((long)this.persistTime);
                this.put(this.getTagHash(tag), tag);
                if (this.tagTableListener != null) {
                    this.tagTableListener.tagAdded(tag);
                }

                return true;
            } else {
                existingTag.updateTag(tag);
                if (this.tagTableListener != null) {
                    this.tagTableListener.tagRenewed(existingTag);
                }

                return false;
            }
        }
    }

    public boolean removeTag(Tag tag) {
        if (tag == null) {
            return false;
        } else {
            Object object = this.remove(this.getTagHash(tag));
            return object != null;
        }
    }

    public boolean removeOldTags() {
        if (this.size() <= 0) {
            return false;
        } else if (this.persistTime == -1) {
            return false;
        } else {
            boolean result = false;
            Enumeration tagList = this.keys();
            synchronized(tagList) {
                while(tagList.hasMoreElements()) {
                    String tagHash = (String)tagList.nextElement();
                    Tag tag = (Tag)this.get(tagHash);
                    if (tag != null) {
                        long timeToLive = tag.getTimeToLive();
                        if (timeToLive <= 0L) {
                            this.remove(tagHash);
                            if (this.tagTableListener != null) {
                                this.tagTableListener.tagRemoved(tag);
                            }

                            result = true;
                        }
                    }
                }

                return result;
            }
        }
    }

    public Tag[] getTagList() {
        if (this.size() <= 0) {
            return null;
        } else {
            Vector vector = new Vector();
            Enumeration tagList = this.keys();

            while(tagList.hasMoreElements()) {
                String tagHash = (String)tagList.nextElement();
                Tag tag = (Tag)this.get(tagHash);
                vector.addElement(tag);
            }

            Tag[] results = new Tag[vector.size()];
            vector.copyInto(results);
            return results;
        }
    }

    public TagTableListener getTagTableListener() {
        return this.tagTableListener;
    }

    public void setTagTableListener(TagTableListener tagTableListener) {
        this.tagTableListener = tagTableListener;
    }

    private String getTagHash(Tag tag) {
        return this.combineAntennaData ? tag.getTagID() : tag.getTagID() + "," + tag.getTransmitAntenna();
    }
}
