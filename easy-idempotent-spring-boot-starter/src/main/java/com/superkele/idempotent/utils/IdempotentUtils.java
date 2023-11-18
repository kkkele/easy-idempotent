package com.superkele.idempotent.utils;

import com.superkele.idempotent.annotations.Idempotent;
import com.superkele.idempotent.core.RepeatSubmit;
import com.superkele.idempotent.decorate.AbstractIdempotentDecorator;
import com.superkele.idempotent.decorate.RepeatSubmitWrapper;
import com.superkele.idempotent.decorate.scene.MqRepeatSubmit;
import com.superkele.idempotent.decorate.scene.RestApiRepeatSubmit;
import com.superkele.idempotent.decorate.type.ParamRepeatSubmit;
import com.superkele.idempotent.decorate.type.SpelRepeatSubmit;
import com.superkele.idempotent.decorate.type.TokenRepeatSubmit;
import com.superkele.idempotent.enums.IdempotentScene;
import com.superkele.idempotent.enums.IdempotentType;
import com.superkele.idempotent.enums.StoreUsingType;
import com.superkele.idempotent.exception.IdempotentParamException;
import com.superkele.idempotent.instance.local.LocalIdempotentInstance;
import com.superkele.idempotent.instance.redis.RedisRepeatSubmitInstance;
import org.aspectj.lang.JoinPoint;

public class IdempotentUtils {

    public static RepeatSubmit getInstance(StoreUsingType type, String prefix) {
        return switch (type) {
            case LOCAL -> new LocalIdempotentInstance(prefix);
            case REDIS -> new RedisRepeatSubmitInstance(prefix);
        };
    }

    public static AbstractIdempotentDecorator getInstance(IdempotentScene scene, RepeatSubmitWrapper wrapper, RepeatSubmit originalRepeatSubmit) {
        return switch (scene) {
            case RESTAPI -> new RestApiRepeatSubmit(wrapper, originalRepeatSubmit);
            case MQ -> new MqRepeatSubmit(wrapper, originalRepeatSubmit);
        };
    }

    public static AbstractIdempotentDecorator getInstance(IdempotentType type, AbstractIdempotentDecorator abstractIdempotentDecorator) {
        return switch (type) {
            case TOKEN -> new TokenRepeatSubmit(abstractIdempotentDecorator);
            case SPEL -> new SpelRepeatSubmit(abstractIdempotentDecorator);
            case PARAM -> new ParamRepeatSubmit(abstractIdempotentDecorator);
        };
    }

    public static RepeatSubmit getInstance(Idempotent idempotent, JoinPoint joinPoint, StoreUsingType type, String prefix) {
        RepeatSubmit originRepeatSubmit = getInstance(type, prefix);
        RepeatSubmitWrapper wrapper = new RepeatSubmitWrapper(idempotent, joinPoint);
        AbstractIdempotentDecorator decoratorIdempotent = getInstance(idempotent.scene(), wrapper, originRepeatSubmit);
        for (int i = 0; i < idempotent.type().length; i++) {
            decoratorIdempotent = getInstance(idempotent.type()[i], decoratorIdempotent);
        }
        return decoratorIdempotent;
    }

}
