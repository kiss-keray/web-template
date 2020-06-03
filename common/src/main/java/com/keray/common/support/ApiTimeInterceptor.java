package com.keray.common.support;

import com.keray.common.SysThreadPool;
import com.keray.common.annotation.ApiTimeRecord;
import com.keray.common.config.ServletInvocableHandlerMethodCallback;
import com.keray.common.config.ServletInvocableHandlerMethodHandler;
import com.keray.common.support.api.time.dao.ApiTimeRecordDao;
import com.keray.common.support.api.time.model.ApiTimeRecordModel;
import com.keray.common.utils.CommonUtil;
import com.keray.common.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author by keray
 * date:2019/12/4 2:21 PM
 * 记录api接口调用时长
 */
@Component("apiTimeInterceptor")
@ConditionalOnProperty(value = "keray.api.time", havingValue = "true")
@Slf4j
public class ApiTimeInterceptor implements ServletInvocableHandlerMethodHandler {

    @Resource(name = "apiTimeRecord")
    private ApiTimeRecordDao apiTimeRecordDao;


    @Override
    public Integer order() {
        return 0;
    }

    @Override
    public Object work(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request, ServletInvocableHandlerMethodCallback callback) throws Exception {
        TimeData data = preHandle(handlerMethod);
        try {
            return callback.get(this);
        } finally {
            postHandle(data, request.getNativeRequest(HttpServletRequest.class) , handlerMethod);
        }
    }

    public TimeData preHandle(HandlerMethod handler) {
        ApiTimeRecord record = handler.getMethodAnnotation(ApiTimeRecord.class);
        if (record == null) {
            record = CommonUtil.getClassAllAnnotation(handler.getMethod().getDeclaringClass(), ApiTimeRecord.class);
        }
        if (record != null) {
            TimeData data = new TimeData();
            data.start = System.currentTimeMillis();
            // api接口开启了记录
            data.gt = record.gt();
            if ("".equals(record.value())) {
                data.title = handler.getMethod().getName();
            } else {
                data.title = record.value();
            }
            return data;
        }
        return null;
    }

    private void postHandle(TimeData data, HttpServletRequest request,  HandlerMethod handlerMethod) {
        if (data != null) {
            data.end = System.currentTimeMillis();
            String url;
            try {
                url = request.getRequestURL().toString();
            } catch (Exception ignore) {
                url = "异常";
            }
            String finalUrl = url;
            SysThreadPool.execute(() -> {
                if (data.end - data.start > data.gt) {
                    apiTimeRecordDao.insert(ApiTimeRecordModel.builder()
                            .execTime((int) (data.end - data.start))
                            .gt(data.gt)
                            .title(data.title)
                            .url(finalUrl)
                            .time(TimeUtil.DATE_TIME_FORMATTER_MS.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(data.start), ZoneId.systemDefault())))
                            .methodPath(handlerMethod.getMethod().getDeclaringClass().getName() + "#" + handlerMethod.getMethod().getName())
                            .build());
                }
            }, false);
        }
    }

    private static class TimeData {
        String title;
        int gt;
        long start;
        long end;

    }
}
