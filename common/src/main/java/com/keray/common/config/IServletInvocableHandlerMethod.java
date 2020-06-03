package com.keray.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author by keray
 * date:2020/4/19 1:01 上午
 */
@Slf4j(topic = "api-error")
public class IServletInvocableHandlerMethod extends ServletInvocableHandlerMethod {

    private final ServletInvocableHandlerMethodHandler[] handlers;


    public IServletInvocableHandlerMethod(HandlerMethod handlerMethod, ServletInvocableHandlerMethodHandler[] handlers) {
        super(handlerMethod);
        this.handlers = handlers;
    }

    @Override
    public Object invokeForRequest(NativeWebRequest request, ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception {
        Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
        if (handlers != null) {
            final AtomicInteger index = new AtomicInteger(0);
            AtomicReference<ServletInvocableHandlerMethodCallback> callback1 = new AtomicReference<>(null);
            ServletInvocableHandlerMethodCallback callback = _this -> {
                index.getAndIncrement();
                if (index.get() == handlers.length) {
                    return doInvoke(args);
                }
                return handlers[index.get()].work(this, args, request, callback1.get());
            };
            callback1.set(callback);
            return handlers[index.get()].work(this, args, request, callback);
        } else {
            return doInvoke(args);
        }
    }


}
