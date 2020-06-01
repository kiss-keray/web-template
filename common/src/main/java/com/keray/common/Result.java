package com.keray.common;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("serial")
@Data
@Slf4j
public class Result<T> implements Serializable {

    protected Boolean success = false;
    protected Integer code;
    protected T data;

    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class SuccessResult<T> extends Result<T> {
        SuccessResult(T data) {
            this.code = 1;
            success = true;
            this.data = data;
        }
    }

    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FailResult<E extends Exception, T> extends Result<T> {
        private String message;
        @JsonIgnore
        private E exception;
    }

    public static <E extends Exception, T> FailResult<E, T> fail(T data, Integer errorCode, String errorMsg, E e) {
        FailResult<E, T> failResult = new FailResult<>();
        failResult.setData(data);
        failResult.setCode(errorCode == null ? CommonResultCode.unknown.getCode() : errorCode);
        failResult.setMessage(StrUtil.isBlank(errorMsg) ? CommonResultCode.unknown.getMessage() : errorMsg);
        failResult.setException(e);
        return failResult;
    }

    public static <E extends Exception, T> FailResult<E, T> fail(Integer errorCode, String errorMsg, E e) {
        return fail(null, errorCode, errorMsg, e);
    }

    public static <E extends Exception, T> FailResult<E, T> fail(Integer errorCode, String errorMsg) {
        return fail(null, errorCode, errorMsg, null);
    }

    public static <E extends Exception, T> FailResult<E, T> fail(ResultCode code) {
        return fail(null, code.getCode(), code.getMessage(), null);
    }

    public static <E extends Exception, T> FailResult<E, T> fail(ResultCode code, E e) {
        return fail(null, code.getCode(), code.getMessage(), e);
    }

    public static <E extends Exception, T> FailResult<E, T> fail(Integer errorCode) {
        return fail(null, errorCode, null, null);
    }

    public static <E extends Exception, T> FailResult<E, T> fail(E e) {
        return fail(null, null, null, e);
    }

    public static <T> Result<T> success(T data) {
        return new SuccessResult<>(data);
    }

    public static <T> Result<T> success() {
        return new SuccessResult<>(null);
    }

    public static <T> Result<T> of(Supplier<T> supplier) {
        try {
            T result = supplier.get();
            if (result instanceof Result) {
                return (Result<T>) result;
            } else if (result instanceof Exception) {
                return FailResult.fail((Exception) result);
            } else {
                return SuccessResult.success(result);
            }
        } catch (Exception e) {
            return FailResult.fail(e);
        }
    }

    public <S2> Result<S2> map(Function<T, S2> function) {
        if (this instanceof SuccessResult) {
            return of(() -> function.apply(this.getData()));
        }
        return (Result<S2>) this;
    }

    public Result<T> peek(Consumer<T> consumer) {
        try {
            if (this instanceof SuccessResult) {
                consumer.accept(getData());
            }
        } catch (Exception e) {
            return fail(e);
        }
        return this;
    }

    public <S2> Result<S2> flatMap(Function<T, Result<S2>> function) {
        try {
            return function.apply(this.getData());
        } catch (Exception e) {
            return fail(e);
        }
    }

    public <S2> Result<S2> flat(Function<Result<T>, Result<S2>> function) {
        try {
            return function.apply(this);
        } catch (Exception e) {
            return Result.fail(e);
        }
    }

    public Result<T> failFlat(Function<FailResult<? extends Exception, T>, Result<T>> function) {
        if (this instanceof FailResult) {
            try {
                return function.apply((FailResult<? extends Exception, T>) this);
            } catch (Exception e) {
                return fail(e);
            }
        }
        return this;
    }

    public Result<T> logFail() {
        if (this instanceof FailResult) {
            if (((FailResult) this).getException() != null) {
                ((FailResult) this).getException().printStackTrace();
            }
            log.error(StrUtil.format("logFail:code={},message={}:",
                    ((FailResult) this).getCode(),
                    ((FailResult) this).getMessage()),
                    ((FailResult) this).getException());
        }
        return this;
    }

    public Result<T> throwE() {
        if (this instanceof FailResult) {
            throw new RuntimeException(((FailResult) this).getException());
        }
        return this;
    }
}
