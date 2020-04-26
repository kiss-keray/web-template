package com.keray.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keray.common.exception.BizRuntimeException;
import com.keray.common.service.model.SortBaseModel;
import com.keray.common.service.service.SortService;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author by keray
 * date:2019/7/25 16:03
 */
public interface BaseService<T extends IBaseEntity> {
    /**
     * 获取基础模块操作mapper
     *
     * @return
     */
    IBaseMapper<T> getMapper();

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
     * 基础实体修改
     * 不推荐重写
     *
     * @param entity update实体
     * @return
     */
    default boolean simpleUpdate(T entity) {
        if (StrUtil.isBlank(entity.getId())) {
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
    Boolean delete(String id);

    /**
     * 批量逻辑删除
     *
     * @param ids 实体id
     * @return
     */
    Boolean delete(Collection<? extends Serializable> ids);

    /**
     * 通过id查询数据
     *
     * @param id 实体id
     * @return T
     */
    T getById(String id);

    /**
     * 分页查询
     *
     * @param pager 分页参数
     * @return 分页数据
     */
    IPage<T> page(Page<T> pager);


    /**
     * 分页查询
     *
     * @param pager        分页参数
     * @param queryWrapper
     * @return 分页数据
     */
    IPage<T> page(Page<T> pager, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);


    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);


    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return
     */
    T selectOne(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    Integer selectCount(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

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
    boolean contains(String id);

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


    default <E extends BaseEntity> Page<E> pageProcessing(Page<E> page) {
        if (this instanceof SortService) {
            List<OrderItem> orders = page.orders();
            if (CollUtil.isEmpty(orders)) {
                if (orders.stream().noneMatch(c -> "sort".equals(c.getColumn()))) {
                    page.setOrders(Collections.singletonList(OrderItem.asc("sort")));
                }
            } else {
                LinkedList<OrderItem> newOrders = new LinkedList<>(orders);
                if (orders.stream().noneMatch(c -> "sort".equals(c.getColumn()))) {
                    newOrders.add(OrderItem.asc("sort"));
                }
                newOrders.add(OrderItem.desc("create_time"));
                page.setOrders(newOrders);
            }
        }
        return page;
    }
    default <E extends IBaseEntity> Wrapper<E> wrapperProcessing(Wrapper<E> wrapper) {
        if (this instanceof SortService) {
            if (wrapper == null) {
                wrapper =  Wrappers.lambdaQuery();
            }
            if (wrapper instanceof QueryWrapper) {
                ((QueryWrapper<E>) wrapper).orderByAsc("sort");
            } else if (wrapper instanceof LambdaQueryWrapper) {
                ((LambdaQueryWrapper<SortBaseModel>) wrapper).orderByAsc(SortBaseModel::getSort);
            }
        }
        return wrapper;
    }
}
