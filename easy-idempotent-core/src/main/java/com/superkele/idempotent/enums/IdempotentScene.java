package com.superkele.idempotent.enums;

public enum IdempotentScene {

    /**
     * 基于 RestAPI 场景验证
     */
    RESTAPI,

    /**
     * 基于 MQ 场景验证
     */
    MQ
}
