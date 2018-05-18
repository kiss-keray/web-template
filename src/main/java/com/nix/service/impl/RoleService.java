package com.nix.service.impl;

import com.nix.dao.RoleInterfaceMapper;
import com.nix.dao.RoleMapper;
import com.nix.model.RoleBaseModel;
import com.nix.model.RoleInterfaceModel;
import com.nix.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kiss
 * @date 2018/05/01 22:56
 * 角色service
 */
@Service
public class RoleService extends BaseService<RoleBaseModel> {
    @Autowired
    private RoleMapper roleMapper;
    private RoleInterfaceMapper roleInterfaceMapper;

    public RoleBaseModel findByName(String name) {
        try {
            return roleMapper.findByOneField("name",name).get(0);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleBaseModel add(RoleBaseModel model) throws Exception{
        super.add(model);
        for (RoleInterfaceModel roleInterfaceModel:model.getRoleInterfaces()) {
            roleMapper.insertRoleMiddleInterface(model.getId(),roleInterfaceModel.getId());
        }
        return model;
    }

    /**
     * 判断当前用户是否有某接口的权限
     * */
    public boolean roleHaveTheInterface(RoleBaseModel roleModel, RoleInterfaceModel roleInterfaceModel) {
        try {
            for (RoleInterfaceModel interfaceModel:roleModel.getRoleInterfaces()) {
                if (interfaceModel.getUrl().equals(roleInterfaceModel.getUrl())) {
                    return true;
                }
            }
        } catch ( Exception e) {
        }
        return false;
    }

    @Override
    public RoleBaseModel update(RoleBaseModel model) throws Exception {
        List<RoleInterfaceModel> before = findById(model.getId()).getRoleInterfaces();
        List<RoleInterfaceModel> now = model.getRoleInterfaces();
        for (RoleInterfaceModel deleteModel:before) {
            roleMapper.deleteRoleMiddleInterface(model.getId(),deleteModel.getId());
        }
        for (RoleInterfaceModel deleteModel:now) {
            roleMapper.insertRoleMiddleInterface(model.getId(),deleteModel.getId());
        }
        return super.update(model);
    }

    public RoleBaseModel createRoleModelByInterfacesId(RoleBaseModel roleModel, Integer[] roleInterfaceId) {
        ArrayList<RoleInterfaceModel> roleInterfaceModels = new ArrayList<>();
        if (roleInterfaceId != null && roleInterfaceId.length > 0) {
            for (Integer id:roleInterfaceId) {
                RoleInterfaceModel model = new RoleInterfaceModel();
                model.setId(id);
                roleInterfaceModels.add(model);
            }
            roleModel.setRoleInterfaces(roleInterfaceModels);
        }
        return roleModel;
    }
}
