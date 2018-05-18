package com.nix.model;

import com.nix.model.base.BaseModel;

/**
 * @author Kiss
 * @date 2018/05/01 19:02
 * 接口
 */
public class RoleInterfaceModel extends BaseModel<RoleInterfaceModel> {
   //描述
    private String description;
    //是否开启
    private Boolean enabled;
    //分组
    private String group;
    //url
    private String url;
    //名称
    private String name;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
