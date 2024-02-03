package com.superkele.idempotent.decorate;

import cn.hutool.core.util.StrUtil;
import com.superkele.idempotent.core.RepeatSubmit;


public abstract class AbstractIdempotentDecorator implements RepeatSubmit {
    protected final RepeatSubmitWrapper wrapper;

    protected final RepeatSubmit repeatSubmit;

    public AbstractIdempotentDecorator(RepeatSubmitWrapper wrapper, RepeatSubmit repeatSubmit) {
        this.wrapper = wrapper;
        this.repeatSubmit = repeatSubmit;
        String keyPlus = StrUtil.builder().append(StrUtil.nullToDefault(repeatSubmit.getKey(), "")).append(':').append(plusKey()).toString();
        repeatSubmit.setKey(keyPlus);
    }

    public AbstractIdempotentDecorator(AbstractIdempotentDecorator repeatSubmit) {
        this.wrapper = repeatSubmit.wrapper;
        this.repeatSubmit = repeatSubmit;
        String keyPlus = StrUtil.builder().append(StrUtil.nullToDefault(repeatSubmit.getKey(), "")).append(':').append(plusKey()).toString();
        repeatSubmit.setKey(keyPlus);
    }


    @Override
    public String getValue() {
        return repeatSubmit.getValue();
    }

    @Override
    public void setValue(String key, String value, long interval) {
        repeatSubmit.setValue(key, value, interval);
    }

    protected abstract String plusKey();

    @Override
    public String getKey() {
        return repeatSubmit.getKey();
    }

    @Override
    public void setKey(String key) {
        repeatSubmit.setKey(key);
    }

    @Override
    public Boolean predict(long interval) {
        return repeatSubmit.predict(interval);
    }


    @Override
    public void beforeHandle(long interval) {
        repeatSubmit.beforeHandle(interval);
    }

    @Override
    public void exHandle() {
        repeatSubmit.exHandle();
    }

    @Override
    public void afterHandler(Boolean flag) {
        repeatSubmit.afterHandler(flag);
    }

    @Override
    public void clean() {
        repeatSubmit.clean();
    }
}
