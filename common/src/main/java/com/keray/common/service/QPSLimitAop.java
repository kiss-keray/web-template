package com.keray.common.service;

import com.keray.common.IUserContext;
import com.keray.common.annotation.QPSLimitSupport;
import com.keray.common.utils.QpsLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author by keray
 * date:2019/10/10 14:33
 */

@Aspect
@Service(value = "QPSLimitAop")
@Slf4j
public class QPSLimitAop {

    @Resource(name = "threadLocalUserContext")
    private IUserContext userContext;

    @Pointcut("@annotation(com.keray.common.annotation.QPSLimitSupport)")
    public void qpsAop() {
    }

    @Around("qpsAop()")
    public Object setSysSchedule(ProceedingJoinPoint pjp) throws Throwable {
        try {
            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) pjp;
            Field proxy = methodPoint.getClass().getDeclaredField("methodInvocation");
            proxy.setAccessible(true);
            ReflectiveMethodInvocation j = (ReflectiveMethodInvocation) proxy.get(methodPoint);
            Method method = j.getMethod();
            QPSLimitSupport limitSupport = method.getAnnotation(QPSLimitSupport.class);
            String key = getKey(limitSupport);
            QpsLimit.acceptLimit(key, limitSupport.qps(), limitSupport.strategy(), limitSupport.waitTime(), limitSupport.width());
            return pjp.proceed();
        } catch (Throwable e) {
            throw e;
        }
    }

    private String getKey(QPSLimitSupport limitSupport) {
        if (limitSupport.global()) {
            return limitSupport.key();
        }
        if (userContext.currentUserId() == null) {
            return userContext.currentRequest() == null ? limitSupport.key() : limitSupport.key() + userContext.currentRequest().getSession().getId();
        }
        return limitSupport.key() + userContext.currentUserId();
    }

}
