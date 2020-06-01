package com.keray.common.exception;

import com.keray.common.CommonResultCode;
import com.keray.common.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author by keray
 * date:2019/8/7 10:05
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class BizRuntimeException extends RuntimeException implements CodeException {
    private int code = CommonResultCode.unknown.getCode();

    public BizRuntimeException() {
    }

    public BizRuntimeException(String message, Exception exception, int code) {
        super(message, exception);
        this.code = code;
    }

    public BizRuntimeException(String message, int code) {
        this(message, null, code);
    }


    public BizRuntimeException(Exception exception) {
        super(exception.getMessage(), exception);
        if (exception instanceof BizException) {
            this.code = ((BizException) exception).getCode();
        }
    }

    public BizRuntimeException(ResultCode code) {
        this(code.getMessage(), null, code.getCode());
    }

    public BizRuntimeException(String message) {
        super(message);
    }

    public BizRuntimeException(Exception exception, int code) {
        this(exception.getMessage(), null, code);
    }
}
