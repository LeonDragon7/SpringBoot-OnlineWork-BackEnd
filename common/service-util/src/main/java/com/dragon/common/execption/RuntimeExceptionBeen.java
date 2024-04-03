package com.dragon.common.execption;

import com.dragon.common.result.Result;
import com.dragon.common.result.ResultCodeEnum;
import lombok.Data;

//自定义异常类
@Data
public class RuntimeExceptionBeen extends RuntimeException{
    private Integer code;
    private String message;

    /**
     *
     * @param code 状态码
     * @param message 错误消息
     */
    public RuntimeExceptionBeen(Integer code,String message){
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     *
     * @param resultCodeEnum 枚举类型对象
     */
    public RuntimeExceptionBeen(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.message = resultCodeEnum.getMessage();
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "RuntimeExceptionBeen{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
