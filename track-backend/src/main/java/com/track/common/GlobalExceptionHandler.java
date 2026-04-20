package com.track.common;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result<Void> handleRuntimeException(RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().contains("没有权限")) {
            return Result.error(403, e.getMessage());
        }
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<Void> handleException(Exception e) {
        return Result.error("服务器内部错误: " + e.getMessage());
    }
}
