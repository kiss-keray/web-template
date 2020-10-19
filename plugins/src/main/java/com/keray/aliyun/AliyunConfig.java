package com.keray.aliyun;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author by keray
 * date:2020/9/24 9:58 下午
 */
@Configuration
@ConditionalOnProperty(value = "plugins.aliyun.access-key-id")
@ConfigurationProperties(prefix = "plugins.aliyun")
@Data
public class AliyunConfig {
    private String accessKeyId;
    private String accessKeySecret;
}
