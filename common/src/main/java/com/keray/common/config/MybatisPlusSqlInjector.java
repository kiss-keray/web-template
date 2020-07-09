package com.keray.common.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.baomidou.mybatisplus.core.injector.methods.SelectCount;
import com.baomidou.mybatisplus.core.injector.methods.SelectObjs;
import com.baomidou.mybatisplus.core.injector.methods.UpdateById;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.keray.common.BaseEntity;
import com.keray.common.IUserContext;
import com.keray.common.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.type.LocalDateTimeTypeHandler;
import org.apache.ibatis.type.LocalDateTypeHandler;
import org.apache.ibatis.type.LocalTimeTypeHandler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author by keray
 * date:2019/7/25 16:33
 */
@Configuration
@Aspect
@Slf4j
@Order
@ConditionalOnBean(IUserContext.class)
public class MybatisPlusSqlInjector {

    private final IUserContext userContext;

    @Resource
    private MybatisPlusProperties mybatisPlusProperties;

    public MybatisPlusSqlInjector(IUserContext userContext) {
        this.userContext = userContext;
    }

    @PostConstruct
    public void init() {
        mybatisPlusProperties.getConfiguration().getTypeHandlerRegistry().register(LocalDateTime.class, LocalDateTimeTypeHandler.class);
        mybatisPlusProperties.getConfiguration().getTypeHandlerRegistry().register(LocalDate.class, LocalDateTypeHandler.class);
        mybatisPlusProperties.getConfiguration().getTypeHandlerRegistry().register(LocalTime.class, LocalTimeTypeHandler.class);
    }

    @Bean
    @ConditionalOnMissingBean(ISqlInjector.class)
    public ISqlInjector sqlInjector() {
        return new AbstractSqlInjector() {

            @Override
            public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
                List<AbstractMethod> result = new LinkedList<>(
                        Arrays.asList(new com.baomidou.mybatisplus.core.injector.methods.Update(),
                                new UpdateById(),
                                new ILogicSelectById(),
                                new ILogicSelectBatchByIds(),
                                new ILogicSelectByMap(),
                                new ILogicSelectOne(),
                                new ILogicSelectMaps(),
                                new ILogicSelectMapsPage(),
                                new ILogicSelectList(),
                                new ILogicSelectPage(),
                                new Insert(),
                                new SelectCount(),
                                new ILogicSelectSum(),
                                new SelectObjs())
                );
                return result;
            }

        };
    }

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    @Pointcut("@annotation(com.keray.common.annotation.BaseDbUpdateModel)")
    public void updateById() {
    }

    @Pointcut("@annotation(com.keray.common.annotation.BaseDbUpdateWrapper)")
    public void update() {
    }

    @Pointcut("@annotation(com.keray.common.annotation.BaseDbInsert)")
    public void insert() {
    }

    @Before("updateById()")
    public void beforeUpdateById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        BaseEntity model = (BaseEntity) args[0];
        model.setModifyTime(LocalDateTime.now());
        if (StrUtil.isBlank(model.getModifyBy())) {
            model.setModifyBy(userContext.currentUserId());
        }
    }

    @Before("update()")
    public void beforeUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        BaseEntity model = (BaseEntity) args[0];
        Update update = (Update) args[1];
        if (model != null) {
            model.setModifyTime(LocalDateTime.now());
            if (StrUtil.isBlank(model.getModifyBy())) {
                model.setModifyBy(userContext.currentUserId());
            }
            model.setCreateBy(null);
            model.setCreateTime(null);
        }
        if (update != null && model == null) {
            if (update instanceof LambdaUpdateWrapper) {
                LambdaUpdateWrapper<BaseEntity> updateWrapper = (LambdaUpdateWrapper) update;
                updateWrapper.set(BaseEntity::getModifyTime, LocalDateTime.now())
                        .set(BaseEntity::getModifyBy, userContext.currentUserId());
            } else if (update instanceof UpdateWrapper) {
                try {
                    ((UpdateWrapper<BaseEntity>) update).lambda()
                            .set(BaseEntity::getModifyTime, LocalDateTime.now())
                            .set(BaseEntity::getModifyBy, userContext.currentUserId());
                } catch (MybatisPlusException exception) {
                    ((UpdateWrapper<BaseEntity>) update)
                            .set("modify_time", LocalDateTime.now())
                            .set("modify_by", userContext.currentUserId());
                }
            }
        }
    }

    @Before("insert()")
    public void insert(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        BaseEntity model = (BaseEntity) args[0];
        model.setModifyTime(LocalDateTime.now());
        model.setCreateTime(LocalDateTime.now());
        if (StrUtil.isBlank(model.getId())) {
            model.setId(UUIDUtil.generateUUIDByTimestamp());
        }
        if (StrUtil.isBlank(model.getModifyBy())) {
            model.setModifyBy(userContext.currentUserId());
        }
        if (StrUtil.isBlank(model.getCreateBy())) {
            model.setCreateBy(userContext.currentUserId());
        }
    }

    private static final class ILogicSelectOne extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
//            SqlMethod sqlMethod = SqlMethod.SELECT_ONE;
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, String.format("<script>%s SELECT %s FROM %s %s LIMIT 2 %s\n</script>",
                    sqlFirst(), this.sqlSelectColumns(tableInfo, true), tableInfo.getTableName(),
                    this.sqlWhereEntityWrapper(true, tableInfo), sqlComment()),
                    modelClass);
            return this.addSelectMappedStatementForTable(mapperClass, "selectOne", sqlSource, tableInfo);
        }
    }

    private static final class ILogicSelectById extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            SqlMethod sqlMethod = SqlMethod.SELECT_BY_ID;
            SqlSource sqlSource = new RawSqlSource(configuration, String.format(sqlMethod.getSql(),
                    sqlSelectColumns(tableInfo, false),
                    tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty(),
                    tableInfo.getLogicDeleteSql(true, true)), Object.class);
            return this.addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
        }
    }

    private static final class ILogicSelectBatchByIds extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            SqlMethod sqlMethod = SqlMethod.SELECT_BATCH_BY_IDS;
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, String.format(sqlMethod.getSql(),
                    sqlSelectColumns(tableInfo, false), tableInfo.getTableName(), tableInfo.getKeyColumn(),
                    SqlScriptUtils.convertForeach("#{item}", COLLECTION, null, "item", COMMA),
                    tableInfo.getLogicDeleteSql(true, true)), Object.class);
            return addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
        }
    }

    private static final class ILogicSelectByMap extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            SqlMethod sqlMethod = SqlMethod.SELECT_BY_MAP;
            String sql = String.format(sqlMethod.getSql(), sqlSelectColumns(tableInfo, false),
                    tableInfo.getTableName(), sqlWhereByMap(tableInfo));
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);
            return this.addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
        }
    }

    private static final class ILogicSelectList extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            SqlMethod sqlMethod = SqlMethod.SELECT_LIST;
            String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true),
                    tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment());
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            return this.addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
        }
    }

    private static final class ILogicSelectPage extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            SqlMethod sqlMethod = SqlMethod.SELECT_PAGE;
            String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true),
                    tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment());
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            return this.addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
        }
    }

    private static final class ILogicSelectMaps extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            SqlMethod sqlMethod = SqlMethod.SELECT_MAPS;
            String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true),
                    tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment());
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, Map.class);
        }
    }

    private static final class ILogicSelectMapsPage extends BaseAbstractLogicMethod {
        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            SqlMethod sqlMethod = SqlMethod.SELECT_MAPS_PAGE;
            String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true),
                    tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment());
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, Map.class);
        }
    }

    private static final class ILogicSelectSum extends AbstractMethod {

        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            String methodName = "selectSum";
            String sqlTemp = "<script>\nSELECT sum(%s) FROM %s %s\n</script>";
            String sql = String.format(sqlTemp, sqlSelectColumns(tableInfo, true), tableInfo.getTableName(),
                    sqlWhereEntityWrapper(true, tableInfo));
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            return addSelectMappedStatementForOther(mapperClass, methodName, sqlSource, Double.class);
        }
    }

}

abstract class BaseAbstractLogicMethod extends AbstractMethod {

    /**
     * SQL 查询所有表字段
     *
     * @param table        表信息
     * @param queryWrapper 是否为使用 queryWrapper 查询
     * @return sql 脚本
     */
    @Override
    protected String sqlSelectColumns(TableInfo table, boolean queryWrapper) {
        /* 假设存在 resultMap 映射返回 */
        String selectColumns = ASTERISK;
        if (!queryWrapper) {
            return selectColumns;
        }
        return SqlScriptUtils.convertChoose(String.format("%s != null and %s != null", WRAPPER, Q_WRAPPER_SQL_SELECT),
                SqlScriptUtils.unSafeParam(Q_WRAPPER_SQL_SELECT), selectColumns);
    }
}
