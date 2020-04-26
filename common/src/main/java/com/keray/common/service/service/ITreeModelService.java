package com.keray.common.service.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keray.common.BaseService;
import com.keray.common.service.CacheConstants;
import com.keray.common.service.model.base.BaseTreeModel;
import com.keray.common.IBaseEntity;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by keray
 * date:2019/8/16 16:17
 * 树形结构实体的操作服务
 */
public interface ITreeModelService<T extends BaseTreeModel> extends BaseService<T> {

    /**
     * <p>
     * <h3>>作者 keray</h3>
     * <h3>>时间： 2019/8/16 16:08</h3>
     * 具有上下级的实体 设置树结构
     * 仅当n=-1时缓存
     * </p>
     *
     * @param parent
     * @param n      获取多少级 -1 不限
     * @return <p>  </p>
     */
    @Cacheable(value = CacheConstants.TREE_DATA_CACHE, key = "#parent.id + #n")
    default T setChildren(T parent, int n) {
        try {
            return setChildren(parent, n, Wrappers.lambdaQuery((T) parent.getClass().newInstance()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default T setChildrenNoCache(T parent, int n) {
        try {
            return setChildren(parent, n, Wrappers.lambdaQuery((T) parent.getClass().newInstance()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * <p>
     * <h3>>作者 keray</h3>
     * <h3>>时间： 2019/8/16 16:08</h3>
     * 具有上下级的实体 设置树结构
     * </p>
     *
     * @param parent
     * @param n      获取多少级 -1 不限
     * @return <p>  </p>
     */
    default T setChildren(T parent, int n, LambdaQueryWrapper<T> lambdaQueryWrapper) {
        if (n == 0) {
            return parent;
        }
        List<T> children;
        try {
            LambdaQueryWrapper<T> queryWrapper = lambdaQueryWrapper.clone();
            children = getMapper().selectList(queryWrapper
                    .select(IBaseEntity::getId)
                    .eq(BaseTreeModel::getParentId, parent.getId()));
            if (CollUtil.isEmpty(children)) {
                return parent;
            }
            children = children.stream()
                    .parallel()
                    .map(c -> this.modelDetail(c.getId()))
                    .collect(Collectors.toList());
            parent.setChildren(children);
            for (T c : children) {
                // 深copy父节点 避免循环依赖
                T parentCopy = (T) parent.getClass().newInstance();
                BeanUtil.copyProperties(parent, parentCopy, "parent", "children");
                c.setParent(parentCopy);
                setChildren(c, n - 1, lambdaQueryWrapper);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return parent;
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/10/25 17:24</h3>
     * 属性结构下一级节点
     * </p>
     *
     * @param parent
     * @return <p> {@link List<String>} </p>
     * @throws
     */
    default List<String> selectNextChildrenIds(T parent) {
        try {
            return getMapper()
                    .selectList(
                            Wrappers.lambdaQuery((T) parent.getClass().newInstance())
                                    .select(IBaseEntity::getId)
                                    .eq(BaseTreeModel::getParentId, parent.getId()))
                    .stream()
                    .map(IBaseEntity::getId)
                    .collect(Collectors.toList());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }


    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/10/25 17:24</h3>
     * 属性结构下一级节点的id
     * </p>
     *
     * @param parent
     * @return <p> {@link List<T>} </p>
     * @throws
     */
    default List<T> selectNextChildren(T parent) {
        return selectNextChildrenIds(parent)
                .stream()
                .map(this::modelDetail)
                .collect(Collectors.toList());
    }


    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/10/25 17:24</h3>
     * 节点详情获取
     * </p>
     *
     * @return <p> {@link T} </p>
     * @throws
     */
    default T modelDetail(String id) {
        return getById(id);
    }
}
