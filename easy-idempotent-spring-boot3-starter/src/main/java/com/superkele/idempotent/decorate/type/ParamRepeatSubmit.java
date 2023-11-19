package com.superkele.idempotent.decorate.type;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.superkele.idempotent.decorate.AbstractIdempotentDecorator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ParamRepeatSubmit extends AbstractIdempotentDecorator {

    public ParamRepeatSubmit(AbstractIdempotentDecorator repeatSubmit) {
        super(repeatSubmit);
    }

    @Override
    protected String plusKey() {
        String nowParams = argsArrayToString(wrapper.getJoinPoint().getArgs());
        return nowParams;
    }


    //参考: 疯狂的狮子Li
    private String argsArrayToString(Object... paramsArray) {
        StringJoiner params = new StringJoiner(" ");
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            if (Objects.nonNull(o) && !isFilterObject(o)) {
                params.add(JSONUtil.toJsonStr(o));
            }
        }
        return params.toString();
    }

    private boolean isFilterObject(Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            //如果是MultipartFile的实现类，则需要过滤
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) { //如果是属于集合类型
            Collection collection = (Collection) o;
            for (Object value : collection) {
                //如果是MultipartFile的实现类，则需要过滤
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) { //如果是属于Map对象
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
