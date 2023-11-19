package com.superkele.idempotent.decorate.type;

import cn.hutool.extra.spring.SpringUtil;
import com.superkele.idempotent.core.RepeatSubmit;
import com.superkele.idempotent.core.RepeatToken;
import com.superkele.idempotent.decorate.AbstractIdempotentDecorator;
import com.superkele.idempotent.decorate.RepeatSubmitWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TokenRepeatSubmit extends AbstractIdempotentDecorator {

    private static final Logger logger = LoggerFactory.getLogger(TokenRepeatSubmit.class);

    private static RepeatToken REPEAT_TOKEN;

    static {
        try {
            REPEAT_TOKEN = SpringUtil.getBean(RepeatToken.class);
        } catch (Exception e) {
            logger.info("\u001B[32m" + "[EASY-IDEMPOTENT] 如果使用Token模式，请先手动实现 [com.superkele.idempotent.core.RepeatToken] 类" + "\u001B[0m");
        }
    }

    public TokenRepeatSubmit(AbstractIdempotentDecorator repeatSubmit) {
        super(repeatSubmit);
    }

    @Override
    protected String plusKey() {
        return REPEAT_TOKEN.getToken();
    }
}
