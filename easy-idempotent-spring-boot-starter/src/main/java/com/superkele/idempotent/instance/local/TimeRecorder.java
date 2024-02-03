package com.superkele.idempotent.instance.local;


/**
 * 时间记录类
 */
public class TimeRecorder {

    private String value;
    /**
     * 上次的时间戳
     */
    private long lastTimeStamp;

    /**
     * 过期时间
     */
    private long interval;

    public long overdueTime() {
        return lastTimeStamp + interval;
    }

    public boolean isOverdue() {
        return System.currentTimeMillis() > overdueTime();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    public void setLastTimeStamp(long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public TimeRecorder(String value, long lastTimeStamp, long interval) {
        this.value = value;
        this.lastTimeStamp = lastTimeStamp;
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "TimeRecorder{" +
                "value='" + value + '\'' +
                ", lastTimeStamp=" + lastTimeStamp +
                ", interval=" + interval +
                '}';
    }
}
