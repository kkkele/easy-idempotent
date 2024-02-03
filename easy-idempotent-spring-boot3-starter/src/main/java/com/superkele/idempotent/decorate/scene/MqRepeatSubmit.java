package com.superkele.idempotent.decorate.scene;

import cn.hutool.core.text.StrBuilder;
import com.superkele.idempotent.annotations.Idempotent;
import com.superkele.idempotent.aspect.IdempotentAspect;
import com.superkele.idempotent.core.RepeatSubmit;
import com.superkele.idempotent.decorate.AbstractIdempotentDecorator;
import com.superkele.idempotent.decorate.RepeatSubmitWrapper;
import com.superkele.idempotent.exception.MqRepeatConsumerException;
import org.aspectj.lang.JoinPoint;
import org.springframework.boot.convert.DurationStyle;

public class MqRepeatSubmit extends AbstractIdempotentDecorator {

    private static final String CONSUME_OVER = "1";

    public MqRepeatSubmit(RepeatSubmitWrapper wrapper, RepeatSubmit repeatSubmit) {
        super(wrapper, repeatSubmit);
    }

    public MqRepeatSubmit(AbstractIdempotentDecorator repeatSubmit) {
        super(repeatSubmit);
    }

    @Override
    protected String plusKey() {
        JoinPoint joinPoint = wrapper.getJoinPoint();
        String key = StrBuilder.create().append(joinPoint.getSignature().getDeclaringTypeName()).append('(').append(joinPoint.getSignature().getName()).append(')').toString();
        return key;
    }

    @Override
    public Boolean predict(long interval) {
        Boolean predict = super.predict(interval);
        //MQ场景下如果set 失败，则要判断是否是 异常导致的消息重新消费 还是 极端情况下的消息重复消费(指 B调用A,A消息消费成功，但是因为网络阻塞或者其他原因,过久无给B反应导致 B 重新发送消息)
        if (!predict) {
            //如果值等于消费完毕标识,则不能放行,需要 前置处理器 抛出 重复消费异常
            return !CONSUME_OVER.equals(getValue());
        }
        return true;
    }

    @Override
    public void beforeHandle(long interval) {
        if (!predict(interval)) {
            throw new MqRepeatConsumerException();
        }
        // set成功 或 幂等标识对应的值 不等于 消费过的标识时,需要消费
    }

    @Override
    public void afterHandler(Boolean flag) {
        Idempotent idempotent = wrapper.getIdempotent();
        long interval = (idempotent.interval() < 0) ? switch (idempotent.scene()) {
            case MQ -> DurationStyle.detectAndParse(IdempotentAspect.properties.getMq().getInterval()).toMillis();
            case RESTAPI ->
                    DurationStyle.detectAndParse(IdempotentAspect.properties.getRestApi().getInterval()).toMillis();
        } : idempotent.timeUnit().toMillis(idempotent.interval());
        setValue(getKey(), CONSUME_OVER, interval);

    }

    @Override
    public void exHandle() {
        //抛出异常,则要删除幂等标识，方便重新消费
        repeatSubmit.clean();
    }
}
