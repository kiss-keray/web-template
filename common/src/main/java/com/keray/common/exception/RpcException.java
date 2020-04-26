package com.keray.common.exception;

import com.keray.common.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author by keray
 * date:2020/4/11 6:19 下午
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class RpcException extends BizRuntimeException {

    public RpcException(String message, Exception exception, int code) {
        super(message, exception, code);
    }

    public RpcException(String message, int code) {
        this(message, null, code);
    }


    public RpcException(Exception exception) {
        super(exception);
    }

    public RpcException(ResultCode code) {
        this(code.getMessage(), null, code.getCode());
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Exception exception, int code) {
        this(exception.getMessage(), null, code);
    }
}
