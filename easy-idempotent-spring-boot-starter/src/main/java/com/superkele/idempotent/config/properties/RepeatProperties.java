package com.superkele.idempotent.config.properties;

import com.superkele.idempotent.enums.StoreUsingType;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "idempotent")
public class RepeatProperties {

    /**
     * 唯一前缀
     */
    private String prefix = "";

    /**
     * 记录方式  LOCAL | REDIS
     */
    private StoreUsingType usingType = StoreUsingType.REDIS;

    /**
     * mq 默认配置
     */
    private Repeat mq = new Repeat("600s", "消息已经消费过了");

    /**
     * restApi 默认配置
     */
    private Repeat restApi = new Repeat("3s", "请求正在处理，请勿重复投递");


    /**
     * 是否开启日志打印
     */
    private Boolean enableLog = false;

    public String getPrefix() {
        return prefix;
    }

    public StoreUsingType getUsingType() {
        return usingType;
    }

    public Repeat getMq() {
        return mq;
    }

    public Repeat getRestApi() {
        return restApi;
    }

    public Boolean getEnableLog() {
        return enableLog;
    }

    public static class Repeat {

        /**
         * 格式 30s | 30d | 30m | 3000ms
         */
        private String interval;

        private String message;

        public Repeat(String interval, String message) {
            this.interval = interval;
            this.message = message;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getInterval() {
            return interval;
        }

        public String getMessage() {
            return message;
        }
    }


    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setUsingType(StoreUsingType usingType) {
        this.usingType = usingType;
    }

    public void setMq(Repeat mq) {
        this.mq = mq;
    }

    public void setRestApi(Repeat restApi) {
        this.restApi = restApi;
    }

    public void setEnableLog(Boolean enableLog) {
        this.enableLog = enableLog;
    }
}
