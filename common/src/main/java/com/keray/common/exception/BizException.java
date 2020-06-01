package com.keray.common.exception;

import com.keray.common.CommonResultCode;
import com.keray.common.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author by keray
 * date:2019/8/1 11:30
 * 业务异常
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class BizException extends Exception implements CodeException {
    private int code = CommonResultCode.unknown.getCode();

    public BizException() {
    }

    public BizException(String message, Exception exception, int code) {
        super(message, exception);
        this.code = code;
    }

    public BizException(String message, int code) {
        this(message, null, code);
    }

    public BizException(ResultCode code) {
        this(code.getMessage(), null, code.getCode());
    }

    public BizException(Exception exception) {
        super(exception);
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(Exception exception, int code) {
        this(exception.getMessage(), null, code);
    }
}
