package com.keray.aliyun.oss;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author by keray
 * date:2020/9/19 9:51 上午
 */

@Configuration
@ConditionalOnProperty("plugins.aliyun.oss.endpoint")
@ConfigurationProperties(prefix = "plugins.aliyun.oss" ,ignoreUnknownFields = false)
@Data
public class OssConfig {
    private String endpoint;

    private String bucketName;
    private String basePath;

    private Integer pollCount = 10;
    private Integer pollMax = 1000;
}
