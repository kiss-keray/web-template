package com.nix.common;

import com.nix.model.base.BaseModel;
import com.nix.service.BaseService;

import java.util.List;

/**
 * @author Kiss
 * @date 2018/05/02 1:02
 * 分页插件
 */
public class Pageable<M extends BaseModel<M>> {
    //当前页面
    private Integer page;
    //页面大小
    private Integer limit;
    //排序方式
    private String order;
    //排序字段
    private String sort;
    //条件sql
    private String conditionsSql;
    //执行service
    private BaseService<M> baseService;

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
    public Integer getCount() {
        return baseService.list(null,null,null,null,conditionsSql).size();
    }

    /**
     * 获取分页内容
     * */
    public List<M> getList(BaseService<M> service) {
        baseService = service;
        return service.list(page,limit,order,sort,conditionsSql);
    }
}
