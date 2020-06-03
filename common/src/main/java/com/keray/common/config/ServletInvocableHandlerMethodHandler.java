package com.keray.common.config;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * @author by keray
 * date:2020/6/3 9:34 上午
 */
public interface ServletInvocableHandlerMethodHandler {

    default Integer order() {
        return Integer.MAX_VALUE;
    }

    default Object work(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request, ServletInvocableHandlerMethodCallback callback) throws Exception {
        return callback.get(this);
    }
}
