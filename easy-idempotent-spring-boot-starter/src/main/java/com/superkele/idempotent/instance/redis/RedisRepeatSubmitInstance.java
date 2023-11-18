package com.superkele.idempotent.instance.redis;

import cn.hutool.extra.spring.SpringUtil;
import com.superkele.idempotent.core.RepeatSubmit;
import com.superkele.idempotent.exception.RedisDurableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisRepeatSubmitInstance implements RepeatSubmit {

    private static RedisTemplate client;
    private static Logger logger = LoggerFactory.getLogger(RedisRepeatSubmitInstance.class);

    static {
        try {
            client = SpringUtil.getBean(StringRedisTemplate.class);
        } catch (Exception e) {
            logger.error("[EASY-IDEMPOTENT]使用REDIS持久化模式，请先配置REDIS");
            throw new RedisDurableException();
        }
    }

    private String key;

    public RedisRepeatSubmitInstance(String prefix) {
        this.key = prefix;
    }

    @Override
    public void setValue(String key, String value, long interval) {
        client.opsForValue().set(key, value, interval, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getValue() {
        return (String) client.opsForValue().get(getKey());
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
        Boolean b = client.opsForValue().setIfAbsent(getKey(), "", interval, TimeUnit.MILLISECONDS);
        return b != null && b;
    }

    @Override
    public void beforeHandle(long interval) {
        // ....
    }


    @Override
    public void exHandle() {
        clean();
    }

    @Override
    public void afterHandler(Boolean flag) {
        //...
        if (flag) {
            clean();
        }
    }

    @Override
    public void clean() {
        client.delete(getKey());
    }
}
