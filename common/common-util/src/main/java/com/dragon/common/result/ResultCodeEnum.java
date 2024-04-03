package com.dragon.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
@Getter
public enum ResultCodeEnum{
    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(500, "服务异常"),
    DATA_ERROR(204, "登录数据异常"),
    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(209, "没有权限")
    ;

    private Integer code;

    private String message;
}
