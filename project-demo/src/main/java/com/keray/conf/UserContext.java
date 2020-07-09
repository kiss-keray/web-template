package com.keray.conf;

import com.keray.common.IUserContext;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author by keray
 * date:2020/6/26 10:35 上午
 */
@Configuration
public class UserContext implements IUserContext {
    @Override
    public String currentUserId() {
        return null;
    }

    @Override
    public void setCurrentUserId(String userId) {

    }

    @Override
    public String currentMerchantsCode() {
        return null;
    }

    @Override
    public void setCurrentMerchantsCode(String merchantsCode) {

    }

    @Override
    public String currentIp() {
        return null;
    }

    @Override
    public HttpServletRequest currentRequest() {
        return null;
    }

    @Override
    public void setCurrentRequest(HttpServletRequest request) {

    }

    @Override
    public void setCurrentIp(String ip) {

    }
}
