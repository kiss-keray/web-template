package com.keray.common.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.keray.common.IBaseEntity;
import com.keray.common.Result;
import lombok.extern.slf4j.Slf4j;
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
public class ErrorLogServletInvocableHandlerMethodHandler implements ServletInvocableHandlerMethodHandler{
    @Override
    public Object work(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request, ServletInvocableHandlerMethodCallback callback) throws Exception {
        for (Object o : args) {
            if (o instanceof IBaseEntity) {
                ((IBaseEntity) o).clearBaseField();
            }
        }
        Consumer<Object> logFail = result -> {
            try {
                if (result instanceof Result.FailResult || result instanceof Exception) {
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
                    logFail(result, url, flag, args, handlerMethod.getMethodParameters());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        try {
            Object result = callback.get(this);
            logFail.accept(result);
            return result;
        } catch (Exception e) {
            logFail.accept(e);
            throw e;
        }
    }

    private void logFail(Object result, String url, String flag, Object[] args, MethodParameter[] parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n").append("============接口异常============").append("\n");
        builder.append("  flag:").append(flag).append("\n");
        builder.append("   url:").append(url).append("\n");
        builder.append("  args:").append("\n");
        for (int i = 0; i < parameters.length; i++) {
            String json = "json解析失败";
            try {
                json = args[i] == null ? null : JSON.toJSONString(args[i]);
            } catch (Exception ignore) {
            }
            builder.append(parameters[i].getParameterName()).append("=").append(json).append("\n");
        }
        if (result instanceof Result.FailResult) {
            builder.append("result:").append(StrUtil.format("code={},message={}", ((Result) result).getCode(), ((Result.FailResult) result).getMessage())).append("\n");
        } else {
            builder.append("result:").append(result.getClass()).append("\n");
        }
        builder.append("============end============");
        builder.append("\n");
        log.error(builder.toString());
    }
}
