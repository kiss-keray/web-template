package com.keray.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.TypeUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.keray.common.annotation.BaseDbInsert;
import com.keray.common.annotation.BaseDbUpdateModel;
import com.keray.common.annotation.BaseDbUpdateWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author by keray
 * date:2019/7/25 16:56
 */
public interface IBaseMapper<T extends IBaseEntity> extends BaseMapper<T> {

    Map<Class<?>, IBaseEntity> DELETE_CACHE = new ConcurrentHashMap<>(128);
    AtomicReference<IUserContext> USER_CONTEXT_ATOMIC_REFERENCE = new AtomicReference<>();

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象
     * @return
     */
    @Override
    @BaseDbUpdateModel
    int updateById(@Param(Constants.ENTITY) T entity);

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        实体对象 (set 条件值,可以为 null)
     * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     * @return
     */
    @Override
    @BaseDbUpdateWrapper
    int update(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/10/24 17:43</h3>
     * 查询sum
     * </p>
     *
     * @param wrapper
     * @return <p> {@link String} </p>
     * @throws
     */
    Double selectSum(@Param(Constants.WRAPPER) Wrapper<T> wrapper);

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return
     */
    @Override
    @BaseDbInsert
    int insert(T entity);

    @Override
    T selectById(Serializable id);

    default boolean contains(String id) {
        return selectCount(Wrappers.<T>query().eq("id", id)) == 1;
    }

    /**
     * 删除（根据ID 批量删除）
     *
     * @param id 主键ID列表(不能为 null 以及 empty)
     */
    @Override
    default int deleteById(Serializable id) {
        return this.delete(Wrappers.<T>update().eq("id", id));
    }

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    @Override
    default int deleteBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList) {
        return this.delete(Wrappers.<T>update().in("id", idList));
    }


    @Override
    default int delete(Wrapper<T> wrapper) {
        IBaseEntity entityV3 = this.getDeleteEntity();
        if (wrapper instanceof LambdaUpdateWrapper) {
            ((LambdaUpdateWrapper<T>) wrapper).setSql("deleted = 1");
        } else if (wrapper instanceof UpdateWrapper) {
            ((UpdateWrapper<T>) wrapper).set("deleted", true);
        } else {
            throw new IllegalArgumentException("delete只允许传入UpdateWrapper类型 now= " + wrapper.getClass());
        }
        return this.update((T) entityV3, wrapper);
    }

    default Boolean canDelete(@Param("ids") Collection<? extends Serializable> ids) {
        return true;
    }

    /**
     * 根据 columnMap 条件，删除记录
     *
     * @param columnMap 表字段 map 对象
     */
    @Override
    default int deleteByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap) {
        throw new RuntimeException("不支持");
    }

    /**
     * <p>
     * <h3>作者 keray</h3>
     * <h3>时间： 2019/9/4 21:20</h3>
     * 获取mapper下删除对象
     * </p>
     *
     * @param
     * @return <p> {@link IBaseEntity} </p>
     * @throws
     */
    default IBaseEntity getDeleteEntity() {
        IBaseEntity entityV3 = DELETE_CACHE.get(this.getClass());
        if (entityV3 == null) {
            Class<T> modelClazz = (Class<T>) TypeUtil.getTypeArgument(this.getClass().getInterfaces()[0].getGenericInterfaces()[0]);
            try {
                entityV3 = modelClazz.newInstance();
                entityV3.setDeleted(true);
                IBaseEntity v = DELETE_CACHE.putIfAbsent(this.getClass(), entityV3);
                if (v != null) {
                    entityV3 = v;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (entityV3 == null) {
            throw new NullPointerException();
        }
        IUserContext userContext = USER_CONTEXT_ATOMIC_REFERENCE.get();
        if (userContext == null) {
            userContext = SpringContextHolder.getBean(IUserContext.class);
            USER_CONTEXT_ATOMIC_REFERENCE.set(userContext);
        }
        LocalDateTime now = LocalDateTime.now();
        entityV3.setDeleteTime(now);
        entityV3.setModifyTime(now);
        entityV3.setModifyBy(userContext.currentUserId());
        return entityV3;
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
    @Transactional(rollbackFor = Exception.class)
    default boolean insertBatch(List<T> entityList, int batchSize) {
        if (CollUtil.isEmpty(entityList)) {
            throw new IllegalArgumentException("Error: entityList must not be empty");
        }
        try (SqlSession batchSqlSession = sqlSessionBatch()) {
            int size = entityList.size();
            String sqlStatement = sqlStatement(SqlMethod.INSERT_ONE);
            for (int i = 0; i < size; i++) {
                batchSqlSession.insert(sqlStatement, entityList.get(i));
                if (i >= 1 && i % batchSize == 0) {
                    batchSqlSession.flushStatements();
                }
            }
            batchSqlSession.flushStatements();
        } catch (Throwable e) {
            throw new MybatisPlusException("Error: Cannot execute insertBatch Method. Cause", e);
        }
        return true;
    }

    /**
     * <p>
     * 批量操作 SqlSession
     * </p>
     */
    default SqlSession sqlSessionBatch() {
        return SqlHelper.sqlSessionBatch(currentModelClass());
    }
    /**
     * 获取SqlStatement
     *
     * @param sqlMethod 方法
     * @return String
     */
    default String sqlStatement(SqlMethod sqlMethod) {
        return SqlHelper.table(currentModelClass()).getSqlStatement(sqlMethod.getMethod());
    }

    /**
     * <p>
     * 获取当前class
     * </p>
     */
    default Class currentModelClass() {
        return getDeleteEntity().getClass();
    }


}
