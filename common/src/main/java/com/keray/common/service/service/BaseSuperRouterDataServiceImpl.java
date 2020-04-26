package com.keray.common.service.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keray.common.BaseEntity;
import com.keray.common.CommonResultCode;
import com.keray.common.exception.BizRuntimeException;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by keray
 * date:2019/12/6 3:32 PM
 */
public abstract class BaseSuperRouterDataServiceImpl<T extends BaseEntity> implements SuperRouterDataService<T> {


    @Override
    public Boolean insert(T entity) {
        return routerMapper(entity).insert(routerEntity(entity)) == 1;
    }

    @Override
    public Boolean update(T entity) {
        if (StrUtil.isBlank(entity.getId())) {
            throw new BizRuntimeException("update必须拥有Id", CommonResultCode.dataChangeError.getCode());
        }
        return routerMapper(entity).updateById(routerEntity(entity)) == 1;
    }

    @Override
    public Boolean delete(T superModel, String id) {
        if (!this.canDelete(superModel, Collections.singletonList(id))) {
            throw new BizRuntimeException(CommonResultCode.dataNotAllowDelete);
        }
        return routerMapper(superModel).deleteById(id) == 1;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean delete(T superModel, Collection<? extends Serializable> ids) {
        if (!this.canDelete(superModel, ids)) {
            throw new BizRuntimeException(CommonResultCode.dataNotAllowDelete);
        }
        if (routerMapper(superModel).deleteBatchIds(ids) != ids.size()) {
            throw new BizRuntimeException(CommonResultCode.dataNotAllowDelete);
        }
        return true;
    }


    @Override
    public T getById(T superModel, String id) {
        return routerEntityTrans(routerMapper(superModel).selectById(id));
    }

    @Override
    public IPage<T> page(T superModel, Page<T> pager) {
        return routerMapper(superModel).selectPage(pager, null)
                .convert(this::routerEntityTrans);
    }

    @Override
    public IPage<T> page(T superModel, Page<T> pager, Wrapper<T> queryWrapper) {
        return routerMapper(superModel).selectPage(pager, queryWrapper)
                .convert(this::routerEntityTrans);
    }

    @Override
    public List<T> selectList(T superModel, Wrapper<T> queryWrapper) {
        return routerMapper(superModel).selectList(queryWrapper)
                .stream()
                .map(this::routerEntityTrans)
                .collect(Collectors.toList());
    }

    @Override
    public T selectOne(T superModel, Wrapper<T> queryWrapper) {
        return routerEntityTrans(routerMapper(superModel).selectOne(queryWrapper));
    }

    @Override
    public Integer selectCount(T superModel, Wrapper<T> queryWrapper) {
        return routerMapper(superModel).selectCount(queryWrapper);
    }

    @Override
    public boolean contains(T superModel, String id) {
        return routerMapper(superModel).contains(id);
    }
}
