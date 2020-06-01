package com.keray.common.service;

import com.keray.common.BaseService;
import com.keray.common.IBaseEntity;

/**
 * @author by keray
 * date:2019/10/9 14:40
 */
public interface TypeEnum<T extends IBaseEntity,S extends BaseService<T>> {
    String getServiceBeanName();
}
