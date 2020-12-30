package com.keray.common.service;

import com.keray.common.BaseService;
import com.keray.common.IBaseEntity;
import com.keray.common.SpringContextHolder;

/**
 * @author by keray
 * date:2019/10/9 14:38
 */
public class TypeServiceRouter {

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/10/9 14:56</h3>
     * 获取通用类型 serviceBean
     * </p>
     *
     * @param typeEnum
     * @return <p> {@link BaseService <T>} </p>
     * @throws
     */
    public static <T extends IBaseEntity, S extends BaseService<T>> S router(TypeEnum<T, S> typeEnum) {
        return SpringContextHolder.getBean(typeEnum.getServiceBeanName());
    }


}
