package com.superkele.idempotent.aspect;


import cn.hutool.core.text.StrJoiner;
import cn.hutool.core.util.StrUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.convert.DurationStyle;

import java.util.function.Supplier;

@Aspect
public class IdempotentAspect {

    public static RepeatProperties properties;
    private static Logger logger = LoggerFactory.getLogger(IdempotentAspect.class);

    public IdempotentAspect(RepeatProperties properties) {
        this.properties = properties;
    }


    private static void printLog(JoinPoint joinPoint, String logContent, Supplier... methods) {
        if (properties.getEnableLog()) {
            Object[] params = new Object[methods.length];
            for (int i = 0; i < params.length; i++) {
                params[i] = methods[i].get();
            }
            String targetMethod = genTargetMethod(joinPoint);
            String content = targetMethod + "=>" + logContent;
            logger.info("\u001B[32m" + content + "\u001B[0m", params);
        }
    }

    private static String genTargetMethod(JoinPoint joinPoint) {
        String clazzName = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        return StrJoiner.of("_").append(clazzName).append(methodName).toString();
    }

    @Around("@annotation(idempotent)")
    public Object idempotentHandler(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        RepeatSubmit repeatSubmit = IdempotentUtils.getInstance(idempotent, joinPoint, properties.getUsingType(), properties.getPrefix());
        Object resultObj = null;
        try {
            long interval = 0L;
            if (idempotent.interval() < 0) {
                switch (idempotent.scene()) {
                    case MQ:
                        interval = DurationStyle.detectAndParse(IdempotentAspect.properties.getMq().getInterval()).toMillis();
                        break;
                    case RESTAPI:
                        interval = DurationStyle.detectAndParse(IdempotentAspect.properties.getRestApi().getInterval()).toMillis();
                        break;
                }
            } else {
                interval = idempotent.timeUnit().toMillis(idempotent.interval());
            }
            //前置处理
            repeatSubmit.beforeHandle(interval);
            long finalInterval = interval;
            printLog(joinPoint, "新建幂等标识{} ,{}s内不可重复消费", () -> repeatSubmit.getKey(), () -> finalInterval / 1000);
            resultObj = joinPoint.proceed();
            //后置处理
            repeatSubmit.afterHandler(SpelUtils.parseResult(idempotent.clean(), resultObj, Boolean.class));
            printLog(joinPoint, "幂等标识{},执行后置处理器成功", () -> repeatSubmit.getKey(), () -> finalInterval / 1000);
        } catch (RepeatSubmitException e) {
            printLog(joinPoint, "幂等标识{} [{}]", () -> repeatSubmit.getKey(), () -> StrUtil.blankToDefault(idempotent.message(), properties.getRestApi().getMessage()));
            throw new RepeatSubmitException(StrUtil.blankToDefault(idempotent.message(), properties.getRestApi().getMessage()));
        } catch (MqRepeatConsumerException e) {
            printLog(joinPoint, "幂等标识{} [{}]", () -> repeatSubmit.getKey(), () -> StrUtil.blankToDefault(idempotent.message(), properties.getMq().getMessage()));
            return null;
        } catch (Throwable e) {
            repeatSubmit.exHandle();
            printLog(joinPoint, "幂等标识{}执行业务异常处理器", () -> repeatSubmit.getKey());
            try {
                throw e;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            //....
        }
        return resultObj;
    }
}
