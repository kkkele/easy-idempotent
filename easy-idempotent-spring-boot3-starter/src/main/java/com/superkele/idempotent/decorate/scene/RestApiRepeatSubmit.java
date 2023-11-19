package com.superkele.idempotent.decorate.scene;

import com.superkele.idempotent.core.RepeatSubmit;
import com.superkele.idempotent.decorate.AbstractIdempotentDecorator;
import com.superkele.idempotent.decorate.RepeatSubmitWrapper;
import com.superkele.idempotent.exception.RepeatSubmitException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RestApiRepeatSubmit extends AbstractIdempotentDecorator {
    public RestApiRepeatSubmit(RepeatSubmitWrapper wrapper, RepeatSubmit repeatSubmit) {
        super(wrapper, repeatSubmit);
    }

    public RestApiRepeatSubmit(AbstractIdempotentDecorator repeatSubmit) {
        super(repeatSubmit);
    }

    @Override
    protected String plusKey() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest().getRequestURI();
    }

    @Override
    public void beforeHandle(long interval) {
        super.beforeHandle(interval);
        if (!predict(interval)) {
            throw new RepeatSubmitException();
        }
    }
}
