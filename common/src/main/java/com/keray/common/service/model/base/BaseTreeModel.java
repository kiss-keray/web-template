package com.keray.common.service.model.base;

import com.keray.common.IBaseEntity;

import java.util.List;

/**
 * @author by keray
 * date:2019/8/16 16:15
 * 树形实体接口
 */
public interface BaseTreeModel<T extends BaseTreeModel> extends IBaseEntity {

    String getParentId();

    T getParent();

    void setChildren(List<T> children);

    void setParent(T parent);
}
