package com.superkele.idempotent.instance.local;

import com.superkele.idempotent.core.RepeatSubmit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalIdempotentInstance implements RepeatSubmit {

    private static final Map<String, TimeRecorder> CACHE_MAP = new ConcurrentHashMap<>();

    private String key;

    public LocalIdempotentInstance(String key) {
        this.key = key;
    }

    @Override
    public void setValue(String key, String value, long interval) {
        CACHE_MAP.put(key, new TimeRecorder(value, System.currentTimeMillis(), interval));
    }

    @Override
    public String getValue() {
        return CACHE_MAP.get(getKey()).getValue();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Boolean predict(long interval) {
        long nowTimeStamp = System.currentTimeMillis();
        if (CACHE_MAP.containsKey(getKey())) {
            TimeRecorder timeRecorder = CACHE_MAP.get(getKey());
            if (timeRecorder.isOverdue()) {
                timeRecorder.setLastTimeStamp(nowTimeStamp);
            } else {
                return false;
            }
        } else {  //如果没有的话
            CACHE_MAP.putIfAbsent(getKey(), new TimeRecorder("", nowTimeStamp, interval));
        }
        return true;
    }

    @Override
    public void beforeHandle(long interval) {
        //..
    }

    @Override
    public void exHandle() {
        clean();
    }

    @Override
    public void afterHandler(Boolean flag) {
        if (flag) {
            clean();
        }
    }

    @Override
    public void clean() {
        CACHE_MAP.remove(getKey());
        cleanOverdueKey();
        System.out.println(CACHE_MAP);
    }

    private void cleanOverdueKey() {
        if (CACHE_MAP.size() > 200) {
            CACHE_MAP.forEach((key, recorder) -> {
                if (recorder.isOverdue()) {
                    CACHE_MAP.remove(key);
                }
            });
        }
    }
}
