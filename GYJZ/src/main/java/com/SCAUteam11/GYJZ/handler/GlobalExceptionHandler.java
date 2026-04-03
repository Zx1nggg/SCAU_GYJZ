package com.SCAUteam11.GYJZ.handler;

import com.SCAUteam11.GYJZ.entity.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody // 返回json
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result handleRuntimeException(Exception e){
        return Result.fail(e.getMessage());
    }
}
