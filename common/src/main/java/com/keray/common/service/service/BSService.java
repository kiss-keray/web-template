package com.keray.common.service.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keray.common.CommonResultCode;
import com.keray.common.IBSEntity;
import com.keray.common.IBSMapper;
import com.keray.common.exception.BizRuntimeException;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by keray
 * date:2019/7/25 16:03
 */
public interface BSService<BS extends IBSEntity<BS, ID>, ID extends Serializable> extends BService<BS> {
    /**
     * 获取基础模块操作mapper
     *
     * @return
     */
    IBSMapper<BS, ID> getMapper();


    /**
     * 修改
     *
     * @param entity update实体
     * @return
     */
    default Boolean update(BS entity) {
        if (ObjectUtil.isEmpty(entity.getId())) {
            throw new BizRuntimeException("update必须拥有Id", CommonResultCode.dataChangeError.getCode());
        }
        return getMapper().updateById(entity) == 1;
    }

    /**
     * 基础实体修改
     * 不推荐重写
     *
     * @param entity update实体
     * @return
     */
    default boolean simpleUpdate(BS entity) {
        if (ObjectUtil.isEmpty(entity.getId())) {
            throw new BizRuntimeException("update必须拥有Id", CommonResultCode.dataChangeError.getCode());
        }
        return getMapper().updateById(entity) == 1;
    }

    /**
     * 删除
     *
     * @param id 实体id
     * @return
     */
    default Boolean delete(String id) {
        if (!this.canDelete(Collections.singletonList(id))) {
            throw new BizRuntimeException(CommonResultCode.dataNotAllowDelete);
        }
        return getMapper().deleteById(id) == 1;
    }

    /**
     * 批量逻辑删除
     *
     * @param ids 实体id
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    default Boolean delete(Collection<? extends Serializable> ids) {
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

    /**
     * 通过id查询数据
     *
     * @param id 实体id
     * @return T
     */
    default BS getById(String id) {
        return getMapper().selectById(id);
    }

    /**
     * 分页查询
     *
     * @param pager 分页参数
     * @return 分页数据
     */
    default IPage<BS> page(Page<BS> pager) {
        return this.page(pager, null);
    }

    /**
     * 分页查询
     *
     * @param pager        分页参数
     * @param queryWrapper
     * @return 分页数据
     */
    default IPage<BS> page(Page<BS> pager, @Param(Constants.WRAPPER) Wrapper<BS> queryWrapper) {
        return getMapper().selectPage(pager, queryWrapper);
    }


    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    default List<BS> selectList(@Param(Constants.WRAPPER) Wrapper<BS> queryWrapper) {
        return getMapper().selectList(queryWrapper);
    }


    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    default BS selectOne(@Param(Constants.WRAPPER) Wrapper<BS> queryWrapper) {
        return getMapper().selectOne(queryWrapper);
    }

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    default Integer selectCount(@Param(Constants.WRAPPER) Wrapper<BS> queryWrapper) {
        return getMapper().selectCount(queryWrapper);
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/16 14:14</h3>
     * 是否存在id
     * </p>
     *
     * @param id
     * @return <p> {@link boolean} </p>
     * @throws
     */
    default boolean contains(String id) {
        return getMapper().contains(id);
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/11/25 10:24 AM</h3>
     * 实体删除时校验是否能被删除
     * </p>
     *
     * @param keys ids | codes | 其他  子类自行实现
     * @return <p> {@link boolean} </p>
     * @throws
     */
    default Boolean canDelete(Collection<? extends Serializable> keys) {
        return getMapper().canDelete(keys);
    }
}
