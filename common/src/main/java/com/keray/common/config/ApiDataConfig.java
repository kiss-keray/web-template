package com.keray.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author by keray
 * date:2019/10/22 15:21
 */
@Configuration
@ConfigurationProperties(prefix = "api.data", ignoreInvalidFields = true)
@Data
public class ApiDataConfig {

    private Integer secondScope = 48 * 3600;

    private Integer minuteScope = 7 * 24 * 60;

    private Integer hourScope = 90 * 24;

    private Integer dayScope = 365;

    private Integer hourSyncMinute = 5;

    private Integer daySyncMinute = 10;

    private String apiDataSecondKey = "api:data:second:";

    private String apiDataMinuteKey = "api:data:minute:";

    private String apiDataHourKey = "api:data:hour:";

    private String apiDataDayKey = "api:data:day:";
}
