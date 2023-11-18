package com.superkele.idempotent.decorate.type;

import com.superkele.idempotent.decorate.AbstractIdempotentDecorator;
import com.superkele.idempotent.exception.IdempotentParamException;
import com.superkele.idempotent.utils.SpelUtils;

public class SpelRepeatSubmit extends AbstractIdempotentDecorator {

    public SpelRepeatSubmit(AbstractIdempotentDecorator repeatSubmit) {
        super(repeatSubmit);
    }

    @Override
    protected String plusKey() {
        try {
            return SpelUtils.parseSpel(wrapper.getIdempotent().spelKey(), wrapper.getJoinPoint(), String.class);
        } catch (Throwable e) {
            throw new IdempotentParamException("spel表达式参数设置错误");
        }
    }
}
