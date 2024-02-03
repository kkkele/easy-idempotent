package com.superkele.idempotent.instance.redis;

import cn.hutool.extra.spring.SpringUtil;
import com.superkele.idempotent.core.RepeatSubmit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisRepeatSubmitInstance implements RepeatSubmit {

    private static final RedisTemplate CLIENT = SpringUtil.getBean(StringRedisTemplate.class);

    private String key;

    public RedisRepeatSubmitInstance(String prefix) {
        this.key = prefix;
    }

    @Override
    public void setValue(String key, String value, long interval) {
        CLIENT.opsForValue().set(key, value, interval, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getValue() {
        return (String) CLIENT.opsForValue().get(getKey());
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
        Boolean b = CLIENT.opsForValue().setIfAbsent(getKey(), "", interval, TimeUnit.MILLISECONDS);
        return b != null && b;
    }

    @Override
    public void beforeHandle(long interval) {
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
        CLIENT.delete(getKey());
    }
}
