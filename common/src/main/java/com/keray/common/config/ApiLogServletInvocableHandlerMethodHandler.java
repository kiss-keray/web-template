package com.keray.common.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.keray.common.IBaseEntity;
import com.keray.common.Result;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Consumer;

/**
 * @author by keray
 * date:2020/6/3 10:08 上午
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "api.log")
public class ApiLogServletInvocableHandlerMethodHandler implements ServletInvocableHandlerMethodHandler {

    @Getter
    @Setter
    private Boolean all;

    @Override
    public Integer order() {
        return 0;
    }

    @Override
    public Object work(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request, ServletInvocableHandlerMethodCallback callback) throws Exception {
        long start = System.nanoTime();
        for (Object o : args) {
            if (o instanceof IBaseEntity) {
                ((IBaseEntity) o).clearBaseField();
            }
        }
        Consumer<Object> logFail = result -> {
            try {
                if (result instanceof Result.FailResult || result instanceof Exception || all) {
                    String url = null;
                    String flag = null;
                    try {
                        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
                        if (servletRequest != null) {
                            url = servletRequest.getRequestURL().toString();
                            if (StrUtil.isBlank(url)) {
                                url = "错误";
                            }
                            flag = servletRequest.getHeader("X-User-Agent");
                            if (StrUtil.isBlank(flag)) {
                                flag = servletRequest.getHeader("User-Agent");
                            }
                            if (StrUtil.isBlank(flag)) {
                                flag = "未知";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    apiLog(result instanceof Result.FailResult || result instanceof Exception, result, url, flag, args, handlerMethod.getMethodParameters(), start);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        try {
            Object result = callback.get();
            logFail.accept(result);
            return result;
        } catch (Exception e) {
            logFail.accept(e);
            throw e;
        }
    }

    private void apiLog(boolean fail, Object result, String url, String flag, Object[] args, MethodParameter[] parameters, long start) {
        StringBuilder builder = new StringBuilder();
        if (fail) {
            builder.append(System.lineSeparator()).append("============接口异常============").append(System.lineSeparator());
        } else {
            builder.append(System.lineSeparator()).append("============api start============").append(System.lineSeparator());
        }
        builder.append("  flag:").append(flag).append(System.lineSeparator());
        builder.append("   url:").append(url).append(System.lineSeparator());
        builder.append("  args:").append(System.lineSeparator());
        for (int i = 0; i < parameters.length; i++) {
            String json = "json解析失败";
            try {
                json = args[i] == null ? null : JSON.toJSONString(args[i]);
            } catch (Exception ignore) {
            }
            builder.append(parameters[i].getParameterName()).append("=").append(json).append(System.lineSeparator());
        }
        if (result instanceof Result.FailResult) {
            builder.append("result:").append(StrUtil.format("code={},message={}", ((Result) result).getCode(), ((Result.FailResult) result).getMessage())).append(System.lineSeparator());
        } else if (result instanceof Result.SuccessResult){
            builder.append("result:").append(JSON.toJSONString(((Result.SuccessResult) result).getData())).append(System.lineSeparator());
        } else {
            builder.append("result:").append(result.getClass()).append(System.lineSeparator());
        }
        builder.append(String.format("============end time=ns:%s  ============",System.nanoTime() - start));
        builder.append(System.lineSeparator());
        log.error(builder.toString());
    }
}
