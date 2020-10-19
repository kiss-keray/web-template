package com.keray.aliyun;

import com.keray.IPlugins;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * @author by keray
 * date:2020/9/24 9:59 下午
 */
public class AliyunPlugins implements IPlugins {
    @Autowired(required = false)
    protected AliyunConfig aliyunConfig;
}
