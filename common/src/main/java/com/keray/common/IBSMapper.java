package com.keray.common;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author by keray
 * date:2020/7/15 9:39 上午
 */
public interface IBSMapper<BS extends IBSEntity<BS, ID>, ID extends Serializable> extends IBMapper<BS> {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return
     */
    @Override
    int insert(BS entity);

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象
     * @return
     */
    @Override
    int updateById(@Param(Constants.ENTITY) BS entity);


    @Override
    BS selectById(Serializable id);

    default boolean contains(String id) {
        return selectCount(Wrappers.<BS>query().eq("id", id)) == 1;
    }

    /**
     * 删除（根据ID 批量删除）
     *
     * @param id 主键ID列表(不能为 null 以及 empty)
     */
    @Override
    default int deleteById(Serializable id) {
        return this.delete(Wrappers.<BS>update().eq("id", id));
    }

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    @Override
    default int deleteBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList) {
        return this.delete(Wrappers.<BS>update().in("id", idList));
    }

    default Boolean canDelete(@Param("ids") Collection<? extends Serializable> ids) {
        return true;
    }
}
