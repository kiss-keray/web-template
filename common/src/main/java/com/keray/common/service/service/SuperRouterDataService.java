package com.keray.common.service.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keray.common.IBaseMapper;
import com.keray.common.IBaseEntity;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author by keray
 * date:2019/12/6 2:56 PM
 */
public interface SuperRouterDataService<T extends IBaseEntity> {
    <R extends T> IBaseMapper<R> routerMapper(T superModel);

    <R extends T> R routerEntity(T superModel);

    <R extends T> T routerEntityTrans(R entity);

    /**
     * 添加
     *
     * @param entity insert实体
     * @return
     */
    Boolean insert(T entity);

    /**
     * 修改
     *
     * @param entity update实体
     * @return
     */
    Boolean update(T entity);

    /**
     * 删除
     *
     * @param id 实体id
     * @return
     */
    Boolean delete(T superModel, String id);

    /**
     * 批量逻辑删除
     *
     * @param ids 实体id
     * @return
     */
    Boolean delete(T superModel, Collection<? extends Serializable> ids);

    /**
     * 通过id查询数据
     *
     * @param id 实体id
     * @return T
     */
    T getById(T superModel, String id);

    /**
     * 分页查询
     *
     * @param pager 分页参数
     * @return 分页数据
     */
    IPage<T> page(T superModel, Page<T> pager);


    /**
     * 分页查询
     *
     * @param pager        分页参数
     * @param queryWrapper
     * @return 分页数据
     */
    IPage<T> page(T superModel, Page<T> pager, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);


    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    List<T> selectList(T superModel, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);


    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    T selectOne(T superModel, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    Integer selectCount(T superModel, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

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
    boolean contains(T superModel, String id);

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
    default Boolean canDelete(T superModel, Collection<? extends Serializable> keys) {
        return true;
    }
}
