package com.superkele.idempotent.aspect;

import cn.hutool.core.text.StrJoiner;
import com.superkele.idempotent.annotations.Idempotent;
import com.superkele.idempotent.config.properties.RepeatProperties;
import com.superkele.idempotent.core.RepeatSubmit;
import com.superkele.idempotent.exception.MqRepeatConsumerException;
import com.superkele.idempotent.exception.RepeatSubmitException;
import com.superkele.idempotent.utils.IdempotentUtils;
import com.superkele.idempotent.utils.SpelUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.convert.DurationStyle;

@Aspect
public class IdempotentNoLogAspect{

    public static RepeatProperties properties;

    public IdempotentNoLogAspect(RepeatProperties properties) {
        this.properties = properties;
    }

    @Around("@annotation(idempotent)")
    public Object idempotentHandler(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        RepeatSubmit repeatSubmit = IdempotentUtils.getInstance(idempotent, joinPoint, properties.getUsingType(), properties.getPrefix());
        Object resultObj = null;
        try {
            long interval = (idempotent.interval() < 0) ? switch (idempotent.scene()) {
                case MQ -> DurationStyle.detectAndParse(properties.getMq().getInterval()).toMillis();
                case RESTAPI -> DurationStyle.detectAndParse(properties.getRestApi().getInterval()).toMillis();
            } : idempotent.timeUnit().toMillis(idempotent.interval());
            //前置处理
            repeatSubmit.beforeHandle(interval);
            resultObj = joinPoint.proceed();
            //后置处理
            repeatSubmit.afterHandler(SpelUtils.parseResult(idempotent.clean(), resultObj, Boolean.class));
        } catch (RepeatSubmitException e) {
            throw new RepeatSubmitException(properties.getRestApi().getMessage());
        } catch (MqRepeatConsumerException e) {
            return null;
        } catch (Throwable e) {
            repeatSubmit.exHandle();
            throw new RuntimeException(e);
        } finally {
            //....
        }
        return resultObj;
    }
}
