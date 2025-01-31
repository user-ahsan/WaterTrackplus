package com.ahsan.watertrackplus.data;

public class WaterIntakeRecord {
    private long id;
    private int amount;
    private long timestamp;

    public WaterIntakeRecord(long id, int amount, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        return new java.text.SimpleDateFormat("HH:mm")
            .format(new java.util.Date(timestamp));
    }
} 