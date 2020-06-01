package com.keray.common.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.keray.common.IBaseEntity;
import com.keray.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author by keray
 * date:2020/4/19 1:01 上午
 */
@Slf4j(topic = "api-error")
public class IServletInvocableHandlerMethod extends ServletInvocableHandlerMethod {

    public IServletInvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }

    @Override
    public Object invokeForRequest(NativeWebRequest request, ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception {
        Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
        for (Object o : args) {
            if (o instanceof IBaseEntity) {
                ((IBaseEntity) o).clearBaseField();
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Arguments: " + Arrays.toString(args));
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
                    logFail(result, url, flag, args);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        try {
            Object result = doInvoke(args);
            logFail.accept(result);
            return result;
        } catch (Exception e) {
            logFail.accept(e);
            throw e;
        }
    }

    private void logFail(Object result, String url, String flag, Object[] args) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n").append("============接口异常============").append("\n");
        builder.append("  flag:").append(flag).append("\n");
        builder.append("   url:").append(url).append("\n");
        builder.append("  args:").append("\n");
        for (int i = 0; i < getMethodParameters().length; i++) {
            String json = "json解析失败";
            try {
                json = args[i] == null ? null : JSON.toJSONString(args[i]);
            } catch (Exception ignore) {
            }
            builder.append(getMethodParameters()[i].getParameterName()).append("=").append(json).append("\n");
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
