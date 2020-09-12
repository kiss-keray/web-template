package com.keray.common;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
    public static class FailResult<E extends Throwable, T> extends Result<T> {
        private String message;
        @JsonIgnore
        private E error;
    }

    public static <E extends Throwable, T> FailResult<E, T> fail(T data, Integer errorCode, String errorMsg, E e) {
        FailResult<E, T> failResult = new FailResult<>();
        failResult.setData(data);
        failResult.setCode(errorCode == null ? CommonResultCode.unknown.getCode() : errorCode);
        failResult.setMessage(StrUtil.isBlank(errorMsg) ? CommonResultCode.unknown.getMessage() : errorMsg);
        failResult.setError(e);
        return failResult;
    }

    public static <E extends Throwable, T> FailResult<E, T> fail(Integer errorCode, String errorMsg, E e) {
        return fail(null, errorCode, errorMsg, e);
    }

    public static <E extends Throwable, T> FailResult<E, T> fail(Integer errorCode, String errorMsg) {
        return fail(null, errorCode, errorMsg, null);
    }

    public static <E extends Throwable, T> FailResult<E, T> fail(ResultCode code) {
        return fail(null, code.getCode(), code.getMessage(), null);
    }

    public static <E extends Throwable, T> FailResult<E, T> fail(ResultCode code, E e) {
        return fail(null, code.getCode(), code.getMessage(), e);
    }

    public static <E extends Throwable, T> FailResult<E, T> fail(Integer errorCode) {
        return fail(null, errorCode, null, null);
    }

    public static <E extends Throwable, T> FailResult<E, T> fail(E e) {
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
            } else if (result instanceof Throwable) {
                return FailResult.fail((Throwable) result);
            } else {
                return SuccessResult.success(result);
            }
        } catch (Throwable e) {
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
        } catch (Throwable e) {
            return fail(e);
        }
        return this;
    }

    public <S2> Result<S2> flatMap(Function<T, Result<S2>> function) {
        try {
            return function.apply(this.getData());
        } catch (Throwable e) {
            return fail(e);
        }
    }

    public <S2> Result<S2> flat(Function<Result<T>, Result<S2>> function) {
        try {
            return function.apply(this);
        } catch (Throwable e) {
            return Result.fail(e);
        }
    }


}
