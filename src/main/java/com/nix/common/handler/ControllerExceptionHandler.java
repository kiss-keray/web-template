package com.nix.common.handler;

import com.nix.Exception.WebException;
import com.nix.common.ReturnObject;
import com.nix.util.ReturnUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 11723
 * controller异常处理类
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    /**
     * 对于controller方法执行出现
     * {@link WebException}
     * 处理中自定义异常
     * 异常进行处理
     * */
    @ResponseBody
    @ExceptionHandler(value = WebException.class)
    public ReturnObject controllerHandle(WebException e) {
        e.printStackTrace();
        return ReturnUtil.fail(e.getCode(),e.getMessage(),null);
    }
    /**
     * 对于controller方法执行出现
     * {@link IllegalArgumentException}
     *Assert校验异常
     * 异常进行处理
     * */
    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ReturnObject exceptionHandle(IllegalArgumentException e) {
        e.printStackTrace();
        return ReturnUtil.fail(-1,e.getMessage(),null);
    }
    /**
     * 对于controller方法执行出现
     * {@link Exception}
     * 前面没处理的异常
     * 异常进行处理
     * */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ReturnObject exceptionHandle(Exception e) {
        e.printStackTrace();
        return ReturnUtil.fail(-1,"未知错误",null);
    }
}
