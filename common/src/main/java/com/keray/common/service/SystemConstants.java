package com.keray.common.service;

/**
 * @author by keray
 * date:2019/9/4 16:03
 * 系统全局变量
 */
public class SystemConstants {
    /**
     * 系统全局最高商户code
     */
    public static final String SYSTEM_MERCHANTS_CODE = "system";


    /**
     * 一天时间的毫秒数
     */
    public static final int ONE_DAY_MS = 24 * 60 * 60 * 1000;
    /**
     * 一小时的毫秒数
     */
    public static final int ONE_HOURS_MS = 60 * 60 * 1000;
    /**
     * 一分钟的毫秒数
     */
    public static final int ONE_MINUTES_MS = 60 * 1000;


    /**
     * diamond 更新通知
     */
    public static final String DIAMOND_MQ_ROUTER_KEY = "diamond-mq.#";

    /**
     * diamond 更新通知
     */
    public static final String SYSTEM_CURRENT_USER_ID = "system";

    /**
     * diamond mq路由
     */
    public static final String DIAMOND_MQ_EXCHANGE = "c.s.d.diamond";


}
