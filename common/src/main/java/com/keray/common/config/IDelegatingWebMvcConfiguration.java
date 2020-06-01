package com.keray.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

/**
 * @author by keray
 * date:2020/4/19 12:59 上午
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Primary
@Order(0)
public class IDelegatingWebMvcConfiguration extends DelegatingWebMvcConfiguration {


    @Override
    protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        return new RequestMappingHandlerAdapter() {
            @Override
            public int getOrder() {
                return super.getOrder() - 1;
            }

            @Override
            protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
                return new IServletInvocableHandlerMethod(handlerMethod);
            }
        };
    }
}
