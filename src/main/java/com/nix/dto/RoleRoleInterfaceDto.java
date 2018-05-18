package com.nix.dto;

import com.nix.model.RoleInterfaceModel;

import java.util.List;

/**
 * @author Kiss
 * @date 2018/05/04 17:37
 */
public class RoleRoleInterfaceDto {
    //权限接口
    private RoleInterfaceModel roleInterface;
    //是否用户改接口的权限
    private Boolean have;

    public RoleInterfaceModel getRoleInterface() {
        return roleInterface;
    }

    public void setRoleInterface(RoleInterfaceModel roleInterface) {
        this.roleInterface = roleInterface;
    }

    public Boolean getHave() {
        return have;
    }

    public void setHave(Boolean have) {
        this.have = have;
    }
}
