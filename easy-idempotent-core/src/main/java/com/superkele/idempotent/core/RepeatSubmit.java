package com.superkele.idempotent.core;

public interface RepeatSubmit {


    /**
     * 直接设置幂等标识的值
     */
    void setValue(String key, String value, long interval);

    /**
     * 获取幂等标识的值
     */
    String getValue();


    /**
     * 获取 幂等标识
     *
     * @return
     */
    String getKey();

    void setKey(String key);

    /**
     * 前置判断
     *
     * @param interval 多少 ms 内不能重复提交
     */
    Boolean predict(long interval);


    /**
     * 前置处理
     *
     * @param interval 多少 ms 内不能重复提交
     */
    void beforeHandle(long interval);

    /**
     * 异常处理
     */
    void exHandle();

    /**
     * 方法正常返回后置处理
     *
     * @param flag 是否清理幂等标识
     */
    void afterHandler(Boolean flag);

    /**
     * 清理 幂等标识
     */
    void clean();
}
