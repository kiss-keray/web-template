package com.nix.dao.base;
import com.nix.model.base.BaseModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by 11723 on 2017/5/4.
 */
@Repository
public interface BaseMapper<M extends BaseModel<M>>{
    /**
     * 添加实体
     * @param object
     * */
    void insert(M object);
    /**
     * 删除实体
     * @param id
     * */
    void delete(@Param("id") Integer id);
    /**
     * 更新实体
     * @param model
     * */
    void update(M model);
    /**
     * 查询实体
     * @param id
     * @return
     * */
    M select(@Param("id") Integer id);
    /**
     * 获取实体数据库的最大id值
     * @return
     * */
    Integer maxId(M m);
    /**
     * 获取实体的数目
     * @return
     * */
    Long count();
    /**
     * 根据每个字段查找实体列表
     * @param field 字段
     * @param value 值
     * @return 符合条件的实体列表
     * */
    List<M> findByOneField(@Param("field") String field,@Param("value") String value);
    /**
     * 查找分页实体列表
     * @param offset 偏移量
     * @param limit 分页大小
     * @param sort 排序字段
     * @param order 排序方式
     * @param conditions 查询条件的sql（where 后面的条件）
     * @return 分页内容
     * */
    List<M> list(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("order") String order, @Param("sort") String sort, @Param("conditions") String conditions);
}
