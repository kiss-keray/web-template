package com.keray;

import com.alibaba.fastjson.parser.ParserConfig;
import com.keray.common.SysThreadPool;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan(value = {
        "com.keray.common.service.mapper"
})
@EnableConfigurationProperties
@EnableCaching
@RestController
public class WebStart {


    public static void main(String[] args) {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        SpringApplication.run(WebStart.class, args);
    }

    @EventListener
    public void started(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            System.out.println("addp-app-start-success");
        } else if (applicationEvent instanceof ApplicationFailedEvent) {
            System.out.println("addp-app-start-fail");
        } else if (applicationEvent instanceof ContextClosedEvent) {
            SysThreadPool.close();
        }
    }

    @GetMapping("/check-health")
    public String checkHealth() {
        return "ok";
    }
}
