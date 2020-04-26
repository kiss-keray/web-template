package com.keray.common.service.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keray.common.BaseEntity;
import com.keray.common.BaseService;
import com.keray.common.CommonResultCode;
import com.keray.common.exception.BizRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by keray
 * date:2019/7/25 16:19
 */
@Slf4j
public abstract class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {

    @Override
    public Boolean insert(T entity) {
        return getMapper().insert(entity) == 1;
    }

    @Override
    public Boolean update(T entity) {
        if (StrUtil.isBlank(entity.getId())) {
            throw new BizRuntimeException("update必须拥有Id", CommonResultCode.dataChangeError.getCode());
        }
        return getMapper().updateById(entity) == 1;
    }

    @Override
    public Boolean delete(String id) {
        if (!this.canDelete(Collections.singletonList(id))) {
            throw new BizRuntimeException(CommonResultCode.dataNotAllowDelete);
        }
        return getMapper().deleteById(id) == 1;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean delete(Collection<? extends Serializable> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new BizRuntimeException(CommonResultCode.illegalArgument);
        }
        if (ids.size() == 1) {
            return delete((String) ((List) ids).get(0));
        }
        ids = ids.stream().distinct().collect(Collectors.toList());
        if (!this.canDelete(ids)) {
            throw new BizRuntimeException(CommonResultCode.dataNotAllowDelete);
        }
        if (getMapper().deleteBatchIds(ids) != ids.size()) {
            throw new BizRuntimeException(CommonResultCode.dataChangeError);
        }
        return true;
    }

    @Override
    public T getById(String id) {
        return getMapper().selectById(id);
    }

    @Override
    public IPage<T> page(Page<T> pager) {
        return this.page(pager, null);
    }

    @Override
    public IPage<T> page(Page<T> pager, Wrapper<T> queryWrapper) {
        return getMapper().selectPage(this.pageProcessing(pager), queryWrapper);
    }

    @Override
    public List<T> selectList(Wrapper<T> queryWrapper) {
        return getMapper().selectList(this.wrapperProcessing(queryWrapper));
    }

    @Override
    public T selectOne(Wrapper<T> queryWrapper) {
        return getMapper().selectOne(queryWrapper);
    }

    @Override
    public Integer selectCount(Wrapper<T> queryWrapper) {
        return getMapper().selectCount(queryWrapper);
    }

    @Override
    public boolean contains(String id) {
        return getMapper().contains(id);
    }


}
