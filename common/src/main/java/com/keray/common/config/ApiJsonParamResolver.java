package com.keray.common.config;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.keray.common.annotation.ApiJsonParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by keray
 * date:2019/9/2 18:05
 * api端json参数解析
 */
@Slf4j
public class ApiJsonParamResolver extends RequestResponseBodyMethodProcessor implements HandlerInterceptor {

    /**
     * request缓存
     */
    private final ThreadLocal<ServletWebRequest> cache = new ThreadLocal<>();

    /**
     * resolver 列表
     */
    private final List<HandlerMethodArgumentResolver> argumentResolvers;

    /**
     * resolver 映射缓存
     */
    private final Map<MethodParameter, HandlerMethodArgumentResolver> argumentResolverCache =
            new ConcurrentHashMap<>(256);

    /**
     * 全局开关 全局开发打开意味着改装载器会处理所有application/json请求的方法，不开启全局开关时仅当方法具有@ApiJsonParam(value = true)时有效
     */
    private final Boolean globalSwitch;

    private final static String ROOT_JSON_KEY = "root-json-key";

    public ApiJsonParamResolver(List<HttpMessageConverter<?>> converters, List<HandlerMethodArgumentResolver> argumentResolvers) {
        this(converters, argumentResolvers, false);
    }

    public ApiJsonParamResolver(List<HttpMessageConverter<?>> converters, List<HandlerMethodArgumentResolver> argumentResolvers, boolean globalSwitch) {
        super(converters);
        this.argumentResolvers = argumentResolvers;
        this.globalSwitch = globalSwitch;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        ApiJsonParam apiJsonParam = parameter.getMethodAnnotation(ApiJsonParam.class);
        // 对于RequestBody注解的方法不做处理
        if (globalSwitch) {
            if (apiJsonParam != null) {
                return apiJsonParam.value() && parameter.getParameterAnnotation(RequestBody.class) == null;
            }
            return parameter.getParameterAnnotation(RequestBody.class) == null;
        } else {
            return apiJsonParam != null && apiJsonParam.value() && parameter.getParameterAnnotation(RequestBody.class) == null;
        }
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        // 仅当content-type为app/json时 处理 其他情况走原有的处理
        all:
        if (httpServletRequest != null && httpServletRequest.getContentType() != null &&
                httpServletRequest.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            if (cache.get() == null) {
                MethodParameter mapParam = new MethodParameter(parameter) {
                    // 重写为了将body的json字符串转换为map对象
                    @Override
                    public Class<?> getParameterType() {
                        return Map.class;
                    }
                };
                Map<String, Object> data = (Map<String, Object>) readWithMessageConverters(webRequest, mapParam, mapParam.getParameterType());
                if (data == null) {
                    log.warn("解析body为空json");
                    break all;
                }
                Map<String, String[]> paramMap = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (entry.getValue() == null) {
                        paramMap.put(entry.getKey(), null);
                        continue;
                    }
                    // 处理基本数据类型
                    if (ClassUtil.isSimpleValueType(entry.getValue().getClass())) {
                        paramMap.put(entry.getKey(), new String[]{entry.getValue().toString()});
                    }
                    // 处理数组类型
                    else if (entry.getValue() instanceof List) {
                        paramMap.put(entry.getKey(), ((List<Object>) entry.getValue())
                                .stream()
                                .map(s -> {
                                    if (s == null) {
                                        return null;
                                    }
                                    if (ClassUtil.isSimpleValueType(s.getClass())) {
                                        return s.toString();
                                    }
                                    return JSON.toJSONString(s);
                                }).toArray(String[]::new));
                    }
                    // 处理对象类型 数组类型
                    else if (entry.getValue() instanceof Map) {
                        // 将map转换为json放到[0]
                        paramMap.put(entry.getKey(), new String[]{JSON.toJSONString(entry.getValue())});
                    } else {
                        log.warn("无法解析复杂类型 value={},class={}", entry.getValue(), entry.getValue().getClass());
                    }
                }
                // 保留一份原始的json
                paramMap.put(ROOT_JSON_KEY, new String[]{JSON.toJSONString(data)});
                ServletWebRequest servletWebRequest = new ServletWebRequest(new IHttpServletRequest(httpServletRequest),
                        (HttpServletResponse) webRequest.getNativeResponse());
                servletWebRequest.getParameterMap().putAll(paramMap);
                cache.set(servletWebRequest);
            }
            webRequest = cache.get();
            // 处理对象类型
            ModelAttribute attribute = parameter.getParameterAnnotation(ModelAttribute.class);
            if (attribute != null) {
                String attributeJson;
                // 一级ModelAttribute时直接将整个json放入body
                if (StrUtil.isBlank(attribute.value())) {
                    String[] data = webRequest.getParameterMap().get(ROOT_JSON_KEY);
                    attributeJson = data == null ? null : data[0];
                }
                // 二级ModelAttribute（最多只能出现两级），在上面放到ParameterMap的json字符串拿出
                else {
                    //
                    if (Collection.class.isAssignableFrom(parameter.getParameterType())) {
                        String[] value = webRequest.getParameterValues(attribute.value());
                        if (value == null) {
                            attributeJson = "[]";
                        } else {
                            attributeJson = StrUtil.format("[{}]",
                                    String.join(",", value));
                        }
                    } else {
                        attributeJson = webRequest.getParameter(attribute.value());
                    }
                }
                // 拿到自定义的HttpServletRequest
                IHttpServletRequest iHttpServletRequest = (IHttpServletRequest) webRequest.getNativeRequest();
                // 将json字符串写入自定义的HttpServletRequest的body里
                iHttpServletRequest.setBody(attributeJson == null ? new byte[0] : attributeJson.getBytes());
                // 最后直接使用标准的RequestBody装载器装载参数
                // 使用RequestBody装载器装载可以使得复杂model也能完成装载
                return super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
            }
        }
        HandlerMethodArgumentResolver nowHandlerResolver = getArgumentResolver(parameter);
        Assert.notNull(nowHandlerResolver, String.format("未找到%s对应的HandlerMethodArgumentResolver", parameter));
        return nowHandlerResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        cache.remove();
    }

    private HandlerMethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
        HandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
        if (result == null) {
            for (HandlerMethodArgumentResolver methodArgumentResolver : this.argumentResolvers) {
                if (!(methodArgumentResolver instanceof ApiJsonParamResolver) &&
                        methodArgumentResolver.supportsParameter(parameter)
                ) {
                    result = methodArgumentResolver;
                    this.argumentResolverCache.put(parameter, result);
                    break;
                }
            }
        }
        return result;
    }
}

class IHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String[]> paramMap = new LinkedHashMap<>(32);
    private byte[] body = null;

    public IHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String[] o = paramMap.get(name);
        return o == null || o.length == 0 ? null : o[0];
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(paramMap.keySet());
    }

    /**
     * 重写getParameterMap方法，原生调用HTTPRequestServlet#getParameterMap被锁定，无法调用
     * 这里重写了只对HandlerMethodArgumentResolver使用getParameterMap解析参数有效，其他情况下改装载器无法装载
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return paramMap;
    }

    @Override
    public String[] getParameterValues(String name) {
        return paramMap.get(name);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.body == null) {
            return super.getInputStream();
        }
        return new ICoyoteInputStream(new ByteArrayInputStream(this.body), super.getInputStream());
    }
}

class ICoyoteInputStream extends ServletInputStream {

    private final ByteArrayInputStream byteArrayInputStream;

    private final ServletInputStream old;

    public ICoyoteInputStream(ByteArrayInputStream byteArrayInputStream, ServletInputStream old) {
        this.byteArrayInputStream = byteArrayInputStream;
        this.old = old;
    }

    @Override
    public boolean isFinished() {
        return old.isFinished();
    }

    @Override
    public boolean isReady() {
        return old.isReady();
    }

    @Override
    public void setReadListener(ReadListener listener) {
        old.setReadListener(listener);
    }


    @Override
    public int read(byte[] b) throws IOException {
        return byteArrayInputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return byteArrayInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return byteArrayInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return byteArrayInputStream.available();
    }

    @Override
    public void close() throws IOException {
        byteArrayInputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        byteArrayInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        byteArrayInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return byteArrayInputStream.markSupported();
    }

    @Override
    public int read() throws IOException {
        return byteArrayInputStream.read();
    }
}

