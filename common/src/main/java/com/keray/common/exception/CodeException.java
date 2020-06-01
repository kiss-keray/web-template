package com.keray.common.exception;

/**
 * @author by keray
 * date:2020/4/11 6:45 下午
 */
public interface CodeException {
    int getCode();

    String getMessage();

    Throwable getCause();
}
