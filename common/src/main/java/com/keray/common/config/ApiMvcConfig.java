package com.keray.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author by keray
 * date:2020/1/10 3:33 PM
 */
@Slf4j
@ConditionalOnProperty(name = "api.json.open", havingValue = "true")
@ConfigurationProperties(prefix = "api.json", ignoreInvalidFields = true)
@AutoConfigureAfter(RequestMappingHandlerAdapter.class)
@Configuration
public class ApiMvcConfig {

    @Resource(name = "requestMappingHandlerAdapter")
    @Lazy
    private RequestMappingHandlerAdapter adapter;

    @Getter
    @Setter
    private Boolean jsonFormat = false;

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/3 14:50</h3>
     * 添加自定义ApiJsonParam装载器
     * </p>
     *
     * @param
     * @return <p> {@link } </p>
     * @throws
     */
    @Bean(name = "apiJsonParamResolver")
    public ApiJsonParamResolver apiJsonParamResolver(@Qualifier("kObjectMapper") ObjectMapper objectMapper) {
        configBug(objectMapper);
        List<HandlerMethodArgumentResolver> resolvers = new LinkedList<>();
        ApiJsonParamResolver apiJsonParamResolver = new ApiJsonParamResolver(adapter.getMessageConverters(), resolvers, jsonFormat);
        resolvers.add(apiJsonParamResolver);
        resolvers.add(new PageableHandlerMethodArgumentResolver());
        if (adapter.getArgumentResolvers() == null) {
            log.warn("adapter 没有原生的装载器");
        } else {
            resolvers.addAll(adapter.getArgumentResolvers());
        }
        adapter.setArgumentResolvers(resolvers);
        return apiJsonParamResolver;
    }


    public void configBug(ObjectMapper objectMapper) {
        // （原有：项目原先混乱的结构导致直接注入的支持LocalDatetime的JavaTimeModule被不支持的覆盖掉，这里就直接替换了MappingJackson2HttpMessageConverter）
        // 替换
        for (HttpMessageConverter c : adapter.getMessageConverters()) {
            if (c instanceof MappingJackson2HttpMessageConverter) {
                if (!Collections.replaceAll(adapter.getMessageConverters(), c, new MappingJackson2HttpMessageConverter(objectMapper))) {
                    throw new RuntimeException("添加localDatetime转换器异常");
                }
                break;
            }
        }
        // 交换xml json解析器
        int a = -1, b = -1;
        for (int i = 0; i < adapter.getMessageConverters().size(); i++) {
            if (adapter.getMessageConverters().get(i) instanceof MappingJackson2HttpMessageConverter) {
                a = i;
            }
            if (adapter.getMessageConverters().get(i) instanceof MappingJackson2XmlHttpMessageConverter) {
                b = i;
            }
        }
        if (a != -1 && b != -1 && a > b) {
            HttpMessageConverter<?> mappingJackson2HttpMessageConverter = adapter.getMessageConverters().get(a);
            adapter.getMessageConverters().set(a, adapter.getMessageConverters().get(b));
            adapter.getMessageConverters().set(b, mappingJackson2HttpMessageConverter);
        }
        adapter.getMessageConverters().add(addConverter());
    }

    public BufferedImageHttpMessageConverter addConverter() {
        return new BufferedImageHttpMessageConverter();
    }
}
