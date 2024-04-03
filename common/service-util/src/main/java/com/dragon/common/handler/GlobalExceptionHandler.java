package com.dragon.common.handler;

import com.dragon.common.execption.RuntimeExceptionBeen;
import com.dragon.common.result.Result;
import com.dragon.common.result.ResultCodeEnum;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//全局异常处理
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        return Result.fail().message("全局异常处理：" + e.getMessage());
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        return Result.fail().message("特定异常处理：" + e.getMessage());
    }

    @ExceptionHandler(RuntimeExceptionBeen.class)
    @ResponseBody
    public Result error(RuntimeExceptionBeen e){
        return Result.fail().message(e.getMessage()).code(e.getCode());
    }

    /**
     * spring security异常
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) throws AccessDeniedException {
        return Result.build(null, ResultCodeEnum.PERMISSION);
    }


}
