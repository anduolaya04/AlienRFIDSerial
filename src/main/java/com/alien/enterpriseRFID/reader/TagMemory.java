package com.alien.enterpriseRFID.reader;

public class TagMemory {
    private int bank;
    private int ptr;
    private int len;
    private String data;

    public TagMemory(int bank, int ptr, int len, String data) {
        this.bank = 0;
        this.ptr = 0;
        this.len = 0;
        this.data = "";
        this.bank = bank;
        this.ptr = ptr;
        this.len = len;
        this.data = data;
    }

    public TagMemory(int bank, int ptr, int len) {
        this(bank, ptr, len, "");
    }

    public TagMemory(String g2MaskStr) {
        this.bank = 0;
        this.ptr = 0;
        this.len = 0;
        this.data = "";
        if (g2MaskStr != null && !g2MaskStr.equals("0") && !g2MaskStr.equals("00") && !g2MaskStr.equals("-1") && !g2MaskStr.equals("All")) {
            String[] parts = g2MaskStr.split("[, ]+", 4);
            if (parts != null && parts.length == 4) {
                this.bank = Integer.parseInt(parts[0]);
                this.ptr = Integer.parseInt(parts[1]);
                this.len = Integer.parseInt(parts[2]);
                this.data = parts[3];
            }
        } else {
            this.bank = 0;
            this.ptr = 0;
            this.len = 0;
            this.data = "";
        }

    }

    public TagMemory() {
        this(0, 0, 0, "");
    }

    public int getBank() {
        return this.bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public int getPtr() {
        return this.ptr;
    }

    public void setPtr(int ptr) {
        this.ptr = ptr;
    }

    public int getLen() {
        return this.len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String toString() {
        return this.bank + ", " + this.ptr + ", " + this.len + ", " + this.data;
    }
}
