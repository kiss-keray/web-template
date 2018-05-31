package com.nix.common;


import com.nix.model.base.BaseModel;
import com.nix.service.BaseService;
import com.nix.util.SQLUtil;

import java.util.List;

/**
 * @author Kiss
 * @date 2018/05/02 1:02
 * 分页插件
 */
public class Pageable<M extends BaseModel<M>> {
    private Integer page;
    private Integer limit;
    private String order;
    private String sort;
    private String field;
    private String value;
    private String tables;
    private String conditionsSql;
    private BaseService<M> baseService;
    private List list;
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getConditionsSql() {
        return conditionsSql;
    }

    public void setConditionsSql(String conditionsSql) {
        this.conditionsSql = conditionsSql;
    }
    public void setConditionsSql(String conditionsSql,Object ... values) {
        this.conditionsSql = SQLUtil.sqlFormat(conditionsSql,values);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public List<M> getList(BaseService<M> service) {
        baseService = service;
        list = service.list(tables,page,limit,order,sort,conditionsSql);
        return list;
    }

    public Integer getCount() {
        return baseService.list(tables,null,null,null,null,conditionsSql).size();
    }

}
