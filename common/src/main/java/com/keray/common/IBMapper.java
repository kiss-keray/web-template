package com.keray.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.TypeUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author by keray
 * date:2019/7/25 16:56
 */
public interface IBMapper<B extends IBEntity<B>> extends BaseMapper<B> {

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        实体对象 (set 条件值,可以为 null)
     * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     * @return
     */
    @Override
    int update(@Param(Constants.ENTITY) B entity, @Param(Constants.WRAPPER) Wrapper<B> updateWrapper);

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
    Double selectSum(@Param(Constants.WRAPPER) Wrapper<B> wrapper);

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return
     */
    @Override
    int insert(B entity);


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
    default boolean insertBatch(List<B> entityList, int batchSize) {
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


    default boolean exits(Wrapper<B> wrapper) {
        return selectCount(wrapper) > 0;
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
    default Class<B> currentModelClass() {
        return  (Class<B>) TypeUtil.getTypeArgument(this.getClass().getInterfaces()[0].getGenericInterfaces()[0]);
    }

}
