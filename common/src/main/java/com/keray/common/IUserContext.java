package com.keray.common;

import cn.hutool.core.map.MapUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author by keray
 * date:2019/7/26 14:21
 */
public interface IUserContext {
    /**
     * 获取当前登陆用户的Id
     *
     * @return 用户Id
     */
    String currentUserId();

    /**
     * 设置当前用户Id
     *
     * @param userId
     */
    void setCurrentUserId(String userId);

    /**
     * 当前商户coded
     *
     * @return 当前商户Code
     */
    String currentMerchantsCode();

    /**
     * 设置当前商户code
     *
     * @param merchantsCode
     */
    void setCurrentMerchantsCode(String merchantsCode);

    /**
     * 当前使用Ip
     *
     * @return 当前IP
     */
    String currentIp();

    HttpServletRequest currentRequest();

    void setCurrentRequest(HttpServletRequest request);

    /**
     * 设置当前Ip
     *
     * @param ip
     */
    void setCurrentIp(String ip);

    default Map<String, Object> export() {
        return MapUtil.<String, Object>builder()
                .put("userId", currentUserId())
                .put("ip", currentIp())
                .put("merchantCode", currentMerchantsCode())
                .build();
    }

    default void importConf(@NotNull Map<String, Object> map) {
        if (map == null) {
            return;
        }
        setCurrentUserId((String) map.get("userId"));
        setCurrentIp((String) map.get("ip"));
        setCurrentMerchantsCode((String) map.get("merchantCode"));
    }

    default void clear() {
        setCurrentMerchantsCode(null);
        setCurrentIp(null);
        setCurrentUserId(null);
    }
}
