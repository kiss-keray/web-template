package com.keray.conf;

import com.keray.common.annotation.DiamondSupport;
import com.keray.common.service.service.impl.SysConfigService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

/**
 * @author by keray
 * date:2020/6/1 3:32 下午
 */
@Configuration
public class DiamondConfig extends SysConfigService {

    @DiamondSupport(name = "网站标题", key = "sys-web-title")
    @Getter
    @Setter
    private String webTitle;
}
