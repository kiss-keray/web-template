package com.keray.common.config;

import com.keray.common.CommonResultCode;
import com.keray.common.Result;
import com.keray.common.exception.BizException;
import com.keray.common.exception.BizRuntimeException;
import com.keray.common.exception.CodeException;
import com.keray.common.utils.QpsLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * @author by keray
 * date:2020/9/7 9:36 下午
 */
@Slf4j
@Configuration
public class ResultServletInvocableHandlerMethodHandler<E extends Throwable> implements ServletInvocableHandlerMethodHandler, ExceptionHandler<E> {

    private final static ExceptionHandler<Throwable>[] EXCEPTION_HANDLERS = new ExceptionHandler[]{
            new RuntimeExceptionHandler(),
            new CodeExceptionHandler(),
            new QpsExceptionHandler()
    };

    private final ExceptionHandler<Throwable> defaultExceptionHandler = new DefaultExceptionHandler();

    @Override
    public Integer order() {
        return 1;
    }

    @Override
    public Object work(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request, ServletInvocableHandlerMethodCallback callback) throws Exception {
        try {
            Object result = callback.get();
            if (result instanceof Result) {
                if (result instanceof Result.FailResult && ((Result.FailResult<?, ?>) result).getError() != null) {
                    ExceptionHandler<Throwable> exceptionHandler = giveExceptionHandler(((Result.FailResult<?, ?>) result).getError());
                    if (exceptionHandler == null) {
                        return result;
                    }
                    return exceptionHandler.errorHandler(((Result.FailResult<?, ?>) result).getError());
                }
                return result;
            }
            return Result.success(result);
        } catch (Throwable error) {
            return errorHandler(error);
        }
    }


    @Override
    public boolean supper(Throwable e) {
        return true;
    }

    @Override
    public Result<?> errorHandler(Throwable error) {
        ExceptionHandler<Throwable> exceptionHandler = giveExceptionHandler(error);
        return exceptionHandler != null ? exceptionHandler.errorHandler(error) : defaultExceptionHandler.errorHandler(error);
    }

    private ExceptionHandler<Throwable> giveExceptionHandler(Throwable e) {
        for (ExceptionHandler<Throwable> eh : EXCEPTION_HANDLERS) {
            if (eh.supper(e)) {
                return eh;
            }
        }
        return null;
    }
}

interface ExceptionHandler<E extends Throwable> {
    boolean supper(Throwable e);

    Result<?> errorHandler(E error);
}

class RuntimeExceptionHandler implements ExceptionHandler<RuntimeException> {

    @Override
    public boolean supper(Throwable e) {
        return e instanceof RuntimeException;
    }

    @Override
    public Result<?> errorHandler(RuntimeException error) {
        return runtimeException(error);
    }

    private Result<?> runtimeException(RuntimeException runtimeException) {
        Throwable exception = runtimeException;
        int i = 0;
        for (; exception != null && i < 10; i++) {
            if (exception instanceof BizRuntimeException) {
                if (((BizRuntimeException) exception).getCode() != CommonResultCode.unknown.getCode()) {
                    return Result.fail(((BizRuntimeException) exception).getCode(), exception.getMessage(), exception);
                }
                exception = exception.getCause();
                continue;
            }
            if (exception instanceof BizException) {
                return Result.fail(((BizRuntimeException) exception).getCode(), exception.getMessage(), exception);
            }
            exception = exception.getCause();
        }
        return Result.fail(CommonResultCode.unknown);
    }

}

/**
 * 具有code-message的错误处理
 */
class CodeExceptionHandler implements ExceptionHandler<Throwable> {

    @Override
    public boolean supper(Throwable e) {
        return e instanceof CodeException;
    }

    @Override
    public Result<?> errorHandler(Throwable error) {
        CodeException ex = (CodeException) error;
        return Result.fail(ex.getCode(), ex.getMessage(), error);
    }
}


class QpsExceptionHandler implements ExceptionHandler<QpsLimit.QPSFailException> {

    @Override
    public boolean supper(Throwable e) {
        return e instanceof QpsLimit.QPSFailException;
    }

    @Override
    public Result<?> errorHandler(QpsLimit.QPSFailException error) {
        return Result.fail(CommonResultCode.limitedAccess);
    }
}

class DefaultExceptionHandler implements ExceptionHandler<Throwable> {

    @Override
    public boolean supper(Throwable e) {
        return true;
    }

    @Override
    public Result<?> errorHandler(Throwable error) {
        return Result.fail(error);
    }
}
