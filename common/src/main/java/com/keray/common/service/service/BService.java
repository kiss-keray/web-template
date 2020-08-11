package com.keray.common.service.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keray.common.IBEntity;
import com.keray.common.IBMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author by keray
 * date:2019/7/25 16:03
 */
public interface BService<B extends IBEntity<B>> {
    /**
     * 获取基础模块操作mapper
     *
     * @return
     */
    IBMapper<B> getMapper();
    /**
     * 添加
     *
     * @param entity insert实体
     * @return
     */
    default Boolean insert(B entity) {
        return getMapper().insert(entity) == 1;
    }

    /**
     * <p>
     * 插入（批量）
     * </p>
     *
     * @param entityList 实体对象列表
     * @param batchSize  插入批次数量
     * @return boolean
     */
    default boolean insertBatch(List<B> entityList, int batchSize) {
        return getMapper().insertBatch(entityList, batchSize);
    }

    @Transactional(rollbackFor = Exception.class)
    default boolean insertBatch(List<B> entityList) {
        return getMapper().insertBatch(entityList, 50);
    }

    /**
     * 分页查询
     *
     * @param pager 分页参数
     * @return 分页数据
     */
    default IPage<B> page(Page<B> pager) {
        return this.page(pager, null);
    }

    /**
     * 分页查询
     *
     * @param pager        分页参数
     * @param queryWrapper
     * @return 分页数据
     */
    default IPage<B> page(Page<B> pager, @Param(Constants.WRAPPER) Wrapper<B> queryWrapper) {
        return getMapper().selectPage(pager, queryWrapper);
    }


    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    default List<B> selectList(@Param(Constants.WRAPPER) Wrapper<B> queryWrapper){
        return getMapper().selectList(queryWrapper);
    }


    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    default B selectOne(@Param(Constants.WRAPPER) Wrapper<B> queryWrapper){
        return getMapper().selectOne(queryWrapper);
    }

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    default Integer selectCount(@Param(Constants.WRAPPER) Wrapper<B> queryWrapper){
        return getMapper().selectCount(queryWrapper);
    }


}
