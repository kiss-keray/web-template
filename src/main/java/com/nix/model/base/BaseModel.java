package com.nix.model.base;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 *
 * @author 11723
 * @date 2017/5/4
 * 实体基类
 */
public class BaseModel<M extends BaseModel<M>> {

    /**
     * 实体主键id
     * 自增
     * */
    protected Integer id;

    /**
     * 实体创建日期
     * */
    protected Date createDate;

    /**
     * 实体修改日期
     * */
    protected Date updateDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * 随机编号何生成
     * */
    public String generateSn() {
        return String.valueOf(System.currentTimeMillis());
    }
}
