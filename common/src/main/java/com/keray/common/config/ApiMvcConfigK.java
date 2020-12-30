package com.keray.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


/**
 * @author 11723
 */
@Slf4j
@Configuration("apiSpringMvcConfig")
@ConditionalOnProperty(name = "keray.api.json.open", havingValue = "true")
public class ApiMvcConfigK implements WebMvcConfigurer {

    @Resource(name = "apiJsonParamResolver")
    @Lazy
    private ApiJsonParamResolver apiJsonParamResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiJsonParamResolver);
    }

}
