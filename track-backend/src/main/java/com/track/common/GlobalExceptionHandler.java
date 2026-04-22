package com.track.common;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result<Void> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        if (message != null && message.toLowerCase().contains("no permission")) {
            return Result.error(403, message);
        }
        return Result.error(message == null ? "Runtime error" : message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<Void> handleException(Exception e) {
        return Result.error("Internal server error: " + e.getMessage());
    }
}
