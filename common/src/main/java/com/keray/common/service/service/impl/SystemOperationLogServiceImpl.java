package com.keray.common.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.keray.common.BaseService;
import com.keray.common.IBaseMapper;
import com.keray.common.annotation.SystemOperationLogAop;
import com.keray.common.service.ienum.SystemOperationAction;
import com.keray.common.service.mapper.SystemOperationLogMapper;
import com.keray.common.service.model.SystemOperationLogModel;
import com.keray.common.service.service.ISystemOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author by keray
 * date:2019/8/6 14:39
 */
@Slf4j
@Service("v3SystemOperationLogService")
@Aspect
@ConditionalOnProperty(value = "keray.operation-log",havingValue = "true")
public class SystemOperationLogServiceImpl implements BaseService<SystemOperationLogModel>, ISystemOperationLogService {
    @Resource
    private SystemOperationLogMapper systemOperationLogMapper;

    @Override
    public void log(String tableName, String operationName, SystemOperationAction action, String remark) {
        try {
            systemOperationLogMapper.insert(SystemOperationLogModel.builder()
                    .tableName(tableName)
                    .remark(remark)
                    .operationName(operationName)
                    .systemOperationAction(action)
                    .build());
        } catch (Exception e) {
            log.error("系统操作记录添加失败 tableName={} action={} remark={} exception={}", tableName, action.getDesc(), remark, e);
        }
    }

    @Override
    public IBaseMapper<SystemOperationLogModel> getMapper() {
        return systemOperationLogMapper;
    }


    //===========================================中间表操作记录===================================================//

    @Pointcut("@annotation(com.keray.common.annotation.SystemOperationLogAop)")
    public void systemOperationLogAop() {
    }

    @Before("systemOperationLogAop()")
    public void systemOperationLogAopBefore(JoinPoint joinPoint) {
        try {
            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) joinPoint;
            Field proxy = methodPoint.getClass().getDeclaredField("methodInvocation");
            proxy.setAccessible(true);
            ReflectiveMethodInvocation j = (ReflectiveMethodInvocation) proxy.get(methodPoint);
            Method method = j.getMethod();
            SystemOperationLogAop systemOperationLogAop = method.getAnnotation(SystemOperationLogAop.class);
            Object[] args = joinPoint.getArgs();
            String positionId = (String) args[0];
            List<String> actionIds = (List<String>) args[1];
            this.log(
                    systemOperationLogAop.tableName(),
                    systemOperationLogAop.operationName(),
                    systemOperationLogAop.action(),
                    StrUtil.format("{} -> {}", positionId, actionIds.toString())
            );
        } catch (Exception e) {
            log.error("aop记录异常 args={},e={}", joinPoint.getArgs(), e);
        }
    }

    @After("systemOperationLogAop()")
    public void systemOperationLogAopAfter() {

    }
}
