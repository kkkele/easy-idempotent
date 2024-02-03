package com.superkele.idempotent.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.superkele.idempotent.exception.IdempotentParamException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SpelUtils {
    /**
     * 定义spel表达式解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    /**
     * 定义spel解析模版
     */
    private static final ParserContext PARSER_CONTEXT = new TemplateParserContext();
    private static final ParameterNameDiscoverer PND = new DefaultParameterNameDiscoverer();

    public static <T> T parseSpel(String spelKey, JoinPoint joinPoint, Class<T> clazz) {
        //获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod(); //获取方法 (包含参数名)
        Object[] args = joinPoint.getArgs();  //获取方法参数值 (与上面形成 key-value ,注入到spel context中)
        String[] parameterNames = PND.getParameterNames(method);
        if (ArrayUtil.isEmpty(parameterNames)) {
            throw new IdempotentParamException("幂等 spel解析异常,该方法未含有参数");
        }
        /**
         * 定义spel上下文对象进行解析
         */
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        Expression expression;
        if (StrUtil.startWith(spelKey, PARSER_CONTEXT.getExpressionPrefix())
                && StrUtil.endWith(spelKey, PARSER_CONTEXT.getExpressionSuffix())) {
            expression = PARSER.parseExpression(spelKey, PARSER_CONTEXT);
        } else {
            expression = PARSER.parseExpression(spelKey);
        }
        return expression.getValue(context, clazz);
    }

    public static <T> T parseResult(String spelKey, Object result, Class<T> clazz) {
        /**
         * 定义spel上下文对象进行解析
         */
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("result", result);
        Expression expression;
        if (StrUtil.startWith(spelKey, PARSER_CONTEXT.getExpressionPrefix())
                && StrUtil.endWith(spelKey, PARSER_CONTEXT.getExpressionSuffix())) {
            expression = PARSER.parseExpression(spelKey, PARSER_CONTEXT);
        } else {
            expression = PARSER.parseExpression(spelKey);
        }
        return expression.getValue(context, clazz);
    }
}
