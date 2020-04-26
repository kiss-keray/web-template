package com.keray.common.support;

import com.keray.common.annotation.ApiTimeRecord;
import com.keray.common.SysThreadPool;
import com.keray.common.support.api.time.dao.ApiTimeRecordDao;
import com.keray.common.support.api.time.model.ApiTimeRecordModel;
import com.keray.common.utils.CommonUtil;
import com.keray.common.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class ApiTimeInterceptor extends HandlerInterceptorAdapter {

    @Resource(name = "apiTimeRecord")
    private ApiTimeRecordDao<ApiTimeRecordModel> apiTimeRecordDao;

    private final ThreadLocal<TimeData> timeRecord = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = ((HandlerMethod) handler);
            ApiTimeRecord record = method.getMethodAnnotation(ApiTimeRecord.class);
            if (record == null) {
                record = CommonUtil.getClassAllAnnotation(((HandlerMethod) handler).getMethod().getDeclaringClass(), ApiTimeRecord.class);
            }
            if (record != null) {
                TimeData data = new TimeData();
                data.start = System.currentTimeMillis();
                timeRecord.set(data);
                // api接口开启了记录
                data.gt = record.gt();
                if ("".equals(record.value())) {
                    data.title = method.getMethod().getName();
                } else {
                    data.title = record.value();
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        TimeData data = timeRecord.get();
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
                    HandlerMethod handlerMethod = (HandlerMethod) handler;
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        timeRecord.remove();
        super.afterCompletion(request, response, handler, ex);
    }

    private static class TimeData {
        String title;
        int gt;
        long start;
        long end;

    }
}
