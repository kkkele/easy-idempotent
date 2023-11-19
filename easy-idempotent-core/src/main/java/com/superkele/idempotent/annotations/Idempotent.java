package com.superkele.idempotent.annotations;

import com.superkele.idempotent.enums.IdempotentScene;
import com.superkele.idempotent.enums.IdempotentType;
import com.superkele.idempotent.enums.OverStrategy;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    int REPEAT_DEFAULT_INTERVAL_TIME = -1;


    IdempotentType[] type() default IdempotentType.PARAM;

    /**
     * 支持参数的spel表达式  只有显示的在参数中带上 '#'时才会解析，否则会报错
     */
    String spelKey() default "";

    IdempotentScene scene() default IdempotentScene.RESTAPI;

    /**
     * 间隔时间(ms)，小于此时间视为重复提交
     */
    int interval() default REPEAT_DEFAULT_INTERVAL_TIME;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 后置请求处理结束后是否清理掉幂等标识 (默认清理) 支持spel表达式 (例如: #result!=null ,#{#result} == null)
     */
    String clean() default "true";

    /**
     * 提示消息 支持国际化 格式为 {code}
     */
    String message() default "";
}
